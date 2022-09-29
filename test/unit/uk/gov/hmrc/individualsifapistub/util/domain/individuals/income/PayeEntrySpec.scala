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

package unit.uk.gov.hmrc.individualsifapistub.util.domain.individuals.income

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain.individuals.IncomePaye._
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class PayeEntrySpec extends UnitSpec {

  val validPayeEntry = PayeEntry(
    Some("K971"),
    Some("36"),
    Some(19157.5),
    Some(3095.89),
    Some(159228.49),
    Some("345/34678"),
    Some(LocalDate.parse("2006-02-27")),
    Some(16533.95),
    Some("18-19"),
    Some("3"),
    Some("2"),
    Some("W4"),
    Some(198035.8),
    Some(createValidEmployeeNics()),
    Some(createValidEmployeePensionContribs()),
    Some(createValidBenefits()),
    Some(createValidStatutoryPayToDate()),
    Some(createValidStudentLoan()),
    Some(createValidPostGradLoan()),
    Some(createValodIFGrossEarningsForNics()),
    Some(createValidTotalEmployerNics()),
    Some(createValidAdditionalFields())
  )

  val invalidPayeEntry = PayeEntry(
    Some("TEST"),
    Some("TEST"),
    Some(19157.5),
    Some(3095.89),
    Some(159228.49),
    Some("TEST"),
    Some(LocalDate.parse("2006-02-27")),
    Some(16533.95),
    Some("TEST"),
    Some("TEST"),
    Some("TEST"),
    Some("TEST"),
    Some(198035.8),
    Some(createValidEmployeeNics()),
    Some(createValidEmployeePensionContribs()),
    Some(createValidBenefits()),
    Some(createValidStatutoryPayToDate()),
    Some(createValidStudentLoan()),
    Some(createValidPostGradLoan()),
    Some(createValodIFGrossEarningsForNics()),
    Some(createValidTotalEmployerNics()),
    Some(createValidAdditionalFields())
  )

  "PayeEntry" should {
    "Write to json" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "taxCode": "K971",
          |  "paidHoursWorked": "36",
          |  "taxablePayToDate": 19157.5,
          |  "totalTaxToDate": 3095.89,
          |  "taxDeductedOrRefunded": 159228.49,
          |  "employerPayeRef": "345/34678",
          |  "paymentDate": "2006-02-27",
          |  "taxablePay": 16533.95,
          |  "taxYear": "18-19",
          |  "monthlyPeriodNumber": "3",
          |  "weeklyPeriodNumber": "2",
          |  "payFrequency": "W4",
          |  "dednsFromNetPay": 198035.8,
          |  "employeeNICs": {
          |    "inPayPeriod1": 15797.45,
          |    "inPayPeriod2": 13170.69,
          |    "inPayPeriod3": 16193.76,
          |    "inPayPeriod4": 30846.56,
          |    "ytd1": 10633.5,
          |    "ytd2": 15579.18,
          |    "ytd3": 110849.27,
          |    "ytd4": 162081.23
          |  },
          |  "employeePensionContribs": {
          |    "paidYTD": 169731.51,
          |    "notPaidYTD": 173987.07,
          |    "paid": 822317.49,
          |    "notPaid": 818841.65
          |  },
          |  "benefits": {
          |    "taxedViaPayroll": 506328.1,
          |    "taxedViaPayrollYTD": 246594.83
          |  },
          |  "statutoryPayYTD": {
          |    "maternity":15797.45,
          |    "paternity":13170.69,
          |    "adoption":16193.76,
          |    "parentalBereavement":30846.56
          |  },
          |  "studentLoan": {
          |    "planType": "02",
          |    "repaymentsInPayPeriod": 88478,
          |    "repaymentsYTD": 545
          |  },
          |  "postGradLoan": {
          |    "repaymentsInPayPeriod": 15636,
          |    "repaymentsYTD": 46849
          |  },
          |  "grossEarningsForNICs": {
          |    "inPayPeriod1": 169731.51,
          |    "inPayPeriod2": 173987.07,
          |    "inPayPeriod3": 822317.49,
          |    "inPayPeriod4": 818841.65
          |  },
          |  "totalEmployerNICs": {
          |    "inPayPeriod1": 15797.45,
          |    "inPayPeriod2": 13170.69,
          |    "inPayPeriod3": 16193.76,
          |    "inPayPeriod4": 30846.56,
          |    "ytd1": 10633.5,
          |    "ytd2": 15579.18,
          |    "ytd3": 110849.27,
          |    "ytd4": 162081.23
          |  },
          |  "payroll": {
          |    "id": "yxz8Lt5?/`/>6]5b+7%>o-y4~W5suW"
          |  },
          |  "employee": {
          |    "hasPartner": false
          |  }
          |}
          |""".stripMargin
      )

      val result = Json.toJson(validPayeEntry)

      result shouldBe expectedJson
    }

    "Validate successfully when given valid PayeEntry" in {
      val result = Json.toJson(validPayeEntry).validate[PayeEntry]
      result.isSuccess shouldBe true
    }

    "Validate unsuccessfully when given invalid PayeEntry" in {
      val result = Json.toJson(invalidPayeEntry).validate[PayeEntry]
      result.isError shouldBe true
    }
  }

  private def createValidEmployeeNics() = {
    EmployeeNics(
      Some(15797.45),
      Some(13170.69),
      Some(16193.76),
      Some(30846.56),
      Some(10633.5),
      Some(15579.18),
      Some(110849.27),
      Some(162081.23)
    )
  }

  private def createValidEmployeePensionContribs() = EmployeePensionContribs(Some(169731.51), Some(173987.07), Some(822317.49), Some(818841.65))

  private def createValidBenefits() = Benefits(Some(506328.1), Some(246594.83))

  private def createValidStudentLoan() = StudentLoan(Some("02"), Some(88478), Some(545))

  private def createValidPostGradLoan() = PostGradLoan(Some(15636), Some(46849))

  def createValodIFGrossEarningsForNics() =
    GrossEarningsForNics(Some(169731.51), Some(173987.07), Some(822317.49), Some(818841.65))

  private def createValidAdditionalFields() =
    AdditionalFields(Some(false), Some("yxz8Lt5?/`/>6]5b+7%>o-y4~W5suW"))

  def createValidTotalEmployerNics() =
    TotalEmployerNics(
      Some(15797.45),
      Some(13170.69),
      Some(16193.76),
      Some(30846.56),
      Some(10633.5),
      Some(15579.18),
      Some(110849.27),
      Some(162081.23)
    )

  def createValidStatutoryPayToDate() =
    StatutoryPayYTD(
      Some(15797.45),
      Some(13170.69),
      Some(16193.76),
      Some(30846.56)
    )
}
