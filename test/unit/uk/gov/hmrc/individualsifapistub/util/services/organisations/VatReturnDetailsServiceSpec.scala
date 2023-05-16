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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{VatReturn, VatReturnDetails, VatReturnDetailsEntry, VatTaxYear}
import uk.gov.hmrc.individualsifapistub.repository.organisations.VatReturnDetailsRepository
import uk.gov.hmrc.individualsifapistub.services.organisations.VatReturnDetailsService

import scala.concurrent.Future

class VatReturnDetailsServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  val mockRepository: VatReturnDetailsRepository = mock[VatReturnDetailsRepository]
  val service = new VatReturnDetailsService(mockRepository)

  val vatReturn: List[VatReturn] = List(VatReturn(1, 10, 5, 6243, "", Some("")))
  val vatTaxYear: List[VatTaxYear] = List(VatTaxYear("2019", vatReturn))
  val serviceRequest: VatReturnDetails = VatReturnDetails("12345678", Some("123"), vatTaxYear)
  val repositoryRequest: VatReturnDetailsEntry = VatReturnDetailsEntry(serviceRequest.vrn, serviceRequest)

  "create" should {
    "return response when creating" in {
      when(mockRepository.create(repositoryRequest)).thenReturn(Future.successful(repositoryRequest))
      val result = service.create(serviceRequest.vrn, serviceRequest)
      result.map(x => x shouldBe repositoryRequest)
    }

    "throw error when repository fails to create" in {
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
