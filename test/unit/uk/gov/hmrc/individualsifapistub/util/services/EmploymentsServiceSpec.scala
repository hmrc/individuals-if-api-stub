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
import uk.gov.hmrc.individualsifapistub.domain.{CreateEmploymentRequest, Employment}
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository
import uk.gov.hmrc.individualsifapistub.services.EmploymentsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class EmploymentsServiceSpec extends TestSupport {
  trait Setup {

    val idType = "idType"
    val idValue = "idValue"

    val request = CreateEmploymentRequest("something")

    val mockEmploymentRepository = mock[EmploymentRepository]
    val underTest = new EmploymentsService(mockEmploymentRepository)
  }

  "Employments Service" when {
    "Create" should {
      "Return the created employment when created" in new Setup {
        val employment = Employment(s"$idType-$idValue", request.body)
        when(mockEmploymentRepository.create(s"$idType-$idValue", request)).thenReturn(Future.successful(employment));
        val response = await(underTest.create(idType, idValue, request))
        response shouldBe employment
      }

      "Return failure when unable to create Employment object" in new Setup {
        when(mockEmploymentRepository.create(s"$idType-$idValue", request)).thenReturn(Future.failed(new Exception));
        assertThrows[Exception] {
          await(underTest.create(idType, idValue, request))
        }
      }
    }

    "Get" should {
      "Return employment when successfully retrieved from mongo" in new Setup {
        val employment = Employment(s"$idType-$idValue", request.body)
        when(mockEmploymentRepository.findById(s"$idType-$idValue")).thenReturn(Future.successful(Some(employment)));
        val response = await(underTest.get(idType, idValue))
        response shouldBe Some(employment)
      }

      "Return none if cannot be found in mongo" in new Setup {
        when(mockEmploymentRepository.findById(s"$idType-$idValue")).thenReturn(Future.successful(None));
        val response = await(underTest.get(idType,idValue))
        response shouldBe None
      }
    }
  }
}
