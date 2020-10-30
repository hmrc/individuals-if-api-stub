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

package unit.uk.gov.hmrc.individualsifapistub.util.services

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository
import uk.gov.hmrc.individualsifapistub.services.EmploymentsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class EmploymentsServiceSpec extends TestSupport {
  trait Setup {

    val idType = "idType"
    val idValue = "idValue"

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
    val request = EmploymentEntry(Id(Some("XH123456A"), None), Seq(employment))
    val mockEmploymentRepository = mock[EmploymentRepository]
    val underTest = new EmploymentsService(mockEmploymentRepository)
  }

  "Employments Service" when {
    "Create" should {
      "Return the created employment when created" in new Setup {
        when(mockEmploymentRepository.create(idType, idValue, employments)).thenReturn(Future.successful(employments));
        val response = await(underTest.create(idType, idValue, employments))
        response shouldBe employments
      }

      "Return failure when unable to create Employment object" in new Setup {
        when(mockEmploymentRepository.create(idType, idValue, employments)).thenReturn(Future.failed(new Exception));
        assertThrows[Exception] {
          await(underTest.create(idType, idValue, employments))
        }
      }
    }

    "Get" should {
      "Return employment when successfully retrieved from mongo" in new Setup {
        when(mockEmploymentRepository.findByIdAndType(idType, idValue)).thenReturn(Future.successful(Some(employments)));
        val response = await(underTest.get(idType, idValue))
        response shouldBe Some(employments)
      }

      "Return none if cannot be found in mongo" in new Setup {
        when(mockEmploymentRepository.findByIdAndType(idType, idValue)).thenReturn(Future.successful(None));
        val response = await(underTest.get(idType,idValue))
        response shouldBe None
      }
    }
  }
}
