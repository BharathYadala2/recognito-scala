package com.disneystreaming.recognito.client.middleware

import cats.effect.IO
import com.disneystreaming.recognito.client.models.RecognitoCreds
import com.disneystreaming.recognito.client.utils.AuthUtils
import com.disneystreaming.recognito.client.utils.AuthEntities.AuthenticationResult
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Request}

object AuthMiddleware {

  private def getOrCreateToken(creds: RecognitoCreds): IO[AuthenticationResult] =
    TokenCache.get(creds.username).flatMap {
      case Some(value) => IO.pure(value)
      case None =>
        for {
          authResult <- AuthUtils.generateToken(creds)
          _ <- TokenCache.put(creds.username, authResult)
        } yield authResult
    }

  def injectTo(request: Request[IO], creds: RecognitoCreds): IO[Request[IO]] =
    getOrCreateToken(creds)
      .map { token =>
        val authHeader = Authorization(Credentials.Token(AuthScheme.Bearer, token.IdToken))
        request.putHeaders(authHeader)
      }

}
