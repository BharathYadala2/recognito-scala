package com.disneystreaming.recognito.client.middleware

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.disneystreaming.recognito.client.models.AuthEntities.AuthenticationResult
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import com.disneystreaming.recognito.client.utils.AuthUtils
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Request}

object AuthMiddleware {

  private val tokenCache = IO.ref(Map.empty[String, AuthenticationResult])

  def apply(request: Request[IO], credentials: RecognitoCredentials)(client: Client[IO]): IO[Request[IO]] =
    for {
      ref <- tokenCache
      cachedValue <- ref.get.map(_.get(credentials.username))
      token <- cachedValue.map(IO.pure).getOrElse {
        AuthUtils.generateToken(credentials)
          .map { authResult =>
            ref.update(_ + (credentials.username -> authResult))
            authResult
          }
      }
    } yield request.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.IdToken)))

}
