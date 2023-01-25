package com.disneystreaming.recognito.client.utils

import com.disneystreaming.recognito.client.models.AuthEntities._
import cats.effect._
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import com.disneystreaming.recognito.client.models.AuthEntities.{AuthRequest, AuthResponse, AuthenticationResult, TokenRequest, TokenResponse}
import io.circe.generic.auto._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.syntax.all._
import org.http4s.{Entity, Headers, Method, Request}

object AuthUtils {

  private val cognitoUrl = uri"https://cognito-idp.us-east-1.amazonaws.com"
  private val authflow = "CUSTOM_AUTH"
  private val challengeName = "CUSTOM_CHALLENGE"

  private val authRequestEncoder = jsonEncoderOf[IO, AuthRequest]
  private val tokenRequestEncoder = jsonEncoderOf[IO, TokenRequest]

  private val authResponseHeader: Headers =
    Headers(
      "X-Amz-Target" -> "AWSCognitoIdentityProviderService.InitiateAuth",
      "Content-Type" -> "application/x-amz-json-1.1"
    )

  private val tokenResponseHeader: Headers =
    Headers(
      "X-Amz-Target" -> "AWSCognitoIdentityProviderService.RespondToAuthChallenge",
      "Content-Type" -> "application/x-amz-json-1.1"
    )

  private def getAuthResponse(authRequest: Entity[IO])(implicit client: Client[IO]): IO[AuthResponse] = {
    val request = Request[IO](
      method = Method.POST,
      uri = cognitoUrl,
      body = authRequest.body,
      headers = authResponseHeader
    )

    client.expect(request)(jsonOf[IO, AuthResponse])
  }

  private def getTokenResponse(tokenRequest: Entity[IO])(implicit client: Client[IO]): IO[TokenResponse] = {
    val request = Request[IO](
      method = Method.POST,
      uri = cognitoUrl,
      body = tokenRequest.body,
      headers = tokenResponseHeader
    )

    client.expect(request)(jsonOf[IO, TokenResponse])
  }

  def generateToken(creds: RecognitoCredentials): IO[AuthenticationResult] =
    EmberClientBuilder.default[IO].build.use { implicit client =>
      val authRequest = AuthRequest(
        AuthParameters = AuthParameters(creds.username),
        AuthFlow = authflow,
        ClientId = creds.clientId
      )

      val tokenRequest = TokenRequest(
        ChallengeName = challengeName,
        ChallengeResponses = ChallengeResponses(creds.password, creds.username),
        ClientId = creds.clientId,
        ClientMetadata = ClientMetadata(creds.issuedForService),
        Session = None
      )

      for {
        authResponse <- getAuthResponse(authRequestEncoder.toEntity(authRequest))
        updatedTokenReq = tokenRequest.copy(Session = Some(authResponse.Session))
        tokenResponse <- getTokenResponse(tokenRequestEncoder.toEntity(updatedTokenReq))
      } yield tokenResponse.AuthenticationResult
    }

}
