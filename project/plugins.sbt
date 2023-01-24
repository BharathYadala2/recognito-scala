externalResolvers := Seq(
  Resolver.url(
    "DSS sbt artifactory",
    url("https://artifactory.us-east-1.bamgrid.net/org-sbt-scala-repo-cache/")
  )(Resolver.ivyStylePatterns)
)

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
