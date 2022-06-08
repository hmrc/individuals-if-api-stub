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

import testUtils.RepositoryTestHelper
import uk.gov.hmrc.individualsifapistub.domain.individuals.{Application, Applications}
import uk.gov.hmrc.individualsifapistub.repository.individuals.TaxCreditsRepository

class TaxCreditsRepositorySpec extends RepositoryTestHelper {

  val repository = fakeApplication.injector.instanceOf[TaxCreditsRepository]

  val application: Application = Application(
    id = Some(12345),
    ceasedDate = Some("2012-12-12"),
    entStartDate = Some("2012-12-12"),
    entEndDate = Some("2012-12-12"),
    None
  )

  val nino = "XH123456A"
  val trn = "123456789"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"

  val applications = Applications(Seq(application))

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

  "create" should {

    "create an application with a nino" in {
      val result = await(repository.create("nino", nino, startDate, endDate, useCase, applications))
      result shouldBe applications
    }

    "fail to create duplicate" in {
      await(repository.create("nino", nino, startDate, endDate, useCase, applications))
      intercept[Exception](await(repository.create("nino", nino, startDate, endDate, useCase, applications)))
    }

    "create an application with a trn" in {
      val result = await(repository.create("trn", trn, startDate, endDate, useCase, applications))
      result shouldBe applications
    }

  }

  "findByIdAndType" should {

    "return None when there are no details for a given nino" in {
      await(repository.findByIdAndType("nino", nino, startDate, endDate, Some(fields))) shouldBe None
    }

    "return None when there are no details for a given trn" in {
      await(repository.findByIdAndType("trn", trn, startDate, endDate, Some(fields))) shouldBe None
    }

    "return a single record with id" in {
      await(repository.create("nino", nino, startDate, endDate, useCase, applications))
      val result = await(repository.findByIdAndType("nino", nino, startDate, endDate, Some(fields)))
      result.get shouldBe applications
    }
  }
}
