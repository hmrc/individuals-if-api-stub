/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.individualsifapistub.domain.individuals

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps, unlift}
import play.api.libs.json.Reads.{maxLength, minLength, pattern, verifying}
import play.api.libs.json.{Format, JsPath, Reads}
import uk.gov.hmrc.individualsifapistub.domain.individuals.IncomePaye.paymentAmountValidator
import uk.gov.hmrc.individualsifapistub.domain.individuals.IncomeSa.{payeWholeUnitsPaymentTypeValidator, payeWholeUnitsPositivePaymentTypeValidator}

import java.time.LocalDate

case class StudentLoan(
                        planType: Option[String],
                        repaymentsInPayPeriod: Option[Int],
                        repaymentsYTD: Option[Int]
                      )

object StudentLoan {
  private val studentLoanPlanTypePattern = "^(01|02)$".r

  implicit val format: Format[StudentLoan] = Format(
    (
      (JsPath \ "planType").readNullable[String]
        (pattern(studentLoanPlanTypePattern, "Invalid student loan plan type")) and
        (JsPath \ "repaymentsInPayPeriod").readNullable[Int](verifying(payeWholeUnitsPaymentTypeValidator)) and
        (JsPath \ "repaymentsYTD").readNullable[Int](verifying(payeWholeUnitsPositivePaymentTypeValidator))
      ) (StudentLoan.apply _),
    (
      (JsPath \ "planType").writeNullable[String] and
        (JsPath \ "repaymentsInPayPeriod").writeNullable[Int] and
        (JsPath \ "repaymentsYTD").writeNullable[Int]
      ) (unlift(StudentLoan.unapply))
  )
}

case class PostGradLoan(
                         repaymentsInPayPeriod: Option[Int],
                         repaymentsYtd: Option[Int]
                       )

object PostGradLoan {
  implicit val format: Format[PostGradLoan] = Format(
    (
      (JsPath \ "repaymentsInPayPeriod").readNullable[Int](verifying(payeWholeUnitsPaymentTypeValidator)) and
        (JsPath \ "repaymentsYTD").readNullable[Int](verifying(payeWholeUnitsPositivePaymentTypeValidator))
      ) (PostGradLoan.apply _),
    (
      (JsPath \ "repaymentsInPayPeriod").writeNullable[Int] and
        (JsPath \ "repaymentsYTD").writeNullable[Int]
      ) (unlift(PostGradLoan.unapply))
  )
}

case class Benefits(taxedViaPayroll: Option[Double], taxedViaPayrollYtd: Option[Double])

object Benefits {
  implicit val format: Format[Benefits] = Format(
    (
      (JsPath \ "taxedViaPayroll").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "taxedViaPayrollYTD").readNullable[Double](verifying(paymentAmountValidator))
      ) (Benefits.apply _),
    (
      (JsPath \ "taxedViaPayroll").writeNullable[Double] and
        (JsPath \ "taxedViaPayrollYTD").writeNullable[Double]
      ) (unlift(Benefits.unapply))
  )
}

case class EmployeePensionContribs(
                                    paidYtd: Option[Double],
                                    notPaidYtd: Option[Double],
                                    paid: Option[Double],
                                    notPaid: Option[Double]
                                  )

object EmployeePensionContribs {
  implicit val format: Format[EmployeePensionContribs] = Format(
    (
      (JsPath \ "paidYTD").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "notPaidYTD").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "paid").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "notPaid").readNullable[Double](verifying(paymentAmountValidator))
      ) (EmployeePensionContribs.apply _),
    (
      (JsPath \ "paidYTD").writeNullable[Double] and
        (JsPath \ "notPaidYTD").writeNullable[Double] and
        (JsPath \ "paid").writeNullable[Double] and
        (JsPath \ "notPaid").writeNullable[Double]
      ) (unlift(EmployeePensionContribs.unapply))
  )
}

