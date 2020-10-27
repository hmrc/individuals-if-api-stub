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

package unit.uk.gov.hmrc.individualsifapistub.util.domain

import play.api.libs.json.Json
import testUtils.AddressHelpers
import uk.gov.hmrc.individualsifapistub.domain.DetailsResponse.addressFormat
import uk.gov.hmrc.individualsifapistub.domain.EmploymentsResponse._
import uk.gov.hmrc.individualsifapistub.domain._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class EmploymentsResponseSpec extends UnitSpec with AddressHelpers {

  val ninoDetails = Id(Some("XH123456A"), None)
  val trnDetails = Id(None, Some("12345678"))

  val address = Address(
    Some("line1"),
    Some("line2"),
    Some("line3"),
    Some("line4"),
    Some("line5"),
    Some("postcode")
  )

  val employer = Employer(
    name = Some("Name"),
    address = Some(address),
    districtNumber = Some("ABC"),
    schemeRef = Some("ABC")
  )

  val employmentDetail = EmploymentDetail(
    startDate = Some("2001-12-31"),
    endDate = Some("2002-05-12"),
    payFrequency = Some("W2"),
    payrollId = Some("12341234"),
    address = Some(address))

  val payment = Payment(
    date = Some("2001-12-31"),
    ytdTaxablePay = Some(120.02),
    paidTaxablePay = Some(112.75),
    paidNonTaxOrNICPayment = Some(123123.32),
    week = Some(52),
    month = Some(12)
  )

  val employment = Employment(
    employer = Some(employer),
    employment = Some(employmentDetail),
    payments = Some(Seq(payment))
  )

  "Address" should {
    "Write to JSON successfully" in {
      val result = Json.toJson(address).validate[Address]
      result.isSuccess shouldBe true
    }
  }

  "Employer" should {
    "Write to JSON successfully" in {
      val result = Json.toJson(employer).validate[Employer]
      result.isSuccess shouldBe true
    }
  }

  "EmploymentDetail" should {
    "Write to JSON successfully" in {
      val result = Json.toJson(employmentDetail).validate[EmploymentDetail]
      result.isSuccess shouldBe true
    }

    "fail when payFrequency is not one of: W1, W2, W4, M1, M3, M6, MA, IO, IR" in {
      val result = Json.toJson(employmentDetail.copy(payFrequency = Some("XX"))).validate[EmploymentDetail]
      result.isError shouldBe true
    }

    "fail to validate incorrect start date" in {
      val result = Json.toJson(employmentDetail.copy(startDate = Some("2020-12-50"))).validate[EmploymentDetail]
      result.isError shouldBe true
    }

    "fail to validate incorrect end date" in {
      val result = Json.toJson(employmentDetail.copy(endDate = Some("2020-12-50"))).validate[EmploymentDetail]
      result.isError shouldBe true
    }
  }

  "Payment" should {
    "Write to JSON successfully" in {
      val result = Json.toJson(payment).validate[Payment]
      result.isSuccess shouldBe true
    }

    "fail to validate when not a multiple of 0.01" in {
      val result = Json.toJson(payment.copy(ytdTaxablePay = Some(123.4312123123123))).validate[Payment]
      result.isError shouldBe true
    }

    "fail to validate when value is smaller than min value" in {
      val result = Json.toJson(payment.copy(ytdTaxablePay = Some(EmploymentsResponse.minValue - 1.0))).validate[Payment]
      result.isError shouldBe true
    }

    "fail to validate when value is larger than max value" in {
      val result = Json.toJson(payment.copy(ytdTaxablePay = Some(EmploymentsResponse.maxValue + 1.0))).validate[Payment]
      result.isError shouldBe true
    }
  }

  "Employment Response" should {
    "Write to JSON successfully" in {
      val result = Json.toJson( Seq(employment)).validate[Seq[Employment]]
      result.isSuccess shouldBe true
    }
  }
}
