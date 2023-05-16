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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{VatReturn, VatReturnDetails, VatReturnDetailsEntry, VatTaxYear}
import uk.gov.hmrc.individualsifapistub.repository.organisations.VatReturnDetailsRepository

class VatReturnDetailsRepositorySpec extends RepositoryTestHelper {

  val repository = fakeApplication.injector.instanceOf[VatReturnDetailsRepository]
  val vatReturn: List[VatReturn] = List(VatReturn(1, 10, 5, 6243, "", Some("")))
  val vatTaxYear: List[VatTaxYear] = List(VatTaxYear("2019", vatReturn))
  val serviceRequest: VatReturnDetails = VatReturnDetails("12345678", Some("123"), vatTaxYear)
  val repositoryEntry: VatReturnDetailsEntry = VatReturnDetailsEntry(serviceRequest.vrn, serviceRequest)

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
    "create a CT Return Details repsonse with a valid utr" in {
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
      await(repository.retrieve(repositoryEntry.vatReturnDetails.vrn)) shouldBe None
    }

    "return Some when match by id is found" in {
      await(repository.create(repositoryEntry))
      await(repository.retrieve(repositoryEntry.vatReturnDetails.vrn)) shouldBe Some(repositoryEntry)
    }
  }
}
