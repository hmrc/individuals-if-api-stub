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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers.organisations

import controllers.Assets._
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.organisations.CorporationTaxCompanyDetailsController
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxCompanyDetails._
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, CommunicationDetails, CorporationTaxCompanyDetailsResponse, CreateCorporationTaxCompanyDetailsRequest, Name, RegisteredDetails}
import uk.gov.hmrc.individualsifapistub.repository.organisations.CorporationTaxCompanyDetailsRepository
import uk.gov.hmrc.individualsifapistub.services.organisations.CorporationTaxCompanyDetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class CorporationTaxCompanyDetailsControllerSpec extends TestSupport {

  val mockService = mock[CorporationTaxCompanyDetailsService]
  val controller = new CorporationTaxCompanyDetailsController(bodyParsers, controllerComponents, mockService)
  val repository = fakeApplication.injector.instanceOf[CorporationTaxCompanyDetailsRepository]

  val address = Address(
    Some("Alfie House"),
    Some("Main Street"),
    Some("Manchester"),
    Some("Londonberry"),
    Some("LN1 1AG"))

  val name = Name("Waitrose", "And Partners")

  val registeredDetails = RegisteredDetails(name, address)
  val communicationDetails = CommunicationDetails(name, address)

  val request = CreateCorporationTaxCompanyDetailsRequest("1234567890", "12345678", Some(registeredDetails), Some(communicationDetails))
  val response = CorporationTaxCompanyDetailsResponse("1234567890", "12345678", Some(registeredDetails), Some(communicationDetails))

  "create" should {
    "return response with created status when successful" in {
      when(mockService.create(request)).thenReturn(Future.successful(response))

      val httpRequest =
        FakeRequest()
          .withMethod("Post")
          .withBody(Json.toJson(request))

      val result = controller.create(response.utr)(httpRequest)

      result.map(x => {
        x.header.status shouldBe CREATED
        jsonBodyOf(x) shouldBe Json.toJson(response)
      })
    }

    "fail when invalid request provided" in {
      when(mockService.create(request)).thenReturn(Future.successful(response))

      val httpRequest =
        FakeRequest()
          .withMethod("POST")
          .withBody(Json.parse("{}"))

      val result = controller.create(response.utr)(httpRequest)

      result.map(x => {
        x.header.status shouldBe BAD_REQUEST
      })
    }
  }

  "retrieve" should {
    "return response when entry found by service" in {
      when(mockService.get(request.utr)).thenReturn(Future.successful(Some(response)))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      val result = controller.retrieve(request.utr)(httpRequest)

      result.map(x => {
        x.header.status shouldBe OK
        jsonBodyOf(x) shouldBe Json.toJson(response)
      })
    }

    "fails when an entry cannot be found" in {
      when(mockService.get(request.utr)).thenReturn(Future.failed(new Exception))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      assertThrows[Exception] { await(controller.retrieve(request.utr)(httpRequest)) }
    }
  }
}
