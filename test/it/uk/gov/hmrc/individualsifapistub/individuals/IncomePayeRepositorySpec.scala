/*
 * Copyright 2022 HM Revenue & Customs
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

package it.uk.gov.hmrc.individualsifapistub.individuals

import play.api.Configuration
import testUtils.RepositoryTestHelper
import uk.gov.hmrc.individualsifapistub.domain.individuals.IncomePaye
import uk.gov.hmrc.individualsifapistub.repository.individuals.IncomePayeRepository
import unit.uk.gov.hmrc.individualsifapistub.util.testUtils.IncomePayeHelpers

class IncomePayeRepositorySpec
    extends RepositoryTestHelper
    with IncomePayeHelpers {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val repository = fakeApplication.injector.instanceOf[IncomePayeRepository]

  val nino = "XH123456A"
  val trn = "12345678"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"

  val innerValue = Seq(createValidPayeEntry(), createValidPayeEntry())
  val request = IncomePaye(Some(innerValue))

  "collection" should {

    "have a unique index on nino and trn" in {

      repository.indexes.find{ i =>
        i.getOptions.getName.contains("id") &&
          i.getKeys.toBsonDocument.getFirstKey == "id" &&
          i.getOptions.isBackground &&
          i.getOptions.isUnique
      } should not be None
    }

  }

  "create when type is nino" should {

    "create a paye record" in {
      val result = await(repository.create("nino", nino, Some(startDate), Some(endDate), Some(useCase), request))
      result shouldBe request
    }

    "fail to create duplicate" in {
      await(repository.create("nino", nino, Some(startDate), Some(endDate), Some(useCase), request))
      intercept[Exception](await(repository.create("nino", nino, Some(startDate), Some(endDate), Some(useCase), request)))
    }
  }

  "create when type is trn" should {

    "create a paye" in {
      val result = await(repository.create("trn", trn, Some(startDate), Some(endDate), Some(useCase), request))
      result shouldBe request
    }

  }

  "find by id when type is Nino" should {

    "return None when there are no paye records for a given utr" in {
      await(repository.findByTypeAndId("nino", nino, startDate, endDate, Some(fields))) shouldBe None
    }

    "return the paye response" in {
      await(repository.create("nino", nino, Some(startDate), Some(endDate), Some(useCase), request))
      val result = await(repository.findByTypeAndId("nino", nino, startDate, endDate, Some(fields)))
      result shouldBe Some(request)
    }
  }

  "find by id when type is trn" should {

    "return None when there are no paye records for a given utr" in {
      await(repository.findByTypeAndId("trn", trn, startDate, endDate, Some(fields))) shouldBe None
    }

    "return the paye response" in {
      await(repository.create("trn", trn, Some(startDate), Some(endDate), Some(useCase), request))
      val result = await(repository.findByTypeAndId("trn", trn, startDate, endDate, Some(fields)))
      result shouldBe Some(request)
    }
  }
}
