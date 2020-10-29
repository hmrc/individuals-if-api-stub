/*
 * Copyright 2020 HM Revenue & Customs
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

package it.uk.gov.hmrc.individualsifapistub

import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import reactivemongo.api.indexes.IndexType.Ascending
import uk.gov.hmrc.individualsifapistub.domain.{BenefitsAndCredits, CreateBenefitsAndCreditsRequest, DuplicateException}
import uk.gov.hmrc.individualsifapistub.repository.BenefitsAndCreditsRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

class BenefitsAndCreditsRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val repository = fakeApplication.injector.instanceOf[BenefitsAndCreditsRepository]

  val id = "2432552635"
  val request = CreateBenefitsAndCreditsRequest("request")
  val benefitsAndCredits = BenefitsAndCredits(id, request.body)

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override def afterEach() {
    await(repository.drop)
  }

  "collection" should {
    "have a unique index on id" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("idIndex") &&
        i.key == Seq("id" -> Ascending) &&
        i.background &&
        i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a benefits and credits record" in {
      val result = await(repository.create(benefitsAndCredits.id, request))

      result shouldBe benefitsAndCredits
    }

    "fail to create a duplicate benefits and credits record" in {
      await(repository.create(benefitsAndCredits.id, request))

      intercept[DuplicateException](
        await(repository.create(benefitsAndCredits.id, request)))
    }
  }

  "find by id" should {
    "return None when there are no benefits and credits record for a given id" in {
      await(repository.findById(id)) shouldBe None
    }

    "return the benefits and credits record" in {
      await(repository.create(benefitsAndCredits.id, request))

      val result = await(repository.findById(id))

      result shouldBe Some(benefitsAndCredits)
    }
  }
}
