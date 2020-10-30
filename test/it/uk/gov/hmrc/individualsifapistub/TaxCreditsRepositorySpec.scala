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
import uk.gov.hmrc.individualsifapistub.domain.{Application, Applications, DuplicateException, Id,  TaxCreditsEntry}
import uk.gov.hmrc.individualsifapistub.repository.TaxCreditsRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

class TaxCreditsRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val repository = fakeApplication.injector.instanceOf[TaxCreditsRepository]

  val application: Application = Application(
    id = 12345,
    ceasedDate = Some("2012-12-12"),
    entStartDate = Some("2012-12-12"),
    entEndDate = Some("2012-12-12"),
    None
  )

  val idType = "nino"
  val idValue = "XH123456A"

  val id = Id(Some(idValue), None)

  val request = Applications(Seq(application))

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override def afterEach() {
    await(repository.drop)
  }

  "collection" should {
    "have a unique index on nino" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("nino") &&
          i.key == Seq("id.nino" -> Ascending) &&
          i.background &&
          i.unique
      }) should not be None
    }
    "have a unique index on trn" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("trn") &&
          i.key == Seq("id.trn" -> Ascending) &&
          i.background &&
          i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a benefits and credits record" in {
      val result = await(repository.create(idType, idValue, request))
      result shouldBe request
    }

    "fail to create a duplicate benefits and credits record" in {
      await(repository.create(idType, idValue, request))

      intercept[DuplicateException](
        await(repository.create(idType, idValue, request)))
    }
  }

  "find by id and type" should {
    "return None when there are no benefits and credits record for a given id" in {
      await(repository.findByIdAndType(idType, idValue)) shouldBe None
    }

    "return the benefits and credits record" in {
      await(repository.create(idType, idValue, request))

      val result = await(repository.findByIdAndType(idType, idValue))

      result shouldBe Some(request)
    }
  }
}
