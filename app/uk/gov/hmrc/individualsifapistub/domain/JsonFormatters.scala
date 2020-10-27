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

import org.joda.time.DateTime
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.JodaWrites._
import play.api.libs.json.Reads.{maxLength, minLength, pattern}
import play.api.libs.json._
import uk.gov.hmrc.individualsifapistub.domain.SaResponseObject.{dateStringPattern, paymentAmountValidator, taxYearPattern, utrPattern}
import play.api.libs.functional.syntax._
import uk.gov.hmrc.individualsifapistub.domain.PayeResponseObject.{employerPayeRefPattern, isInPayFrequency, monthlyPeriodNumberPattern, paidHoursWorkPattern, payFrequencyValues, paymentDatePattern, studentLoanPlanTypePattern, taxCodePattern, weeklyPeriodNumberPattern}

import scala.util.{Failure, Try}

object JsonFormatters {
  implicit val dateFormatDefault = new Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = JodaReads.DefaultJodaDateTimeReads.reads(json)

    override def writes(o: DateTime): JsValue = JodaDateTimeNumberWrites.writes(o)
  }

  implicit val addressFormat: Format[Address] = Format(
    (
      (JsPath \ "line1").readNullable[String](minLength[String](0).andKeep(maxLength[String](100))) and
        (JsPath \ "line2").readNullable[String](minLength[String](0).andKeep(maxLength[String](100))) and
        (JsPath \ "line3").readNullable[String](minLength[String](0).andKeep(maxLength[String](100))) and
        (JsPath \ "line4").readNullable[String](minLength[String](0).andKeep(maxLength[String](100))) and
        (JsPath \ "postcode").readNullable[String](minLength[String](1).andKeep(maxLength[String](10)))
      ) (Address.apply _),
    (
      (JsPath \ "line1").writeNullable[String] and
        (JsPath \ "line2").writeNullable[String] and
        (JsPath \ "line3").writeNullable[String] and
        (JsPath \ "line4").writeNullable[String] and
        (JsPath \ "postcode").writeNullable[String]
      ) (unlift(Address.unapply))
  )

  implicit val saIncomeFormat: Format[SaIncome] = Format(
    (
      (JsPath \ "selfAssessment").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "allEmployments").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "ukInterest").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "foreignDivs").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "ukDivsAndInterest").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "partnerships").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "pensions").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "selfEmployment").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "trusts").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "ukProperty").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "foreign").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "lifePolicies").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "shares").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "other").readNullable[Double](paymentAmountValidator)
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

  implicit val saReturnTypeFormat: Format[SaReturnType] = Format(
    (
      (JsPath \ "utr").readNullable[String](pattern(utrPattern, "Invalid UTR")) and
        (JsPath \ "caseStartDate").readNullable[String](pattern(dateStringPattern, "Invalid Case Start Date")) and
        (JsPath \ "receivedDate").readNullable[String](pattern(dateStringPattern, "Invalid Received Date")) and
        (JsPath \ "businessDescription").readNullable[String](minLength[String](0).keepAnd(maxLength[String](100))) and
        (JsPath \ "telephoneNumber").readNullable[String](minLength[String](0).keepAnd(maxLength[String](100))) and
        (JsPath \ "busStartDate").readNullable[String](pattern(dateStringPattern, "Invalid Business Start Date")) and
        (JsPath \ "busEndDate").readNullable[String](pattern(dateStringPattern, "Invalid Business End Date")) and
        (JsPath \ "totalTaxPaid").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "totalNIC").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "turnover").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "otherBusIncome").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "tradingIncomeAllowance").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "address").readNullable[Address] and
        (JsPath \ "income").readNullable[SaIncome]
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
        (JsPath \ "income").writeNullable[SaIncome]
      ) (unlift(SaReturnType.unapply))
  )

  implicit val saTaxYearEntryFormat: Format[SaTaxYearEntry] = Format(
    (
      (JsPath \ "taxYear").readNullable[String](pattern(taxYearPattern, "Invalid Tax Year")) and
        (JsPath \ "income").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "returnList").readNullable[Seq[SaReturnType]]
      ) (SaTaxYearEntry.apply _),
    (
      (JsPath \ "taxYear").writeNullable[String] and
        (JsPath \ "income").writeNullable[Double] and
        (JsPath \ "returnList").writeNullable[Seq[SaReturnType]]
      ) (unlift(SaTaxYearEntry.unapply))
  )

  implicit val postGradLoanFormat: Format[PostGradLoan] = Format(
    (
      (JsPath \ "repaymentsInPayPeriod").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "repaymentsYTD").readNullable[Double](paymentAmountValidator)
      ) (PostGradLoan.apply _),
    (
      (JsPath \ "repaymentsInPayPeriod").writeNullable[Double] and
        (JsPath \ "repaymentsYTD").writeNullable[Double]
      ) (unlift(PostGradLoan.unapply))
  )

  implicit val studentLoanFormat: Format[StudentLoan] = Format(
    (
      (JsPath \ "planType").readNullable[String](pattern(studentLoanPlanTypePattern, "Invalid student loan plan type")) and
        (JsPath \ "repaymentsInPayPeriod").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "repaymentsYTD").readNullable[Double](paymentAmountValidator)
      ) (StudentLoan.apply _),
    (
      (JsPath \ "planType").writeNullable[String] and
        (JsPath \ "repaymentsInPayPeriod").writeNullable[Double] and
        (JsPath \ "repaymentsYTD").writeNullable[Double]
      ) (unlift(StudentLoan.unapply))
  )

  implicit val benefitsFormat: Format[Benefits] = Format(
    (
      (JsPath \ "taxedViaPayroll").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "taxedViaPayrollYTD").readNullable[Double](paymentAmountValidator)
      ) (Benefits.apply _),
    (
      (JsPath \ "taxedViaPayroll").writeNullable[Double] and
        (JsPath \ "taxedViaPayrollYTD").writeNullable[Double]
      ) (unlift(Benefits.unapply))
  )

  implicit val employeePensionContribsFormat: Format[EmployeePensionContribs] = Format(
    (
      (JsPath \ "paidYTD").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "notPaidYTD").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "paid").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "notPaid").readNullable[Double](paymentAmountValidator)
      ) (EmployeePensionContribs.apply _),
    (
      (JsPath \ "paidYTD").writeNullable[Double] and
        (JsPath \ "notPaidYTD").writeNullable[Double] and
        (JsPath \ "paid").writeNullable[Double] and
        (JsPath \ "notPaid").writeNullable[Double]
      ) (unlift(EmployeePensionContribs.unapply))
  )

  implicit val grossEarningsForNicsFormat: Format[GrossEarningsForNics] = Format(
    (
      (JsPath \ "inPayPeriod1").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "inPayPeriod2").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "inPayPeriod3").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "inPayPeriod4").readNullable[Double](paymentAmountValidator)
      ) (GrossEarningsForNics.apply _),
    (
      (JsPath \ "inPayPeriod1").writeNullable[Double] and
        (JsPath \ "inPayPeriod2").writeNullable[Double] and
        (JsPath \ "inPayPeriod3").writeNullable[Double] and
        (JsPath \ "inPayPeriod4").writeNullable[Double]
      ) (unlift(GrossEarningsForNics.unapply))
  )

  implicit val employeeNicsFormat: Format[EmployeeNics] = Format(
    (
      (JsPath \ "inPayPeriod1").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "inPayPeriod2").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "inPayPeriod3").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "inPayPeriod4").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "ytd1").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "ytd2").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "ytd3").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "ytd4").readNullable[Double](paymentAmountValidator)
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

  implicit val payeEntryFormat: Format[PayeEntry] = Format(
    (
      (JsPath \ "taxCode").readNullable[String](minLength[String](2).keepAnd(maxLength[String](7).keepAnd(pattern(taxCodePattern,"Invalid Tax Code")))) and
        (JsPath \ "paidHoursWork").readNullable[String](maxLength[String](35).keepAnd(pattern(paidHoursWorkPattern,"Invalid Paid Hours Work"))) and
        (JsPath \ "taxablePayToDate").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "totalTaxToDate").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "taxDeductedOrRefunded").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "employerPayeRef").readNullable[String](maxLength[String](10).keepAnd(pattern(employerPayeRefPattern,"Invalid employer PAYE reference")))  and
        (JsPath \ "paymentDate").readNullable[String](pattern(paymentDatePattern, "Invalid Payment Date")) and
        (JsPath \ "taxablePay").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "taxYear").readNullable[String](pattern(taxYearPattern, "Invalid Tax Year")) and
        (JsPath \ "monthlyPeriodNumber").readNullable[String](pattern(monthlyPeriodNumberPattern,"Invalid Monthly Period Number").keepAnd(minLength[String](1)).keepAnd(maxLength[String](2))) and
        (JsPath \ "weeklyPeriodNumber").readNullable[String](pattern(weeklyPeriodNumberPattern,"Invalid Weekly Period Number").keepAnd(minLength[String](1)).keepAnd(maxLength[String](2))) and
        (JsPath \ "payFrequency").readNullable[String](isInPayFrequency) and
        (JsPath \ "dednsFromNetPay").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "employeeNics").readNullable[EmployeeNics] and
        (JsPath \ "employeePensionContribs").readNullable[EmployeePensionContribs] and
        (JsPath \ "benefits").readNullable[Benefits] and
        (JsPath \ "statutoryPayYTD" \ "parentalBereavement").readNullable[Double](paymentAmountValidator) and
        (JsPath \ "studentLoan").readNullable[StudentLoan] and
        (JsPath \ "postGradLoan").readNullable[PostGradLoan]
      )(PayeEntry.apply _),
    (
      (JsPath \ "taxCode").writeNullable[String] and
        (JsPath \ "paidHoursWork").writeNullable[String] and
        (JsPath \ "taxablePayToDate").writeNullable[Double] and
        (JsPath \ "totalTaxToDate").writeNullable[Double] and
        (JsPath \ "taxDeductedOrRefunded").writeNullable[Double] and
        (JsPath \ "employerPayeRef").writeNullable[String] and
        (JsPath \ "paymentDate").writeNullable[String] and
        (JsPath \ "taxablePay").writeNullable[Double] and
        (JsPath \ "taxYear").writeNullable[String] and
        (JsPath \ "monthlyPeriodNumber").writeNullable[String] and
        (JsPath \ "weeklyPeriodNumber").writeNullable[String] and
        (JsPath \ "payFrequency").writeNullable[String] and
        (JsPath \ "dednsFromNetPay").writeNullable[Double] and
        (JsPath \ "employeeNics").writeNullable[EmployeeNics] and
        (JsPath \ "employeePensionContribs").writeNullable[EmployeePensionContribs] and
        (JsPath \ "benefits").writeNullable[Benefits] and
        (JsPath \ "statutoryPayYTD" \ "parentalBereavement").writeNullable[Double] and
        (JsPath \ "studentLoan").writeNullable[StudentLoan] and
        (JsPath \ "postGradLoan").writeNullable[PostGradLoan]
      )(unlift(PayeEntry.unapply))
  )

  implicit val saResponseFormat = Json.format[SaResponse]

  implicit val payeResponseFormat = Json.format[PayeResponse]

  implicit val createEmploymentRequestFormat = Json.format[CreateEmploymentRequest]
  implicit val createDetailsRequestFormat = Json.format[CreateDetailsRequest]
  implicit val createBenefitsAndCreditsRequestFormat = Json.format[CreateBenefitsAndCreditsRequest]

  implicit val employmentFormat = Json.format[Employment]
  implicit val detailsFormat = Json.format[Details]
  implicit val benefitsAndCreditsFormat = Json.format[BenefitsAndCredits]

  implicit val testAddressFormat = Json.format[TestAddress]
  implicit val testIndividualFormat = Json.format[TestIndividual]
  implicit val testOrganisationDetailsFormat = Json.format[TestOrganisationDetails]
  implicit val testOrganisationFormat = Json.format[TestOrganisation]

  implicit val incomeFormat = Json.format[Income]
  implicit val createIncomeRequestFormat = Json.format[CreateIncomeRequest]

  implicit val errorInvalidRequestFormat = new Format[ErrorInvalidRequest] {
    def reads(json: JsValue): JsResult[ErrorInvalidRequest] = JsSuccess(
      ErrorInvalidRequest((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidRequest): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}

object EnumJson {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        Try(JsSuccess(enum.withName(s))) recoverWith {
          case _: NoSuchElementException => Failure(new InvalidEnumException(enum.getClass.getSimpleName, s))
        } get
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}

class InvalidEnumException(className: String, input: String) extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")
