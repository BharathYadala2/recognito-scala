package com.disneystreaming.recognito.client.middleware

import com.disneystreaming.recognito.client.utils.AuthEntities.AuthenticationResult

import cats.effect.IO

import scala.collection.mutable

trait TokenCache {

  def put(username: String, authResult: AuthenticationResult): IO[Unit]

  def get(username: String): IO[Option[AuthenticationResult]]

  def flushExpired(): IO[Unit]

}

object TokenCache extends TokenCache {

  private val tokenMap: mutable.Map[String, AuthenticationResult] = mutable.Map.empty

  private def tokenValidity(authResult: AuthenticationResult): Boolean = {
    System.currentTimeMillis() < authResult.expirationAt
  }

  override def put(username: String, authResult: AuthenticationResult): IO[Unit] =
    IO(tokenMap.put(username, authResult)).void

  override def get(username: String): IO[Option[AuthenticationResult]] =
    IO(tokenMap.get(username).filter(value => tokenValidity(value)))

  override def flushExpired(): IO[Unit] =
    IO(tokenMap.filter(x => tokenValidity(x._2)))

}
