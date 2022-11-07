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

import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.repository.individuals.EmploymentRepository
import uk.gov.hmrc.individualsifapistub.services.individuals.EmploymentsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class EmploymentsServiceSpec extends TestSupport {
  trait Setup {

    val idType = "Nino"
    val idValue = "XH123456A"
    val startDate = "2020-01-01"
    val endDate = "2020-21-31"
    val useCase = "TEST"
    val fields = "some(values)"
    val ident = Identifier(Some("XH123456A"), None, Some(startDate), Some(endDate), Some(useCase))
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$useCase"
    val utr = SaUtr("2432552635")

    val testIndividual = TestIndividual(
      saUtr = Some(utr)
    )

    val employment =
        Employment(
          employer = Some(Employer(
            name = Some("Name"),
            address = Some(Address(
              Some("line1"),
              Some("line2"),
              Some("line3"),
              Some("line4"),
              Some("line5"),
              Some("postcode")
            )),
            districtNumber = Some("ABC"),
            schemeRef = Some("ABC")
          )),
          employerRef = Some("247/ZT6767895A"),
          employment = Some(EmploymentDetail(
            startDate = Some("2001-12-31"),
            endDate = Some("2002-05-12"),
            payFrequency = Some("W2"),
            payrollId = Some("12341234"),
            address = Some(Address(
              Some("line1"),
              Some("line2"),
              Some("line3"),
              Some("line4"),
              Some("line5"),
              Some("postcode")
            )))),
          payments = Some(Seq(Payment(
            date = Some(LocalDate.parse("2001-12-31")),
            ytdTaxablePay = Some(120.02),
            paidTaxablePay = Some(112.75),
            paidNonTaxOrNICPayment = Some(123123.32),
            week = Some(52),
            month = Some(12)
          )
        )
      )
    )

    implicit val hc = HeaderCarrier()
    val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]

    val employments = Employments(Seq(employment))
    val request = EmploymentEntry(id, Seq(employment), None)
    val mockEmploymentRepository = mock[EmploymentRepository]
    val servicesConfig = mock[ServicesConfig]
    val underTest = new EmploymentsService(mockEmploymentRepository, apiPlatformTestUserConnector, servicesConfig)

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(any())).
      thenReturn(Future.successful(testIndividual))
  }

  "Employments Service" when {

    "Create" should {

      "Return the created employment when created" in new Setup {

        when(mockEmploymentRepository.create(idType, idValue, Some(startDate), Some(endDate), Some(useCase), employments)).thenReturn(
          Future.successful(employments)
        )

        val response = await(underTest.create(idType, idValue, Some(startDate), Some(endDate), Some(useCase), employments))

        response shouldBe employments

      }

      "Return failure when unable to create Employment object" in new Setup {

        when(mockEmploymentRepository.create(idType, idValue, Some(startDate), Some(endDate), Some(useCase), employments)).thenReturn(
          Future.failed(new Exception)
        )

        assertThrows[Exception] {
          await(underTest.create(idType, idValue, Some(startDate), Some(endDate), Some(useCase), employments))
        }

      }
    }

    "Get" should {

      "Return employment when successfully retrieved from mongo" in new Setup {

        when(mockEmploymentRepository.findByIdAndType(idType, idValue, startDate, endDate, Some(fields), None)).thenReturn(
          Future.successful(Some(employments))
        )

        val response = await(underTest.get(idType, idValue, startDate, endDate, Some(fields), None))

        response shouldBe Some(employments)

      }

      "Return none if cannot be found in mongo" in new Setup {
        when(mockEmploymentRepository.findByIdAndType(idType, idValue, startDate, endDate, Some(fields), None)).thenReturn(
          Future.successful(None)
        )

        val response = await(underTest.get(idType, idValue, startDate, endDate, Some(fields), None))

        response shouldBe None

      }
    }
  }
}
