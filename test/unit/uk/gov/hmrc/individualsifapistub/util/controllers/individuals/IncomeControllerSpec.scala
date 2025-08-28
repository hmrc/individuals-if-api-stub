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
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.individuals.IncomeController
import uk.gov.hmrc.individualsifapistub.domain.individuals.{IncomePaye, IncomeSa}
import uk.gov.hmrc.individualsifapistub.domain.{RecordNotFoundException, TestIndividual}
import uk.gov.hmrc.individualsifapistub.repository.individuals.{IncomePayeRepository, IncomeSaRepository}
import uk.gov.hmrc.individualsifapistub.services.individuals.IncomeService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport
import unit.uk.gov.hmrc.individualsifapistub.util.testUtils.{IncomePayeHelpers, IncomeSaHelpers}

import scala.concurrent.Future
import play.api.mvc.{AnyContentAsEmpty, Result}
import uk.gov.hmrc.individualsifapistub.domain.individuals.{PayeEntry, SaTaxYearEntry}

class IncomeControllerSpec extends TestSupport with IncomeSaHelpers with IncomePayeHelpers {

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val apiPlatformTestUserConnector: ApiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
    val incomePayeRepo: IncomePayeRepository = mock[IncomePayeRepository]
    val incomeSaRepo: IncomeSaRepository = mock[IncomeSaRepository]
    val servicesConfig: ServicesConfig = mock[ServicesConfig]
    val incomeService = new IncomeService(incomeSaRepo, incomePayeRepo, apiPlatformTestUserConnector, servicesConfig)
    val underTest = new IncomeController(loggingAction, controllerComponents, incomeService)

    val utr: SaUtr = SaUtr("2432552635")