case class GrossEarningsForNics(
                                 inPayPeriod1: Option[Double],
                                 inPayPeriod2: Option[Double],
                                 inPayPeriod3: Option[Double],
                                 inPayPeriod4: Option[Double]
                               )

object GrossEarningsForNics {
  implicit val format: Format[GrossEarningsForNics] = Format(
    (
      (JsPath \ "inPayPeriod1").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod2").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod3").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod4").readNullable[Double](verifying(paymentAmountValidator))
      ) (GrossEarningsForNics.apply _),
    (
      (JsPath \ "inPayPeriod1").writeNullable[Double] and
        (JsPath \ "inPayPeriod2").writeNullable[Double] and
        (JsPath \ "inPayPeriod3").writeNullable[Double] and
        (JsPath \ "inPayPeriod4").writeNullable[Double]
      ) (unlift(GrossEarningsForNics.unapply))
  )
}

case class AdditionalFields(
                             employeeHasPartner: Option[Boolean],
                             payrollId: Option[String]
                           )

object AdditionalFields {
  implicit val format: Format[AdditionalFields] = Format(
    (
      (JsPath \ "employee" \ "hasPartner").readNullable[Boolean] and
        (JsPath \ "payroll" \ "id").readNullable[String]
      ) (AdditionalFields.apply _),
    (
      (JsPath \ "employee" \ "hasPartner").writeNullable[Boolean] and
        (JsPath \ "payroll" \ "id").writeNullable[String]
      ) (unlift(AdditionalFields.unapply))
  )
}

case class TotalEmployerNics(
                              inPayPeriod1: Option[Double],
                              inPayPeriod2: Option[Double],
                              inPayPeriod3: Option[Double],
                              inPayPeriod4: Option[Double],
                              ytd1: Option[Double],
                              ytd2: Option[Double],
                              ytd3: Option[Double],
                              ytd4: Option[Double]
                            )

object TotalEmployerNics {
  implicit val format: Format[TotalEmployerNics] = Format(
    (
      (JsPath \ "inPayPeriod1").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod2").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod3").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod4").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd1").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd2").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd3").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd4").readNullable[Double](verifying(paymentAmountValidator))
      ) (TotalEmployerNics.apply _),
    (
      (JsPath \ "inPayPeriod1").writeNullable[Double] and
        (JsPath \ "inPayPeriod2").writeNullable[Double] and
        (JsPath \ "inPayPeriod3").writeNullable[Double] and
        (JsPath \ "inPayPeriod4").writeNullable[Double] and
        (JsPath \ "ytd1").writeNullable[Double] and
        (JsPath \ "ytd2").writeNullable[Double] and
        (JsPath \ "ytd3").writeNullable[Double] and
        (JsPath \ "ytd4").writeNullable[Double]
      ) (unlift(TotalEmployerNics.unapply))
  )
}

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

object EmployeeNics {
  implicit val format: Format[EmployeeNics] = Format(
    (
      (JsPath \ "inPayPeriod1").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod2").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod3").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "inPayPeriod4").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd1").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd2").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd3").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ytd4").readNullable[Double](verifying(paymentAmountValidator))
      ) (EmployeeNics.apply _),
    (
      (JsPath \ "inPayPeriod1").writeNullable[Double] and
        (JsPath \ "inPayPeriod2").writeNullable[Double] and
        (JsPath \ "inPayPeriod3").writeNullable[Double] and
        (JsPath \ "inPayPeriod4").writeNullable[Double] and
        (JsPath \ "ytd1").writeNullable[Double] and
        (JsPath \ "ytd2").writeNullable[Double] and
        (JsPath \ "ytd3").writeNullable[Double] and
        (JsPath \ "ytd4").writeNullable[Double]
      ) (unlift(EmployeeNics.unapply))
  )
}

case class StatutoryPayYTD(
                            maternity: Option[Double],
                            paternity: Option[Double],
                            adoption: Option[Double],
                            parentalBereavement: Option[Double]
                          )

