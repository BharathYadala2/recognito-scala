package com.disneystreaming.recognito

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import io.circe.parser._
import com.guizmaii.scalajwt.implementations._
import com.guizmaii.scalajwt.{InvalidToken, JwtToken, JwtValidator}
import com.nimbusds.jwt.SignedJWT
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.Authorization

object Recognito {
    /**
     * Required Configuration for Recognito
     * <p>
     * Shared settings are loaded from Vault.
     * Configuration mapping is keyed by S3Region, where each has a unique user pool
     */
    type PlatformConfig = Map[String, RegionConfig]

    /**
     * Service unique configuration for Recognito
     *
     * @param allowedServices
     *   A list of allowed yellowpages service names that tokens may come from.
     *   If this list is empty while the authentication check is enabled, it
     *   may result limited access to the service.  Be sure to populate this
     *   once you are ready to protect your service.
     * @param enabled
     *   toggle the entire permission check for the service.  Turning this off
     *   is useful when migrating a system that did not have permission checks
     *   before and is in the in process of establishing communication between
     *   services.
     */
    case class Config(
        allowedServices: List[String] = List.empty,
        enabled: Boolean = true
    )

    type ProtectedRoutes = PartialFunction[Request[IO], Boolean]

    /**
     * Credentials to use Cognito in a region
     *
     * https://wiki.disneystreaming.com/display/SDEDPE/RecogNito+Secret+Spec
     *
     * @param username
     *   The username for the Yellowpages service's user. This will be of the format <Service AWS Namespace>-<Service ID>
     * @param password
     *   The password for the Yellowpages service's user
     * @param app_client_id
     *   This is the App Client ID specific to the region/environment that is used when authenticating with AWS Cognito
     * @param user_pool_id
     *   This is the User Pool ID for the specific region/environment that will be used to retrieve the keys to verify a token
     */
    case class RegionConfig(
        username: String = "",
        password: String = "",
        app_client_id: String = "",
        user_pool_id: String = "",
   )
}

/**
 * Base Tools for Recognito, the DSS standard for using
 * AWS Cognito for Service-Service Authentication with JWT
 * <p>
 * https://wiki.disneystreaming.com/display/SDEDPE/RecogNito+ID+Token+JWT+Spec
 *
 * @param config
 */
case class Recognito(
    region: String = "us-east-1",
    config: Recognito.Config,
    platformConfig: Recognito.PlatformConfig,
) {
    /**
     * Validator instance used to check JWTs.<br>
     * <p>
     * We use AWS Cognito, so the Validator is based on fetching JWKs from Cognito
     * and validating against them, as opposed to fixed secrets and public keys.
     */
    var jwtValidator: JwtValidator = {
        val s3Region = S3Region(value = region)
        val regionConfig = platformConfig.getOrElse(region, Recognito.RegionConfig())
        val pool = CognitoUserPoolId(value = regionConfig.user_pool_id)

        AwsCognitoJwtValidator(s3Region, pool)
    }

    /**
     * Validates the JWT token against JOSE.
     *
     * @param content encoded JWT as a string.  Typically you will extract this from a request header
     * @return
     *  On success it returns the decoded Token Payload.<br>
     *  On failure, returns an InvalidToken exception
     */
    def validateToken(content: String): Either[Throwable, TokenPayload] = {
        val token = JwtToken(content)
        val payload = for {
            _ <- jwtValidator.validate(token)
            jsonPayload <- parse(SignedJWT.parse(content).getPayload.toString)
            payload <- jsonPayload.as[TokenPayload]
        } yield payload

        payload match {
            case Right(tp) if config.allowedServices.contains(tp.yellowPagesId) => Right(tp)
            case Left(err) => Left(err)
            case _ => Left(InvalidToken(new RuntimeException("Invalid Permissions")))
        }
    }

    /**
     * create a Http4s Auth user from a signed bearer token
     */
    val authUser: Kleisli[IO, Request[IO], Either[String, TokenPayload]] = Kleisli(request => IO {
        (
          for {
              header <- request.headers.get[Authorization].collect {
                  case Authorization(Credentials.Token(AuthScheme.Bearer, token)) => token
              }.toRight[Throwable](InvalidToken(new RuntimeException("Token not found in headers")))
              token <- validateToken(header)
          } yield token
          ) match {
            case Left(err) => Left(err.getMessage)
            case Right(user) => Right(user)
        }
    })

    /**
     * Throw a Forbidden status as the error behavior when Auth fails.
     */
    val onFailure: AuthedRoutes[String, IO] =
        Kleisli(req => {
            OptionT.liftF(Forbidden(req.context))
        })

    /**
     * Applies Recognito as middleware onto an Http4s service.
     *
     * Conditionally performs the authentication check based on if the request is for a path that is protected.
     *
     * @param httpRoutes
     * @param protectedRoutes list of endpoints that require an Auth check
     * @return
     */
    def wrapServer(httpRoutes: HttpRoutes[IO], protectedRoutes: Recognito.ProtectedRoutes): HttpRoutes[IO] = {
        Kleisli { (req: Request[IO]) => {
            OptionT {
                if (config.enabled && PartialFunction.cond(req)(protectedRoutes))
                    authUser(req).flatMap {
                        case Left(err) => onFailure(AuthedRequest(err, req)).value
                        case Right(_) => httpRoutes(req).value
                    }
                else httpRoutes(req).value
            }
        } }
    }
}
