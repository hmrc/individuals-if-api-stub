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

import testUtils.RepositoryTestHelper
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository

class EmploymentRepositorySpec extends RepositoryTestHelper {

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

  val repository =
    fakeApplication.injector.instanceOf[EmploymentRepository]

  "collection" should {
    "have a unique index on nino and trn" in {
      await(repository.collection.indexesManager.list()).find({ i => {
        i.name.contains("nino-trn") &&
          i.key.exists(key => key._1 == "id.nino") &&
          i.key.exists(key => key._1 == "id.trn")
          i.background &&
          i.unique
      }
      }) should not be None
    }
  }

  "create" should {
    "create an employment with a nino" in {
      val result = await(repository.create("nino", nino, employments))
      result shouldBe employments
    }

    "create an employment with a trn" in {
      val result = await(repository.create("trn", trn, employments))
      result shouldBe employments
    }

    "fail to create duplicate details" in {
      await(repository.create("nino", nino, employments))
      intercept[Exception](await(repository.create("nino", nino, employments)))
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
      await(repository.create("nino", nino, employments))
      val result = await(repository.findByIdAndType("nino", nino))
      result.get shouldBe employments
    }
  }
}
