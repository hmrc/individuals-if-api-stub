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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, CorporationTaxCompanyDetails, Name, NameAddressDetails}
import uk.gov.hmrc.individualsifapistub.repository.organisations.CorporationTaxCompanyDetailsRepository

class CorporationTaxCompanyDetailsRepositorySpec extends RepositoryTestHelper {

  val repository = fakeApplication.injector.instanceOf[CorporationTaxCompanyDetailsRepository]

  val address =
    Address(Some("Alfie House"), Some("Main Street"), Some("Manchester"), Some("Londonberry"), Some("LN1 1AG"))

  val name = Name("Waitrose", "And Partners")

  val registeredDetails = NameAddressDetails(name, address)
  val communicationDetails = NameAddressDetails(name, address)

  val request =
    CorporationTaxCompanyDetails("1234567890", "12345678", Some(registeredDetails), Some(communicationDetails))
  val response =
    CorporationTaxCompanyDetails("1234567890", "12345678", Some(registeredDetails), Some(communicationDetails))

  "collection" should {
    "have a unique index on a request's crn" in {
      repository.indexes.find { i =>
        i.getOptions.getName.contains("id") &&
        i.getKeys.toBsonDocument.getFirstKey == "id" &&
        i.getOptions.isBackground &&
        i.getOptions.isUnique
      } should not be None
    }

    "create" should {
      "create a CT Company Details response with a valid crn" in {
        val result = await(repository.create(request))
        result shouldBe response
      }

      "fail to create a duplicate" in {
        await(repository.create(request))
        assertThrows[DuplicateException] {
          await(repository.create(request))
        }
      }
    }

    "find" should {
      "return None when no ids match" in {
        await(repository.find(request.crn)) shouldBe None
      }

      "return Some when match by id is found" in {
        await(repository.create(request))
        await(repository.find(request.crn)) shouldBe Some(response)
      }
    }

  }

}
