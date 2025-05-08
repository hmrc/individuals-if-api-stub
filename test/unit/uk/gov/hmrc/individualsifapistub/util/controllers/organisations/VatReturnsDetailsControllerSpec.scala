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
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.organisations.VatReturnDetailsController
import uk.gov.hmrc.individualsifapistub.domain.RecordNotFoundException
import uk.gov.hmrc.individualsifapistub.domain.organisations.{VatPeriod, VatReturnsDetails, VatReturnsDetailsEntry}
import uk.gov.hmrc.individualsifapistub.services.organisations.VatReturnsDetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import java.time.LocalDateTime
import scala.concurrent.Future

class VatReturnsDetailsControllerSpec extends TestSupport {

  val mockService: VatReturnsDetailsService = mock[VatReturnsDetailsService]
  val controller = new VatReturnDetailsController(loggingAction, controllerComponents, mockService)

  val vatPeriods: List[VatPeriod] = List(
    VatPeriod(Some("23AG"), Some("2023-12-01"), Some("2023-12-31"), Some(30), Some(102), Some("ret"), Some("s"))
  )
  val request: VatReturnsDetails = VatReturnsDetails("12345678", Some("123"), Some("2023-12-31"), vatPeriods)
  val response: VatReturnsDetailsEntry = VatReturnsDetailsEntry("id", request, LocalDateTime.now())

  "create" should {
    "return response with created status when successful" in {
      when(mockService.create(request.vrn, request)).thenReturn(Future.successful(response))

      val httpRequest = FakeRequest().withMethod("POST").withBody(Json.toJson(request))

      val result = controller.create(response.vatReturnsDetails.vrn)(httpRequest)

      result.map { x =>
        x.header.status shouldBe CREATED
        jsonBodyOf(x) shouldBe Json.toJson(response)
      }
    }

    "fail with invalid request provided" in {
      when(mockService.create(request.vrn, request)).thenReturn(Future.successful(response))

      val httpRequest = FakeRequest().withMethod("POST").withBody(Json.parse("{}"))

      val result = controller.create(response.vatReturnsDetails.vrn)(httpRequest)

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
