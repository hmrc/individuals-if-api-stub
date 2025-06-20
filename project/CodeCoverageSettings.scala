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

import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {
  val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "app.*",
    "prod.*",
    "definition",
    "testOnlyDoNotUseInAppConf",
    "uk.gov.hmrc.individualsifapistub.views.txt",
    "uk.gov.hmrc.individualsifapistub.Binders"
  )

  val settings: Seq[Setting[?]] = Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
