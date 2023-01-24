package com.disneystreaming.recognito

import cats.effect._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s._
import com.disneystreaming.recognito.Recognito._
import com.guizmaii.scalajwt.JwtValidator
import com.nimbusds.jwt.JWTClaimsSet
import org.http4s.headers.Authorization
import weaver.SimpleIOSuite

object MiddlewareTest extends SimpleIOSuite {

  /**
   * Sample unsigned JWT generated from cognito documentation and put through jwt.io
   *
   * payload
   *
   * {
   *  "sub": "test-user",
   *  "aud": "dss",
   *  "email_verified": true,
   *  "token_use": "id",
   *  "auth_time": 1500009400,
   *  "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_example",
   *  "cognito:username": "testUser",
   *  "custom:YP_Service_ID": "test-service",
   *  "exp": 1500013000,
   *  "given_name": "Tester",
   *  "iat": 1500009400,
   *  "email": "test@test.com",
   *  "jti": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
   *  "origin_jti": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
   * }
   */
  val TEST_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJhdWQiOiJkc3MiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE1MDAwMDk0MDAsImlzcyI6Imh0dHBzOi8vY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb20vdXMtZWFzdC0xX2V4YW1wbGUiLCJjb2duaXRvOnVzZXJuYW1lIjoidGVzdFVzZXIiLCJjdXN0b206WVBfU2VydmljZV9JRCI6InRlc3Qtc2VydmljZSIsImV4cCI6MTUwMDAxMzAwMCwiZ2l2ZW5fbmFtZSI6IlRlc3RlciIsImlhdCI6MTUwMDAwOTQwMCwiZW1haWwiOiJ0ZXN0QHRlc3QuY29tIiwianRpIjoiYWFhYWFhYWEtYmJiYi1jY2NjLWRkZGQtZWVlZWVlZWVlZWVlIiwib3JpZ2luX2p0aSI6ImFhYWFhYWFhLWJiYmItY2NjYy1kZGRkLWVlZWVlZWVlZWVlZSJ9.PP2BxR0w_UZjzh6u42yy1OjoyVchJGghV2yvPWO0dus"

  private def mockServer(
    server: HttpRoutes[IO],
    authGuard: ProtectedRoutes,
    jwtValidator: JwtValidator = null,
    config: Recognito.Config = Recognito.Config(
      allowedServices = List("test-service")
    )
  ) = {
    val recognito = new Recognito(
      "us-east-1",
      config,
      Map(
        "us-east-1" -> RegionConfig(
          username = "test",
          password = "secret",
          app_client_id = "recognito-test",
          user_pool_id = "unit-test"
        )
      )
    )

    if (jwtValidator != null) recognito.jwtValidator = jwtValidator

    recognito.wrapServer(server, authGuard).orNotFound
  }

  test("request with no auth header should return Unauthorized error") {
    val server = mockServer(
      HttpRoutes.of[IO] {
        case GET -> Root / "test" => Ok("good")
      },
      {
        case GET -> Root / "test" => true
      }
    )

    for {
      response <- server.run(Request(
        method = Method.GET,
        uri = uri"/test",
        headers = Headers.empty
      ))
    } yield expect(response.status == Status.Forbidden)
  }

  test("valid JWT auth") {
    val server = mockServer(
      HttpRoutes.of[IO] {
        case GET -> Root / "test" => Ok("good")
      },
      {
        case GET -> Root / "test" => true
      },
      _ => Right(new JWTClaimsSet.Builder().build())
    )

    for {
      response <- server.run(
        Request(
          method = Method.GET,
          uri = uri"/test"
        ).withHeaders(
          Header.Raw(Authorization.name, "Bearer " + TEST_JWT)
        )
      )
    } yield expect(response.status == Status.Ok)
  }

  test("do not require auth on unprotected routes") {
    val server = mockServer(
      HttpRoutes.of[IO] {
        case GET -> Root / "test" => Ok("good")
        case GET -> Root / "demo" => Ok("demo")
      },
      {
        case GET -> Root / "test" => true
      }
    )

    for {
      response <- server.run(Request(
        method = Method.GET,
        uri = uri"/demo",
        headers = Headers.empty
      ))
    } yield expect(response.status == Status.Ok)
  }

  test("reject JWT from services not included in allow list") {
    val server = mockServer(
      HttpRoutes.of[IO] {
        case GET -> Root / "test" => Ok("good")
      },
      {
        case GET -> Root / "test" => true
      },
      _ => Right(new JWTClaimsSet.Builder().build()),
      config = Recognito.Config(
        allowedServices = List(
          "demo-service"
        )
      )
    )

    for {
      response <- server.run(
        Request(
          method = Method.GET,
          uri = uri"/test",
        ).withHeaders(
          Header.Raw(Authorization.name, "Bearer " + TEST_JWT)
        )
      )
    } yield expect(response.status == Status.Forbidden)
  }

  test("protect paths with parameters") {
    val server = mockServer(
      HttpRoutes.of[IO] {
        case GET -> Root / "test-user" / _ => Ok("good")
      },
      {
        case GET -> Root / "test-user" / _ => true
      }
    )

    for {
      response <- server.run(Request(
        method = Method.GET,
        uri = uri"/test-user/user123",
        headers = Headers.empty
      ))
    } yield expect(response.status == Status.Forbidden)
  }

  test("disable recognito permission checks") {
    val server = mockServer(
      HttpRoutes.of[IO] {
        case GET -> Root / "test" => Ok("good")
      },
      {
        case GET -> Root / "test" => true
      },
      config = Recognito.Config(
        enabled = false,
        allowedServices = List(
          "demo-service"
        )
      )
    )

    for {
      response <- server.run(
        Request(
          method = Method.GET,
          uri = uri"/test",
        ).withHeaders(
          Header.Raw(Authorization.name, "Bearer " + TEST_JWT)
        )
      )
    } yield expect(response.status == Status.Ok)
  }
}