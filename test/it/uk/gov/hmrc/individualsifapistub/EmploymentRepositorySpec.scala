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
import uk.gov.hmrc.individualsifapistub.domain.Employment
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global

class EmploymentRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach {

  val id = "1234567890"

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val employmentRepository =
    fakeApplication.injector.instanceOf[EmploymentRepository]

  override def beforeEach() {
    await(employmentRepository.drop)
    await(employmentRepository.ensureIndexes)
  }

  override def afterEach() {
    await(employmentRepository.drop)
  }

  "create" should {
    "create an employment" in {
      val result = await(employmentRepository.create(id))
      result shouldBe Employment(id)
    }
  }

  "findById" should {

    "return a single record with id" in {
      val employment = Employment(id)
      await(employmentRepository.create(id))
      val result = await(employmentRepository.findById(id))

      result.get shouldBe employment
    }

    "return an empty list if no records exist for a given pay reference and nino" in {
      await(employmentRepository.create(id))
      val result = await(employmentRepository.findById("not an id"))
      result.isEmpty shouldBe true
    }
  }
}
