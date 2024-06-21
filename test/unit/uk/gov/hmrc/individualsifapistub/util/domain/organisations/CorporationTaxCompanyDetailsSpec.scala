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
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxCompanyDetails._
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, CorporationTaxCompanyDetails, Name, NameAddressDetails}
import uk.gov.hmrc.individualsifapistub.domain.{TestAddress, TestOrganisation, TestOrganisationDetails}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class CorporationTaxCompanyDetailsSpec extends UnitSpec {

  "NameAddressDetails reads from JSON successfully" in {
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

    val expectedResult = NameAddressDetails(
      Name("Waitrose", "And Partners"),
      Address(
        Some("Alfie House"),
        Some("Main Street"),
        Some("Manchester"),
        Some("Londonberry"),
        Some("LN1 1AG")
      )
    )

    val result = Json.parse(json).validate[NameAddressDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "NameAddressDetails reads from JSON unsuccessfully when address field is incorrect" in {
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

    val result = Json.parse(json).validate[NameAddressDetails]

    result.isSuccess shouldBe false
  }

  "NameAddressDetails reads from JSON unsuccessfully when name field is incorrect" in {
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

    val result = Json.parse(json).validate[NameAddressDetails]

    result.isSuccess shouldBe true
  }

  "CreateCorporationTaxCompanyDetailsRequest reads from JSON unsuccessfully when crn is incorrect" in {
    val json =
      """
        |    {
        |      "utr": "123456789",
        |      "crn": "|~"
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[CorporationTaxCompanyDetails]

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

    val result = Json.parse(json).validate[CorporationTaxCompanyDetails]

    result.isSuccess shouldBe false
  }

  "CreateCorporationTaxCompanyDetailsRequest converts successfully from TestOrganisation" in {
    val empRef = EmpRef("123", "AI45678")
    val testOrganisation = TestOrganisation(
      Some(empRef),
      Some("0123456789"),
      Some("123456789"),
      TestOrganisationDetails("Disney Inc", TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ"))
    )

    val expectedResult = CorporationTaxCompanyDetails(
      "0123456789",
      "123456789",
      Some(
        NameAddressDetails(
          Name("Disney Inc", ""),
          Address(
            Some("Capital Tower"),
            Some("Aberdeen"),
            None,
            None,
            Some("SW1 4DQ")
          )
        )
      ),
      None
    )

    val result = CorporationTaxCompanyDetails.fromApiPlatformTestUser(testOrganisation)

    result shouldBe expectedResult
  }

}
