package com.disneystreaming.recognito.client

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.disneystreaming.recognito.client.models.RecognitoCreds
import org.http4s.implicits._
import org.http4s.{Method, Request}

object ApiUsage extends App {

  private val challengeAnswer = "xjvTpWxVc2xTzGkf" // *
  private val issuedForService = "6286e09dbb07d2d2cab2c1c9" // *
  private val clientId = "538gsfq2es360re9bldpcspm57" // *
  private val username = "recognitosamplejava-6286e09dbb07d2d2cab2c1c9" // *

  val creds1 = RecognitoCreds(clientId, username, challengeAnswer, issuedForService)


  val creds2 = RecognitoCreds(
    clientId = "538gsfq2es360re9bldpcspm57",
    username = "recognitosamplepython-6286baaa4339ad0f834b8955",
    password = "ozEshAQn7tfhb6fL",
    issuedForService = "6286e09dbb07d2d2cab2c1c9"
  )

  val request: Request[IO] = Request(
    method = Method.GET,
    uri = uri"https://google.com"
  )

  // 1. Using the enriched version of Request[IO] type

  import com.disneystreaming.recognito.client._

  //val updatedRequest_v1: Request[IO] = request.withAuthHeader(creds)

  //println(updatedRequest_v1.headers)

  // 2. Using the AuthMiddleware instance directly

  import com.disneystreaming.recognito.client.middleware.AuthMiddleware

  val updatedRequest_v11 = AuthMiddleware.injectTo(request, creds1).unsafeRunSync()
  val updatedRequest_v12 = AuthMiddleware.injectTo(request, creds1).unsafeRunSync()

  Thread.sleep(15000)

  val updatedRequest_v13 = AuthMiddleware.injectTo(request, creds1).unsafeRunSync()

  //val updatedRequest_v21 = AuthMiddleware.injectTo(request, creds2).unsafeRunSync()

  println("1st => " + updatedRequest_v11.headers)
  println("2nd => " + updatedRequest_v12.headers)
  println("3rd => " + updatedRequest_v13.headers)
  //println("3rd => " + updatedRequest_v21.headers)

}
