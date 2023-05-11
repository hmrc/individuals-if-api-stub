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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, CorporationTaxCompanyDetails, Name, NameAddressDetails}
import uk.gov.hmrc.individualsifapistub.repository.organisations.CorporationTaxCompanyDetailsRepository
import uk.gov.hmrc.individualsifapistub.services.organisations.CorporationTaxCompanyDetailsService

import scala.concurrent.Future

class CorporationTaxCompanyDetailsServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  val mockRepository = mock[CorporationTaxCompanyDetailsRepository]
  val service = new CorporationTaxCompanyDetailsService(mockRepository)

  val address = Address(
    Some("Alfie House"),
    Some("Main Street"),
    Some("Manchester"),
    Some("Londonberry"),
    Some("LN1 1AG"))

  val name = Name("Waitrose", "And Partners")

  val registeredDetails = NameAddressDetails(name, address)
  val communicationDetails = NameAddressDetails(name, address)

  val request = CorporationTaxCompanyDetails("1234567890", "12345678", Some(registeredDetails), Some(communicationDetails))
  val response = CorporationTaxCompanyDetails("1234567890", "12345678", Some(registeredDetails), Some(communicationDetails))


  "create" should {
    "return response when creating" in {
      when(mockRepository.create(request)).thenReturn(Future.successful(response))
      val result = service.create(request)
      result.map(x => x shouldBe response)
    }

    "throw error when repository fails to create" in {
      when(mockRepository.create(request)).thenReturn(Future.failed(new Exception()))
      recoverToSucceededIf[Exception] { service.create(request) }
    }
  }

  "get" should {
    "return found item if it exists" in {
      when(mockRepository.find(request.crn)).thenReturn(Future.successful(Some(response)))
      val result = service.get(request.crn);
      result.map(x => x shouldBe Some(response))
    }

    "return None if item does not exists" in {
      when(mockRepository.find(request.crn)).thenReturn(Future.successful(None))
      val result = service.get(request.crn);
      result.map(x => x shouldBe None)
    }
  }
}