object StatutoryPayYTD {
  implicit val format: Format[StatutoryPayYTD] = Format(
    (
      (JsPath \ "maternity").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "paternity").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "adoption").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "parentalBereavement").readNullable[Double](verifying(paymentAmountValidator))
      ) (StatutoryPayYTD.apply _),
    (
      (JsPath \ "maternity").writeNullable[Double] and
        (JsPath \ "paternity").writeNullable[Double] and
        (JsPath \ "adoption").writeNullable[Double] and
        (JsPath \ "parentalBereavement").writeNullable[Double]
      ) (unlift(StatutoryPayYTD.unapply))
  )
}

case class PayeEntry(
                      taxCode: Option[String],
                      paidHoursWorked: Option[String],
                      taxablePayToDate: Option[Double],
                      totalTaxToDate: Option[Double],
                      taxDeductedOrRefunded: Option[Double],
                      employerPayeRef: Option[String],
                      paymentDate: Option[LocalDate],
                      taxablePay: Option[Double],
                      taxYear: Option[String],
                      monthlyPeriodNumber: Option[String],
                      weeklyPeriodNumber: Option[String],
                      payFrequency: Option[String],
                      dednsFromNetPay: Option[Double],
                      employeeNics: Option[EmployeeNics],
                      employeePensionContribs: Option[EmployeePensionContribs],
                      benefits: Option[Benefits],
                      statutoryPayYTD: Option[StatutoryPayYTD],
                      studentLoan: Option[StudentLoan],
                      postGradLoan: Option[PostGradLoan],
                      grossEarningsForNics: Option[GrossEarningsForNics],
                      totalEmployerNics: Option[TotalEmployerNics],
                      additionalFields: Option[AdditionalFields]
                    )

object PayeEntry {
  private val taxCodePattern = "^([1-9][0-9]{0,5}[LMNPTY])|(BR)|(0T)|(NT)|(D[0-8])|([K][1-9][0-9]{0,5})$".r
  private val paidHoursWorkPattern = "^[^ ].{0,34}$".r
  private val employerPayeRefPattern = "^[^ ].{1,14}$".r
  private val payeTaxYearPattern = "^[0-9]{2}\\-[0-9]{2}$".r
  private val monthlyPeriodNumberPattern = "^([1-9]|1[0-2])$".r
  private val weeklyPeriodNumberPattern = "^([1-9]|[1-4][0-9]|5[0-46])$".r

  private def isInPayFrequency: Reads[String] = {
    val payFrequencyValues = Seq("W1", "W2", "W4", "M1", "M3", "M6", "MA", "IO", "IR")

    verifying(value => payFrequencyValues.contains(value))
  }

