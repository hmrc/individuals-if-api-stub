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

import testUtils.RepositoryTestHelper
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.repository.individuals.EmploymentRepository

class EmploymentRepositorySpec extends RepositoryTestHelper {

  val nino = "XH123456A"
  val trn = "123456789"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"
  val hoRp2Fields = "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate),payments(date,paidTaxablePay))"

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
        employerRef = Some("247/ZT6767895A"),
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

  val employment2: Employment =
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
      employerRef = Some("123/ZT6767895A"),
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

  val employments2 = Employments(Seq(employment2))

  val repository =
    fakeApplication.injector.instanceOf[EmploymentRepository]

  "collection" should {

    "have a unique index on nino and trn" in {

      repository.indexes.find{ i =>
        i.getOptions.getName.contains("id") &&
          i.getKeys.toBsonDocument.getFirstKey == "id" &&
          i.getOptions.isBackground &&
          i.getOptions.isUnique
      } should not be None
    }
  }

  "create" should {

    "create an employment with a nino" in {
      val result = await(repository.create("nino", nino, startDate, endDate, useCase, employments))
      result shouldBe employments
    }

    "fail to create duplicate" in {
      await(repository.create("nino", nino, startDate, endDate, useCase, employments))
      intercept[Exception](await(repository.create("nino", nino, startDate, endDate, useCase, employments)))
    }

    "create an employment with a trn" in {
      val result = await(repository.create("trn", trn, startDate, endDate, useCase, employments))
      result shouldBe employments
    }
  }

  "findByIdAndType" should {

    "return None when there are no details for a given nino" in {
      await(repository.findByIdAndType("nino", nino, startDate, endDate, Some(fields), None)) shouldBe None
    }

    "return None when there are no details for a given trn" in {
      await(repository.findByIdAndType("trn", trn, startDate, endDate, Some(fields), None)) shouldBe None
    }

    "return a single record with id" in {
      await(repository.create("nino", nino, startDate, endDate, useCase, employments))
      val result = await(repository.findByIdAndType("nino", nino, startDate, endDate, Some(fields), None))
      result.get shouldBe employments
    }

    "return correct result based on empRef" in {
      await(repository.create(idType = "nino", nino, startDate, endDate, "HO-RP2", employments))
      await(repository.create(idType = "nino", nino, startDate, endDate, "HO-RP2", employments2))

      val employmentEmployerRef = employments.employments.head.employerRef.get
      val employmentEmployerRef2 = employments2.employments.head.employerRef.get

      val result = await(repository.findByIdAndType("nino", nino, startDate, endDate, Some(hoRp2Fields), Some(s"employments[]/employerRef eq '$employmentEmployerRef'")))
      val result2 = await(repository.findByIdAndType("nino", nino, startDate, endDate, Some(hoRp2Fields), Some(s"employments[]/employerRef eq '$employmentEmployerRef2'")))

      result.get shouldBe employments
      result2.get shouldBe employments2
      result2.get should not be employments
    }
  }
}
