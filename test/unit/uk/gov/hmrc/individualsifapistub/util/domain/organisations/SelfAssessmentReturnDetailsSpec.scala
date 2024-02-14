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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{CreateSelfAssessmentReturnDetailRequest, TaxYear}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class SelfAssessmentReturnDetailsSpec extends UnitSpec {

  "TaxYear read from JSON successfully" in {
    val json =
      """
        |{
        |   "taxyear": "2019",
        |   "businessSalesTurnover": 12343.12
        |}
        |""".stripMargin

    val expectedResult = TaxYear("2019", 12343.12)

    val result = Json.parse(json).validate[TaxYear]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "TaxYear read from JSON unsuccessfully when TaxYear is invalid" in {
    val json =
      """
        |{
        |   "taxyear": "4019",
        |   "businessSalesTurnover": 12343.12
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[TaxYear]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON successfully" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val expectedResult =
      CreateSelfAssessmentReturnDetailRequest("1234567890", "2015-04-21", "Individual", "S", Seq.empty)

    val result = Json.parse(json).validate[CreateSelfAssessmentReturnDetailRequest]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if tax solvency status is not S or I" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "M",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[CreateSelfAssessmentReturnDetailRequest]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if given an invalid start date" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "20111-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[CreateSelfAssessmentReturnDetailRequest]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if given an invalid utr" in {
    val json =
      """
        |{
        |  "utr": "12345678901",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[CreateSelfAssessmentReturnDetailRequest]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if given an invalid taxPayerType" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "2Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[CreateSelfAssessmentReturnDetailRequest]

    result.isSuccess shouldBe false
  }

}
