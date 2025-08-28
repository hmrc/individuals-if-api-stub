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

package it.uk.gov.hmrc.individualsifapistub.organisations

import testUtils.RepositoryTestHelper
import uk.gov.hmrc.individualsifapistub.domain.DuplicateException
import uk.gov.hmrc.individualsifapistub.domain.organisations._
import uk.gov.hmrc.individualsifapistub.repository.organisations.VatReturnsDetailsRepository

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class VatReturnsDetailsRepositorySpec extends RepositoryTestHelper {

  val repository: VatReturnsDetailsRepository = fakeApplication.injector.instanceOf[VatReturnsDetailsRepository]
  val vatPeriods: List[VatPeriod] = List(
    VatPeriod(Some("23AG"), Some("2023-01-01"), Some("2023-01-01"), Some(5), Some(6243), Some("rt"), Some("s"))
  )
  val serviceRequest: VatReturnsDetails = VatReturnsDetails("12345678", Some("123"), Some("2023-01-01"), vatPeriods)
  val repositoryEntry: VatReturnsDetailsEntry = VatReturnsDetailsEntry(
    serviceRequest.vrn,
    serviceRequest,
    LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
  )

  "collection" should {
    "have a unique index on a requests utr" in {
      repository.indexes.find { i =>
        i.getOptions.getName.contains("id") &&
        i.getKeys.toBsonDocument.getFirstKey == "id" &&
        i.getOptions.isBackground &&
        i.getOptions.isUnique
      } should not be None
    }
  }

  "create" should {
    "create Return Details response with a valid vrn" in {
      val result = await(repository.create(repositoryEntry))
      result shouldBe repositoryEntry
    }

    "fail to create a duplicate" in {
      await(repository.create(repositoryEntry))
      assertThrows[DuplicateException] {
        await(repository.create(repositoryEntry))
      }
    }
  }

  "find" should {
    "return None when no id's match" in {
      await(repository.retrieve(repositoryEntry.vatReturnsDetails.vrn)) shouldBe None
    }

    "return Some when match by id is found" in {
      await(repository.create(repositoryEntry))
      await(repository.retrieve(repositoryEntry.vatReturnsDetails.vrn)) shouldBe Some(repositoryEntry)
    }
  }
}
