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

import play.api.libs.json.{JsNumber, Json}
import testUtils.TestHelpers
import uk.gov.hmrc.individualsifapistub.domain.{Address, Employer, Employment, EmploymentDetail, Employments, Identifier, Payment}
import uk.gov.hmrc.individualsifapistub.domain.Employments._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class EmploymentsSpec extends UnitSpec with TestHelpers {

  val ninoDetails = Identifier(Some("XH123456A"), None)
  val trnDetails = Identifier(None, Some("12345678"))

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
    ytdTaxablePay = Some(162081.23),
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

    "write to JSON successfully" in {
      val result = Json.toJson(address).validate[Address]
      result.isSuccess shouldBe true
    }

    "validate successfully" when {
      "lines are equal to min length" in {
        val line = ""
        val result = Json.toJson(address.copy(line1 = Some(line))).validate[Address]
        result.isSuccess shouldBe true
      }

      "lines are equal to max length" in {
        val line = generateString(100)
        val result = Json.toJson(address.copy(line1 = Some(line))).validate[Address]
        result.isSuccess shouldBe true
      }
    }

    "fail to validate" when {

      "lines are longer than max length" in {
        val line = generateString(101)
        val result = Json.toJson(address.copy(line1 = Some(line))).validate[Address]
        result.isError shouldBe true
      }
    }
  }

  "Employer" should {

    "write to JSON successfully" in {
      val result = Json.toJson(employer).validate[Employer]
      result.isSuccess shouldBe true
    }

    "read successfully" when {

      "JSON is complete" in {

        val employerJson: String =
          """{
            |  "name" : "Name",
            |  "address" : {
            |    "line1" : "line1",
            |    "line2" : "line2",
            |    "line3" : "line3",
            |    "line4" : "line4",
            |    "line5" : "line5",
            |    "postcode" : "postcode"
            |  },
            |  "districtNumber" : "ABC",
            |  "schemeRef" : "ABC"
            |}""".stripMargin

        val result = Json.parse(employerJson).validate[Employer]
        result.isSuccess shouldBe true
        result.get shouldBe employer
      }

      "JSON is incomplete" in {

        val employerJson: String =
          """{
            |  "name" : "Name",
            |  "address" : {
            |    "line1" : "line1",
            |    "line2" : "line2",
            |    "line3" : "line3",
            |    "postcode" : "postcode"
            |  }
            |}""".stripMargin

        val result = Json.parse(employerJson).validate[Employer]
        result.isSuccess shouldBe true
      }
    }
  }

  "EmploymentDetail" should {

    "write to JSON successfully" in {
      val result = Json.toJson(employmentDetail).validate[EmploymentDetail]
      result.isSuccess shouldBe true
    }

    "read successfully" when {

      "JSON is complete" in {

        val employmentDetailJson = """{
                                     |  "startDate" : "2001-12-31",
                                     |  "endDate" : "2002-05-12",
                                     |  "payFrequency" : "W2",
                                     |  "payrollId" : "12341234",
                                     |  "address" : {
                                     |    "line1" : "line1",
                                     |    "line2" : "line2",
                                     |    "line3" : "line3",
                                     |    "line4" : "line4",
                                     |    "line5" : "line5",
                                     |    "postcode" : "postcode"
                                     |  }
                                     |}""".stripMargin

        val result = Json.parse(employmentDetailJson).validate[EmploymentDetail]
        result.isSuccess shouldBe true
        result.get shouldBe employmentDetail
      }

      "JSON is incomplete" in {

        val employmentDetailJson = """{
                                     |  "startDate" : "2001-12-31",
                                     |  "endDate" : "2002-05-12",
                                     |  "payFrequency" : "W2"
                                     |}""".stripMargin

        val result = Json.parse(employmentDetailJson).validate[EmploymentDetail]
        result.isSuccess shouldBe true
      }
    }

    "fail validation" when {

      "payFrequency is not one of: W1, W2, W4, M1, M3, M6, MA, IO, IR" in {
        val result = Json.toJson(employmentDetail.copy(payFrequency = Some("XX"))).validate[EmploymentDetail]
        result.isError shouldBe true
      }

      "start date is invalid" in {
        val result = Json.toJson(employmentDetail.copy(startDate = Some("2020-12-50"))).validate[EmploymentDetail]
        result.isError shouldBe true
      }

      "end date is invalid" in {
        val result = Json.toJson(employmentDetail.copy(endDate = Some("2020-12-50"))).validate[EmploymentDetail]
        result.isError shouldBe true
      }
    }
  }

  "paymentAmountValidator" should {

    "validate successfully" when {

      "value is larger than min value" in {
        val result = JsNumber(Employments.minValue + 1.0).validate[Double](paymentAmountValidator)
        result.isSuccess shouldBe true
      }

      "value is smaller than max value" in {
        val result = JsNumber(Employments.maxValue - 1.0).validate[Double](paymentAmountValidator)
        result.isSuccess shouldBe true
      }
    }

    "fail validation" when {

      "not a multiple of 0.01" in {
        val result = JsNumber(123.4312123123123).validate[Double](paymentAmountValidator)
        result.isError shouldBe true
      }

      "value is smaller than min value" in {
        val result = JsNumber(Employments.minValue - 1.0).validate[Double](paymentAmountValidator)
        result.isError shouldBe true
      }

      "value is larger than max value" in {
        val result = JsNumber(Employments.maxValue + 1.0).validate[Double](paymentAmountValidator)
        result.isError shouldBe true
      }
    }
  }

  "Payment" should {
    "write to JSON successfully" in {
      val result = Json.toJson(payment).validate[Payment]
      result.isSuccess shouldBe true
    }

    "read successfully" when {

      "JSON is complete" in {
        val paymentJson = """{
                            |  "date" : "2001-12-31",
                            |  "ytdTaxablePay" : 162081.23,
                            |  "paidTaxablePay" : 112.75,
                            |  "paidNonTaxOrNICPayment" : 123123.32,
                            |  "week" : 52,
                            |  "month" : 12
                            |}""".stripMargin

        val result = Json.parse(paymentJson).validate[Payment]
        result.isSuccess shouldBe true
        result.get shouldBe payment
      }

      "JSON is incomplete" in {
        val paymentJson = """{
                            |  "date" : "2001-12-31",
                            |  "ytdTaxablePay" : 162081.23,
                            |  "month" : 12
                            |}""".stripMargin

        val result = Json.parse(paymentJson).validate[Payment]
        result.isSuccess shouldBe true
      }
    }

    "fail validation" when {

      "week is below 1" in {
        val result = Json.toJson(payment.copy(week = Some(0))).validate[Payment]
        result.isError shouldBe true
      }

      "week is above 56" in {
        val result = Json.toJson(payment.copy(week = Some(57))).validate[Payment]
        result.isError shouldBe true
      }

      "month is below 1" in {
        val result = Json.toJson(payment.copy(month = Some(0))).validate[Payment]
        result.isError shouldBe true
      }

      "month is above 12" in {
        val result = Json.toJson(payment.copy(month = Some(13))).validate[Payment]
        result.isError shouldBe true
      }
    }
  }

  "Employments" should {
    "write to JSON successfully"  when {
      "employments is not empty" in {
        val result = Json.toJson( Employments(Seq(employment))).validate[Employments]
        result.isSuccess shouldBe true
      }

      "employments is empty" in {
        val result = Json.toJson( Employments(Seq())).validate[Employments]
        result.isSuccess shouldBe true
      }
    }

    "read from JSON successfully" in {

      val employmentsJson:String =
        """
          |{
          |  "employments" : [ {
          |    "employer" : {
          |      "name" : "Name",
          |      "address" : {
          |        "line1" : "line1",
          |        "line2" : "line2",
          |        "line3" : "line3",
          |        "line4" : "line4",
          |        "line5" : "line5",
          |        "postcode" : "postcode"
          |      },
          |      "districtNumber" : "ABC",
          |      "schemeRef" : "ABC"
          |    },
          |    "employment" : {
          |      "startDate" : "2001-12-31",
          |      "endDate" : "2002-05-12",
          |      "payFrequency" : "W2",
          |      "payrollId" : "12341234",
          |      "address" : {
          |        "line1" : "line1",
          |        "line2" : "line2",
          |        "line3" : "line3",
          |        "line4" : "line4",
          |        "line5" : "line5",
          |        "postcode" : "postcode"
          |      }
          |    },
          |    "payments" : [ {
          |      "date" : "2001-12-31",
          |      "ytdTaxablePay" : 162081.23,
          |      "paidTaxablePay" : 112.75,
          |      "paidNonTaxOrNICPayment" : 123123.32,
          |      "week" : 52,
          |      "month" : 12
          |    } ]
          |  } ]
          |}
          |""".stripMargin

      val result = Json.parse(employmentsJson).validate[Employments]
      result.isSuccess shouldBe true
      result.get shouldBe Employments(Seq(employment))
    }
  }
}
