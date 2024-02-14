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

package unit.uk.gov.hmrc.individualsifapistub.util.services.organisations

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.individualsifapistub.domain.RecordNotFoundException
import uk.gov.hmrc.individualsifapistub.domain.organisations._
import uk.gov.hmrc.individualsifapistub.repository.organisations.{VatInformationRepository, VatReturnsDetailsRepository}
import uk.gov.hmrc.individualsifapistub.services.organisations.VatReturnsDetailsService
import uk.gov.hmrc.individualsifapistub.util.DateTimeProvider

import java.time.LocalDateTime
import scala.concurrent.Future

class VatReturnsDetailsServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  val mockRepository: VatReturnsDetailsRepository = mock[VatReturnsDetailsRepository]
  val mockVatInformationRepository: VatInformationRepository = mock[VatInformationRepository]
  val timeProvider: DateTimeProvider = mock[DateTimeProvider]
  val service = new VatReturnsDetailsService(mockRepository, mockVatInformationRepository, timeProvider)

  val vatEntry: VatInformationEntry = VatInformationEntry(
    "1",
    VatInformation(VatApprovedInformation(VatCustomerDetails("H"), VatPPOB(VatAddress("l1", "p")))),
    LocalDateTime.now()
  )
  val vatPeriods: List[VatPeriod] = List(
    VatPeriod(Some("23AA"), Some("2023-01-01"), Some("2023-01-31"), Some(30), Some(6243), Some("rt"), Some("s"))
  )
  val serviceRequest: VatReturnsDetails = VatReturnsDetails("12345678", Some("123"), Some("2023-01-31"), vatPeriods)
  val repositoryRequest: VatReturnsDetailsEntry =
    VatReturnsDetailsEntry(serviceRequest.vrn, serviceRequest, LocalDateTime.now())

  "create" should {
    "return response when creating" in {
      when(mockVatInformationRepository.retrieve(serviceRequest.vrn)).thenReturn(Future.successful(Some(vatEntry)))
      when(mockRepository.create(repositoryRequest)).thenReturn(Future.successful(repositoryRequest))
      when(timeProvider.now()).thenReturn(repositoryRequest.createdAt)
      val result = service.create(serviceRequest.vrn, serviceRequest)
      result.map(x => x shouldBe repositoryRequest)
    }

    "throw not found error when the organisations is not found" in {
      when(mockVatInformationRepository.retrieve(serviceRequest.vrn)).thenReturn(Future.successful(None))
      recoverToSucceededIf[RecordNotFoundException] {
        service.create(serviceRequest.vrn, serviceRequest)
      }
    }

    "throw error when repository fails to create" in {
      when(mockVatInformationRepository.retrieve(serviceRequest.vrn)).thenReturn(Future.successful(Some(vatEntry)))
      when(mockRepository.create(repositoryRequest)).thenReturn(Future.failed(new Exception()))
      recoverToSucceededIf[Exception] {
        service.create(serviceRequest.vrn, serviceRequest)
      }
    }
  }

  "retrieve" should {
    "return found item if it exists" in {
      when(mockRepository.retrieve(repositoryRequest.id)).thenReturn(Future.successful(Some(repositoryRequest)))
      val result = service.retrieve(serviceRequest.vrn)
      result.map(x => x shouldBe Some(repositoryRequest))
    }

    "return None if item does not exists" in {
      when(mockRepository.retrieve(repositoryRequest.id)).thenReturn(Future.successful(None))
      val result = service.retrieve(serviceRequest.vrn)
      result.map(x => x shouldBe None)
    }
  }
}
