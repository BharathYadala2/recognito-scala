package com.disneystreaming.recognito.client.middleware

import cats.effect.{IO, Ref}
import cats.effect.unsafe.implicits.global
import com.disneystreaming.recognito.client.models.AuthEntities.AuthenticationResult
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import com.disneystreaming.recognito.client.utils.AuthUtils
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Request}

object AuthMiddleware {

  def apply(
             request: Request[IO],
             credentials: RecognitoCredentials
           )(
             ref: Ref[IO, Map[String, AuthenticationResult]],
             client: Client[IO]
           ): IO[Request[IO]] =
    for {
      cachedValue <- ref.get.map(_.get(credentials.username))
      token <- cachedValue match {
        case Some(value) => IO.pure(value)
        case None =>
          for {
            generated <- AuthUtils.generateToken(credentials)
            newToken <- ref.modify(x => (x + (credentials.username -> generated), generated))
          } yield newToken
      }
    } yield request.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.IdToken)))

}
