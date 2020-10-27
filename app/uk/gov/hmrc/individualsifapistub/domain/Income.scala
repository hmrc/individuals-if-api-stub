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

package uk.gov.hmrc.individualsifapistub.domain

import play.api.libs.json._
import play.api.libs.json.Reads._

case class Income(id: String, body: String)

case class Address(line1: Option[String], line2: Option[String], line3: Option[String], line4: Option[String], postcode: Option[String])

case class PostGradLoan(repaymentsInPayPeriod: Option[Double], repaymentsYtd: Option[Double])

case class StudentLoan(
                        planType: Option[String],
                        repaymentsInPayPeriod: Option[Double],
                        repaymentsYTD: Option[Double]
                      )

case class Benefits(taxedViaPayroll: Option[Double], taxedViaPayrollYtd: Option[Double])

case class EmployeePensionContribs(
                                    paidYtd: Option[Double],
                                    notPaidYtd: Option[Double],
                                    paid: Option[Double],
                                    notPaid: Option[Double]
                                  )

case class GrossEarningsForNics(
                                 inPayPeriod1: Option[Double],
                                 inPayPeriod2: Option[Double],
                                 inPayPeriod3: Option[Double],
                                 inPayPeriod4: Option[Double]
                               )

case class EmployeeNics(
                         inPayPeriod1: Option[Double],
                         inPayPeriod2: Option[Double],
                         inPayPeriod3: Option[Double],
                         inPayPeriod4: Option[Double],
                         ytd1: Option[Double],
                         ytd2: Option[Double],
                         ytd3: Option[Double],
                         ytd4: Option[Double]
                       )

case class PayeEntry(
                      taxCode: Option[String],
                      paidHoursWork: Option[String],
                      taxablePayToDate: Option[Double],
                      totalTaxToDate: Option[Double],
                      taxDeductedOrRefunded: Option[Double],
                      employerPayeRef: Option[String],
                      paymentDate: Option[String],
                      taxablePay: Option[Double],
                      taxYear: Option[String],
                      monthlyPeriodNumber: Option[String],
                      weeklyPeriodNumber: Option[String],
                      payFrequency: Option[String],
                      dednsFromNetPay: Option[Double],
                      employeeNics: Option[EmployeeNics],
                      employeePensionContribs: Option[EmployeePensionContribs],
                      benefits: Option[Benefits],
                      parentalBereavement: Option[Double], // REMINDER TO SEL, THIS IS NESTED statutoryPayYTD / parentalBereavement
                      studentLoan: Option[StudentLoan],
                      postGradLoan: Option[PostGradLoan]
                    )

case class PayeResponse(paye: Option[Seq[PayeEntry]])

object PayeResponseObject {
  val studentLoanPlanTypePattern = "^(01|02)$".r
  val taxCodePattern = "^([1-9][0-9]{0,5}[LMNPTY])|(BR)|(0T)|(NT)|(D[0-8])|([K][1-9][0-9]{0,5})$".r
  val paidHoursWorkPattern = "^[^ ].{0,34}$".r
  val employerPayeRefPattern = "^[^ ].{1,9}$".r
  val paymentDatePattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  val taxYearPattern = "^[0-9]{2}\\-[0-9]{2}$".r
  val monthlyPeriodNumberPattern = "^([1-9]|1[0-2])$".r
  val weeklyPeriodNumberPattern = "^([1-9]|[1-4][0-9]|5[0-46])$".r

  val payFrequencyValues = Seq("W1", "W2", "W4", "M1", "M3", "M6", "MA", "IO", "IR")

  def isInPayFrequency(implicit rds: Reads[String]) : Reads[String] = {
    verifying(value => payFrequencyValues.contains(value))
  }
}

case class SaIncome(
                     selfAssessment: Option[Double],
                     allEmployments: Option[Double],
                     ukInterest: Option[Double],
                     foreignDivs: Option[Double],
                     ukDivsAndInterest: Option[Double],
                     partnerships: Option[Double],
                     pensions: Option[Double],
                     selfEmployment: Option[Double],
                     trusts: Option[Double],
                     ukProperty: Option[Double],
                     foreign: Option[Double],
                     lifePolicies: Option[Double],
                     shares: Option[Double],
                     other: Option[Double]
                   )

case class SaReturnType(
                         utr: Option[String],
                         caseStartDate: Option[String],
                         receivedDate: Option[String],
                         businessDescription: Option[String],
                         telephoneNumber: Option[String],
                         busStartDate: Option[String],
                         busEndDate: Option[String],
                         totalTaxPaid: Option[Double],
                         totalNIC: Option[Double],
                         turnover: Option[Double],
                         otherBusinessIncome: Option[Double],
                         tradingIncomeAllowance: Option[Double],
                         address: Option[Address],
                         income: Option[SaIncome]
                       )

case class SaTaxYearEntry(taxYear: Option[String], income: Option[Double], returnList: Option[Seq[SaReturnType]])

case class SaResponse(sa: Option[Seq[SaTaxYearEntry]])

object SaResponseObject {

  val minValue = -9999999999.99
  val maxValue = 9999999999.99

  val utrPattern = "^[0-9]{10}$".r
  val dateStringPattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  val taxYearPattern = "^20[0-9]{2}$".r

  def isMultipleOfPointZeroOne(value: Double): Boolean = (value * 100.0) % 1 == 0

  def isInRange(value: Double): Boolean = value > minValue && value < maxValue

  def paymentAmountValidator(implicit rds: Reads[Double]): Reads[Double] =
    verifying[Double](value => isInRange(value) && isMultipleOfPointZeroOne(value))
}

case class CreateIncomeRequest(sa: Option[SaResponse], paye: Option[PayeResponse])

case class IncomeResponse(sa: Option[Seq[SaTaxYearEntry]], paye: Option[PayeResponse])
