/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsifapistub.util.testUtils

import uk.gov.hmrc.individualsifapistub.domain.{Address, SaIncome, SaReturnType, SaTaxYearEntry}

trait IncomeSaHelpers {
  def createValidSaTaxYearEntry() = {
    val returnTypeList = Seq(createValidSaReturnType())
    SaTaxYearEntry(Some("2020"), Some(100.01), Some(returnTypeList))
  }

  private def createValidSaReturnType() = {
    val validSaIncome = SaIncome(
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0),
      Some(100.0)
    )

    SaReturnType(
      Some("1234567890"),
      Some("2020-01-01"),
      Some("2020-01-01"),
      Some("This is a business description"),
      Some("12345678901"),
      Some("2020-01-01"),
      Some("2020-01-30"),
      Some(100.01),
      Some(100.01),
      Some(100.01),
      Some(100.01),
      Some(100.01),
      Some(Address(Some("line1"), Some("line2"), Some("line3"), Some("line4"), None, Some("QW123QW"))),
      Some(validSaIncome)
    )
  }
}
