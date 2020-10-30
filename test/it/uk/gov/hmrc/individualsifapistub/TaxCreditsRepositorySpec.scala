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
import uk.gov.hmrc.individualsifapistub.domain.{Application, Applications}
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

  val nino = "XH123456A"
  val trn = "123456789"

  val applications = Applications(Seq(application))

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
    "create an application with a nino" in {
      val result = await(repository.create("nino", nino, applications))
      result shouldBe applications
    }

    "create an application with a trn" in {
      val result = await(repository.create("trn", trn, applications))
      result shouldBe applications
    }

    "fail to create duplicate details" in {
      await(repository.create("nino", nino, applications))
      intercept[Exception](await(repository.create("nino", nino, applications)))
    }
  }

  "findByIdAndType" should {

    "return None when there are no details for a given nino" in {
      await(repository.findByIdAndType("nino", nino)) shouldBe None
    }

    "return None when there are no details for a given trn" in {
      await(repository.findByIdAndType("trn", trn)) shouldBe None
    }

    "return a single record with id" in {
      await(repository.create("nino", nino, applications))
      val result = await(repository.findByIdAndType("nino", nino))
      result.get shouldBe applications
    }
  }
}
