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
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.BenefitsAndCreditsController
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.domain.{Application, BenefitsAndCredits, CreateBenefitsAndCreditsRequest}
import uk.gov.hmrc.individualsifapistub.services.BenefitsAndCreditsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class BenefitsAndCreditsControllerSpec extends TestSupport {

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockBenefitsAndCreditsService = mock[BenefitsAndCreditsService]
    val underTest = new BenefitsAndCreditsController(bodyParsers, controllerComponents, mockBenefitsAndCreditsService)
  }

  val application: Application = Application(
    id = 12345,
    ceasedDate = Some("2012-12-12"),
    entStartDate = Some("2012-12-12"),
    entEndDate = Some("2012-12-12"),
    None
  )

  val idType = "idType"
  val idValue = "idValue"

  val request = Seq(application)

  "Create BenefitsAndCredits" should {
    "Successfully create a details record and return created record as response" in new Setup {
      val employment = BenefitsAndCredits(s"$idType-$idValue", request)
      when(mockBenefitsAndCreditsService.create(idType, idValue, request)).thenReturn(Future.successful(employment))

      val result = await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(employment)
    }

    "Fail when a request is not provided" in new Setup {
      val employment = BenefitsAndCredits(s"$idType-$idValue", request)
      when(mockBenefitsAndCreditsService.create(idType, idValue, request)).thenReturn(Future.successful(employment))
      assertThrows[Exception] { await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson("")))) }
    }
  }

  "Retrieve Details" should {
    "Return details when successfully retrieved from service" in new Setup {
      val employment = BenefitsAndCredits(s"$idType-$idValue", request)
      when(mockBenefitsAndCreditsService.get(idType, idValue)).thenReturn(Future.successful(Some(employment)))

      val result = await(underTest.retrieve(idType, idValue)(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(Some(employment))
    }

    "Fail when it cannot get from service" in new Setup {
      when(mockBenefitsAndCreditsService.get(idType, idValue)).thenReturn(Future.failed(new Exception))
      assertThrows[Exception] { await(underTest.retrieve(idType, idValue)(fakeRequest)) }
    }
  }
}
