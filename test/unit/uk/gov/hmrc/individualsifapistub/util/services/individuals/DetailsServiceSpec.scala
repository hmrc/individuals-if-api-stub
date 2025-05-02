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
import testUtils.TestHelpers
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain.TestIndividual
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.repository.individuals.DetailsRepository
import uk.gov.hmrc.individualsifapistub.services.individuals.DetailsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class DetailsServiceSpec extends TestSupport with TestHelpers {

  trait Setup {

    val idType = "NINO"
    val idValue = "XH123456A"
    val startDate = "2020-01-01"
    val endDate = "2020-21-31"
    val useCase = "TEST"
    val fields = "some(values)"
    val utr = SaUtr("2432552635")

    val testIndividual = TestIndividual(
      saUtr = Some(utr)
    )

    val request = CreateDetailsRequest(
      Some(
        Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9, "MOBILE TELEPHONE", "07123 987655"))
      ),
      Some(
        Seq(
          Residence(residenceType = Some("BASE"), address = generateAddress(2)),
          Residence(residenceType = Some("NOMINATED"), address = generateAddress(1))
        )
      )
    )

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]

    val mockDetailsRepository = mock[DetailsRepository]
    val servicesConfig = mock[ServicesConfig]
    val underTest = new DetailsService(mockDetailsRepository, apiPlatformTestUserConnector, servicesConfig)

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(any()))
      .thenReturn(Future.successful(Some(testIndividual)))

  }

  "Details Service" when {

    "Create" should {

      "Return the created details when created with a NINO" in new Setup {

        val returnVal = DetailsResponseNoId(request.contactDetails, request.residences)

        when(mockDetailsRepository.create("NINO", idValue, useCase, request)).thenReturn(
          Future.successful(returnVal)
        )

        val response = await(underTest.create(idType, idValue, useCase, request))

        response shouldBe returnVal

      }

      "Return failure when unable to create Details object" in new Setup {
        when(mockDetailsRepository.create(idType, idValue, useCase, CreateDetailsRequest(None, None))).thenReturn(
          Future.failed(new Exception)
        )

        assertThrows[Exception] {
          await(underTest.create(idType, idValue, useCase, request))
        }

      }
    }

    "Get" should {
      "Return details when successfully retrieved from mongo" in new Setup {

        val ident = Identifier(Some(idValue), None, None, None, Some(useCase))
        val id = s"${ident.nino.getOrElse(ident.trn.get)}-$useCase"

        val detailsResponse = DetailsResponse(id, request.contactDetails, request.residences)

        when(mockDetailsRepository.findByIdAndType(idType, idValue, Some(fields))).thenReturn(
          Future.successful(Some(detailsResponse))
        )

        val response = await(underTest.get(idType, idValue, Some(fields)))

        response shouldBe Some(detailsResponse)

      }

      "Return none if cannot be found in mongo" in new Setup {

        when(mockDetailsRepository.findByIdAndType(idType, idValue, Some(fields))).thenReturn(Future.successful(None))

        val response = await(underTest.get(idType, idValue, Some(fields)))

        response shouldBe None

      }
    }
  }

}
