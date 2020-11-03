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
import play.api.http.Status.{CREATED, OK, BAD_REQUEST}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.IncomeController
import uk.gov.hmrc.individualsifapistub.domain.IncomePaye._
import uk.gov.hmrc.individualsifapistub.domain.IncomeSa._
import uk.gov.hmrc.individualsifapistub.domain.{IncomePaye, IncomeSa}
import uk.gov.hmrc.individualsifapistub.services.IncomeService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport
import unit.uk.gov.hmrc.individualsifapistub.util.testUtils.{IncomePayeHelpers, IncomeSaHelpers}

import scala.concurrent.Future

class IncomeControllerSpec extends TestSupport with IncomeSaHelpers with IncomePayeHelpers {

  trait Setup {
    val fakeRequest = FakeRequest()
    val incomeService = mock[IncomeService]
    val underTest = new IncomeController(bodyParsers, controllerComponents, incomeService)
  }

  val idType = "nino"
  val idValue = "ANINO123"

  val innerSaValue = Seq(createValidSaTaxYearEntry(), createValidSaTaxYearEntry())
  val incomeSaResponse = IncomeSa(Some(innerSaValue))

  val innerPayeValue = Seq(createValidPayeEntry(), createValidPayeEntry())
  val incomePayeResponse = IncomePaye(Some(innerPayeValue))

  "Sa" should {
    "Create Sa" should {
      "Successfully create a SA record and return the SA as response" in new Setup {
        when(incomeService.createSa(idType, idValue, incomeSaResponse)).thenReturn(Future.successful(incomeSaResponse))

        val result = await(underTest.createSa(idType, idValue)(fakeRequest.withBody(Json.toJson(incomeSaResponse))))

        status(result) shouldBe CREATED
        jsonBodyOf(result) shouldBe Json.toJson(incomeSaResponse)
      }

      "Fails to create SaIncome when a request is not provided" in new Setup {
        when(incomeService.createSa(idType, idValue, incomeSaResponse)).thenReturn(Future.successful(incomeSaResponse))
        assertThrows[Exception] {
          await(underTest.createSa(idType, idValue)(fakeRequest.withBody(Json.toJson(""))))
        }
      }
    }

    "Retrieve Sa" should {
      "Return Paye when successfully retrieved from service" in new Setup {
        when(incomeService.getSa(idType, idValue)).thenReturn(Future.successful(Some(incomeSaResponse)))

        val result = await(underTest.retrieveSa(idType, idValue)(fakeRequest))

        status(result) shouldBe OK
        jsonBodyOf(result) shouldBe Json.toJson(Some(incomeSaResponse))
      }

      "Fail when it cannot get sa from service" in new Setup {
        when(incomeService.getSa(idType, idValue)).thenReturn(Future.failed(new Exception))
        assertThrows[Exception] {
          await(underTest.retrieveSa(idType, idValue)(fakeRequest))
        }
      }
    }
  }

  "Paye" should {
    "Create Paye" should {
      "Successfully create a PAYE record and return the PAYE as response" in new Setup {
        when(incomeService.createPaye(idType, idValue, incomePayeResponse)).thenReturn(Future.successful(incomePayeResponse))

        val result = await(underTest.createPaye(idType, idValue)(fakeRequest.withBody(Json.toJson(incomePayeResponse))))

        status(result) shouldBe CREATED
        jsonBodyOf(result) shouldBe Json.toJson(incomePayeResponse)
      }

      "Fails to create PayeIncome when a request is not provided" in new Setup {
        when(incomeService.createPaye(idType, idValue, incomePayeResponse)).thenReturn(Future.successful(incomePayeResponse))
        assertThrows[Exception] {
          await(underTest.createPaye(idType, idValue)(fakeRequest.withBody(Json.toJson(""))))
        }
      }
    }

    "Retrieve Paye" should {
      "Return aye when successfully retrieved from service" in new Setup {
        when(incomeService.getPaye(idType, idValue)).thenReturn(Future.successful(Some(incomePayeResponse)))

        val result = await(underTest.retrievePaye(idType, idValue)(fakeRequest))

        status(result) shouldBe OK
        jsonBodyOf(result) shouldBe Json.toJson(Some(incomePayeResponse))
      }

      "Fail when it cannot get from service" in new Setup {
        when(incomeService.getPaye(idType, idValue)).thenReturn(Future.failed(new Exception))
        assertThrows[Exception] {
          await(underTest.retrievePaye(idType, idValue)(fakeRequest))
        }
      }
    }
  }
}
