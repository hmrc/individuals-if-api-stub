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

package unit.uk.gov.hmrc.individualsifapistub.util.domain.income

import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain.{Address, SaIncome, SaReturnType, SaTaxYearEntry}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec
import uk.gov.hmrc.individualsifapistub.domain.IncomeSa._

class SaTaxYearEntrySpec extends UnitSpec {

  val returnTypeList = Seq(createValidSaReturnType())

  val validSaTaxYearEntry = SaTaxYearEntry(Some("2020"), Some(100.01), Some(returnTypeList))
  val invalidSaTaxYearEntry = SaTaxYearEntry(Some("-42"), Some(100.001), Some(returnTypeList))

  "SaTaxYearEntry" should {
    "WriteToJson" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "taxYear" : "2020",
          |  "income" : 100.01,
          |  "returnList" : [ {
          |    "utr" : "1234567890",
          |    "caseStartDate" : "2020-01-01",
          |    "receivedDate" : "2020-01-01",
          |    "businessDescription" : "This is a business description",
          |    "telephoneNumber" : "12345678901",
          |    "busStartDate" : "2020-01-01",
          |    "busEndDate" : "2020-01-30",
          |    "totalTaxPaid" : 100.01,
          |    "totalNIC" : 100.01,
          |    "turnover" : 100.01,
          |    "otherBusIncome" : 100.01,
          |    "tradingIncomeAllowance" : 100.01,
          |    "address" : {
          |      "line1" : "line1",
          |      "line2" : "line2",
          |      "line3" : "line3",
          |      "line4" : "line4",
          |      "postcode" : "QW123QW"
          |    },
          |    "income" : {
          |      "selfAssessment" : 100,
          |      "allEmployments" : 100,
          |      "ukInterest" : 100,
          |      "foreignDivs" : 100,
          |      "ukDivsAndInterest" : 100,
          |      "partnerships" : 100,
          |      "pensions" : 100,
          |      "selfEmployment" : 100,
          |      "trusts" : 100,
          |      "ukProperty" : 100,
          |      "foreign" : 100,
          |      "lifePolicies" : 100,
          |      "shares" : 100,
          |      "other" : 100
          |    }
          |  } ]
          |}
          |""".stripMargin
      )

      val result = Json.toJson(validSaTaxYearEntry)
      result shouldBe expectedJson
    }

    "Validate successfully when provided valid SaTaxYearEntry" in {
      val result = Json.toJson(validSaTaxYearEntry).validate[SaTaxYearEntry]
      result.isSuccess shouldBe true
    }

    "Validate unsuccessfully when provided invalid SaTaxYearEntry" in {
      val result = Json.toJson(invalidSaTaxYearEntry).validate[SaTaxYearEntry]
      result.isError shouldBe true
    }
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
      Some(Address(Some("line1"), Some("line2"), Some("line3"), Some("line4"), Some("QW123QW"))),
      Some(validSaIncome)
    )
  }
}
