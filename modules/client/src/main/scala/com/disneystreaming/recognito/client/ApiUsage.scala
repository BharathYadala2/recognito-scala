package com.disneystreaming.recognito.client

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import com.disneystreaming.recognito.client.models.RecognitoCredentials
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._
import org.http4s.{Method, Request}
import com.disneystreaming.recognito.client.middleware.AuthMiddleware
import org.http4s.client.Client

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

  val program = for {
    client <- EmberClientBuilder.default[IO].build
    authorizedClient = AuthMiddleware(creds1)(client)
    response1 <- authorizedClient.run(request)
    response2 <- authorizedClient.run(request)
  } yield (response1, response2)

  val (res1, res2) = program.use(res => IO(res)).unsafeRunSync()

  println(s"Response 1 status: ${res1.status}")
  println(s"Response 2 status: ${res2.status}")

}
