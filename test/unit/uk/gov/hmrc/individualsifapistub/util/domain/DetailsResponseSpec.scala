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

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import testUtils.AddressHelpers
import uk.gov.hmrc.individualsifapistub.domain.{ContactDetail, CreateDetailsRequest, Details, DetailsResponse, Residence}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class DetailsResponseSpec extends UnitSpec with AddressHelpers {

  val idValue = "2432552635"
  val request = DetailsResponse(Details(Some("NI123412N"), None),
    Some(Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9,"MOBILE TELEPHONE", "07123 987655"))),
    Some(Seq(Residence(Some("BASE"),createAddress(2)), Residence(Some("NOMINATED"),createAddress(1)))))

    "Domain model" should {
      "Write to JSON" in {
        Json.prettyPrint(Json.toJson(request))
      }
    }
}
