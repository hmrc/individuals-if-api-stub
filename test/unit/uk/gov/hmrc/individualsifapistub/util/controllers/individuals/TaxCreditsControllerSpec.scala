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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers.individuals

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, CREATED, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.individuals.TaxCreditsController
import uk.gov.hmrc.individualsifapistub.domain.individuals.{Application, Applications, Identifier}
import uk.gov.hmrc.individualsifapistub.domain.{RecordNotFoundException, TestIndividual}
import uk.gov.hmrc.individualsifapistub.repository.individuals.TaxCreditsRepository
import uk.gov.hmrc.individualsifapistub.services.individuals.TaxCreditsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future
import play.api.mvc.AnyContentAsEmpty

class TaxCreditsControllerSpec extends TestSupport {

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val apiPlatformTestUserConnector: ApiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
    val taxCreditsRepo: TaxCreditsRepository = mock[TaxCreditsRepository]
    val servicesConfig: ServicesConfig = mock[ServicesConfig]
    val mockTaxCreditsService = new TaxCreditsService(taxCreditsRepo, apiPlatformTestUserConnector, servicesConfig)
    val underTest = new TaxCreditsController(loggingAction, controllerComponents, mockTaxCreditsService)

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(any()))
      .thenReturn(Future.successful(Some(testIndividual)))
  }

  val application: Application = Application(
    id = Some(12345),
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
  val utr: SaUtr = SaUtr("2432552635")

  val testIndividual: TestIndividual = TestIndividual(
    saUtr = Some(utr)
  )

  val ident: Identifier = Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))

  val request: Applications = Applications(Seq(application))

  "Create TaxCredits" should {
    "Successfully create a record and return created record as response" in new Setup {

      when(mockTaxCreditsService.create(idType, idValue, startDate, endDate, useCase, request)).thenReturn(
        Future.successful(request)
      )

      val result = await(
        underTest.create(idType, idValue, startDate, endDate, useCase)(fakeRequest.withBody(Json.toJson(request)))
      )

      status(result) shouldBe CREATED

      jsonBodyOf(result) shouldBe Json.toJson(request)

    }

    "Fail when an invalid nino is provided" in new Setup {

      when(apiPlatformTestUserConnector.getIndividualByNino(any())(any()))
        .thenReturn(Future.failed(new RecordNotFoundException))

      when(mockTaxCreditsService.create(idType, idValue, startDate, endDate, useCase, request)).thenReturn(
        Future.successful(request)
      )

      val result =
        await(underTest.create(idType, idValue, startDate, endDate, useCase)(fakeRequest.withBody(Json.toJson(""))))

      status(result) shouldBe BAD_REQUEST

    }

    "fail when a request is not provided" in new Setup {

      when(mockTaxCreditsService.create(idType, idValue, startDate, endDate, useCase, request)).thenReturn(
        Future.successful(request)
      )

      val response =
        await(underTest.create(idType, idValue, startDate, endDate, useCase)(fakeRequest.withBody(Json.toJson(""))))

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
