/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{AccountingPeriod, Address, CorporationTaxReturnDetailsResponse, CreateCorporationTaxReturnDetailsRequest, CreateSelfAssessmentTaxPayerRequest, SelfAssessmentTaxPayerResponse, TaxPayerDetails}
import uk.gov.hmrc.individualsifapistub.repository.organisations.SelfAssessmentTaxPayerRepository

class SelfAssessmentTaxPayerRepositorySpec extends RepositoryTestHelper  {

  val repository = fakeApplication.injector.instanceOf[SelfAssessmentTaxPayerRepository]

  val exampleAddress = Address(Some("Alfie House"),
                        Some("Main Street"),
                        Some("Birmingham"),
                        Some("West midlands"),
                        Some("B14 6JH"))

  val taxPayerDetails = Seq(TaxPayerDetails("John Smith II", "Registered", exampleAddress))
  val request = CreateSelfAssessmentTaxPayerRequest("1234567890", "Individual", taxPayerDetails)
  val response = SelfAssessmentTaxPayerResponse("1234567890", "Individual", taxPayerDetails)

  "collection" should {
    "have a unique index on a requests utr" in {
      await(repository.collection.indexesManager.list()).find({ i =>
      {
        i.name.contains("id") &&
          i.key.exists(key => key._1 == "id")
        i.background &&
          i.unique
      }
      }) should not be None
    }

    "create" should {
      "create an SA tax payer with a valid utr" in {
        val result = await(repository.create(request))
        result shouldBe response
      }

      "fail to create a duplicate" in {
        await(repository.create(request))
        intercept[Exception](await(repository.create(request)))
      }
    }

    "find" should {
      "return None when no ids match" in {
        await(repository.find(request.utr)) shouldBe None
      }

      "return Some when match by id is found" in {
        await(repository.create(request))
        await(repository.find(request.utr)) shouldBe Some(response)
      }
    }

  }

}
