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
import uk.gov.hmrc.individualsifapistub.domain.organisations._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class VatInformationSpec extends UnitSpec {

  "VatCustomerDetails reads successfully from json" in {

    val json = Json.obj("organisationName" -> "Ancient Antiques")
    val expectedResult = VatCustomerDetails("Ancient Antiques")
    val result = json.validate[VatCustomerDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "VatCustomerDetails reads unsuccessfully from json when organisationName is incorrect" in {

    val json = Json.obj("organisationName" -> 1)
    val result = json.validate[VatCustomerDetails]
    result.isSuccess shouldBe false
  }

  "VatAddress reads successfully from json" in {

    val json = Json.obj("line1" -> "VAT ADDR 1", "postCode" -> "SW1A 2BQ")
    val expectedResult = VatAddress("VAT ADDR 1", "SW1A 2BQ")
    val result = json.validate[VatAddress]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "VatAddress reads unsuccessfully from json when line1 is incorrect" in {

    val json = Json.obj("line1" -> 1, "postCode" -> "SW1A 2BQ")
    val result = json.validate[VatAddress]
    result.isSuccess shouldBe false
  }

  "VatAddress reads unsuccessfully from json when postCode is incorrect" in {

    val json = Json.obj("line1" -> "VAT ADDR 1", "postCode" -> 1)
    val result = json.validate[VatAddress]
    result.isSuccess shouldBe false
  }

  "VatPPOB reads successfully from json" in {

    val json =
      """
        |   {
        |     "address": {
        |       "line1": "VAT ADDR 1",
        |       "postCode": "SW1A 2BQ"
        |        }
        |   }
        |""".stripMargin

    val expectedResult = VatPPOB(VatAddress("VAT ADDR 1", "SW1A 2BQ"))
    val result = Json.parse(json).validate[VatPPOB]

    result.get shouldBe expectedResult
    result.isSuccess shouldBe true
  }

  "VatPPOB reads unsuccessfully from json when address line1" in {

    val json =
      """
        |   {
        |     "address": {
        |       "line1": 2,
        |       "postCode": "SW1A 2BQ"
        |        }
        |   }
        |""".stripMargin

    val result = Json.parse(json).validate[VatPPOB]

    result.isSuccess shouldBe false
  }

  "VatPPOB reads unsuccessfully from json when address postCode" in {

    val json =
      """
        |   {
        |     "address": {
        |       "line1": "VAT ADDR 1",
        |       "postCode": 2
        |        }
        |   }
        |""".stripMargin

    val result = Json.parse(json).validate[VatPPOB]

    result.isSuccess shouldBe false
  }

  "VatApprovedInformation reads successfully from json" in {

    val json =
      """
        |{
        |   "customerDetails": {
        |      "organisationName": "Ancient Antiques"
        |   },
        |   "PPOB": {
        |      "address": {
        |         "line1": "VAT ADDR 1",
        |         "postCode": "SW1A 2BQ"
        |      }
        |   }
        |}
        |""".stripMargin

    val expectedResult = VatApprovedInformation(
      customerDetails = VatCustomerDetails(organisationName = "Ancient Antiques"),
      PPOB = VatPPOB(address = VatAddress("VAT ADDR 1", "SW1A 2BQ"))
    )

    val result = Json.parse(json).validate[VatApprovedInformation]

    result.get shouldBe expectedResult
    result.isSuccess shouldBe true
  }

  "VatApprovedInformation reads unsuccessfully from json when customerDetails is incorrect" in {

    val json =
      """
        |{
        |   "customerDetails": {
        |      "organisationName": 1
        |   },
        |   "PPOB": {
        |      "address": {
        |         "line1": "VAT ADDR 1",
        |         "postCode": "SW1A 2BQ"
        |      }
        |   }
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatApprovedInformation]

    result.isSuccess shouldBe false

  }

  "VatInformation reads successfully from json" in {

    val json =
      """
        |{
        |   "approvedInformation": {
        |      "customerDetails": {
        |         "organisationName": "Ancient Antiques"
        |      },
        |      "PPOB": {
        |         "address": {
        |            "line1": "VAT ADDR 1",
        |            "postCode": "SW1A 2BQ"
        |         }
        |      }
        |   }
        |}
        |""".stripMargin

    val expectedResult = VatInformation(
      VatApprovedInformation(
        customerDetails = VatCustomerDetails(organisationName = "Ancient Antiques"),
        PPOB = VatPPOB(address = VatAddress("VAT ADDR 1", "SW1A 2BQ"))
      )
    )

    val result = Json.parse(json).validate[VatInformation]

    result.get shouldBe expectedResult
    result.isSuccess shouldBe true
  }

}
