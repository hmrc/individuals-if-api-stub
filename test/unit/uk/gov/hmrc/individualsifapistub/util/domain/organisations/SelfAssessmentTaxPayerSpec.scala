/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.individualsifapistub.domain.organisations.SelfAssessmentTaxPayer._
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, CreateSelfAssessmentTaxPayerRequest, TaxPayerDetails}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class SelfAssessmentTaxPayerSpec extends UnitSpec {

  val address = Address(
    Some("line1"),
    Some("line2"),
    Some("line3"),
    Some("line4"),
    Some("postcode")
  )

  "TaxPayerDetails read from JSON successfully" in {
    val json =
      """
        |{
        |   "name": "John Smith",
        |   "addressType": "Individual",
        |   "address" : {
        |     "line1" : "line1",
        |     "line2" : "line2",
        |     "line3" : "line3",
        |     "line4" : "line4",
        |     "postcode" : "postcode"
        |  }
        |}
        |""".stripMargin

    val expectedResult = TaxPayerDetails("John Smith", "Individual", address)

    val result = Json.parse(json).validate[TaxPayerDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "TaxPayerDetails read from JSON unsuccessfully if addressType is invalid" in {
    val json =
      """
        |{
        |   "name": "John Smith",
        |   "addressType": "~`",
        |    "address" : {
        |      "line1" : "line1",
        |      "line2" : "line2",
        |      "line3" : "line3",
        |      "line4" : "line4",
        |      "postcode" : "postcode"
        |  }
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[TaxPayerDetails]

    result.isSuccess shouldBe false
  }

  "TaxPayerDetails read from JSON unsuccessfully if address is invalid" in {
    val json =
      """
        |{
        |   "name": "John Smith",
        |   "addressType": "Individual",
        |    "address" : {
        |      "line1" : "line1",
        |      "line2" : "line2",
        |      "line3" : "line3",
        |      "line4" : "line4",
        |      "postcode" : "12345678901"
        |  }
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[TaxPayerDetails]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentTaxPayerRequest read from JSON successfully" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxPayerType": "Individual",
        |  "taxPayerDetails": []
        |}""".stripMargin

    val expectedResult = CreateSelfAssessmentTaxPayerRequest("1234567890", "Individual", Seq.empty)

    val result = Json.parse(json).validate[CreateSelfAssessmentTaxPayerRequest]

    println(result)
    println(expectedResult)
    println(result.isSuccess)
    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CreateSelfAssessmentTaxPayerRequest read from JSON unsuccessfully because of invalid utr" in {
    val json =
      """
        |{
        |  "utr": "12345678901",
        |  "taxpayerType": "Individual",
        |  "taxPayerDetails": []
        |}""".stripMargin

    val result = Json.parse(json).validate[CreateSelfAssessmentTaxPayerRequest]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentTaxPayerRequest read from JSON unsuccessfully because of invalid taxPayerType" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxpayerType": "1",
        |  "taxPayerDetails": []
        |}""".stripMargin

    val result = Json.parse(json).validate[CreateSelfAssessmentTaxPayerRequest]

    result.isSuccess shouldBe false
  }
}
