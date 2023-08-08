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
import uk.gov.hmrc.individualsifapistub.repository.organisations.VatInformationRepository

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class VatInformationRepositorySpec extends RepositoryTestHelper {
  val repository = fakeApplication.injector.instanceOf[VatInformationRepository]

  val vrn = "12345678"
  val customerDetails: VatCustomerDetails = VatCustomerDetails("Ancient Antiques")
  val vatAddress: VatAddress = VatAddress("VAT ADDR 1", "SW1A 2BQ")
  val vatPPOB: VatPPOB = VatPPOB(vatAddress)
  val vatApprovedInformation: VatApprovedInformation = VatApprovedInformation(customerDetails, vatPPOB)
  val request: VatInformation = VatInformation(vatApprovedInformation)

  val repositoryEntry: VatInformationEntry = VatInformationEntry(vrn, request, LocalDateTime.now())

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
    "create Return Information response with a valid vrn" in {
      val input = VatInformationEntry(vrn, request, LocalDateTime.now())
      val result = await(repository.create(input))
      result shouldBe input
    }

    "fail to create a duplicate" in {
      await(repository.create(VatInformationEntry(vrn, request, LocalDateTime.now())))
      assertThrows[DuplicateException] {
        await(repository.create(VatInformationEntry(vrn, request, LocalDateTime.now())))
      }
    }
  }

  "find" should {
    "return None when no id's match" in {
      await(repository.retrieve(repositoryEntry.id)) shouldBe None
    }

    "return Some when match by id is found" in {
      val entry = VatInformationEntry(vrn, request, LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
      await(repository.create(entry))
      await(repository.retrieve(vrn)) shouldBe Some(entry)
    }
  }

}
