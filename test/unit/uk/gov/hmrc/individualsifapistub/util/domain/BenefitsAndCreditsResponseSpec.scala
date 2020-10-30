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
import uk.gov.hmrc.individualsifapistub.domain.BenefitsAndCredits._
import uk.gov.hmrc.individualsifapistub.domain._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class BenefitsAndCreditsResponseSpec extends UnitSpec with AddressHelpers {

  val idValue: Double = 12345

  val validPayments = Payments(
    Some("2012-12-12"),
    Some("2012-12-12"),
    Some("2012-12-12"),
    Some("2012-12-12"),
    Some("A"),
    Some("2012-12-12"),
    Some("2012-12-12"),
    Some(12),
    Some("2012-12-12"),
    Some(1234134123),
    Some("2012-12-12")
  )

  val validChildTaxCredit: ChildTaxCredit = ChildTaxCredit(
    Some(12345),
    Some(12345),
    Some(12345),
    Some(12345),
    Some(12345),
    Some(12345)
  )

  val validWorkTaxCredit: WorkTaxCredit = WorkTaxCredit(
    Some(12345),
    Some(12345),
    Some(12345)
  )

  val validAwards: Awards = Awards(
    payProfCalcDate = Some("2012-12-12"),
    startDate = Some("2012-12-12"),
    endDate = Some("2012-12-12"),
    totalEntitlement = Some(12345),
    workTaxCredit = Some(validWorkTaxCredit),
    childTaxCredit = Some(validChildTaxCredit),
    grossTaxYearAmount = Some(12345),
    payments = Some(validPayments)
  )

  val validResponse: Application = Application(
    id = idValue,
    ceasedDate = Some("2012-12-12"),
    entStartDate = Some("2012-12-12"),
    entEndDate = Some("2012-12-12"),
    Some(validAwards)
  )

  "Payments" should {
    "Write to JSON successfully" in {
      val result = Json.toJson(validPayments).validate[Payments]
      result.isSuccess shouldBe true
    }

    "fail when status is not one of: A, D, S, C, X" in {
      val result = Json.toJson(validPayments.copy(status = Some("XX"))).validate[Payments]
      result.isError shouldBe true
    }

    "fail to validate incorrect period start date" in {
      val result = Json.toJson(validPayments.copy(periodStartDate = Some("2012-12-50"))).validate[Payments]
      result.isError shouldBe true
    }

    "fail to validate incorrect period end date" in {
      val result = Json.toJson(validPayments.copy(periodEndDate = Some("2020-12-50"))).validate[Payments]
      result.isError shouldBe true
    }

    "fail to validate incorrect start date" in {
      val result = Json.toJson(validPayments.copy(startDate = Some("2020-12-50"))).validate[Payments]
      result.isError shouldBe true
    }

    "fail to validate incorrect end date" in {
      val result = Json.toJson(validPayments.copy(endDate = Some("2020-12-50"))).validate[Payments]
      result.isError shouldBe true
    }

    "fail to validate incorrect posted date" in {
      val result = Json.toJson(validPayments.copy(postedDate = Some("2020-12-50"))).validate[Payments]
      result.isError shouldBe true
    }

    "fail to validate incorrect next due date" in {
      val result = Json.toJson(validPayments.copy(nextDueDate = Some("2020-12-50"))).validate[Payments]
      result.isError shouldBe true
    }
  }

  "ChildTaxCredit" should {
    "Write to JSON successfully" in {
      val result = Json.toJson(validChildTaxCredit).validate[ChildTaxCredit]
      result.isSuccess shouldBe true
    }

    "fail to validate when not a multiple of 0.01" in {
      val result = Json.toJson(validChildTaxCredit.copy(childCareAmount = Some(123.4312123123123))).validate[ChildTaxCredit]
      result.isError shouldBe true
    }

//    "fail to validate when value is smaller than min value" in {
//      val result = Json.toJson(validChildTaxCredit.copy(childCareAmount = Some(Bene.minValue - 1.0))).validate[ChildTaxCredit]
//      result.isError shouldBe true
//    }
//
//    "fail to validate when value is larger than max value" in {
//      val result = Json.toJson(payment.copy(ytdTaxablePay = Some(Employments.maxValue + 1.0))).validate[Payment]
//      result.isError shouldBe true
//    }
  }
//
//  "Payment" should {
//    "Write to JSON successfully" in {
//      val result = Json.toJson(payment).validate[Payment]
//      result.isSuccess shouldBe true
//    }
//
//    "fail to validate when week is below 1" in {
//      val result = Json.toJson(payment.copy(week = Some(0))).validate[Payment]
//      result.isError shouldBe true
//    }
//
//    "fail to validate when week is above 56" in {
//      val result = Json.toJson(payment.copy(week = Some(57))).validate[Payment]
//      result.isError shouldBe true
//    }
//
//    "fail to validate when month is below 1" in {
//      val result = Json.toJson(payment.copy(month = Some(0))).validate[Payment]
//      result.isError shouldBe true
//    }
//
//    "fail to validate when month is above 12" in {
//      val result = Json.toJson(payment.copy(month = Some(13))).validate[Payment]
//      result.isError shouldBe true
//    }
//
//    "fail to validate when not a multiple of 0.01" in {
//      val result = Json.toJson(payment.copy(ytdTaxablePay = Some(123.4312123123123))).validate[Payment]
//      result.isError shouldBe true
//    }
//
//    "fail to validate when value is smaller than min value" in {
//      val result = Json.toJson(payment.copy(ytdTaxablePay = Some(Employments.minValue - 1.0))).validate[Payment]
//      result.isError shouldBe true
//    }
//
//    "fail to validate when value is larger than max value" in {
//      val result = Json.toJson(payment.copy(ytdTaxablePay = Some(Employments.maxValue + 1.0))).validate[Payment]
//      result.isError shouldBe true
//    }
//  }
//
//  "Employments" should {
//    "Write to JSON successfully" in {
//      val result = Json.toJson( Employments(Seq(employment))).validate[Employments]
//      result.isSuccess shouldBe true
//    }
//
//    "Write to JSON successfully when employments is empty" in {
//      val result = Json.toJson( Employments(Seq())).validate[Employments]
//      result.isSuccess shouldBe true
//    }
//  }
}
