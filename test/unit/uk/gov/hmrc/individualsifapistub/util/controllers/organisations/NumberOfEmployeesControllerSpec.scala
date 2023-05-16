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
import uk.gov.hmrc.individualsifapistub.controllers.organisations.NumberOfEmployeesController
import uk.gov.hmrc.individualsifapistub.domain.organisations.{NumberOfEmployeeCounts, NumberOfEmployeeReferences, NumberOfEmployeeReferencesRequest, NumberOfEmployeesRequest, NumberOfEmployeesResponse}
import uk.gov.hmrc.individualsifapistub.domain.organisations.NumberOfEmployees._
import uk.gov.hmrc.individualsifapistub.services.organisations.NumberOfEmployeesService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class NumberOfEmployeesControllerSpec extends TestSupport {
  val mockService = mock[NumberOfEmployeesService]

  val counts = NumberOfEmployeeCounts("2019-10", 554)
  val reference = NumberOfEmployeeReferences("456", "RT882d", Seq(counts))
  val postRequest = NumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq(reference))
  val response = NumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq(reference))

  val getReference = NumberOfEmployeeReferencesRequest("456", "RT882d")
  val getRequest = NumberOfEmployeesRequest("2019-10-01", "2020-04-05", Seq(getReference))

  val controller = new NumberOfEmployeesController(loggingAction, bodyParsers, controllerComponents, mockService)

  "create" should {
    "return response with created status when successful" in {
      when(mockService.create(postRequest)).thenReturn(Future.successful(response))

      val httpRequest =
        FakeRequest()
          .withMethod("Post")
          .withBody(Json.toJson(postRequest))

      val result = controller.create()(httpRequest)

      result.map(x => {
        x.header.status shouldBe CREATED
        jsonBodyOf(x) shouldBe Json.toJson(response)
      })
    }

    "fail when invalid request provided" in {
      when(mockService.create(postRequest)).thenReturn(Future.successful(response))

      val httpRequest =
        FakeRequest()
          .withMethod("POST")
          .withBody(Json.parse("{}"))

      val result = controller.create()(httpRequest)

      result.map(x => {
        x.header.status shouldBe BAD_REQUEST
      })
    }
  }

  "retrieve" should {
    "return response when entry found by service" in {
      when(mockService.get(getRequest)).thenReturn(Future.successful(Some(response)))

      val httpRequest =
        FakeRequest()
          .withMethod("Get")
          .withBody(Json.toJson(getRequest))

      val result = controller.retrieve()(httpRequest)

      result.map(x => {
        x.header.status shouldBe OK
        jsonBodyOf(x) shouldBe Json.toJson(response)
      })
    }

    "fails when an entry cannot be found" in {
      when(mockService.get(getRequest)).thenReturn(Future.failed(new Exception))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")
          .withBody(Json.toJson(getRequest))

      assertThrows[Exception] { await(controller.retrieve()(httpRequest)) }
    }
  }
}
