package com.disneystreaming.recognito

import io.circe.{Decoder, HCursor}

/**
 * Parsed JWT payload from AWS Cognito with DSS custom fields recognized
 *
 */
case class TokenPayload(
  cognitoUsername: String,
  yellowPagesId: String
)

object TokenPayload {
  implicit val decodeFoo: Decoder[TokenPayload] = new Decoder[TokenPayload] {
    final def apply(c: HCursor): Decoder.Result[TokenPayload] =
      for {
        cognitoUsername <- c.downField("cognito:username").as[String]
        yellowPagesId <- c.downField("custom:YP_Service_ID").as[String]
      } yield TokenPayload(cognitoUsername, yellowPagesId)
  }
}