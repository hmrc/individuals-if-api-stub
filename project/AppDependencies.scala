import play.core.PlayVersion
import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val hmrc = "uk.gov.hmrc"

  val compile = Seq(
    ws,
    hmrc                  %% "bootstrap-backend-play-28"  % "5.12.0",
    hmrc                  %% "auth-client"                % "5.2.0-play-28",
    hmrc                  %% "domain"                     % "5.9.0-play-27",
    hmrc                  %% "play-hmrc-api"              % "6.2.0-play-28",
    hmrc                  %% "simple-reactivemongo"       % "8.0.0-play-28",
    "com.typesafe.play"   %% "play-json-joda"             % "2.9.1"
  )

  def test(scope: String = "test,it") = Seq(
    hmrc                     %% "bootstrap-test-play-28"   % "5.12.0"                 % Test,
    hmrc                     %% "reactivemongo-test"       % "4.21.0-play-27"         % scope,
    hmrc                     %% "service-integration-test" % "0.12.0-play-27"         % scope,
    "org.pegdown"            % "pegdown"                   % "1.6.0"                  % scope,
    "org.mockito"            %% "mockito-scala-scalatest"  % "1.16.37" % Test,
    "com.typesafe.play"      %% "play-test"                % PlayVersion.current      % scope,
    "org.scalaj"             %% "scalaj-http"              % "2.4.2"                  % scope,
    "com.github.tomakehurst" % "wiremock-jre8"             % "2.27.2"                 % scope,
    "org.scalatest"          %% "scalatest"                % "3.2.9"                  % Test,
    "com.typesafe.play"      %% "play-test"                % current                  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"              % "0.35.10"                % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"                  % scope
  )

}
