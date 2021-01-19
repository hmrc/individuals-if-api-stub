/*
 * Copyright 2021 HM Revenue & Customs
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
import testUtils.TestHelpers
import uk.gov.hmrc.individualsifapistub.domain.TaxCredits._
import uk.gov.hmrc.individualsifapistub.domain.{Application, Awards, ChildTaxCredit, Employer, Payments, TaxCredits, WorkTaxCredit}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class TaxCreditsSpec extends UnitSpec with TestHelpers {

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
    Some("ETC"),
    Some(1234134123),
    Some("R")
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

  val validApplication: Application = Application(
    id = idValue,
    ceasedDate = Some("2012-12-12"),
    entStartDate = Some("2012-12-12"),
    entEndDate = Some("2012-12-12"),
    awards = Some(Seq(validAwards))
  )

  "Payments" should {

    "write to JSON successfully" in {
      val result = Json.toJson(validPayments).validate[Payments]
      result.isSuccess shouldBe true
    }

    "read from JSON successfully" in {

      val paymentsJson: String = """{
       |  "periodStartDate" : "2012-12-12",
       |  "periodEndDate" : "2012-12-12",
       |  "startDate" : "2012-12-12",
       |  "endDate" : "2012-12-12",
       |  "status" : "A",
       |  "postedDate" : "2012-12-12",
       |  "nextDueDate" : "2012-12-12",
       |  "frequency" : 12,
       |  "tcType" : "ETC",
       |  "amount" : 1234134123,
       |  "method" : "R"
       |}""".stripMargin

      val result = Json.parse(paymentsJson).validate[Payments]
      result.isSuccess shouldBe true
      result.get shouldBe validPayments
    }

    "fail validation" when {
      "status is not one of: A, D, S, C, X" in {
        val result = Json.toJson(validPayments.copy(status = Some("XX"))).validate[Payments]
        result.isError shouldBe true
      }

      "method is not one of: R, O, M" in {
        val result = Json.toJson(validPayments.copy(method = Some("X"))).validate[Payments]
        result.isError shouldBe true
      }

      "period start date is incorrect" in {
        val result = Json.toJson(validPayments.copy(periodStartDate = Some("2012-12-50"))).validate[Payments]
        result.isError shouldBe true
      }

      "period end date is incorrect" in {
        val result = Json.toJson(validPayments.copy(periodEndDate = Some("2020-12-50"))).validate[Payments]
        result.isError shouldBe true
      }

      "start date is incorrect" in {
        val result = Json.toJson(validPayments.copy(startDate = Some("2020-12-50"))).validate[Payments]
        result.isError shouldBe true
      }

      "end date is incorrect" in {
        val result = Json.toJson(validPayments.copy(endDate = Some("2020-12-50"))).validate[Payments]
        result.isError shouldBe true
      }

      "posted date is incorrect" in {
        val result = Json.toJson(validPayments.copy(postedDate = Some("2020-12-50"))).validate[Payments]
        result.isError shouldBe true
      }

      "next due date is incorrect" in {
        val result = Json.toJson(validPayments.copy(nextDueDate = Some("2020-12-50"))).validate[Payments]
        result.isError shouldBe true
      }
    }
  }

  "ChildTaxCredit" should {

    "write to JSON successfully" in {
      val result = Json.toJson(validChildTaxCredit).validate[ChildTaxCredit]
      result.isSuccess shouldBe true
    }

    "read from JSON successfully" in {

      val childTaxCreditJson:String =
        """
          |{
          |  "childCareAmount" : 12345,
          |  "ctcChildAmount" : 12345,
          |  "familyAmount" : 12345,
          |  "babyAmount" : 12345,
          |  "entitlementYTD" : 12345,
          |  "paidYTD" : 12345
          |}
          |""".stripMargin

      val result = Json.parse(childTaxCreditJson).validate[ChildTaxCredit]
      result.isSuccess shouldBe true
      result.get shouldBe validChildTaxCredit

    }

    "fail validation" when {

      "not a multiple of 0.01" in {
        val result = Json.toJson(validChildTaxCredit.copy(childCareAmount = Some(123.4312123123123))).validate[ChildTaxCredit]
        result.isError shouldBe true
      }


      "value is smaller than min value" in {
        val result = Json.toJson(validChildTaxCredit.copy(childCareAmount = Some(TaxCredits.minPaymentValue - 1.0))).validate[ChildTaxCredit]
        result.isError shouldBe true
      }


      "value is larger than max value" in {
        val result = Json.toJson(validChildTaxCredit.copy(childCareAmount = Some(TaxCredits.maxPaymentValue + 1.0))).validate[ChildTaxCredit]
        result.isError shouldBe true
      }
    }
  }


  "WorkTaxCredit" should {
    "write to JSON successfully" in {
      val result = Json.toJson(validWorkTaxCredit).validate[WorkTaxCredit]
      result.isSuccess shouldBe true
    }

    "read from JSON successfully" in {
      val workTaxCreditJson: String =
        """
          |{
          |  "amount" : 12345,
          |  "entitlementYTD" : 12345,
          |  "paidYTD" : 12345
          |}
          |""".stripMargin

      val result = Json.parse(workTaxCreditJson).validate[WorkTaxCredit]
      result.isSuccess shouldBe true
      result.get shouldBe validWorkTaxCredit
    }
  }

  "Awards" should {
    "write to JSON successfully" in {
      val result = Json.toJson(validAwards).validate[Awards]
      result.isSuccess shouldBe true
    }

    "read from JSON successfully" in {

      val awardsJson: String =
        """
          |{
          |  "payProfCalcDate" : "2012-12-12",
          |  "startDate" : "2012-12-12",
          |  "endDate" : "2012-12-12",
          |  "totalEntitlement" : 12345,
          |  "workTaxCredit" : {
          |    "amount" : 12345,
          |    "entitlementYTD" : 12345,
          |    "paidYTD" : 12345
          |  },
          |  "childTaxCredit" : {
          |    "childCareAmount" : 12345,
          |    "ctcChildAmount" : 12345,
          |    "familyAmount" : 12345,
          |    "babyAmount" : 12345,
          |    "entitlementYTD" : 12345,
          |    "paidYTD" : 12345
          |  },
          |  "grossYearTaxAmount" : 12345,
          |  "payments" : {
          |    "periodStartDate" : "2012-12-12",
          |    "periodEndDate" : "2012-12-12",
          |    "startDate" : "2012-12-12",
          |    "endDate" : "2012-12-12",
          |    "status" : "A",
          |    "postedDate" : "2012-12-12",
          |    "nextDueDate" : "2012-12-12",
          |    "frequency" : 12,
          |    "tcType" : "ETC",
          |    "amount" : 1234134123,
          |    "method" : "R"
          |  }
          |}
          |""".stripMargin

      val result = Json.parse(awardsJson).validate[Awards]
      result.isSuccess shouldBe true
      result.get shouldBe validAwards
    }
  }


  "Application" should {
    "write to JSON successfully" in {
      val result = Json.toJson(validApplication).validate[Application]
      result.isSuccess shouldBe true
    }

    "read from JSON successfully" in {

      val applicationJson: String =
        """
          |{
          |  "id" : 12345,
          |  "ceasedDate" : "2012-12-12",
          |  "entStartDate" : "2012-12-12",
          |  "entEndDate" : "2012-12-12",
          |  "awards" : [ {
          |    "payProfCalcDate" : "2012-12-12",
          |    "startDate" : "2012-12-12",
          |    "endDate" : "2012-12-12",
          |    "totalEntitlement" : 12345,
          |    "workTaxCredit" : {
          |      "amount" : 12345,
          |      "entitlementYTD" : 12345,
          |      "paidYTD" : 12345
          |    },
          |    "childTaxCredit" : {
          |      "childCareAmount" : 12345,
          |      "ctcChildAmount" : 12345,
          |      "familyAmount" : 12345,
          |      "babyAmount" : 12345,
          |      "entitlementYTD" : 12345,
          |      "paidYTD" : 12345
          |    },
          |    "grossYearTaxAmount" : 12345,
          |    "payments" : {
          |      "periodStartDate" : "2012-12-12",
          |      "periodEndDate" : "2012-12-12",
          |      "startDate" : "2012-12-12",
          |      "endDate" : "2012-12-12",
          |      "status" : "A",
          |      "postedDate" : "2012-12-12",
          |      "nextDueDate" : "2012-12-12",
          |      "frequency" : 12,
          |      "tcType" : "ETC",
          |      "amount" : 1234134123,
          |      "method" : "R"
          |    }
          |  } ]
          |}
          |""".stripMargin

      val result = Json.parse(applicationJson).validate[Application]
      result.isSuccess shouldBe true
      result.get shouldBe validApplication
    }
  }
}
