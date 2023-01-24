import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "1.0.1"
ThisBuild / publishTo := {
  val nexus = "https://artifactory.prod.hulu.com/artifactory/"
  if (version.value.endsWith("SNAPSHOT"))
    Some("hulu snapshots" at nexus + "hulu-mvn-snapshots")
  else
    Some("hulu releases" at nexus + "hulu-mvn-releases")
}

lazy val credentialSettings = {
  (sys.env.get("ARTIFACT_REPOSITORY_USER"), sys.env.get("ARTIFACT_REPOSITORY_TOKEN")) match {
    case (Some(user), Some(token)) if user.nonEmpty && token.nonEmpty =>
      Seq(
        Credentials("Artifactory Realm", "artifactory.prod.hulu.com", user, token)
      )
    case _ =>
      Seq.empty
  }
}

credentials ++= credentialSettings

lazy val commonSettings = Seq(
  organization := "com.hulu.authandpolicy"
)

lazy val root = project
  .in(file("."))
  .settings(
    publish := {},
    publishLocal := {},
    name := "recognito-scala"
  )
  .aggregate(server,client)


lazy val server = project
  .in(file("modules/server"))
  .settings(commonSettings)
  .settings(
    name := "recognito-scala",
    description := "Http4s support for verifying authentication token from RecogNito",
    libraryDependencies ++= Seq(
      TypeLevel.catsCore,
      TypeLevel.catsEffect,
      Jwt.jwt,
      Jwt.circe,
      Jwt.jose,
      Shapeless.shapeless,
      Circe.core,
      Circe.generic,
      Circe.parser,
      Http4s.core,
      Http4s.dsl,
      Munit.munit,
      Weaver.weaver,
      Weaver.weaverCats
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

lazy val client = project
  .in(file("modules/client"))
  .settings(commonSettings)
  .settings(
    name := "recognito-scala-client",
    description := "Http4s support for verifying authentication token from RecogNito",
    libraryDependencies ++= Seq(
      Http4s.dsl,
      Http4s.circe,
      Http4s.ember,
      Circe.generic
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

