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

import org.mockito.Mockito.{clearInvocations, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.organisations.VatInformationController
import uk.gov.hmrc.individualsifapistub.domain.organisations.{ VatAddress, VatApprovedInformation, VatCustomerDetails, VatInformation, VatInformationEntry, VatPPOB }
import uk.gov.hmrc.individualsifapistub.services.organisations.VatInformationService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import java.time.LocalDateTime
import scala.concurrent.Future

class VatInformationControllerSpec extends TestSupport with BeforeAndAfterEach {

  val mockService: VatInformationService = mock[VatInformationService]
  val controller = new VatInformationController(loggingAction, bodyParsers, controllerComponents, mockService)

  val vrn = "12345678"
  val customerDetails: VatCustomerDetails = VatCustomerDetails("Ancient Antiques")
  val vatAddress: VatAddress = VatAddress("VAT ADDR 1", "SW1A 2BQ")
  val vatPPOB: VatPPOB = VatPPOB(vatAddress)
  val vatApprovedInformation: VatApprovedInformation = VatApprovedInformation(customerDetails, vatPPOB)
  val request: VatInformation = VatInformation(vatApprovedInformation)
  val serviceResponse: VatInformationEntry = VatInformationEntry("id", request, LocalDateTime.now())


  override def afterEach(): Unit = {
    clearInvocations(mockService)
  }

  "create" should {
    "return response with created status when successful" in {
      when(mockService.create(vrn, request)).thenReturn(Future.successful(serviceResponse))

      val httpRequest = FakeRequest().withMethod("Post").withBody(Json.toJson(request))

      val result = controller.create(vrn)(httpRequest)

      result.map { x =>
        x.header.status shouldBe CREATED
        jsonBodyOf(x) shouldBe Json.toJson(serviceResponse)
      }

    }

    "fail with invalid request provided" in {

      when(mockService.create(vrn, request)).thenReturn(Future.successful(serviceResponse))

      val httpRequest = FakeRequest().withMethod("POST").withBody(Json.obj())

      val result = controller.create(vrn)(httpRequest)

      result.map(_.header.status shouldBe BAD_REQUEST)
    }
  }

  "retrieve" should {
    "return response when entry found by service" in {
      when(mockService.retrieve(vrn)).thenReturn(Future.successful(Some(serviceResponse)))

      val httpRequest = FakeRequest().withMethod("GET")

      val result = controller.retrieve(vrn, None)(httpRequest)
      result.map { x =>
        x.header.status shouldBe OK
        jsonBodyOf(x) shouldBe Json.toJson(serviceResponse)
      }
    }

    "fails when an entry cannot be found" in {
      when(mockService.retrieve(vrn)).thenReturn(Future.failed(new Exception))

      val httpRequest =
        FakeRequest().withMethod("GET")

      val result = await(controller.retrieve(vrn, None)(httpRequest))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
