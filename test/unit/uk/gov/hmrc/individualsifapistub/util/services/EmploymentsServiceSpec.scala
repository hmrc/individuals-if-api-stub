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

import org.joda.time.DateTime
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.individualsifapistub.domain.{CreateEmploymentRequest, Employment}
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository
import uk.gov.hmrc.individualsifapistub.services.EmploymentsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class EmploymentsServiceSpec extends TestSupport {
  trait Setup {

    val matchId = "ABC123"
    val startDate = new DateTime (2000,10,1, 0,0)
    val endDate = new DateTime(2000,12,25,0,0)

    val request = CreateEmploymentRequest("something")

    val mockEmploymentRepository = mock[EmploymentRepository]
    val underTest = new EmploymentsService(mockEmploymentRepository)
  }

  "Details Service" when {
    "Create" should {
      "Return the created details when created" in new Setup {
        val employment = Employment(s"$matchId-$startDate-$endDate", request.body)
        when(mockEmploymentRepository.create(s"$matchId-$startDate-$endDate", request)).thenReturn(Future.successful(employment));
        val response = await(underTest.create(matchId, startDate, endDate, request))
        response shouldBe employment
      }

      "Return failure when unable to create Details object" in new Setup {
        when(mockEmploymentRepository.create(s"$matchId-$startDate-$endDate", request)).thenReturn(Future.failed(new Exception));
        assertThrows[Exception] {
          await(underTest.create(matchId, startDate, endDate, request))
        }
      }
    }

    "Get" should {
      "Return details when successfully retrieved from mongo" in new Setup {
        val employment = Employment(s"$matchId-$startDate-$endDate", request.body)
        when(mockEmploymentRepository.findById(s"$matchId-$startDate-$endDate")).thenReturn(Future.successful(Some(employment)));
        val response = await(underTest.get(matchId, startDate, endDate))
        response shouldBe Some(employment)
      }

      "Return none if cannot be found in mongo" in new Setup {
        when(mockEmploymentRepository.findById(s"$matchId-$startDate-$endDate")).thenReturn(Future.successful(None));
        val response = await(underTest.get(matchId, startDate, endDate))
        response shouldBe None
      }
    }
  }
}
