import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val hmrc = "uk.gov.hmrc"
  val hmrcMongo = s"$hmrc.mongo"
  val hmrcMongoVersion = "0.73.0"
  val bootstrapVersion = "7.23.0"
  val playVersion = "play-28"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc      %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    hmrc      %% "domain"                          % s"8.1.0-$playVersion",
    hmrc      %% "play-hal"                        % s"3.4.0-$playVersion",
    hmrc      %% "play-hmrc-api"                   % s"7.1.0-$playVersion",
    hmrc      %% "json-encryption"                 % s"5.1.0-$playVersion",
    hmrcMongo %% s"hmrc-mongo-$playVersion"        % hmrcMongoVersion
  )

  def test(scope: Configuration = Test): Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play"            % "5.1.0"          % scope,
    "org.scalatestplus"      %% "mockito-3-4"                   % "3.2.1.0"        % scope,
    "com.vladsch.flexmark"   % "flexmark-all"                   % "0.35.10"        % scope,
    "org.scalaj"             %% "scalaj-http"                   % "2.4.2"          % scope,
    "org.pegdown"            % "pegdown"                        % "1.6.0"          % scope,
    "com.github.tomakehurst" % "wiremock-jre8"                  % "2.27.2"         % scope,
    hmrcMongo                %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion % scope,
    hmrc                     %% s"bootstrap-test-$playVersion"  % bootstrapVersion % scope
  )
}
