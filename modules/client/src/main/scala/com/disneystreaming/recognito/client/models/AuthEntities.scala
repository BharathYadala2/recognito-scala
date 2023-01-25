package com.disneystreaming.recognito.client.models

object AuthEntities {

  case class AuthParameters(USERNAME: String)

  case class AuthRequest(AuthParameters: AuthParameters, AuthFlow: String, ClientId: String)

  case class AuthResponse(Session: String)

  case class ChallengeResponses(ANSWER: String, USERNAME: String)

  case class ClientMetadata(IssuedForService: String)

  case class TokenRequest(ChallengeName: String, ChallengeResponses: ChallengeResponses, ClientId: String, ClientMetadata: ClientMetadata, Session: Option[String])

  case class AuthenticationResult(AccessToken: String, ExpiresIn: Int, IdToken: String, RefreshToken: String, TokenType: String) {
    val expirationAt: Long = System.currentTimeMillis() + (ExpiresIn * 60 * 60 * 1000)

    def hasExpired: Boolean = expirationAt > System.currentTimeMillis()
  }

  case class TokenResponse(AuthenticationResult: AuthenticationResult)

}
