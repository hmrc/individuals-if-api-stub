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
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.individualsifapistub.controllers.EmploymentsController
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.domain.EmploymentsResponse._
import uk.gov.hmrc.individualsifapistub.domain.{Address, CreateEmploymentRequest, Employer, Employment, EmploymentDetail, EmploymentsResponse, Id, Payment}
import uk.gov.hmrc.individualsifapistub.services.EmploymentsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.{ExecutionContext, Future}

class EmploymentsControllerSpec extends TestSupport {

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockEmploymentsService = mock[EmploymentsService]
    val underTest = new EmploymentsController(bodyParsers, controllerComponents, mockEmploymentsService)
  }

  val idType = "idType"
  val idValue = "idValue"

  val employment = EmploymentsResponse(
    Seq(
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
      )))

  val request = CreateEmploymentRequest(Id(Some("XH123456A"), None), employment)

  "Create Employment" should {
    "Successfully create a details record and return created record as response" in new Setup {
      when(mockEmploymentsService.create(idType, idValue, request)).thenReturn(Future.successful(employment))

      val result = await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(employment)
    }

    "Fail when a request is not provided" in new Setup {
      when(mockEmploymentsService.create(idType, idValue, request)).thenReturn(Future.successful(employment))
      assertThrows[Exception] { await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson("")))) }
    }
  }

  "Retrieve Employment" should {
    "Return employment when successfully retrieved from service" in new Setup {
      when(mockEmploymentsService.get(idType, idValue)).thenReturn(Future.successful(Some(employment)))

      val result = await(underTest.retrieve(idType, idValue)(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(Some(employment))
    }

    "Fail when it cannot get from service" in new Setup {
      when(mockEmploymentsService.get(idType, idValue)).thenReturn(Future.failed(new Exception))
      assertThrows[Exception] { await(underTest.retrieve(idType, idValue)(fakeRequest)) }
    }
  }
}
