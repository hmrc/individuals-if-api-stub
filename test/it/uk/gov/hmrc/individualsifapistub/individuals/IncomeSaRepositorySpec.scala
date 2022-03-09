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
import uk.gov.hmrc.individualsifapistub.domain.individuals.IncomeSa
import uk.gov.hmrc.individualsifapistub.repository.individuals.IncomeSaRepository
import unit.uk.gov.hmrc.individualsifapistub.util.testUtils.IncomeSaHelpers

class IncomeSaRepositorySpec
  extends RepositoryTestHelper
    with IncomeSaHelpers {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val repository = fakeApplication.injector.instanceOf[IncomeSaRepository]

  val nino = "XH123456A"
  val trn = "12345678"
  val startYear = "2019"
  val endYear = "2020"
  val useCase = "TEST"
  val fields = "some(values)"

  val innerValue = Seq(createValidSaTaxYearEntry(), createValidSaTaxYearEntry())
  val request = IncomeSa(Some(innerValue))

  "collection" should {

    "have a unique index on nino and trn" in {

      await(repository.collection.indexesManager.list()).find({ i =>
      {
        i.name.contains("id") &&
          i.key.exists(key => key._1 == "id")
        i.background &&
          i.unique
      }
      }) should not be None
    }

  }

  "create when type is nino" should {

    "create a self assessment" in {
      val result = await(repository.create("nino", nino, startYear, endYear, useCase, request))
      result shouldBe request
    }

    "fail to create duplicate" in {
      await(repository.create("nino", nino, startYear, endYear, useCase, request))
      intercept[Exception](await(repository.create("nino", nino, startYear, endYear, useCase, request)))
    }

  }

  "create when type is trn" should {

    "create a self assessment" in {
      val result = await(repository.create("trn", trn, startYear, endYear, useCase, request))
      result shouldBe request
    }

  }

  "find by id when type is nino" should {
    "return None when there are no self assessments" in {
      await(repository.findByTypeAndId("nino", nino, startYear, endYear, Some(fields))) shouldBe None
    }

    "return the self assessment" in {
      await(repository.create("nino", nino, startYear, endYear, useCase, request))
      val result = await(repository.findByTypeAndId("nino", nino, startYear, endYear, Some(fields)))
      result shouldBe Some(request)
    }
  }


  "find by id when type is trn" should {

    "return None when there are no self assessments" in {
      await(repository.findByTypeAndId("trn", trn, startYear, endYear, Some(fields))) shouldBe None
    }

    "return the self assessment" in {
      await(repository.create("trn", trn, startYear, endYear, useCase, request))
      val result = await(repository.findByTypeAndId("trn", trn, startYear, endYear, Some(fields)))
      result shouldBe Some(request)
    }
  }
}
