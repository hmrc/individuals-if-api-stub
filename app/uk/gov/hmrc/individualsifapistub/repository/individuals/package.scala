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

package uk.gov.hmrc.individualsifapistub.repository

import org.joda.time.LocalDate
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats
import play.api.libs.json._
import play.api.libs.functional.syntax._

package object individuals {

  import MongoJodaFormats.Implicits._

  implicit val employeeNicsFormat = Json.format[EmployeeNics]
  implicit val employeePensionContribsFormat = Json.format[EmployeePensionContribs]
  implicit val benefitsFormat = Json.format[Benefits]
  implicit val statutoryPayYTDFormat = Json.format[StatutoryPayYTD]
  implicit val studentLoan = Json.format[StudentLoan]
  implicit val postGradLoan = Json.format[PostGradLoan]
  implicit val grossEarningsForNics = Json.format[GrossEarningsForNics]
  implicit val totalEmployerNics = Json.format[TotalEmployerNics]
  implicit val additionalFields = Json.format[AdditionalFields]
  implicit val payeEntryFormat: Format[PayeEntry] = Format(
    (
      (JsPath \ "taxCode").readNullable[String] and
        (JsPath \ "paidHoursWorked").readNullable[String] and
        (JsPath \ "taxablePayToDate").readNullable[Double] and
        (JsPath \ "totalTaxToDate").readNullable[Double] and
        (JsPath \ "taxDeductedOrRefunded").readNullable[Double] and
        (JsPath \ "employerPayeRef").readNullable[String] and
        (JsPath \ "paymentDate").readNullable[LocalDate] and
        (JsPath \ "taxablePay").readNullable[Double] and
        (JsPath \ "taxYear").readNullable[String] and
        (JsPath \ "monthlyPeriodNumber").readNullable[String] and
        (JsPath \ "weeklyPeriodNumber").readNullable[String] and
        (JsPath \ "payFrequency").readNullable[String] and
        (JsPath \ "dednsFromNetPay").readNullable[Double] and
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

  implicit val incomePayeFormat: Format[IncomePaye] = Format(
    (JsPath \ "paye").readNullable[Seq[PayeEntry]].map(value => IncomePaye(value)),
    (JsPath \ "paye").writeNullable[Seq[PayeEntry]].contramap(value => value.paye)
  )

  implicit val incomePayeEntryFormat: Format[IncomePayeEntry] = Format(
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

  // SA
  implicit val addressFormat: Format[Address] = Json.format
  implicit val incomeFormat: Format[SaIncome] = Json.format
  implicit val deductsFormat: Format[Deducts] = Json.format

  implicit val saReturnTypeFormat: Format[SaReturnType] = Json.format

  implicit val saTaxYearEntryFormat: Format[SaTaxYearEntry] = Format(
    (
      ((JsPath \ "taxYear").readNullable[Int].map(_.map(_.toString)) or (JsPath \ "taxYear").readNullable[String]) and
        (JsPath \ "income").readNullable[Double] and
        (JsPath \ "returnList").readNullable[Seq[SaReturnType]]
      ) (SaTaxYearEntry.apply _),
    (
      (JsPath \ "taxYear").writeNullable[Int].contramap[Option[String]](_.map(_.toInt)) and
        (JsPath \ "income").writeNullable[Double] and
        (JsPath \ "returnList").writeNullable[Seq[SaReturnType]]
      ) (unlift(SaTaxYearEntry.unapply))

  )

  implicit val incomeSaFormat: Format[IncomeSa] = Format(
    (JsPath \ "sa").readNullable[Seq[SaTaxYearEntry]].map(IncomeSa.apply),
    (JsPath \ "sa").writeNullable[Seq[SaTaxYearEntry]].contramap(_.sa)
  )

  implicit val incomeSaEntryFormat: Format[IncomeSaEntry] = Format(
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
