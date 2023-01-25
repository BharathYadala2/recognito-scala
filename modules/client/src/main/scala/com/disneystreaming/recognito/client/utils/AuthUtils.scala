package com.disneystreaming.recognito.client.utils

import cats.effect._
import com.disneystreaming.recognito.client.models.AuthEntities._
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.syntax.all._
import org.http4s.{Entity, Headers, Method, Request, Uri}

object AuthUtils {

  private val authflow = "CUSTOM_AUTH"
  private val challengeName = "CUSTOM_CHALLENGE"

  private val authRequestEncoder = jsonEncoderOf[IO, AuthRequest]
  private val tokenRequestEncoder = jsonEncoderOf[IO, TokenRequest]

  private def getCognitoUri(region: String): Uri =
    uri"https://cognito-idp.$region.amazonaws.com"

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

  private def getAuthResponse(authRequest: Entity[IO], uri: Uri)(client: Client[IO]): IO[AuthResponse] = {
    val request = Request[IO](
      method = Method.POST,
      uri = uri,
      body = authRequest.body,
      headers = authResponseHeader
    )

    client.expect(request)(jsonOf[IO, AuthResponse])
  }

  private def getTokenResponse(tokenRequest: Entity[IO], uri: Uri)(client: Client[IO]): IO[TokenResponse] = {
    val request = Request[IO](
      method = Method.POST,
      uri = uri,
      body = tokenRequest.body,
      headers = tokenResponseHeader
    )

    client.expect(request)(jsonOf[IO, TokenResponse])
  }

  def generateToken(credentials: RecognitoCredentials)(client: Client[IO]): IO[AuthenticationResult] = {
    val cognitoUri = getCognitoUri(credentials.region)

    val authRequest = AuthRequest(
      AuthParameters = AuthParameters(credentials.username),
      AuthFlow = authflow,
      ClientId = credentials.clientId
    )

    val tokenRequest = TokenRequest(
      ChallengeName = challengeName,
      ChallengeResponses = ChallengeResponses(credentials.password, credentials.username),
      ClientId = credentials.clientId,
      ClientMetadata = ClientMetadata(credentials.issuedForService),
      Session = None
    )

    for {
      authResponse <- getAuthResponse(authRequestEncoder.toEntity(authRequest), cognitoUri)(client)
      updatedTokenReq = tokenRequest.copy(Session = Some(authResponse.Session))
      tokenResponse <- getTokenResponse(tokenRequestEncoder.toEntity(updatedTokenReq), cognitoUri)(client)
    } yield tokenResponse.AuthenticationResult
  }

}
