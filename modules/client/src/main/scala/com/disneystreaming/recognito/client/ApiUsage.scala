package com.disneystreaming.recognito.client

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.disneystreaming.recognito.client.models.AuthEntities.AuthenticationResult
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

  val program = for {
    ref <- IO.ref(Map.empty[String, AuthenticationResult])
    request1 <- EmberClientBuilder
      .default[IO]
      .build
      .use { client =>
        AuthMiddleware(request, creds1)(ref, client)
      }

    request2 <- EmberClientBuilder
      .default[IO]
      .build
      .use { client =>
        AuthMiddleware(request, creds1)(ref, client)
      }

    request3 <- EmberClientBuilder
      .default[IO]
      .build
      .use { client =>
        AuthMiddleware(request, creds2)(ref, client)
      }
  } yield {
    val header_v1 = request1.headers
    val header_v2 = request2.headers
    val header_v3 = request3.headers

    println(s"Request 1 header: $header_v1")
    println(s"Request 2 header: $header_v2")
    println(s"Request 3 header: $header_v3")

    println(s"Request 1 header == Request 2 header: ${header_v1 == header_v2}")
    println(s"Request 1 header != Request 3 header: ${header_v1 != header_v3}")
  }

  program.unsafeRunSync()

}
