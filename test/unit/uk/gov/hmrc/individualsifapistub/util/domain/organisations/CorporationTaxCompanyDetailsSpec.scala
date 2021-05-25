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
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxCompanyDetails._
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, CommunicationDetails, CreateCorporationTaxCompanyDetailsRequest, Name, RegisteredDetails}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class CorporationTaxCompanyDetailsSpec extends UnitSpec {

  "RegisteredDetails reads from JSON successfully" in {
    val json =
      """
        |{
        |    "name": {
        |      "name1": "Waitrose",
        |      "name2": "And Partners"
        |    },
        |    "address": {
        |      "line1": "Alfie House",
        |      "line2": "Main Street",
        |      "line3": "Manchester",
        |      "line4": "Londonberry",
        |      "postcode": "LN1 1AG"
        |    }
        |}
        |""".stripMargin

    val expectedResult = RegisteredDetails(
      Name("Waitrose", "And Partners"),
      Address(
        Some("Alfie House"),
        Some("Main Street"),
        Some("Manchester"),
        Some("Londonberry"),
        Some("LN1 1AG")
      )
    )

    val result = Json.parse(json).validate[RegisteredDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CommunicationDetails reads from JSON successfully" in {
    val json =
      """
        |{
        |    "name": {
        |      "name1": "Waitrose",
        |      "name2": "And Partners"
        |    },
        |    "address": {
        |      "line1": "Alfie House",
        |      "line2": "Main Street",
        |      "line3": "Manchester",
        |      "line4": "Londonberry",
        |      "postcode": "LN1 1AG"
        |    }
        |}
        |""".stripMargin

    val expectedResult = CommunicationDetails(
      Name("Waitrose", "And Partners"),
      Address(
        Some("Alfie House"),
        Some("Main Street"),
        Some("Manchester"),
        Some("Londonberry"),
        Some("LN1 1AG")
      )
    )

    val result = Json.parse(json).validate[CommunicationDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "RegisteredDetails reads from JSON unsuccessfully when address field is incorrect" in {
    val json =
      """
        |    {
        |      "name": {
        |         "name1": "Matty",
        |         "name2": "Harris"
        |        },
        |       "address": {
        |         "line1": "test1",
        |         "line2": "test2",
        |         "line3": "test3",
        |         "line4": "test4",
        |         "postcode": "01234567890"
        |        }
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[RegisteredDetails]

    result.isSuccess shouldBe false
  }

  "RegisteredDetails reads from JSON unsuccessfully when name field is incorrect" in {
    val json =
      """
        |    {
        |      "name": {
        |         "name1": "|~",
        |         "name2": "Harris"
        |        },
        |       "address": {
        |         "line1": "test1",
        |         "line2": "test2",
        |         "line3": "test3",
        |         "line4": "test4",
        |         "postcode": "testPost"
        |        }
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[RegisteredDetails]

    result.isSuccess shouldBe true
  }

  "CommunicationDetails reads from JSON unsuccessfully when address field is incorrect" in {
    val json =
      """
        |    {
        |      "name": {
        |         "name1": "Matty",
        |         "name2": "Harris"
        |        },
        |       "address": {
        |         "line1": 1,
        |         "line2": "test2",
        |         "line3": "test3",
        |         "line4": "test4",
        |         "postcode": "testPost"
        |        }
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[CommunicationDetails]

    result.isSuccess shouldBe false
  }

  "CreateCorporationTaxCompanyDetailsRequest reads from JSON successfully" in {
    val json =
      """
        |    {
        |      "utr": "1234567890",
        |      "crn": "12345678"
        |    }
        |""".stripMargin

    val expectedResult = CreateCorporationTaxCompanyDetailsRequest("1234567890", "12345678", None, None)

    val result = Json.parse(json).validate[CreateCorporationTaxCompanyDetailsRequest]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CreateCorporationTaxCompanyDetailsRequest reads from JSON unsuccessfully when crn is incorrect" in {
    val json =
      """
        |    {
        |      "utr": "123456789",
        |      "crn": "|~"
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[CreateCorporationTaxCompanyDetailsRequest]

    result.isSuccess shouldBe false
  }

  "CorporationTaxCompanyDetails reads from JSON unsuccessfully when utr is incorrect" in {
    val json =
      """
        |    {
        |      "utr": "|~",
        |      "crn": "12345679"
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[CreateCorporationTaxCompanyDetailsRequest]

    result.isSuccess shouldBe false
  }
}
