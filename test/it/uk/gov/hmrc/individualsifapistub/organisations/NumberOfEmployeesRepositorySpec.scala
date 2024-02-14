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
import uk.gov.hmrc.individualsifapistub.repository.organisations.NumberOfEmployeesRepository

class NumberOfEmployeesRepositorySpec extends RepositoryTestHelper {
  val repository = fakeApplication.injector.instanceOf[NumberOfEmployeesRepository]

  val counts = NumberOfEmployeeCounts("2019-10", 554)
  val reference = NumberOfEmployeeReferences("456", "RT882d", Seq(counts))
  val postRequest = NumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq(reference))
  val response = NumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq(reference))

  val getReference = NumberOfEmployeeReferencesRequest("456", "RT882d")
  val getRequest = NumberOfEmployeesRequest("2019-10-01", "2020-04-05", Seq(getReference))


  "collection" should {
    "have a unique index on a requests utr" in {
      repository.indexes.find{ i =>
        i.getOptions.getName.contains("id") &&
          i.getKeys.toBsonDocument.getFirstKey == "id" &&
          i.getOptions.isBackground &&
          i.getOptions.isUnique
      } should not be None
    }

    "create" should {
      "create a CT Return Details repsonse with a valid utr" in {
        val result = await(repository.create(postRequest))
        result shouldBe response
      }

      "fail to create a duplicate" in {
        await(repository.create(postRequest))
        assertThrows[DuplicateException] {
          await(repository.create(postRequest))
        }
      }
    }

    "find" should {
      "return None when no id's match" in {
        await(repository.find(getRequest)) shouldBe None
      }

      "return Some when match by id is found" in {
        await(repository.create(postRequest))
        await(repository.find(getRequest)) shouldBe Some(response)
      }
    }
  }
}
