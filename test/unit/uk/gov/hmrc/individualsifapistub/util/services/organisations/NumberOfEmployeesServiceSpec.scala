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
import uk.gov.hmrc.individualsifapistub.domain.organisations._
import uk.gov.hmrc.individualsifapistub.repository.organisations.NumberOfEmployeesRepository
import uk.gov.hmrc.individualsifapistub.services.organisations.NumberOfEmployeesService

import scala.concurrent.Future

class NumberOfEmployeesServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {
  val mockRepository = mock[NumberOfEmployeesRepository]
  val service = new NumberOfEmployeesService(mockRepository)

  val counts = NumberOfEmployeeCounts("2019-10", 554)
  val reference = NumberOfEmployeeReferences("456", "RT882d", Seq(counts))
  val postRequest = NumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq(reference))
  val response = NumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq(reference))

  val getReference = NumberOfEmployeeReferencesRequest("456", "RT882d")
  val getRequest = NumberOfEmployeesRequest("2019-10-01", "2020-04-05", Seq(getReference))

  "create" should {
    "return response when creating" in {
      when(mockRepository.create(postRequest)).thenReturn(Future.successful(response))
      val result = service.create(postRequest)
      result.map(x => x shouldBe response)
    }

    "throw error when repository fails to create" in {
      when(mockRepository.create(postRequest)).thenReturn(Future.failed(new Exception()))
      recoverToSucceededIf[Exception] { service.create(postRequest) }
    }
  }

  "get" should {
    "return found item if it exists" in {
      when(mockRepository.find(getRequest)).thenReturn(Future.successful(Some(response)))
      val result = service.get(getRequest);
      result.map(x => x shouldBe Some(response))
    }

    "return None if item does not exists" in {
      when(mockRepository.find(getRequest)).thenReturn(Future.successful(None))
      val result = service.get(getRequest);
      result.map(x => x shouldBe None)
    }
  }
}
