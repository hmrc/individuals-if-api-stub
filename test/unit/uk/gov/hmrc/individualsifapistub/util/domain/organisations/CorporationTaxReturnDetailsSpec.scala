/*
 * Copyright 2023 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsifapistub.util.domain.organisations

import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain.organisations.{AccountingPeriod, CorporationTaxReturnDetailsResponse, CreateCorporationTaxReturnDetailsRequest}
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxReturnDetails._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class CorporationTaxReturnDetailsSpec extends UnitSpec {
  "AccountingPeriod reads from JSON successfully" in {
    val json =
      """
        |    {
        |      "apStartDate": "2018-04-06",
        |      "apEndDate": "2018-10-05",
        |      "turnover": 38390
        |    }
        |""".stripMargin

    val expectedResult = AccountingPeriod("2018-04-06", "2018-10-05", 38390)

    val result = Json.parse(json).validate[AccountingPeriod]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "AccountingPeriod reads from JSON unsuccessfully when startDate is incorrect" in {
    val json =
      """
        |    {
        |      "apStartDate": "20111-04-06",
        |      "apEndDate": "2018-10-05",
        |      "turnover": 38390
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[AccountingPeriod]

    result.isSuccess shouldBe false
  }

  "AccountingPeriod reads from JSON unsuccessfully when endDate is incorrect" in {
    val json =
      """
        |    {
        |      "apStartDate": "2018-04-06",
        |      "apEndDate": "20111-10-05",
        |      "turnover": 38390
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[AccountingPeriod]

    result.isSuccess shouldBe false
  }

  "CreateCorporationTaxReturnDetailsRequest reads from JSON successfully" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val expectedResult = CreateCorporationTaxReturnDetailsRequest("1234567890", "2015-04-21", "V", Seq.empty)

    val result = Json.parse(json).validate[CreateCorporationTaxReturnDetailsRequest]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CreateCorporationTaxReturnDetailsRequest reads from JSON unsuccessfully when utr is incorrect" in {
    val json =
      """
        |{
        |  "utr": "123456789A",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[CreateCorporationTaxReturnDetailsRequest]

    result.isSuccess shouldBe false
  }

  "CreateCorporationTaxReturnDetailsRequest reads from JSON unsuccessfully when taxpayerStartDate is incorrect" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxpayerStartDate": "20111-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[CreateCorporationTaxReturnDetailsRequest]

    result.isSuccess shouldBe false
  }

  "CreateCorporationTaxReturnDetailsRequest reads from JSON unsuccessfully when tax solvency status is not one of V, S, I, A" in {
    val json =
      """
        |{
        |  "utr": "123456789A",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "X",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[CreateCorporationTaxReturnDetailsRequest]

    result.isSuccess shouldBe false
  }

  "CorporationTaxReturnDetailsResponse reads from JSON successfully" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val expectedResult = CorporationTaxReturnDetailsResponse("1234567890", "2015-04-21", "V", Seq.empty)

    val result = Json.parse(json).validate[CorporationTaxReturnDetailsResponse]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CorporationTaxReturnDetailsResponse reads from JSON unsuccessfully when utr is incorrect" in {
    val json =
      """
        |{
        |  "utr": "123456789A",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[CorporationTaxReturnDetailsResponse]

    result.isSuccess shouldBe false
  }

  "CorporationTaxReturnDetailsResponse reads from JSON unsuccessfully when taxpayerStartDate is incorrect" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxpayerStartDate": "20111-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[CorporationTaxReturnDetailsResponse]

    result.isSuccess shouldBe false
  }

  "CorporationTaxReturnDetailsResponse reads from JSON unsuccessfully when tax solvency status is not one of V, S, I, A" in {
    val json =
      """
        |{
        |  "utr": "123456789A",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "X",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[CorporationTaxReturnDetailsResponse]

    result.isSuccess shouldBe false
  }
}
