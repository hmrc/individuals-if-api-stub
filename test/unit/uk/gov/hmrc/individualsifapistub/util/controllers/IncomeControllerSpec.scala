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
import uk.gov.hmrc.individualsifapistub.controllers.IncomeController
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.domain.{CreateIncomeRequest, Income}
import uk.gov.hmrc.individualsifapistub.services.IncomeService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class IncomeControllerSpec extends TestSupport {

  trait Setup {
    val fakeRequest = FakeRequest()
    val incomeService = mock[IncomeService]
    val underTest = new IncomeController(bodyParsers, controllerComponents, incomeService)
  }

  val incomeType = "SA"
  val idType = "idType"
  val idValue = "idValue"

  val request = CreateIncomeRequest("something")

  "Create Income" should {
    "Successfully create a income record and return created record as response" in new Setup {
      val income = Income(s"$incomeType-$idType-$idValue", request.body)
      when(incomeService.create(incomeType,idType, idValue, request)).thenReturn(Future.successful(income))

      val result = await(underTest.create(incomeType, idType, idValue)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(income)
    }

    "Fail when a request is not provided" in new Setup {
      val income = Income(s"$incomeType-$idType-$idValue", request.body)
      when(incomeService.create(incomeType, idType, idValue, request)).thenReturn(Future.successful(income))
      assertThrows[Exception] { await(underTest.create(incomeType, idType, idValue)(fakeRequest.withBody(Json.toJson("")))) }
    }
  }

  "Retrieve Income" should {
    "Return income when successfully retrieved from service" in new Setup {
      val income = Income(s"$incomeType-$idType-$idValue", request.body)
      when(incomeService.get(incomeType, idType, idValue)).thenReturn(Future.successful(Some(income)))

      val result = await(underTest.retrieve(incomeType, idType, idValue)(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(Some(income))
    }

    "Fail when it cannot get from service" in new Setup {
      when(incomeService.get(incomeType, idType, idValue)).thenReturn(Future.failed(new Exception))
      assertThrows[Exception] { await(underTest.retrieve(incomeType, idType, idValue)(fakeRequest)) }
    }
  }
}
