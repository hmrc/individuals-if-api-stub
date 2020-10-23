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

import play.api.libs.functional.syntax.unlift
import uk.gov.hmrc.individualsifapistub.domain.DetailsResponse.{ninoPattern, trnPattern, addressFormat}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.util.matching.Regex

case class Id(nino: Option[String], trn: Option[String])

case class Employer(name: Option[String], address: Option[Address], districtNumber: Option[String], schemeRef: Option[String])

case class EmploymentDetail(startDate: Option[String], endDate: Option[String], payFrequency: Option[String], payrollId:Option[String], address: Option[Address])

case class Payment(date: Option[String],
                   ytdTaxablePay: Option[Double],
                   paidTaxablePay: Option[Double],
                   paidNonTaxOrNICPayment: Option[Double],
                   week: Option[Int],
                   month: Option[Int])

case class Employment(employer: Option[Employer], employment: Option[EmploymentDetail], payments: Option[Seq[Payment]])

case class EmploymentsResponse(employments: Seq[Employment])

case class CreateEmploymentRequest(id: Id, employment: EmploymentsResponse)

object EmploymentsResponse {

  val dateFormat:Regex = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  val payFrequency:Regex = "^(W1|W2|W4|M1|M3|M6|MA|IO|IR)$".r
  val ninoPattern: Regex = "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D\\s]?$".r
  val trnPattern: Regex = "^[0-9]{8}$".r

  val minValue = -9999999999.99
  val maxValue = 9999999999.99

  def isMultipleOfPointZeroOne(value: Double): Boolean = (value * 100.0) % 1 == 0

  def isInRange(value: Double): Boolean = value > minValue && value < maxValue

  def paymentAmountValidator(implicit rds: Reads[Double]):Reads[Double] =
    verifying[Double](value => isInRange(value) && isMultipleOfPointZeroOne(value))

  implicit val employerFormat: Format[Employer] = Format(
    (
      (JsPath \ "name").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
      (JsPath \ "address").readNullable[Address] and
      (JsPath \ "districtNumber").readNullable[String](minLength[String](0) keepAnd maxLength[String](3)) and
      (JsPath \ "schemeRef").readNullable[String](minLength[String](0) keepAnd maxLength[String](10))
    )(Employer.apply _),
    (
      (JsPath \ "name").writeNullable[String] and
      (JsPath \ "address").writeNullable[Address] and
      (JsPath \ "districtNumber").writeNullable[String] and
      (JsPath \ "schemeRef").writeNullable[String]
    )(unlift(Employer.unapply))
  )

  implicit val employmentDetailFormat: Format[EmploymentDetail] = Format(
    (
      (JsPath \ "startDate").readNullable[String](pattern(dateFormat, "Date format is incorrect")) and
      (JsPath \ "endDate").readNullable[String](pattern(dateFormat, "Date format is incorrect")) and
      (JsPath \ "payFrequency").readNullable[String](pattern(payFrequency, "Pay frequency must be one of: W1, W2, W4, M1, M3, M6, MA, IO, IR")) and
      (JsPath \ "payrollId").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
      (JsPath \ "address").readNullable[Address]
    )(EmploymentDetail.apply _),
    (
      (JsPath \ "startDate").writeNullable[String] and
        (JsPath \ "endDate").writeNullable[String] and
        (JsPath \ "payFrequency").writeNullable[String] and
        (JsPath \ "payrollId").writeNullable[String] and
        (JsPath \ "address").writeNullable[Address]
      )(unlift(EmploymentDetail.unapply))
  )

  implicit val paymentFormat: Format[Payment] = Format(
    (
      (JsPath \ "date").readNullable[String](pattern(dateFormat, "Date format is incorrect")) and
      (JsPath \ "ytdTaxablePay").readNullable[Double](paymentAmountValidator) and
      (JsPath \ "paidTaxablePay").readNullable[Double](paymentAmountValidator) and
      (JsPath \ "paidNonTaxOrNICPayment").readNullable[Double](paymentAmountValidator) and
      (JsPath \ "week").readNullable[Int](min(1) keepAnd max(56)) and
      (JsPath \ "month").readNullable[Int](min(1) keepAnd max(12))
    )(Payment.apply _),
    (
      (JsPath \ "date").writeNullable[String] and
      (JsPath \ "ytdTaxablePay").writeNullable[Double] and
      (JsPath \ "paidTaxablePay").writeNullable[Double] and
      (JsPath \ "paidNonTaxOrNICPayment").writeNullable[Double] and
      (JsPath \ "week").writeNullable[Int] and
      (JsPath \ "month").writeNullable[Int]
    )(unlift(Payment.unapply))
  )

  implicit val employmentFormat: Format[Employment] = Format(
    (
      (JsPath \ "employer").readNullable[Employer] and
      (JsPath \ "employment").readNullable[EmploymentDetail] and
      (JsPath \ "payments").readNullable[Seq[Payment]]
    )(Employment.apply _),
    (
      (JsPath \ "employer").writeNullable[Employer] and
      (JsPath \ "employment").writeNullable[EmploymentDetail] and
      (JsPath \ "payments").writeNullable[Seq[Payment]]
    )(unlift(Employment.unapply))
  )

  implicit val idFormat: Format[Id] = Format(
    (
      (JsPath \ "nino").readNullable[String](pattern(ninoPattern, "InvalidNino")) and
      (JsPath \ "trn").readNullable[String](pattern(trnPattern, "InvalidTrn"))
    )(Id.apply _),
    (
      (JsPath \ "nino").writeNullable[String] and
      (JsPath \ "trn").writeNullable[String]
    )(unlift(Id.unapply))
  )

  implicit val employmentResponseFormat: Format[EmploymentsResponse] = Format(
    fjs = (JsPath \ "employments").read[Seq[Employment]](EmploymentsResponse.apply _),
    tjs = (JsPath \ "employments").write[Seq[Employment]](unlift(EmploymentsResponse.unapply))
  )

  implicit val createEmploymentRequestFormat: Format[CreateEmploymentRequest] = Format(
    (
      (JsPath \ "id").read[Id] and
      (JsPath \ "employment").read[EmploymentsResponse]
    )(CreateEmploymentRequest.apply _),
    (
      (JsPath \ "id").write[Id] and
      (JsPath \ "employment").write[EmploymentsResponse]
    )(unlift(CreateEmploymentRequest.unapply))
  )
}