  implicit val format: Format[PayeEntry] = Format(
    (
      (JsPath \ "taxCode").readNullable[String]
        (minLength[String](2)
          .keepAnd(maxLength[String](7)
            .keepAnd(pattern(taxCodePattern, "Invalid Tax Code"))
          )
        ) and
        (JsPath \ "paidHoursWorked").readNullable[String]
          (maxLength[String](35)
            .keepAnd(pattern(paidHoursWorkPattern, "Invalid Paid Hours Work"))
          ) and
        (JsPath \ "taxablePayToDate").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "totalTaxToDate").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "taxDeductedOrRefunded").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "employerPayeRef").readNullable[String]
          (maxLength[String](14).keepAnd(pattern(employerPayeRefPattern, "Invalid employer PAYE reference"))) and
        (JsPath \ "paymentDate").readNullable[LocalDate] and
        (JsPath \ "taxablePay").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "taxYear").readNullable[String](pattern(payeTaxYearPattern, "Invalid Tax Year")) and
        (JsPath \ "monthlyPeriodNumber").readNullable[String]
          (pattern(monthlyPeriodNumberPattern, "Invalid Monthly Period Number")
            .keepAnd(minLength[String](1)).keepAnd(maxLength[String](2))
          ) and
        (JsPath \ "weeklyPeriodNumber").readNullable[String]
          (pattern(weeklyPeriodNumberPattern, "Invalid Weekly Period Number")
            .keepAnd(minLength[String](1)).keepAnd(maxLength[String](2))
          ) and
        (JsPath \ "payFrequency").readNullable[String](isInPayFrequency) and
        (JsPath \ "dednsFromNetPay").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "employeeNICs").readNullable[EmployeeNics] and
        (JsPath \ "employeePensionContribs").readNullable[EmployeePensionContribs] and
        (JsPath \ "benefits").readNullable[Benefits] and
        (JsPath \ "statutoryPayYTD").readNullable[StatutoryPayYTD] and
        (JsPath \ "studentLoan").readNullable[StudentLoan] and
        (JsPath \ "postGradLoan").readNullable[PostGradLoan] and
        (JsPath \ "grossEarningsForNICs").readNullable[GrossEarningsForNics] and
        (JsPath \ "totalEmployerNICs").readNullable[TotalEmployerNics] and
        JsPath.readNullable[AdditionalFields]
      ) (PayeEntry.apply _),
    (
      (JsPath \ "taxCode").writeNullable[String] and
        (JsPath \ "paidHoursWorked").writeNullable[String] and
        (JsPath \ "taxablePayToDate").writeNullable[Double] and
        (JsPath \ "totalTaxToDate").writeNullable[Double] and
        (JsPath \ "taxDeductedOrRefunded").writeNullable[Double] and
        (JsPath \ "employerPayeRef").writeNullable[String] and
        (JsPath \ "paymentDate").writeNullable[LocalDate] and
        (JsPath \ "taxablePay").writeNullable[Double] and
        (JsPath \ "taxYear").writeNullable[String] and
        (JsPath \ "monthlyPeriodNumber").writeNullable[String] and
        (JsPath \ "weeklyPeriodNumber").writeNullable[String] and
        (JsPath \ "payFrequency").writeNullable[String] and
        (JsPath \ "dednsFromNetPay").writeNullable[Double] and
        (JsPath \ "employeeNICs").writeNullable[EmployeeNics] and
        (JsPath \ "employeePensionContribs").writeNullable[EmployeePensionContribs] and
        (JsPath \ "benefits").writeNullable[Benefits] and
        (JsPath \ "statutoryPayYTD").writeNullable[StatutoryPayYTD] and
        (JsPath \ "studentLoan").writeNullable[StudentLoan] and
        (JsPath \ "postGradLoan").writeNullable[PostGradLoan] and
        (JsPath \ "grossEarningsForNICs").writeNullable[GrossEarningsForNics] and
        (JsPath \ "totalEmployerNICs").writeNullable[TotalEmployerNics] and
        JsPath.writeNullable[AdditionalFields]
      ) (unlift(PayeEntry.unapply))
  )
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

object SaIncome {
  implicit val format: Format[SaIncome] = Format(
    (
      (JsPath \ "selfAssessment").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "allEmployments").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ukInterest").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "foreignDivs").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ukDivsAndInterest").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "partnerships").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "pensions").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "selfEmployment").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "trusts").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ukProperty").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "foreign").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "lifePolicies").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "shares").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "other").readNullable[Double](verifying(paymentAmountValidator))
      ) (SaIncome.apply _),
    (
      (JsPath \ "selfAssessment").writeNullable[Double] and
        (JsPath \ "allEmployments").writeNullable[Double] and
        (JsPath \ "ukInterest").writeNullable[Double] and
        (JsPath \ "foreignDivs").writeNullable[Double] and
        (JsPath \ "ukDivsAndInterest").writeNullable[Double] and
        (JsPath \ "partnerships").writeNullable[Double] and
        (JsPath \ "pensions").writeNullable[Double] and
        (JsPath \ "selfEmployment").writeNullable[Double] and
        (JsPath \ "trusts").writeNullable[Double] and
        (JsPath \ "ukProperty").writeNullable[Double] and
        (JsPath \ "foreign").writeNullable[Double] and
        (JsPath \ "lifePolicies").writeNullable[Double] and
        (JsPath \ "shares").writeNullable[Double] and
        (JsPath \ "other").writeNullable[Double]
      ) (unlift(SaIncome.unapply))
  )
}

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
                         income: Option[SaIncome],
                         deducts: Option[Deducts]
                       )

