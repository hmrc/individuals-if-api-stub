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
import uk.gov.hmrc.individualsifapistub.controllers.individuals.EmploymentsController
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.Employments._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.repository.individuals.EmploymentRepository
import uk.gov.hmrc.individualsifapistub.services.individuals.EmploymentsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class EmploymentsControllerSpec extends TestSupport {

  trait Setup {
    implicit val hc = HeaderCarrier()
    val fakeRequest = FakeRequest()
    val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
    val employmentsRepo = mock[EmploymentRepository]
    val servicesConfig = mock[ServicesConfig]
    val mockEmploymentsService = new EmploymentsService(employmentsRepo, apiPlatformTestUserConnector, servicesConfig)
    val underTest = new EmploymentsController(bodyParsers, controllerComponents, mockEmploymentsService)

    val testIndividual = TestIndividual(
      saUtr = Some(utr)
    )

    val utr = SaUtr("2432552635")

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(any())).
      thenReturn(Future.successful(testIndividual))
  }

  val idType = Nino.toString
  val idValue = "XH123456A"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"

  implicit val cerFormat = Employments.createEmploymentEntryFormat

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
          date = Some("2001-12-31"),
          ytdTaxablePay = Some(120.02),
          paidTaxablePay = Some(112.75),
          paidNonTaxOrNICPayment = Some(123123.32),
          week = Some(52),
          month = Some(12)
        )
      )
    )
  )

  val employments = Employments(Seq(employment))
  val ident = Identifier(Some("XH123456A"), None, Some(startDate), Some(endDate), Some(useCase))
  val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$useCase"
  val request = EmploymentEntry(id, Seq(employment))

  "Create Employment" should {

    "Successfully create a record and return created record as response" in new Setup {

      when(mockEmploymentsService.create(idType, idValue, startDate, endDate, useCase, employments)).thenReturn(
        Future.successful(employments)
      )

      val result = await(underTest.create(idType, idValue, startDate, endDate, useCase)(
        fakeRequest.withBody(Json.toJson(employments)))
      )

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(employments)

    }

    "Fail with an invalid nino" in new Setup {

      when(apiPlatformTestUserConnector.getIndividualByNino(any())(any())).
        thenReturn(Future.failed(new RecordNotFoundException))

      when(mockEmploymentsService.create(idType, idValue, startDate, endDate, useCase, employments)).thenReturn(
        Future.successful(employments)
      )

      val result = await(underTest.create(idType, idValue, startDate, endDate, useCase)(
        fakeRequest.withBody(Json.toJson("")))
      )

      status(result) shouldBe BAD_REQUEST

    }

    "Fail" when {

      "the NINO is invalid" in new Setup {

        when(mockEmploymentsService.create(idType, idValue, startDate, endDate, useCase, employments)).thenReturn(
          Future.successful(employments)
        )

        val result = await(underTest.create(idType, "abc", startDate, endDate, useCase)(
          fakeRequest.withBody(Json.toJson(employments)))
        )

        status(result) shouldBe BAD_REQUEST

      }

      "the TRN is invalid" in new Setup {

        when(mockEmploymentsService.create(idType, idValue, startDate, endDate, useCase, employments)).thenReturn(
          Future.successful(employments)
        )

        val result = await(underTest.create(Trn.toString, "abc", startDate, endDate, useCase)(
          fakeRequest.withBody(Json.toJson(employments)))
        )
        status(result) shouldBe BAD_REQUEST

      }

      "the idType is invalid" in new Setup {
        when(mockEmploymentsService.create(idType, idValue, startDate, endDate, useCase, employments)).thenReturn(
          Future.successful(employments)
        )

        val result = await(underTest.create("idType", idValue, startDate, endDate, useCase)(
          fakeRequest.withBody(Json.toJson(employments)))
        )

        status(result) shouldBe BAD_REQUEST

      }

      "a request is not provided" in new Setup {

        when(mockEmploymentsService.create(idType, idValue, startDate, endDate, useCase, employments)).thenReturn(
          Future.successful(employments)
        )

        val result = await(underTest.create(idType, idValue, startDate, endDate, useCase)(
          fakeRequest.withBody(Json.toJson("")))
        )

        status(result) shouldBe BAD_REQUEST

      }
    }
  }

  "Retrieve Employment" should {

    "Return employments when successfully retrieved from service" in new Setup {

      when(mockEmploymentsService.get(idType, idValue, startDate, endDate, Some(fields), None)).thenReturn(
        Future.successful(Some(employments))
      )

      val result = await(underTest.retrieve(idType, idValue, startDate, endDate, Some(fields), None)(fakeRequest))

      status(result) shouldBe OK

      jsonBodyOf(result) shouldBe Json.toJson(Some(employments))

    }

    "fail when it cannot get from service" in new Setup {

      when(mockEmploymentsService.get(idType, idValue, startDate, endDate, Some(fields), None)).thenReturn(
        Future.failed(new Exception)
      )

      assertThrows[Exception] {
        await(underTest.retrieve(idType, idValue, startDate, endDate, Some(fields), None)(fakeRequest))
      }

    }
  }
}
