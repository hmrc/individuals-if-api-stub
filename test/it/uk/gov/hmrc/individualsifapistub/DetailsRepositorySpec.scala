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
import uk.gov.hmrc.individualsifapistub.domain.{CreateDetailsRequest, Details}
import uk.gov.hmrc.individualsifapistub.repository.DetailsRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global

class DetailsRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val repository = fakeApplication.injector.instanceOf[DetailsRepository]

  val id = "2432552635"
  val requestBody = "requestBody"
  val request = CreateDetailsRequest(requestBody)
  val details = Details(id, request.body)

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override def afterEach() {
  }

  "collection" should {
    "have a unique index on saUtr" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("idIndex") &&
        i.key == Seq("id" -> Ascending) &&
        i.background &&
        i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a self assessment" in {
      val result = await(repository.create(id, request))

      result shouldBe details
    }

    "fail to create duplicate details" in {
      await(repository.create(id, request))

      intercept[Exception](await(repository.create(id, request)))
    }
  }

  "find by id" should {
    "return None when there are no details for a given id" in {
      await(repository.findById(id)) shouldBe None
    }

    "return details" in {
      await(repository.create(id, request))

      val result = await(repository.findById(id))

      result shouldBe Some(details
      )
    }
  }
}
