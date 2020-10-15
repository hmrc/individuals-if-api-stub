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
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualsifapistub.domain.{CreateEmploymentRequest, Employment}
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global

class EmploymentRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val employmentRepository =
    fakeApplication.injector.instanceOf[EmploymentRepository]
  val employerReference = EmpRef("123", "DI45678")
  val nino = Nino("NA000799C")

  override def beforeEach() {
    await(employmentRepository.drop)
    await(employmentRepository.ensureIndexes)
  }

  override def afterEach() {
    await(employmentRepository.drop)
  }

  "create" should {
    "create an employment" in {
      val result = await(
        employmentRepository
          .create(employerReference, nino, aCreateEmploymentRequest))
      result shouldBe anEmployment(employerReference, nino)
    }

    "allow multiple employments for the same employer reference and nino" in {
      await(
        employmentRepository
          .create(employerReference, nino, aCreateEmploymentRequest))
      await(
        employmentRepository
          .create(employerReference, nino, aCreateEmploymentRequest))
      val result = await(employmentRepository.findAll())
      result.size shouldBe 2
    }
  }

  "findByReferenceAndNino" should {

    "return all records for a given paye reference and nino" in {
      val employment = anEmployment(employerReference, nino)

      await(
        employmentRepository
          .create(employerReference, nino, aCreateEmploymentRequest))
      await(
        employmentRepository
          .create(EmpRef("321", "EI45678"), nino, aCreateEmploymentRequest))
      await(
        employmentRepository.create(employerReference,
                                    Nino("AA123456C"),
                                    aCreateEmploymentRequest))

      val result = await(
        employmentRepository.findByReferenceAndNino(employerReference, nino))

      result shouldBe Seq(employment)
    }

    "return an empty list if no records exist for a given pay reference and nino" in {
      await(
        employmentRepository
          .create(employerReference, nino, aCreateEmploymentRequest))

      val result = await(
        employmentRepository.findByReferenceAndNino(EmpRef("321", "EI45678"),
                                                    nino))

      result.isEmpty shouldBe true
    }
  }

  "find by nino" should {

    "return an empty sequence when a corresponding employment does not exist" in {
      await(employmentRepository.findBy(nino)).isEmpty shouldBe true
    }

    "return a non-empty sequence when corresponding employments exist" in {
      await(
        employmentRepository
          .create(employerReference, nino, aCreateEmploymentRequest))
      val employments = await(employmentRepository.findBy(nino))
      employments.nonEmpty shouldBe true
      employments.size shouldBe 1
      employments.head shouldBe anEmployment(employerReference, nino)
    }

  }

  private val aCreateEmploymentRequest = CreateEmploymentRequest("foo")

  private def anEmployment(empRef: EmpRef, nino: Nino) = Employment("foo")
}
