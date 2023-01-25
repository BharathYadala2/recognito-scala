package com.disneystreaming.recognito.client

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._
import org.http4s.{Method, Request}

object ApiUsage extends App {

  private val challengeAnswer = "xjvTpWxVc2xTzGkf" // *
  private val issuedForService = "6286e09dbb07d2d2cab2c1c9" // *
  private val clientId = "538gsfq2es360re9bldpcspm57" // *
  private val username = "recognitosamplejava-6286e09dbb07d2d2cab2c1c9" // *

  val creds1 = RecognitoCredentials(clientId, username, challengeAnswer, issuedForService, region = "us-east-1")

  val creds2 = RecognitoCredentials(
    clientId = "538gsfq2es360re9bldpcspm57",
    username = "recognitosamplepython-6286baaa4339ad0f834b8955",
    password = "ozEshAQn7tfhb6fL",
    issuedForService = "6286e09dbb07d2d2cab2c1c9",
    region = "us-east-1"
  )

  val request: Request[IO] = Request(
    method = Method.GET,
    uri = uri"https://google.com"
  )

  import com.disneystreaming.recognito.client.middleware.AuthMiddleware

  val program = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      AuthMiddleware(creds1)(client).map { middleware =>
        for {
          req1 <- middleware.appendTo(request)
          req2 <- middleware.appendTo(request)
        } yield {
          val header_v1 = req1.headers
          val header_v2 = req2.headers

          println(s"Request 1 header: $header_v1")
          println(s"Request 2 header: $header_v2")

          println(s"Request 1 header == Request 2 header: ${header_v1 == header_v2}")
        }
      }
    }
    .flatten

  program.unsafeRunSync()

}
