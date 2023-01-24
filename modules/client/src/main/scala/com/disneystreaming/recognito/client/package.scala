package com.disneystreaming.recognito

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.disneystreaming.recognito.client.middleware.AuthMiddleware
import com.disneystreaming.recognito.client.models.RecognitoCreds
import org.http4s.Request

package object client {

  implicit class EnrichRequestIO(request: Request[IO]) {
    def withAuthHeader(creds: RecognitoCreds)(implicit ru: IORuntime): Request[IO] =
      AuthMiddleware.injectTo(request, creds).unsafeRunSync()
  }

}
