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
import uk.gov.hmrc.individualsifapistub.domain.{CreateDetailsRequest, Details}
import uk.gov.hmrc.individualsifapistub.repository.DetailsRepository
import uk.gov.hmrc.individualsifapistub.services.DetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class DetailsServiceSpec extends TestSupport {
  //TODO :- TEST FOR get
  trait Setup {

    val idType = "NINO"
    val idValue = "QW1234QW"
    val request = CreateDetailsRequest("test")

    val mockEmploymentRepository = mock[DetailsRepository]
    val underTest = new DetailsService(mockEmploymentRepository)
  }

  "Details Service" when {
    "Create" should {
      "Return the created details when created" in new Setup {
        val details = Details(s"$idType-$idValue", request.body)
        when(mockEmploymentRepository.create(s"$idType-$idValue", request)).thenReturn(Future.successful(details));
        val response = await(underTest.create(idType, idValue, request))
        response shouldBe details
      }

      "Return failure when unable to create Details object" in new Setup {
        when(mockEmploymentRepository.create(s"$idType-$idValue", request)).thenReturn(Future.failed(new Exception));
        assertThrows[Exception] {
          await(underTest.create(idType, idValue, request))
        }
      }
    }

    "Get" should {
      "Return details when successfully retrieved from mongo" in new Setup {
        val details = Details(s"$idType-$idValue", request.body)
        when(mockEmploymentRepository.findById(s"$idType-$idValue")).thenReturn(Future.successful(Some(details)));
        val response = await(underTest.get(idType, idValue))
        response shouldBe Some(details)
      }

      "Return none if cannot be found in mongo" in new Setup {
        when(mockEmploymentRepository.findById(s"$idType-$idValue")).thenReturn(Future.successful(None));
        val response = await(underTest.get(idType, idValue))
        response shouldBe None
      }
    }
  }

}
