import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val hmrc = "uk.gov.hmrc"
  val hmrcMongo = "uk.gov.hmrc.mongo"
  val akkaHttpVersion = "10.2.6"
  val hmrcMongoVersion = "0.73.0"
  val bootstrapVersion = "7.23.0"

  val compile = Seq(
    ws,
    hmrc                %% "bootstrap-backend-play-28" % bootstrapVersion,
    hmrc                %% "domain"                    % "8.1.0-play-28",
    hmrc                %% "play-hal"                  % "3.4.0-play-28",
    hmrc                %% "play-hmrc-api"             % "7.1.0-play-28",
    hmrc                %% "json-encryption"           % "5.1.0-play-28",
    hmrcMongo           %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "com.typesafe.play" %% "play-json-joda"            % "2.9.2"
  )

  def test(scope: String = "test,it") = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"          % scope,
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.1.0"        % scope,
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.35.10"        % scope,
    "org.scalaj"             %% "scalaj-http"             % "2.4.2"          % scope,
    "org.pegdown"            % "pegdown"                  % "1.6.0"          % scope,
    "com.github.tomakehurst" % "wiremock-jre8"            % "2.27.2"         % scope,
    hmrcMongo                %% "hmrc-mongo-test-play-28" % hmrcMongoVersion % scope,
    hmrc                     %% "bootstrap-test-play-28"  % bootstrapVersion % scope
  )
}
