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

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{max, maxLength, min, minLength, pattern, verifying}
import play.api.libs.json.{Format, JsPath, Json}

import java.time.LocalDate

case class Employer(
  name: Option[String],
  address: Option[Address],
  districtNumber: Option[String],
  schemeRef: Option[String]
)

object Employer {
  implicit val format: Format[Employer] = Format(
    (
      (JsPath \ "name").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "address").readNullable[Address] and
        (JsPath \ "districtNumber").readNullable[String](using minLength[String](0) keepAnd maxLength[String](3)) and
        (JsPath \ "schemeRef").readNullable[String](using minLength[String](0) keepAnd maxLength[String](10))
    )(Employer.apply),
    Json.writes[Employer]
  )
}

case class EmploymentDetail(
  startDate: Option[String],
  endDate: Option[String],
  payFrequency: Option[String],
  payrollId: Option[String],
  address: Option[Address]
)

object EmploymentDetail {
  private def datePattern =
    ("^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
      "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-]" +
      "(0[1-9]|1[0-9]|2[0-8])))$").r

  private val payFrequencyPattern =
    "^(W1|W2|W4|M1|M3|M6|MA|IO|IR)$".r

  implicit val format: Format[EmploymentDetail] = Format(
    (
      (JsPath \ "startDate").readNullable[String](using pattern(datePattern, "Date format is incorrect")) and
        (JsPath \ "endDate").readNullable[String](using pattern(datePattern, "Date format is incorrect")) and
        (JsPath \ "payFrequency").readNullable[String](using
          pattern(payFrequencyPattern, "Pay frequency must be one of: W1, W2, W4, M1, M3, M6, MA, IO, IR")
        ) and
        (JsPath \ "payrollId").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "address").readNullable[Address]
    )(EmploymentDetail.apply),
    Json.writes[EmploymentDetail]
  )
}

case class Payment(
  date: Option[LocalDate],
  ytdTaxablePay: Option[Double],
  paidTaxablePay: Option[Double],
  paidNonTaxOrNICPayment: Option[Double],
  week: Option[Int],
  month: Option[Int]
)

object Payment {
  private val minValue = -9999999999.99
  private val maxValue = 9999999999.99

  private def isMultipleOfPointZeroOne(value: Double) = (BigDecimal(value) * 100.0) % 1 == 0

  private def isInRange(value: Double) = value > minValue && value < maxValue

  private def paymentAmountValidator =
    verifying[Double](value => isInRange(value) && isMultipleOfPointZeroOne(value))

  implicit val format: Format[Payment] = Format(
    (
      (JsPath \ "date").readNullable[LocalDate] and
        (JsPath \ "ytdTaxablePay").readNullable[Double](using paymentAmountValidator) and
        (JsPath \ "paidTaxablePay").readNullable[Double](using paymentAmountValidator) and
        (JsPath \ "paidNonTaxOrNICPayment").readNullable[Double](using paymentAmountValidator) and
        (JsPath \ "week").readNullable[Int](using min(1) keepAnd max(56)) and
        (JsPath \ "month").readNullable[Int](using min(1) keepAnd max(12))
    )(Payment.apply),
    Json.writes[Payment]
  )
}

case class Employment(
  employer: Option[Employer],
  employerRef: Option[String],
  employment: Option[EmploymentDetail],
  payments: Option[Seq[Payment]]
)

object Employment {
  implicit val format: Format[Employment] = Format(
    (
      (JsPath \ "employer").readNullable[Employer] and
        (JsPath \ "employerRef").readNullable[String](using minLength[String](1) keepAnd maxLength[String](14)) and
        (JsPath \ "employment").readNullable[EmploymentDetail] and
        (JsPath \ "payments").readNullable[Seq[Payment]]
    )(Employment.apply),
    Json.writes[Employment]
  )
}

case class EmploymentEntry(id: String, employments: Seq[Employment], idValue: Option[String])

object EmploymentEntry {
  val format: Format[EmploymentEntry] = Json.format
}

case class Employments(employments: Seq[Employment])

object Employments {
  implicit val format: Format[Employments] = Json.format
}
