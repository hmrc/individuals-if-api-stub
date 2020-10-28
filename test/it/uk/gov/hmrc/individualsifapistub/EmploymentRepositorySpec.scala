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
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

class EmploymentRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach {

  val nino = "XH123456A"
  val trn = "123456789"

  val employment: Employment =
      Employment(
        employer = Some(Employer(
          name = Some("Name"),
          address = Some(Address(
            Some("line1"),
            Some("line2"),
            Some("line3"),
            Some("line4"),
            Some("line5"),
            Some("postcode")
          )),
          districtNumber = Some("ABC"),
          schemeRef = Some("ABC")
        )),
        employment = Some(EmploymentDetail(
          startDate = Some("2001-12-31"),
          endDate = Some("2002-05-12"),
          payFrequency = Some("W2"),
          payrollId = Some("12341234"),
          address = Some(Address(
            Some("line1"),
            Some("line2"),
            Some("line3"),
            Some("line4"),
            Some("line5"),
            Some("postcode")
          )))),
        payments = Some(Seq(Payment(
          date = Some("2001-12-31"),
          ytdTaxablePay = Some(120.02),
          paidTaxablePay = Some(112.75),
          paidNonTaxOrNICPayment = Some(123123.32),
          week = Some(52),
          month = Some(12)
        )
        )
        )
      )

  val employments = Employments(Seq(employment))

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

  "collection" should {
    "have a unique index on nino" in {
      await(employmentRepository.collection.indexesManager.list()).find({ i =>
        i.name.contains("nino") &&
          i.key == Seq("id.nino" -> Ascending) &&
          i.background &&
          i.unique
      }) should not be None
    }
    "have a unique index on trn" in {
      await(employmentRepository.collection.indexesManager.list()).find({ i =>
        i.name.contains("trn") &&
          i.key == Seq("id.trn" -> Ascending) &&
          i.background &&
          i.unique
      }) should not be None
    }
  }

  "create" should {
    "create an employment with a nino" in {
      val result = await(employmentRepository.create("nino", nino, employments))
      result shouldBe employments
    }

    "create an employment with a trn" in {
      val result = await(employmentRepository.create("trn", trn, employments))
      result shouldBe employments
    }

    "fail to create duplicate details" in {
      await(employmentRepository.create("nino", nino, employments))
      intercept[Exception](await(employmentRepository.create("nino", nino, employments)))
    }
  }

  "findByIdAndType" should {

    "return None when there are no details for a given nino" in {
      await(employmentRepository.findByIdAndType("nino", nino)) shouldBe None
    }

    "return None when there are no details for a given trn" in {
      await(employmentRepository.findByIdAndType("trn", trn)) shouldBe None
    }

    "return a single record with id" in {
      await(employmentRepository.create("nino", nino, employments))
      val result = await(employmentRepository.findByIdAndType("nino", nino))
      result.get shouldBe employments
    }

  }
}
