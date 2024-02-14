import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.11"

lazy val microservice = Project("individuals-if-api-stub", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(onLoadMessage := "")
  .settings(scalafmtOnCompile := true)
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.domain._",
      "uk.gov.hmrc.individualsifapistub.domain._",
      "uk.gov.hmrc.individualsifapistub.Binders._"
    )
  )
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    Test / testOptions := Seq(Tests.Filter(_ startsWith "unit"))
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(PlayKeys.playDefaultPort := 8443)
  // Suppress logging of successful tests
  .settings(
    Test / testOptions -= Tests.Argument("-o", "-u", "target/test-reports", "-h", "target/test-reports/html-report")
  )
  .settings(
    Test / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u",
      "target/test-reports",
      "-h",
      "target/test-reports/html-report"
    )
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    // Disable default sbt Test options (might change with new versions of bootstrap)
    testOptions -= Tests
      .Argument("-o", "-u", "target/int-test-reports", "-h", "target/int-test-reports/html-report"),
    testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u",
      "target/int-test-reports",
      "-h",
      "target/int-test-reports/html-report")
  )

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
