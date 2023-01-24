import sbt._

object Dependencies {

  object Versions {

    val cats = "2.7.0"

    val catsEffect = "3.3.11"

    val circe = "0.14.1"

    val http4s = "0.23.18"

    val jwt = "9.1.1"

    val log4Cats = "2.2.0"

    val logback = "1.2.11"

    val munit = "0.7.29"

    val munitCats = "1.0.7"

    val pureConfig = "0.17.1"

    val seeCommon = "1.2.0"

    val shapeless = "2.3.7"

    val weaver = "0.7.11"

  }

  object Cats {

    val munitCats = "org.typelevel" %% "munit-cats-effect-3" % Versions.munitCats % Test

  }

  object Circe {

    val core = "io.circe" %% "circe-core" % Versions.circe

    val generic = "io.circe" %% "circe-generic" % Versions.circe

    val parser = "io.circe" %% "circe-parser" % Versions.circe

    val optics = "io.circe" %% "circe-optics" % Versions.circe

  }

  object Http4s {

    private val org = "org.http4s"

    val core = org %% "http4s-core" % Versions.http4s

    val server = org %% "http4s-server" % Versions.http4s

    val circe = org %% "http4s-circe" % Versions.http4s

    val ember = org %% "http4s-ember-client" % Versions.http4s

    val dsl = org %% "http4s-dsl" % Versions.http4s

    val metrics = org %% "http4s-dropwizard-metrics" % Versions.http4s

  }

  object Jwt {
    private val org = "com.github.jwt-scala"

    val jwt = org %% "jwt-core" % Versions.jwt

    var circe = org %% "jwt-circe" % Versions.jwt

    var jose = "com.guizmaii" %% "scala-nimbus-jose-jwt" % "2.0.0-RC1"
  }

  object Logging {

    private val org = "ch.qos.logback"

    val logBack = org % "logback-classic" % Versions.logback

    val logStashLogBack = "net.logstash.logback" % "logstash-logback-encoder" % "7.2"

  }

  object Munit {

    private val org = "org.scalameta"

    val munit = org %% "munit" % Versions.munit % Test

  }

  object Shapeless {

    val shapeless = "com.chuusai" %% "shapeless" % Versions.shapeless

  }

  object TypeLevel {

    private val org = "org.typelevel"

    val catsEffect = org %% "cats-effect" % Versions.catsEffect

    val catsCore = org %% "cats-core" % Versions.cats

  }

  object Weaver {

    private val org = "com.disneystreaming"

    val weaver = org %% "weaver-cats" % Versions.weaver % Test

    val weaverCats = org %% "weaver-cats" % Versions.weaver % Test

  }
}
