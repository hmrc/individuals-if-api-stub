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

package unit.uk.gov.hmrc.individualsifapistub.util.services.individuals

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain.TestIndividual
import uk.gov.hmrc.individualsifapistub.domain.individuals.{Application, Applications, Identifier}
import uk.gov.hmrc.individualsifapistub.repository.individuals.TaxCreditsRepository
import uk.gov.hmrc.individualsifapistub.services.individuals.TaxCreditsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class TaxCreditsServiceSpec extends TestSupport {
  trait Setup {

    val idType = "nino"
    val idValue = "XH123456A"
    val startDate = "2020-01-01"
    val endDate = "2020-21-31"
    val useCase = "TEST"
    val fields = "some(values)"
    val ident = Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$useCase"
    val utr = SaUtr("2432552635")

    val testIndividual = TestIndividual(
      saUtr = Some(utr)
    )

    val application: Application = Application(
      id = Some(12345),
      ceasedDate = Some("2012-12-12"),
      entStartDate = Some("2012-12-12"),
      entEndDate = Some("2012-12-12"),
      None
    )

    implicit val hc = HeaderCarrier()
    val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]

    val request = Applications(Seq(application))
    val taxCreditsRepository = mock[TaxCreditsRepository]
    val servicesConfig = mock[ServicesConfig]
    val underTest = new TaxCreditsService(taxCreditsRepository, apiPlatformTestUserConnector, servicesConfig)

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(any())).thenReturn(Future.successful(testIndividual))

  }

  "TaxCreditsService" when {

    "create" should {

      "return the created record" in new Setup {

        when(taxCreditsRepository.create(idType, idValue, startDate, endDate, useCase, request)).thenReturn(
          Future.successful(request)
        )

        val response = await(underTest.create(idType, idValue, startDate, endDate, useCase, request))

        response shouldBe request

      }

      "return failure when unable to create" in new Setup {

        when(taxCreditsRepository.create(idType, idValue, startDate, endDate, useCase, request)).thenReturn(
          Future.failed(new Exception)
        )

        assertThrows[Exception] {
          await(underTest.create(idType, idValue, startDate, endDate, useCase, request))
        }

      }
    }

    "Get" should {

      "return record when successfully retrieved from mongo" in new Setup {

        when(taxCreditsRepository.findByIdAndType(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
          Future.successful(Some(request))
        )

        val response = await(underTest.get(idType, idValue, startDate, endDate, Some(fields)))

        response shouldBe Some(request)

      }

      "Return none if cannot be found in mongo" in new Setup {

        when(taxCreditsRepository.findByIdAndType(idType, idValue, startDate, endDate, Some(fields))).thenReturn(
          Future.successful(None)
        )

        val response = await(underTest.get(idType, idValue, startDate, endDate, Some(fields)))

        response shouldBe None
      }
    }
  }
}
