inThisBuild(Seq(
  startYear := Some(2019),
  organization := "com.github.alonsodomin",
  organizationName := "A. Alonso Dominguez",
  description := "Pure random generator of values",
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(url("https://github.com/alonsodomin/scala-gen"), "scm:git:git@github.com:alonsodomin/scala-gen.git"))
))

lazy val commonSettings = Seq(
  libraryDependencies += compilerPlugin("org.typelevel" % "kind-projector" % "0.10.3" cross CrossVersion.binary)
)

lazy val `scala-gen` = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(
    moduleName := "gen",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.0.0",
      "org.typelevel" %% "kittens" % "2.0.0",
      "co.fs2" %% "fs2-core" % "2.0.0"
    )
  )

