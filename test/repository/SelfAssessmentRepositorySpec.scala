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

package repository

import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import reactivemongo.api.indexes.IndexType.Ascending
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualsifapistub.domain.{DuplicateSelfAssessmentException, SelfAssessment}
import uk.gov.hmrc.individualsifapistub.repository.SelfAssessmentRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global

class SelfAssessmentRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val repository = fakeApplication.injector.instanceOf[SelfAssessmentRepository]

  val utr = SaUtr("2432552635")
  val selfAssessment = SelfAssessment("foo")

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override def afterEach() {
    await(repository.drop)
  }

  "collection" should {
    "have a unique index on saUtr" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("saUtrIndex") &&
        i.key == Seq("saUtr" -> Ascending) &&
        i.background &&
        i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a self assessment" in {
      val result = await(repository.create(selfAssessment))

      result shouldBe selfAssessment
    }

    "fail to create a duplicate self assessment" in {
      await(repository.create(selfAssessment))

      intercept[DuplicateSelfAssessmentException](
        await(repository.create(selfAssessment)))
    }
  }

  "find by utr" should {
    "return None when there are no self assessments for a given utr" in {
      await(repository.findByUtr(utr)) shouldBe None
    }

    "return the self assessment" in {
      await(repository.create(selfAssessment))

      val result = await(repository.findByUtr(utr))

      result shouldBe Some(selfAssessment)
    }
  }
}
