import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}

val appName = "individuals-if-api-stub"

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"
lazy val ComponentTest = config("component") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
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
  .settings(scalaSettings: _*)
  .settings(scalaVersion := "2.13.8")
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")
  .settings(defaultSettings(): _*)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    Test / testOptions := Seq(Tests.Filter(unitFilter)),
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test")).value,
    IntegrationTest / unmanagedResourceDirectories := (IntegrationTest / baseDirectory)(base =>
      Seq(base / "test/resources")).value,
    IntegrationTest / testOptions := Seq(Tests.Filter(intTestFilter)),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false
  )
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings): _*)
  .settings(
    ComponentTest / testOptions := Seq(Tests.Filter(componentFilter)),
    ComponentTest / unmanagedSourceDirectories := (ComponentTest / baseDirectory)(base => Seq(base / "test")).value,
    ComponentTest / testGrouping := oneForkedJvmPerTest((ComponentTest / definedTests).value),
    ComponentTest / parallelExecution := false
  )
  .settings(PlayKeys.playDefaultPort := 8443)
  .settings(majorVersion := 0)
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

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
