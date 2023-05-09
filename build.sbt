
import sbt.Tests.{ Group, SubProcess }
import uk.gov.hmrc.DefaultBuildSettings.{ addTestReportOption, defaultSettings, scalaSettings }
import uk.gov.hmrc.ExternalService
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList

val appName = "individuals-if-api-stub"
val hmrc = "uk.gov.hmrc"
lazy val playSettings: Seq[Setting[_]] = Seq(routesImport ++= Seq("uk.gov.hmrc.domain._", "uk.gov.hmrc.individualsifapistub.domain._", "uk.gov.hmrc.individualsifapistub.Binders._"))
lazy val plugins: Seq[Plugins] = Seq.empty

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"
lazy val ComponentTest = config("component") extend Test
lazy val externalServices = List(ExternalService("AUTH"), ExternalService("INDIVIDUALS_MATCHING_API"), ExternalService("DES"))

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins: _*)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(scalaVersion := "2.13.8")
  .settings(defaultSettings(): _*)
  .settings(
    dependencyOverrides ++= AppDependencies.overrides,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    Test / testOptions := Seq(Tests.Filter(unitFilter)),
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(itDependenciesList := externalServices)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test")).value,
    IntegrationTest / unmanagedResourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test/resources")).value,
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

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${ test.name }"))))
  }
}