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

package unit.uk.gov.hmrc.individualsifapistub.util.domain.individuals

import play.api.libs.json.Json
import testUtils.TestHelpers
import uk.gov.hmrc.individualsifapistub.domain.individuals.DetailsResponse._
import uk.gov.hmrc.individualsifapistub.domain.individuals.{ContactDetail, DetailsResponse, Identifier, Residence}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class DetailsResponseSpec extends UnitSpec with TestHelpers {

  val idValue = "2432552635"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"

  val ninoDetails: Identifier = Identifier(Some("XH123456A"), None, Some(startDate), Some(endDate), Some(useCase))
  val idNino: String = s"${ninoDetails.nino.getOrElse(ninoDetails.trn)}-$startDate-$endDate-$useCase"
  val trnDetails: Identifier = Identifier(None, Some("12345678"), Some(startDate), Some(endDate), Some(useCase))
  val idTrn: String = s"${trnDetails.nino.getOrElse(trnDetails.trn)}-$startDate-$endDate-$useCase"

  val contactDetail1: ContactDetail = ContactDetail(9, "MOBILE TELEPHONE", "07123 987654")
  val contactDetail2: ContactDetail = ContactDetail(9, "MOBILE TELEPHONE", "07123 987655")
  val residence1: Residence = Residence(`type` = Some("BASE"), address = generateAddress(2))
  val residence2: Residence = Residence(`type` = Some("NOMINATED"), address = generateAddress(1))

  val response: DetailsResponse = DetailsResponse(
    idNino,
    Some(Seq(contactDetail1, contactDetail1)),
    Some(Seq(residence1, residence2))
  )

  val invalidNinoDetails: Identifier =
    Identifier(Some("QWERTYUIOP"), None, Some(startDate), Some(endDate), Some(useCase))
  val invalidTrnDetails: Identifier =
    Identifier(None, Some("QWERTYUIOP"), Some(startDate), Some(endDate), Some(useCase))
  val invalidContactDetail: ContactDetail = ContactDetail(-42, "abcdefghijklmnopqrstuvwxyz0123456789", "a")
  val invalidResidence: Residence = Residence(`type` = Some(""), address = generateAddress(2))
  val invalidDetailsResponse: DetailsResponse = DetailsResponse(
    idTrn,
    Some(Seq(invalidContactDetail)),
    Some(Seq(invalidResidence))
  )

  "Details" should {
    "Write to JSON when only nino provided" in {
      val result = Json.toJson(ninoDetails)
      val expectedJson = Json.parse("""
                                      |{
                                      |  "nino":"XH123456A",
                                      |  "from":"2020-01-01",
                                      |  "to":"2020-21-31",
                                      |  "useCase":"TEST"
                                      |}
        """.stripMargin)
      result shouldBe expectedJson
    }

    "Write to JSON when only trn provided" in {
      val result = Json.toJson(trnDetails)
      val expectedJson = Json.parse("""
                                      |{
                                      |  "trn":"12345678",
                                      |  "from":"2020-01-01",
                                      |  "to":"2020-21-31",
                                      |  "useCase":"TEST"
                                      |}
        """.stripMargin)

      result shouldBe expectedJson
    }

    "Validate successful when reading valid nino" in {
      val result = Json.toJson(ninoDetails).validate[Identifier]
      result.isSuccess shouldBe true
    }

    "Validate successful when reading valid trn" in {
      val result = Json.toJson(trnDetails).validate[Identifier]
      result.isSuccess shouldBe true
    }

    "Validate unsuccessfully when reading invalid nino" in {
      val result = Json.toJson(invalidNinoDetails).validate[Identifier]
      result.isError shouldBe true
    }

    "Validate unsuccessfully when reading invalid trn" in {
      val result = Json.toJson(invalidTrnDetails).validate[Identifier]
      result.isError shouldBe true
    }

  }

  "Contact details" should {
    "Write to JSON" in {
      val result = Json.toJson(contactDetail1)
      val expectedJson = Json.parse("""
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
      val expectedJson = Json.parse("""
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
      val expectedJson = Json.parse("""
                                      |  {
                                      |    "details" : "XH123456A-2020-01-01-2020-21-31-TEST",
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
