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
import uk.gov.hmrc.individualsifapistub.domain.TaxCredits._
import uk.gov.hmrc.individualsifapistub.domain.{Application, Applications, Identifier}
import uk.gov.hmrc.individualsifapistub.repository.TaxCreditsRepository
import uk.gov.hmrc.individualsifapistub.services.TaxCreditsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class TaxCreditsServiceSpec extends TestSupport {
  trait Setup {

    val idType = "nino"
    val idValue = "XH123456A"
    val id = Identifier(Some(idValue), None)

    val application: Application = Application(
      id = 12345,
      ceasedDate = Some("2012-12-12"),
      entStartDate = Some("2012-12-12"),
      entEndDate = Some("2012-12-12"),
      None
    )

    val request = Applications(Seq(application))

    val taxCreditsRepository = mock[TaxCreditsRepository]
    val underTest = new TaxCreditsService(taxCreditsRepository)
  }

  "TaxCreditsService" when {
    "create" should {
      "return the created record" in new Setup {
        when(taxCreditsRepository.create(idType, idValue, request)).thenReturn(Future.successful(request));
        val response = await(underTest.create(idType, idValue, request))
        response shouldBe request
      }

      "return failure when unable to create" in new Setup {
        when(taxCreditsRepository.create(idType, idValue, request)).thenReturn(Future.failed(new Exception));
        assertThrows[Exception] {
          await(underTest.create(idType, idValue, request))
        }
      }
    }

    "Get" should {
      "return record when successfully retrieved from mongo" in new Setup {
        when(taxCreditsRepository.findByIdAndType(idType, idValue)).thenReturn(Future.successful(Some(request)))
        val response = await(underTest.get(idType, idValue))
        response shouldBe Some(request)
      }

      "Return none if cannot be found in mongo" in new Setup {
        when(taxCreditsRepository.findByIdAndType(idType, idValue)).thenReturn(Future.successful(None));
        val response = await(underTest.get(idType,idValue))
        response shouldBe None
      }
    }
  }
}
