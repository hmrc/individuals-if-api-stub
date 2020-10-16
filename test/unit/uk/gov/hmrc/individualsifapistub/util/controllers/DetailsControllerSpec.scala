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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.DetailsController
import uk.gov.hmrc.individualsifapistub.domain.{CreateDetailsRequest, Details}
import uk.gov.hmrc.individualsifapistub.services.DetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport
import play.api.http.Status._
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._

import scala.concurrent.{ExecutionContext, Future}

class DetailsControllerSpec extends TestSupport {
  //TODO :- TESTS FOR Retrieve ENDPOINT

  implicit lazy val materializer = fakeApplication.materializer
  val controllerComponents: ControllerComponents = fakeApplication.injector.instanceOf[ControllerComponents]
  implicit val ec = fakeApplication.injector.instanceOf[ExecutionContext]

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockDetailsService = mock[DetailsService]
    val underTest = new DetailsController(controllerComponents, mockDetailsService)
  }

  val idType = "NINO"
  val idValue = "QW1234QW"
  val request = CreateDetailsRequest("test")

  "Create details" should {
    "Successfully create a details record and return created record as response" in new Setup {
      val details = Details(s"$idType-$idValue", request.body)
      when(mockDetailsService.create(idType, idValue, request)).thenReturn(Future.successful(details))

      val result = await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(details)
    }

    "Fail when a request is not provided" in new Setup {
      val details = Details(s"$idType-$idValue", request.body)
      when(mockDetailsService.create(idType, idValue, request)).thenReturn(Future.successful(details))
      assertThrows[Exception] { await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson("")))) }
    }
  }

  "Retrieve Details" should {
    "Return details when successfully retrieved from service" in new Setup {
      val details = Details(s"$idType-$idValue", request.body)
      when(mockDetailsService.get(idType, idValue)).thenReturn(Future.successful(Some(details)))

      val result = await(underTest.retrieve(idType, idValue)(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(Some(details))
    }

    "Fails when it cannot get from service" in new Setup {
      when(mockDetailsService.create(idType, idValue, request)).thenReturn(Future.failed(new Exception))
      assertThrows[Exception] { await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson("")))) }
    }
  }
}
