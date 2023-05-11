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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers.organisations

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.organisations.CorporationTaxReturnDetailsController
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxReturnDetails._
import uk.gov.hmrc.individualsifapistub.domain.organisations.{AccountingPeriod, CorporationTaxReturnDetailsResponse, CreateCorporationTaxReturnDetailsRequest}
import uk.gov.hmrc.individualsifapistub.services.organisations.CorporationTaxReturnDetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class CorporationTaxReturnDetailsControllerSpec extends TestSupport {

  val mockService = mock[CorporationTaxReturnDetailsService]
  val controller = new CorporationTaxReturnDetailsController(loggingAction, bodyParsers, controllerComponents, mockService)

  val accountingPeriods = Seq(AccountingPeriod("2018-04-06", "2018-10-05", 38390))
  val request = CreateCorporationTaxReturnDetailsRequest("1234567890", "2015-04-21", "V", accountingPeriods)
  val response = CorporationTaxReturnDetailsResponse("1234567890", "2015-04-21", "V", accountingPeriods)

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
