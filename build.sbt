
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.ExternalService
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList

val appName = "individuals-if-api-stub"
val hmrc = "uk.gov.hmrc"
lazy val playSettings : Seq[Setting[_]] = Seq(routesImport ++= Seq("uk.gov.hmrc.domain._", "uk.gov.hmrc.individualsifapistub.domain._", "uk.gov.hmrc.individualsifapistub.Binders._"))
lazy val plugins : Seq[Plugins] = Seq.empty

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"
lazy val ComponentTest = config("component") extend Test
lazy val externalServices = List(ExternalService("AUTH"), ExternalService("INDIVIDUALS_MATCHING_API"), ExternalService("DES"))

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins: _*)
  .settings(playSettings : _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(scalaVersion := "2.12.11")
  .settings(defaultSettings(): _*)
  .settings(
    dependencyOverrides ++= AppDependencies.overrides,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    testOptions in Test := Seq(Tests.Filter(unitFilter)),
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator  )
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(itDependenciesList := externalServices)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "test")).value,
    unmanagedResourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "test/resources")).value,
    testOptions in IntegrationTest := Seq(Tests.Filter(intTestFilter)),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false)
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings): _*)
  .settings(
    testOptions in ComponentTest := Seq(Tests.Filter(componentFilter)),
    unmanagedSourceDirectories   in ComponentTest := (baseDirectory in ComponentTest)(base => Seq(base / "test")).value,
    testGrouping in ComponentTest := oneForkedJvmPerTest((definedTests in ComponentTest).value),
    parallelExecution in ComponentTest := false
  )
  .settings(PlayKeys.playDefaultPort := 8443)
  .settings(majorVersion := 0)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}