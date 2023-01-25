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

  val creds1 = RecognitoCredentials(clientId, username, challengeAnswer, issuedForService)


  val creds2 = RecognitoCredentials(
    clientId = "538gsfq2es360re9bldpcspm57",
    username = "recognitosamplepython-6286baaa4339ad0f834b8955",
    password = "ozEshAQn7tfhb6fL",
    issuedForService = "6286e09dbb07d2d2cab2c1c9"
  )

  val request: Request[IO] = Request(
    method = Method.GET,
    uri = uri"https://google.com"
  )

  import com.disneystreaming.recognito.client.middleware.AuthMiddleware

  val request_v1 = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      AuthMiddleware(request, creds1)(client)
    }

  val request_v2 = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      AuthMiddleware(request, creds1)(client)
    }

  val request_v3 = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      AuthMiddleware(request, creds2)(client)
    }

  val header_v1 = request_v1.unsafeRunSync().headers
  val header_v2 = request_v2.unsafeRunSync().headers
  val header_v3 = request_v3.unsafeRunSync().headers

  println(s"Request 1 header: $header_v1")
  println(s"Request 2 header: $header_v2")
  println(s"Request 3 header: $header_v3")

  println(s"Request 1 header == Request 2 header: ${header_v1 == header_v2}")
  println(s"Request 1 header != Request 3 header: ${header_v1 != header_v3}")

}
