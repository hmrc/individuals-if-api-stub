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
import play.api.http.Status.{BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.TaxCreditsController

import uk.gov.hmrc.individualsifapistub.domain.TaxCredits._
import uk.gov.hmrc.individualsifapistub.domain.{Application, Applications, Id}
import uk.gov.hmrc.individualsifapistub.services.TaxCreditsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class TaxCreditsControllerSpec extends TestSupport {

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockBenefitsAndCreditsService = mock[TaxCreditsService]
    val underTest = new TaxCreditsController(bodyParsers, controllerComponents, mockBenefitsAndCreditsService)
  }

  val application: Application = Application(
    id = 12345,
    ceasedDate = Some("2012-12-12"),
    entStartDate = Some("2012-12-12"),
    entEndDate = Some("2012-12-12"),
    None
  )

  val idType = "nino"
  val idValue = "XH123456A"
  val id = Id(Some(idValue), None)

  val request = Applications(Seq(application))

  "Create BenefitsAndCredits" should {
    "Successfully create a details record and return created record as response" in new Setup {

      when(mockBenefitsAndCreditsService.create(idType, idValue, request)).thenReturn(Future.successful(request))

      val result = await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(request)
    }

    "Fail when a request is not provided" in new Setup {
      when(mockBenefitsAndCreditsService.create(idType, idValue, request)).thenReturn(Future.successful(request))
      val response = await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson(""))))
      status(response) shouldBe BAD_REQUEST
    }
  }

  "Retrieve Details" should {
    "Return details when successfully retrieved from service" in new Setup {
      when(mockBenefitsAndCreditsService.get(idType, idValue)).thenReturn(Future.successful(Some(request)))
      val result = await(underTest.retrieve(idType, idValue)(fakeRequest))
      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson[Applications](request)
    }

    "Fail when it cannot get from service" in new Setup {
      when(mockBenefitsAndCreditsService.get(idType, idValue)).thenReturn(Future.failed(new Exception))
      assertThrows[Exception] { await(underTest.retrieve(idType, idValue)(fakeRequest)) }
    }
  }
}
