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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{AccountingPeriod, CorporationTaxReturnDetailsResponse, CreateCorporationTaxReturnDetailsRequest}
import uk.gov.hmrc.individualsifapistub.repository.organisations.CorporationTaxReturnDetailsRepository
import uk.gov.hmrc.individualsifapistub.services.organisations.CorporationTaxReturnDetailsService

import scala.concurrent.Future

class CorporationTaxReturnDetailsServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  val mockRepository: CorporationTaxReturnDetailsRepository = mock[CorporationTaxReturnDetailsRepository]
  val service = new CorporationTaxReturnDetailsService(mockRepository)

  val accountingPeriods: Seq[AccountingPeriod] = Seq(AccountingPeriod("2018-04-06", "2018-10-05", 38390))
  val request: CreateCorporationTaxReturnDetailsRequest = CreateCorporationTaxReturnDetailsRequest("1234567890", "2015-04-21", "V", accountingPeriods)
  val response: CorporationTaxReturnDetailsResponse = CorporationTaxReturnDetailsResponse("1234567890", "2015-04-21", "V", accountingPeriods)

  "create" should {
    "return response when creating" in {
      when(mockRepository.create(request)).thenReturn(Future.successful(response))
      val result = service.create(request)
      result.map(x => x shouldBe response)
    }

    "throw error when repository fails to create" in {
      when(mockRepository.create(request)).thenReturn(Future.failed(new Exception()))
      recoverToSucceededIf[Exception](service.create(request))
    }
  }

  "get" should {
    "return found item if it exists" in {
      when(mockRepository.find(request.utr)).thenReturn(Future.successful(Some(response)))
      val result = service.get(request.utr);
      result.map(x => x shouldBe Some(response))
    }

    "return None if item does not exists" in {
      when(mockRepository.find(request.utr)).thenReturn(Future.successful(None))
      val result = service.get(request.utr);
      result.map(x => x shouldBe None)
    }
  }
}