object SaReturnType {
  private val utrPattern = "^[0-9]{10}$".r
  private val dateStringPattern = ("^(((19|20)([2468][048]|[13579][26]|0[48])|2000)" +
    "[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
    "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-]" +
    "(0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-]" +
    "(0[1-9]|1[0-9]|2[0-8])))$").r

  implicit val format: Format[SaReturnType] = Format(
    (
      (JsPath \ "utr").readNullable[String](pattern(utrPattern, "Invalid UTR")) and
        (JsPath \ "caseStartDate").readNullable[String]
          (pattern(dateStringPattern, "Invalid Case Start Date")) and
        (JsPath \ "receivedDate").readNullable[String]
          (pattern(dateStringPattern, "Invalid Received Date")) and
        (JsPath \ "businessDescription").readNullable[String]
          (minLength[String](0).keepAnd(maxLength[String](100))) and
        (JsPath \ "telephoneNumber").readNullable[String]
          (minLength[String](0).keepAnd(maxLength[String](100))) and
        (JsPath \ "busStartDate").readNullable[String]
          (pattern(dateStringPattern, "Invalid Business Start Date")) and
        (JsPath \ "busEndDate").readNullable[String]
          (pattern(dateStringPattern, "Invalid Business End Date")) and
        (JsPath \ "totalTaxPaid").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "totalNIC").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "turnover").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "otherBusIncome").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "tradingIncomeAllowance").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "address").readNullable[Address] and
        (JsPath \ "income").readNullable[SaIncome] and
        (JsPath \ "deducts").readNullable[Deducts]
      ) (SaReturnType.apply _),
    (
      (JsPath \ "utr").writeNullable[String] and
        (JsPath \ "caseStartDate").writeNullable[String] and
        (JsPath \ "receivedDate").writeNullable[String] and
        (JsPath \ "businessDescription").writeNullable[String] and
        (JsPath \ "telephoneNumber").writeNullable[String] and
        (JsPath \ "busStartDate").writeNullable[String] and
        (JsPath \ "busEndDate").writeNullable[String] and
        (JsPath \ "totalTaxPaid").writeNullable[Double] and
        (JsPath \ "totalNIC").writeNullable[Double] and
        (JsPath \ "turnover").writeNullable[Double] and
        (JsPath \ "otherBusIncome").writeNullable[Double] and
        (JsPath \ "tradingIncomeAllowance").writeNullable[Double] and
        (JsPath \ "address").writeNullable[Address] and
        (JsPath \ "income").writeNullable[SaIncome] and
        (JsPath \ "deducts").writeNullable[Deducts]
      ) (unlift(SaReturnType.unapply))
  )
}

case class Deducts(
                    totalBusExpenses: Option[Double],
                    totalDisallowBusExp: Option[Double]
                  )

object Deducts {
  implicit val format: Format[Deducts] = Format(
    (
      (JsPath \ "totalBusExpenses").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "totalDisallowBusExp").readNullable[Double](verifying(paymentAmountValidator))
      ) (Deducts.apply _),
    (
      (JsPath \ "totalBusExpenses").writeNullable[Double] and
        (JsPath \ "totalDisallowBusExp").writeNullable[Double]
      ) (unlift(Deducts.unapply))
  )
}

case class SaTaxYearEntry(taxYear: Option[String], income: Option[Double], returnList: Option[Seq[SaReturnType]])

object SaTaxYearEntry {
  private val taxYearPattern = "^20[0-9]{2}$".r