    val testIndividual: TestIndividual = TestIndividual(
      saUtr = Some(utr)
    )

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(using any()))
      .thenReturn(Future.successful(Some(testIndividual)))
  }

  val idType = "nino"
  val idValue = "XH123456A"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val startYear = "2019"
  val endYear = "2020"
  val useCase = "TEST"
  val fields = "paye(employerPayeRef,monthlyPeriodNumber,paymentDate,taxablePay,weeklyPeriodNumber)"
  val saFields =
    "sa(taxYear,income,returnList(utr,caseStartDate,receivedDate,businessDescription,telephoneNumber,busStartDate,busEndDate,totalTaxPaid,totalNIC,turnover,otherBusIncome,tradingIncomeAllowance,address(line1,line2,line3,line4,postcode),income(selfAssessment,allEmployments,ukInterest,foreignDivs,ukDivsAndInterest,partnerships,pensions,selfEmployment,trusts,ukProperty,foreign,lifePolicies,shares,other),deducts(totalBusExpenses,totalDisallowBusExp)))"

  val innerSaValue: Seq[SaTaxYearEntry] = Seq(createValidSaTaxYearEntry(), createValidSaTaxYearEntry())
  val incomeSaResponse: IncomeSa = IncomeSa(Some(innerSaValue))

  val innerPayeValue: Seq[PayeEntry] = Seq(createValidPayeEntry(), createValidPayeEntry())
  val incomePayeResponse: IncomePaye = IncomePaye(Some(innerPayeValue))

  val innerPayeValueWithFieldFiltered: Seq[PayeEntry] =
    Seq(createValidPayeHOV2FieldsEntry(), createValidPayeHOV2FieldsEntry())
  val incomePayeWithFieldFilteredResponse: IncomePaye = IncomePaye(Some(innerPayeValueWithFieldFiltered))

  "Sa" should {

    "Create Sa" should {

      "Successfully create a SA record and return the SA as response" in new Setup {

        when(incomeService.createSa(idType, idValue, Some(startYear), Some(endYear), Some(useCase), incomeSaResponse))
          .thenReturn(
            Future.successful(incomeSaResponse)
          )

        val result: Result = await(
          underTest.createSa(idType, idValue, Some(startYear), Some(endYear), Some(useCase))(
            fakeRequest.withBody(Json.toJson(incomeSaResponse))
          )
        )

        status(result) shouldBe CREATED
        jsonBodyOf(result) shouldBe Json.toJson(incomeSaResponse)

      }

      "Fail when an invalid nino is provided" in new Setup {

        when(apiPlatformTestUserConnector.getIndividualByNino(any())(using any()))
          .thenReturn(Future.failed(new RecordNotFoundException))

        when(incomeService.createSa(idType, idValue, Some(startYear), Some(endYear), Some(useCase), incomeSaResponse))
          .thenReturn(
            Future.successful(incomeSaResponse)
          )

        assertThrows[Exception] {
          await(
            underTest.createSa(idType, idValue, Some(startYear), Some(endYear), Some(useCase))(
              fakeRequest.withBody(Json.toJson(""))
            )
          )
        }

      }

      "Fails to create SaIncome when a request is not provided" in new Setup {

        when(incomeService.createSa(idType, idValue, Some(startYear), Some(endYear), Some(useCase), incomeSaResponse))
          .thenReturn(
            Future.successful(incomeSaResponse)
          )

        assertThrows[Exception] {
          await(
            underTest.createSa(idType, idValue, Some(startYear), Some(endYear), Some(useCase))(
              fakeRequest.withBody(Json.toJson(""))
            )
          )
        }

      }
    }

    "Retrieve Sa" should {

      "Return data when successfully retrieved from service" in new Setup {

        when(incomeService.getSa(idType, idValue, startYear, endYear, Some(saFields))).thenReturn(
          Future.successful(Some(incomeSaResponse))
        )

        val result: Result = await {
          underTest.retrieveSa(idType, idValue, startYear, endYear, Some(saFields))(fakeRequest)
        }

        status(result) shouldBe OK
        jsonBodyOf(result) shouldBe Json.toJson(Some(incomeSaResponse))

      }

      "Fail when it cannot get sa from service" in new Setup {

        when(incomeService.getSa(idType, idValue, startYear, endYear, Some(fields))).thenReturn(
          Future.failed(new Exception)
        )

        assertThrows[Exception] {
          await(underTest.retrieveSa(idType, idValue, startYear, endYear, Some(fields))(fakeRequest))
        }

      }
    }
  }

  "Paye" should {

    "Create Paye" should {

      "Successfully create a PAYE record and return the PAYE as response" in new Setup {

        when(
          incomeService.createPaye(idType, idValue, Some(startDate), Some(endDate), Some(useCase), incomePayeResponse)
        )
          .thenReturn(
            Future.successful(incomePayeResponse)
          )

        val result: Result = await(
          underTest.createPaye(idType, idValue, Some(startDate), Some(endDate), Some(useCase))(
            fakeRequest.withBody(Json.toJson(incomePayeResponse))
          )
        )

        status(result) shouldBe CREATED

        jsonBodyOf(result) shouldBe Json.toJson(incomePayeResponse)

      }

      "Fails to create PayeIncome when a request is not provided" in new Setup {

        when(
          incomeService.createPaye(idType, idValue, Some(startDate), Some(endDate), Some(useCase), incomePayeResponse)
        )
          .thenReturn(
            Future.successful(incomePayeResponse)
          )

        assertThrows[Exception] {
          await(
            underTest.createPaye(idType, idValue, Some(startDate), Some(endDate), Some(useCase))(
              fakeRequest.withBody(Json.toJson(""))
            )
          )
        }
      }

    }

    "Retrieve Paye" should {

      "Return Paye when successfully retrieved from service" in new Setup {

        when(incomeService.getPaye(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
          Future.successful(Some(incomePayeWithFieldFilteredResponse))
        )

        val result: Result =
          await(underTest.retrievePaye(idType, idValue, startDate, endDate, Some(fields))(fakeRequest))

        status(result) shouldBe OK

        jsonBodyOf(result) shouldBe Json.toJson(Some(incomePayeWithFieldFilteredResponse))

      }

      "Fail when it cannot get from service" in new Setup {

        when(incomeService.getPaye(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
          Future.failed(new Exception)
        )

        assertThrows[Exception] {
          await(underTest.retrievePaye(idType, idValue, startDate, endDate, Some(fields))(fakeRequest))
        }

      }
    }
  }
}
