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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.TaxCreditsController

import uk.gov.hmrc.individualsifapistub.domain.TaxCredits._
import uk.gov.hmrc.individualsifapistub.domain.{Application, Applications, Identifier}
import uk.gov.hmrc.individualsifapistub.services.TaxCreditsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class TaxCreditsControllerSpec extends TestSupport {

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockTaxCreditsService = mock[TaxCreditsService]
    val underTest = new TaxCreditsController(bodyParsers, controllerComponents, mockTaxCreditsService)
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
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"
  val ident = Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))

  val request = Applications(Seq(application))

  "Create TaxCredits" should {
    "Successfully create a record and return created record as response" in new Setup {

      when(mockTaxCreditsService.create(idType, idValue, startDate, endDate, useCase, request)).thenReturn(
        Future.successful(request)
      )

      val result = await(underTest.create(idType, idValue, startDate, endDate, useCase)(
        fakeRequest.withBody(Json.toJson(request)))
      )

      status(result) shouldBe CREATED

      jsonBodyOf(result) shouldBe Json.toJson(request)

    }

    "fail when a request is not provided" in new Setup {

      when(mockTaxCreditsService.create(idType, idValue, startDate, endDate, useCase, request)).thenReturn(
        Future.successful(request)
      )

      val response = await(underTest.create(idType, idValue, startDate, endDate, useCase)(
        fakeRequest.withBody(Json.toJson("")))
      )

      status(response) shouldBe BAD_REQUEST

    }
  }

  "Retrieve tax credits" should {

    "Return applications when successfully retrieved from service" in new Setup {

      when(mockTaxCreditsService.get(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
        Future.successful(Some(request))
      )

      val result = await(underTest.retrieve(idType, idValue, startDate, endDate, Some(fields))(fakeRequest))

      status(result) shouldBe OK

      jsonBodyOf(result) shouldBe Json.toJson[Applications](request)

    }

    "fail when it cannot get from service" in new Setup {

      when(mockTaxCreditsService.get(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
        Future.failed(new Exception)
      )

      assertThrows[Exception] {
        await(underTest.retrieve(idType, idValue, startDate, endDate, Some(fields))(fakeRequest))
      }
    }

  }
}