  implicit val format: Format[SaTaxYearEntry] = Format(
    (
      (JsPath \ "taxYear").readNullable[String](pattern(taxYearPattern, "Invalid Tax Year")) and
        (JsPath \ "income").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "returnList").readNullable[Seq[SaReturnType]]
      ) (SaTaxYearEntry.apply _),
    (
      (JsPath \ "taxYear").writeNullable[String] and
        (JsPath \ "income").writeNullable[Double] and
        (JsPath \ "returnList").writeNullable[Seq[SaReturnType]]
      ) (unlift(SaTaxYearEntry.unapply))
  )
}

case class IncomeSaEntry(id: String, incomeSa: IncomeSa, idValue: Option[String])

object IncomeSaEntry {
  implicit val format: Format[IncomeSaEntry] = Format(
    (
      (JsPath \ "id").read[String] and
        (JsPath \ "incomeSaResponse").read[IncomeSa] and
        (JsPath \ "idValue").readNullable[String]
      ) (IncomeSaEntry.apply _),
    (
      (JsPath \ "id").write[String] and
        (JsPath \ "incomeSaResponse").write[IncomeSa] and
        (JsPath \ "idValue").writeNullable[String]
      ) (unlift(IncomeSaEntry.unapply))
  )
}

case class IncomePayeEntry(id: String, incomePaye: IncomePaye, idValue: String)

object IncomePayeEntry {
  implicit val format: Format[IncomePayeEntry] = Format(
    (
      (JsPath \ "id").read[String] and
        (JsPath \ "incomePaye").read[IncomePaye] and
        (JsPath \ "idValue").read[String]
      ) (IncomePayeEntry.apply _),
    (
      (JsPath \ "id").write[String] and
        (JsPath \ "incomePaye").write[IncomePaye] and
        (JsPath \ "idValue").write[String]
      ) (unlift(IncomePayeEntry.unapply))
  )
}

case class IncomeSa(sa: Option[Seq[SaTaxYearEntry]])

object IncomeSa {
  private val payeWholeUnitsPaymentTypeMinValue = -99999
  private val payeWholeUnitsPaymentTypeMaxValue = 99999
  private val payeWholeUnitsPositivePaymentTypeMinValue = 0
  private val payeWholeUnitsPositivePaymentTypeMaxValue = 99999

  private def isInRangeWholeUnits(value: Double) =
    value >= payeWholeUnitsPaymentTypeMinValue && value <= payeWholeUnitsPaymentTypeMaxValue

  private def isInRangePositiveWholeUnits(value: Double) =
    value >= payeWholeUnitsPositivePaymentTypeMinValue && value <= payeWholeUnitsPositivePaymentTypeMaxValue

  def payeWholeUnitsPaymentTypeValidator(value: Int): Boolean = isInRangeWholeUnits(value)

  def payeWholeUnitsPositivePaymentTypeValidator(value: Int): Boolean = isInRangePositiveWholeUnits(value)

  implicit val format: Format[IncomeSa] = Format(
    (JsPath \ "sa").readNullable[Seq[SaTaxYearEntry]].map(value => IncomeSa(value)),
    (JsPath \ "sa").writeNullable[Seq[SaTaxYearEntry]].contramap(value => value.sa)
  )
}

case class IncomePaye(paye: Option[Seq[PayeEntry]])

object IncomePaye {
  private val minValue = -999999999.99
  private val maxValue = 999999999.99

  private def isMultipleOfPointZeroOne(value: Double) = (BigDecimal(value) * 100.0) % 1 == 0

  private def isInRange(value: Double) = value >= minValue && value <= maxValue

  def paymentAmountValidator(value: Double): Boolean =
    isInRange(value) && isMultipleOfPointZeroOne(value)

  implicit val format: Format[IncomePaye] = Format(
    (JsPath \ "paye").readNullable[Seq[PayeEntry]].map(value => IncomePaye(value)),
    (JsPath \ "paye").writeNullable[Seq[PayeEntry]].contramap(value => value.paye)
  )
}
