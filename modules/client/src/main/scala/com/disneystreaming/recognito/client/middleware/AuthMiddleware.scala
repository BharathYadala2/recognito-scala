package com.disneystreaming.recognito.client.middleware

import cats.effect.{IO, Ref}
import com.disneystreaming.recognito.client.models.AuthEntities.AuthenticationResult
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import com.disneystreaming.recognito.client.utils.AuthUtils
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Request}

class AuthMiddleware(credentials: RecognitoCredentials, ref: Ref[IO, Option[AuthenticationResult]], client: Client[IO]) {

  def appendTo(request: Request[IO]): IO[Request[IO]] =
    for {
      cachedValue <- ref.get
      token <- cachedValue match {
        case Some(value) if !value.hasExpired =>
          IO.pure(value)
        case _ =>
          for {
            generated <- AuthUtils.generateToken(credentials)(client)
            newToken <- ref.modify(_ => (Some(generated), generated))
          } yield newToken
      }
    } yield request.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.IdToken)))

}

object AuthMiddleware {

  def apply(credentials: RecognitoCredentials)(client: Client[IO]): IO[AuthMiddleware] =
    IO.ref(Option.empty[AuthenticationResult]).map(ref => new AuthMiddleware(credentials, ref, client))

}
