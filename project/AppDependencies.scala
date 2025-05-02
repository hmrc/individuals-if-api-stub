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

import sbt.*

object AppDependencies {
  val hmrc             = "uk.gov.hmrc"
  val hmrcMongo        = "uk.gov.hmrc.mongo"
  val hmrcMongoVersion = "1.7.0"
  val bootstrapVersion = "9.11.0"
  val playVersion      = "play-30"

  val compile: Seq[ModuleID] = Seq(
    hmrc      %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    hmrc      %% s"domain-$playVersion"            % "9.0.0",
    hmrc      %% s"play-hal-$playVersion"          % "4.0.0",
    hmrcMongo %% s"hmrc-mongo-$playVersion"        % hmrcMongoVersion
  )

  def test(scope: Configuration = Test): Seq[ModuleID] = Seq(
    "org.scalaj"             %% "scalaj-http"                    % "2.4.2"          % scope,
    hmrcMongo                %% s"hmrc-mongo-test-$playVersion"  % hmrcMongoVersion % scope,
    hmrc                     %% s"bootstrap-test-$playVersion"   % bootstrapVersion % scope
  )
}
