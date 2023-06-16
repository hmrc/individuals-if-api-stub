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

import uk.gov.hmrc.individualsifapistub.services.organisations.VatReturnDetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{ BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK }
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.organisations.VatReturnDetailsController
import uk.gov.hmrc.individualsifapistub.domain.RecordNotFoundException
import uk.gov.hmrc.individualsifapistub.domain.organisations.{ VatReturn, VatReturnDetails, VatReturnDetailsEntry, VatTaxYear }

import scala.concurrent.Future

class VatReturnDetailsControllerSpec extends TestSupport {

  val mockService: VatReturnDetailsService = mock[VatReturnDetailsService]
  val controller = new VatReturnDetailsController(loggingAction, bodyParsers, controllerComponents, mockService)

  val vatReturn: List[VatReturn] = List(VatReturn(1, 10, 5, 6243, "", Some("")))
  val vatTaxYear: List[VatTaxYear] = List(VatTaxYear("2019", vatReturn))
  val request: VatReturnDetails = VatReturnDetails("12345678", Some("123"), vatTaxYear)
  val response: VatReturnDetailsEntry = VatReturnDetailsEntry("id", request)

  "create" should {
    "return response with created status when successful" in {
      when(mockService.create(request.vrn, request)).thenReturn(Future.successful(response))

      val httpRequest = FakeRequest().withMethod("POST").withBody(Json.toJson(request))

      val result = controller.create(response.vatReturnDetails.vrn)(httpRequest)

      result.map { x =>
        x.header.status shouldBe CREATED
        jsonBodyOf(x) shouldBe Json.toJson(response)
      }
    }

    "fail with invalid request provided" in {
      when(mockService.create(request.vrn, request)).thenReturn(Future.successful(response))

      val httpRequest = FakeRequest().withMethod("POST").withBody(Json.parse("{}"))

      val result = controller.create(response.vatReturnDetails.vrn)(httpRequest)

      result.map(_.header.status shouldBe BAD_REQUEST)
    }

    "fail with 404 when a VAT record is not found" in {
      when(mockService.create(request.vrn, request)).thenReturn(Future.failed(RecordNotFoundException("err")))
      val httpRequest = FakeRequest().withMethod("POST").withBody(Json.toJson(request))
      val result = controller.create(request.vrn)(httpRequest)
      result.map(_.header.status shouldBe NOT_FOUND)
    }
  }

  "retrieve" should {
    "return response when entry found by service" in {
      when(mockService.retrieve(request.vrn)).thenReturn(Future.successful(Some(response)))

      val httpRequest = FakeRequest().withMethod("GET")

      val result = controller.retrieve(request.vrn, None)(httpRequest)
      result.map { x =>
        x.header.status shouldBe OK
        jsonBodyOf(x) shouldBe Json.toJson(response)
      }
    }
  }

  "fails when an entry cannot be found" in {
    when(mockService.retrieve(request.vrn)).thenReturn(Future.failed(new Exception))

    val httpRequest = FakeRequest().withMethod("GET")

    val result = await(controller.retrieve(request.vrn, None)(httpRequest))
    status(result) shouldBe INTERNAL_SERVER_ERROR
  }
}
