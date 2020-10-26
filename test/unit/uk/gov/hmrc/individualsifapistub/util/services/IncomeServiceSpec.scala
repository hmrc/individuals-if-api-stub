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
import uk.gov.hmrc.individualsifapistub.domain.{CreateIncomeRequest, Income}
import uk.gov.hmrc.individualsifapistub.repository.IncomeRepository
import uk.gov.hmrc.individualsifapistub.services.IncomeService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class IncomeServiceSpec extends TestSupport {

  //TODO :- Fix me up

  trait Setup {

    val idType = "NINO"
    val idValue = "QW1234QW"
    val incomeType = "incomeType"
    val request = CreateIncomeRequest(None, None)

    val mockSelfAssessmentRepository = mock[IncomeRepository]
    val underTest = new IncomeService(mockSelfAssessmentRepository)
  }

  "Income Service" when {
    "Create" should {
      "Return the created income when created" in new Setup {
        val selfAssessment = Income(s"$incomeType-$idType-$idValue", "FIX ME")
        when(mockSelfAssessmentRepository.create(s"$incomeType-$idType-$idValue", request)).thenReturn(Future.successful(selfAssessment));
        val response = await(underTest.create(incomeType,idType, idValue, request))
        response shouldBe selfAssessment
      }

      "Return failure when unable to create Income object" in new Setup {
        when(mockSelfAssessmentRepository.create(s"$idType-$idValue", request)).thenReturn(Future.failed(new Exception));
        assertThrows[Exception] {
          await(underTest.create(incomeType, idType, idValue, request))
        }
      }
    }

    "Get" should {
      "Return income when successfully retrieved from mongo" in new Setup {
        val selfAssessment = Income(s"$incomeType-$idType-$idValue", "Fix ME")
        when(mockSelfAssessmentRepository.findById(s"$incomeType-$idType-$idValue")).thenReturn(Future.successful(Some(selfAssessment)));
        val response = await(underTest.get(incomeType, idType, idValue))
        response shouldBe Some(selfAssessment)
      }

      "Return none if cannot be found in mongo" in new Setup {
        when(mockSelfAssessmentRepository.findById(s"$incomeType-$idType-$idValue")).thenReturn(Future.successful(None));
        val response = await(underTest.get(incomeType, idType, idValue))
        response shouldBe None
      }
    }
  }

}
