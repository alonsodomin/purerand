import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

inThisBuild(
  Seq(
    name := "purerand",
    startYear := Some(2019),
    organization := "com.github.alonsodomin",
    organizationName := "A. Alonso Dominguez",
    description := "Pure random generator of values",
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/alonsodomin/purerand"),
        "scm:git:git@github.com:alonsodomin/purerand.git"
      )
    ),
    developers += Developer(
      "alonsodomin",
      "A. Alonso Dominguez",
      "",
      url("https://github.com/alonsodomin")
    )
  )
)

lazy val purerand = (project in file("."))
  .settings(globalSettings)
  .aggregate(core.js, core.jvm)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("modules/core"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(globalSettings)
  .settings(commonSettings)
  .settings(
    moduleName := "purerand",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"              % Versions.cats.main,
      "org.typelevel" %%% "cats-testkit-scalatest" % Versions.cats.testkit % Test,
      "org.typelevel" %%% "kittens"                % Versions.kittens,
      "co.fs2"        %%% "fs2-core"               % Versions.fs2,
      "org.scalatest" %%% "scalatest"              % Versions.scalaTest % Test
    ),
    parallelExecution in Test := false
  )

// Command aliases

addCommandAlias(
  "fmt",
  Seq(
    "scalafmt",
    "scalafmtSbt"
  ).mkString(";")
)

addCommandAlias(
  "chkfmt",
  Seq(
    "scalafmtCheck",
    "scalafmtSbtCheck"
  ).mkString(";")
)

// Settings

lazy val globalSettings = Seq(
  sonatypeProfileName := "com.github.alonsodomin"
)

lazy val scalacSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8",                         // Specify character encoding used by source files.
    "-explaintypes",                 // Explain type errors in more detail.
    "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds",         // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
    "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
    "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
    "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",        // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
    "-Ywarn-dead-code",              // Warn when dead code is identified.
    "-Ywarn-numeric-widen",          // Warn when numerics are widened.
    "-Ywarn-value-discard"           // Warn when non-Unit expression results are unused.
  ),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 12 =>
        Seq(
          "-Xlint:constant",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:imports",
          "-Ywarn-unused:locals",
          "-Ywarn-unused:patvars",
          "-Ywarn-unused:privates"
        )
      case Some((2, n)) if n == 11 =>
        Seq(
          "-Ywarn-unused"
        )
      case _ => Nil
    }
  },
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n < 13 =>
        Seq(
          "-Xfuture",
          "-Xlint:by-name-right-associative",
          "-Xlint:unsound-match",
          "-Ypartial-unification",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit"
        )
      case _ => Nil
    }
  },
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
)

lazy val commonSettings = scalacSettings ++ Seq(
  libraryDependencies ++= Seq(
    compilerPlugin("org.typelevel" % "kind-projector"      % "0.10.3" cross CrossVersion.binary),
    compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )
)
