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
import org.scalatest.FutureOutcome
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.FixtureAsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.individualsifapistub.domain.organisations._
import uk.gov.hmrc.individualsifapistub.repository.organisations.VatInformationRepository
import uk.gov.hmrc.individualsifapistub.services.organisations.VatInformationService
import uk.gov.hmrc.individualsifapistub.util.DateTimeProvider

import java.time.LocalDateTime
import scala.concurrent.Future

class VatInformationServiceSpec extends FixtureAsyncWordSpec with Matchers with MockitoSugar {

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val mockRepository: VatInformationRepository = mock[VatInformationRepository]
    val timeProvider: DateTimeProvider = mock[DateTimeProvider]
    val service = new VatInformationService(mockRepository, timeProvider)
    test.apply(Fixture(mockRepository, service, timeProvider))
  }

  case class Fixture(repo: VatInformationRepository, service: VatInformationService, timeProvider: DateTimeProvider)

  override type FixtureParam = Fixture

  val vrn = "12345678"
  val customerDetails: VatCustomerDetails = VatCustomerDetails("Ancient Antiques")
  val vatAddress: VatAddress = VatAddress("VAT ADDR 1", "SW1A 2BQ")
  val vatPPOB: VatPPOB = VatPPOB(vatAddress)
  val vatApprovedInformation: VatApprovedInformation = VatApprovedInformation(customerDetails, vatPPOB)
  val request: VatInformation = VatInformation(vatApprovedInformation)

  val repositoryRequest: VatInformationEntry = VatInformationEntry(vrn, request, LocalDateTime.now())

  "create" should {
    "return response when creating" in { case Fixture(repo, service, timeProvider) =>
      when(timeProvider.now()).thenReturn(repositoryRequest.createdAt)
      when(repo.create(repositoryRequest)).thenReturn(Future.successful(repositoryRequest))
      val result = service.create(vrn, request)
      result.map(x => x shouldBe repositoryRequest)
    }

    "throw error when repository fails to create" in { case Fixture(repo, service, timeProvider) =>
      when(timeProvider.now()).thenReturn(repositoryRequest.createdAt)
      when(repo.create(repositoryRequest)).thenReturn(Future.failed(new Exception()))
      recoverToSucceededIf[Exception] {
        service.create(vrn, request)
      }
    }

  }

  "retrieve" should {
    "return found item if it exists" in { case Fixture(repo, service, _) =>
      when(repo.retrieve(vrn)).thenReturn(Future.successful(Some(repositoryRequest)))
      val result = service.retrieve(vrn)
      result.map(x => x shouldBe Some(repositoryRequest))
    }

    "return None if item does not exists" in { case Fixture(repo, service, _) =>
      when(repo.retrieve(vrn)).thenReturn(Future.successful(None))
      val result = service.retrieve(vrn)
      result.map(x => x shouldBe None)
    }
  }

}
