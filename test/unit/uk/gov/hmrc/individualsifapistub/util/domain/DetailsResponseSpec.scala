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

package unit.uk.gov.hmrc.individualsifapistub.util.domain


import play.api.libs.json.Json
import testUtils.AddressHelpers
import uk.gov.hmrc.individualsifapistub.domain.DetailsResponse._
import uk.gov.hmrc.individualsifapistub.domain.{ContactDetail, Details, DetailsResponse, Residence}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class DetailsResponseSpec extends UnitSpec with AddressHelpers {

  val idValue = "2432552635"

  val ninoDetails = Details(Some("XH123456A"), None)
  val trnDetails = Details(None, Some("12345678"))
  val contactDetail1 = ContactDetail(9, "MOBILE TELEPHONE", "07123 987654")
  val contactDetail2 = ContactDetail(9, "MOBILE TELEPHONE", "07123 987655")
  val residence1 = Residence(residenceType = Some("BASE"), address = createAddress(2))
  val residence2 = Residence(residenceType = Some("NOMINATED"), address = createAddress(1))
  val response = DetailsResponse(
    ninoDetails,
    Some(Seq(contactDetail1, contactDetail1)),
    Some(Seq(residence1, residence2))
  )

  val invalidNinoDetails = Details(Some("QWERTYUIOP"), None)
  val invalidTrnDetails = Details(None, Some("QWERTYUIOP"))
  val invalidContactDetail = ContactDetail(-42, "abcdefghijklmnopqrstuvwxyz0123456789", "a")
  val invalidResidence = Residence(residenceType =  Some(""), address = createAddress(2))
  val invalidDetailsResponse = DetailsResponse(
    invalidNinoDetails,
    Some(Seq(invalidContactDetail)),
    Some(Seq(invalidResidence))
  )


  "Details" should {
    "Write to JSON when only nino provided" in {
      val result = Json.toJson(ninoDetails)
      val expectedJson = Json.parse(
        """
          |{
          |   "nino" : "XH123456A"
          |}
        """.stripMargin)
      result shouldBe expectedJson
    }

    "Write to JSON when only trn provided" in {
      val result = Json.toJson(trnDetails)
      val expectedJson = Json.parse(
        """
          |{
          |  "trn" : "12345678"
          |}
        """.stripMargin)

      result shouldBe expectedJson
    }

    "Validate successful when reading valid nino" in {
      val result = Json.toJson(ninoDetails).validate[Details]
      result.isSuccess shouldBe true
    }

    "Validate successful when reading valid trn" in {
      val result = Json.toJson(trnDetails).validate[Details]
      result.isSuccess shouldBe true
    }

    "Validate unsuccessfully when reading invalid nino" in {
      val result = Json.toJson(invalidNinoDetails).validate[Details]
      result.isError shouldBe true
    }

    "Validate unsuccessfully when reading invalid trn" in {
      val result = Json.toJson(invalidTrnDetails).validate[Details]
      result.isError shouldBe true
    }

  }

  "Contact details" should {
    "Write to JSON" in {
      val result = Json.toJson(contactDetail1)
      val expectedJson = Json.parse(
        """
          |{
          |  "code" : 9,
          |  "type" : "MOBILE TELEPHONE",
          |  "detail" : "07123 987654"
          |}"""".stripMargin)

      result shouldBe expectedJson
    }

    "Validate successfully when reading valid contact details" in {
      val result = Json.toJson(contactDetail1).validate[ContactDetail]
      result.isSuccess shouldBe true
    }

    "Validate unsuccessfully when reading invalid contact details" in {
      val result = Json.toJson(invalidContactDetail).validate[ContactDetail]
      result.isError shouldBe true
    }
  }

  "Residence details" should {
    "Write to JSON" in {
      val result = Json.toJson(residence1)
      val expectedJson = Json.parse(
        """
          |{
          |  "type" : "BASE",
          |  "address" : {
          |      "line1" : "line1-2",
          |      "line2" : "line2-2",
          |      "line3" : "line3-2",
          |      "line4" : "line4-2",
          |      "line5" : "line5-2",
          |      "postcode" : "QW122QW"
          |  }
          |}
        """.stripMargin)

      result shouldBe expectedJson
    }

    "Validate successfully when reading valid Residence information" in {
      val result = Json.toJson(residence1).validate[Residence]
      result.isSuccess shouldBe true
    }

    "Validate unsuccessfully when reading invalid Residence information" in {
      val result = Json.toJson(invalidResidence).validate[Residence]
      result.isError shouldBe true
    }

  }

  "Details Response" should {
    "Write to JSON" in {
      val result = Json.toJson(response)
      val expectedJson = Json.parse(
        """
          |  {
          |    "details" : {
          |       "nino" : "XH123456A"
          |     },
          |     "contactDetails" : [
          |       {
          |         "code" : 9,
          |         "type" : "MOBILE TELEPHONE",
          |         "detail" : "07123 987654"
          |       },
          |       {
          |         "code" : 9,
          |         "type" : "MOBILE TELEPHONE",
          |         "detail" : "07123 987654"
          |       }
          |     ],
          |     "residence" : [
          |       {
          |         "type" : "BASE",
          |         "address" : {
          |           "line1" : "line1-2",
          |           "line2" : "line2-2",
          |           "line3" : "line3-2",
          |           "line4" : "line4-2",
          |           "line5" : "line5-2",
          |           "postcode" : "QW122QW"
          |          }
          |        },
          |        {
          |          "type" : "NOMINATED",
          |          "address" : {
          |            "line1" : "line1-1",
          |            "line2" : "line2-1",
          |            "line3" : "line3-1",
          |            "line4" : "line4-1",
          |            "line5" : "line5-1",
          |            "postcode" : "QW121QW"
          |          }
          |        } ]
          |      }""".stripMargin)

      result shouldBe expectedJson
    }

    "Validates successfully when reading valid Details Response" in {
      val result = Json.toJson(response).validate[DetailsResponse]
      result.isSuccess shouldBe true
    }

    "Validates unsuccessfully when reading invalid Details Response" in {
      val result = Json.toJson(invalidDetailsResponse).validate[DetailsResponse]
      result.isError shouldBe true
    }

  }
}
