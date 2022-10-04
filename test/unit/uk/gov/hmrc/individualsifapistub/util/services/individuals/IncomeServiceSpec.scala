/*
 * Copyright 2022 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsifapistub.util.services.individuals

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain.TestIndividual
import uk.gov.hmrc.individualsifapistub.domain.individuals.{IncomePaye, IncomeSa}
import uk.gov.hmrc.individualsifapistub.repository.individuals.{IncomePayeRepository, IncomeSaRepository}
import uk.gov.hmrc.individualsifapistub.services.individuals.IncomeService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport
import unit.uk.gov.hmrc.individualsifapistub.util.testUtils.{IncomePayeHelpers, IncomeSaHelpers}

import scala.concurrent.Future

class IncomeServiceSpec extends TestSupport with IncomeSaHelpers with IncomePayeHelpers {
  trait Setup {

    val idType = "nino"
    val idValue = "XH123456A"
    val startYear = "2019"
    val endYear = "2020"
    val startDate = "2020-01-01"
    val endDate = "2020-21-31"
    val useCase = "TEST"
    val fields = "some(values)"
    val utr = SaUtr("2432552635")

    val innerSaValue = Seq(createValidSaTaxYearEntry(), createValidSaTaxYearEntry())
    val incomeSaResponse = IncomeSa(Some(innerSaValue))

    val innerPayeValue = Seq(createValidPayeEntry(), createValidPayeEntry())
    val incomePayeResponse = IncomePaye(Some(innerPayeValue))

    val testIndividual = TestIndividual(
      saUtr = Some(utr)
    )

    implicit val hc = HeaderCarrier()
    val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]

    val mockSelfAssessmentRepository = mock[IncomeSaRepository]
    val mockPayeRepository = mock[IncomePayeRepository]
    val servicesConfig = mock[ServicesConfig]
    val underTest = new IncomeService(
      mockSelfAssessmentRepository, mockPayeRepository, apiPlatformTestUserConnector, servicesConfig
    )

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(any())).
      thenReturn(Future.successful(testIndividual))
  }

  "Income Service" when {

    "Sa" should {

      "Create" should {

        "Return the created SA when created" in new Setup {


          when(mockSelfAssessmentRepository.create(idType, idValue, startYear, endYear, useCase, incomeSaResponse)).thenReturn(
            Future.successful(incomeSaResponse)
          )

          val response = await(underTest.createSa(idType, idValue, startYear, endYear, useCase, incomeSaResponse))

          response shouldBe incomeSaResponse

        }

        "Return failure when unable to create SA object" in new Setup {

          when(mockSelfAssessmentRepository.create(idType, idValue, startYear, endYear, useCase, incomeSaResponse)).thenReturn(
            Future.failed(new Exception)
          )

          assertThrows[Exception] {
            await(underTest.createSa(idType, idValue, startYear, endYear, useCase, incomeSaResponse))
          }

        }
      }

      "Get" should {
        "Return SA when successfully retrieved from mongo" in new Setup {

          when(mockSelfAssessmentRepository.findByTypeAndId(idType, idValue, startYear, endYear, Some(fields))).thenReturn(
            Future.successful(Some(incomeSaResponse))
          )

          val response = await(underTest.getSa(idType, idValue, startYear, endYear, Some(fields)))

          response shouldBe Some(incomeSaResponse)

        }

        "Return none if cannot be found in mongo" in new Setup {

          when(mockSelfAssessmentRepository.findByTypeAndId(idType, idValue, startYear, endYear, Some(fields))).thenReturn(
            Future.successful(None)
          )

          val response = await(underTest.getSa(idType, idValue, startYear, endYear, Some(fields)))

          response shouldBe None

        }
      }
    }

    "PAYE" should {

      "Create" should {

        "Return the created PAYE when created" in new Setup {

          when(mockPayeRepository.create(idType, idValue, Some(startDate), Some(endDate), Some(useCase), incomePayeResponse)).thenReturn(
            Future.successful(incomePayeResponse)
          )

          val response = await(underTest.createPaye(idType, idValue, Some(startDate), Some(endDate), Some(useCase), incomePayeResponse))

          response shouldBe incomePayeResponse

        }

        "Return failure when unable to create PAYE object" in new Setup {

          when(mockPayeRepository.create(idType, idValue, Some(startDate), Some(endDate), Some(useCase), incomePayeResponse)).thenReturn(
            Future.failed(new Exception)
          )

          assertThrows[Exception] {
            await(underTest.createPaye(idType, idValue, Some(startDate), Some(endDate), Some(useCase), incomePayeResponse))
          }

        }
      }

      "Get" should {

        "Return PAYE when successfully retrieved from mongo" in new Setup {

          when(mockPayeRepository.findByTypeAndId(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
            Future.successful(Some(incomePayeResponse))
          )

          val response = await(underTest.getPaye(idType, idValue, startDate, endDate, Some(fields)))

          response shouldBe Some(incomePayeResponse)

        }

        "Return none if cannot be found in mongo" in new Setup {

          when(mockPayeRepository.findByTypeAndId(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
            Future.successful(None)
          )

          val response = await(underTest.getPaye(idType, idValue, startDate, endDate, Some(fields)))

          response shouldBe None

        }
      }
    }
  }

}
