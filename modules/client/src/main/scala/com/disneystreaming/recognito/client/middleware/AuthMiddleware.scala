package com.disneystreaming.recognito.client.middleware

import cats.effect.IO
import cats.effect.kernel.{Ref, Resource}
import com.disneystreaming.recognito.client.models.AuthEntities.AuthenticationResult
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import com.disneystreaming.recognito.client.utils.AuthUtils
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials}

object AuthMiddleware {

  def apply(credentials: RecognitoCredentials)(client: Client[IO]): Client[IO] = {
    val tokenResource = Resource.eval(Ref[IO].of[Option[AuthenticationResult]](None))

    Client {
      baseRequest =>
        for {
          token <- tokenResource.evalMap(ref => for {
            cachedValue <- ref.get
            token <- cachedValue match {
              case Some(value) if !value.hasExpired => IO.pure(value)
              case _ =>
                for {
                  generated <- AuthUtils.generateToken(credentials)(client)
                  newToken <- ref.modify(_ => (Some(generated), generated))
                } yield newToken
            }
          } yield token)
          request = baseRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.IdToken)))
          response <- client.run(request)
        } yield response
    }
  }

}
