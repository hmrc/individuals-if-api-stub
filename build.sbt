/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val ItTest = config("it") extend Test

lazy val microservice = Project("individuals-if-api-stub", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(onLoadMessage := "")
  .settings(CodeCoverageSettings.settings *)
  .settings(scalafmtOnCompile := true)
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.domain._",
      "uk.gov.hmrc.individualsifapistub.domain._",
      "uk.gov.hmrc.individualsifapistub.Binders._"
    )
  )
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")
  .settings(scalacOptions += "-Wconf:src=txt/.*:s") //silences warnings from txt files
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
  .configs(ItTest)
  .settings(inConfig(ItTest)(Defaults.testSettings) *)
  .settings(
    ItTest / unmanagedSourceDirectories := (ItTest / baseDirectory)(base => Seq(base / "test")).value,
    ItTest / testOptions := Seq(Tests.Filter((name: String) => name startsWith "it")),
    addTestReportOption(ItTest, "int-test-reports"),
    // Disable default sbt Test options (might change with new versions of bootstrap)
    ItTest / testOptions -= Tests
      .Argument("-o", "-u", "target/int-test-reports", "-h", "target/int-test-reports/html-report"),
    ItTest / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u",
      "target/int-test-reports",
      "-h",
      "target/int-test-reports/html-report")
  )
