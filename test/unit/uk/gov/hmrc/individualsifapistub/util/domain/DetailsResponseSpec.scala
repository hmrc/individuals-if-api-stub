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
import uk.gov.hmrc.individualsifapistub.domain.{ContactDetail, Details, DetailsResponse, Residence}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec
import uk.gov.hmrc.individualsifapistub.domain.DetailsResponse._

class DetailsResponseSpec extends UnitSpec with AddressHelpers {

  val idValue = "2432552635"

  val details = Details(Some("XH123456A"), None)
  val contactDetail1 = ContactDetail(9, "MOBILE TELEPHONE", "07123 987654")
  val contactDetail2 = ContactDetail(9,"MOBILE TELEPHONE", "07123 987655")
  val residence1 = Residence(Some("BASE"),createAddress(2))
  val residence2 = Residence(Some("NOMINATED"),createAddress(1))
  val response = DetailsResponse(
    details,
    Some(Seq(contactDetail1, contactDetail1)),
    Some(Seq(residence1, residence2))
  )

  "Details" should {
    "Write to JSON" in {
      println(Json.toJson(details).validate[Details])
      println(Json.prettyPrint(Json.toJson(details)))
    }
  }

  "Contact details" should {
    "Write to JSON" in {
      println(Json.prettyPrint(Json.toJson(contactDetail1)))
    }
  }

  "Residence details" should {
    "Write to JSON" in {
      println(Json.prettyPrint(Json.toJson(residence1)))
    }
  }

  "Details Response" should {
    "Write to JSON" in {
      println(Json.prettyPrint(Json.toJson(response)))
    }
  }
}
