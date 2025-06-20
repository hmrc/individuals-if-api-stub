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
import play.api.libs.json.Reads.{max, min, pattern, verifying}
import play.api.libs.json.{Format, JsPath, Json}
import uk.gov.hmrc.individualsifapistub.domain.individuals.TaxCredits._

import scala.util.matching.Regex

case class TaxCreditsEntry(id: String, applications: Seq[Application])

object TaxCreditsEntry {
  implicit val format: Format[TaxCreditsEntry] = Json.format
}

case class Applications(applications: Seq[Application])

object Applications {
  implicit val format: Format[Applications] = Format(
    (JsPath \ "applications").read[Seq[Application]].map(x => Applications(x)),
    (JsPath \ "applications").write[Seq[Application]].contramap(x => x.applications)
  )
}

case class Payments(
  periodStartDate: Option[String],
  periodEndDate: Option[String],
  startDate: Option[String],
  endDate: Option[String],
  status: Option[String],
  postedDate: Option[String],
  nextDueDate: Option[String],
  frequency: Option[Int],
  tcType: Option[String],
  amount: Option[Double],
  method: Option[String]
)

object Payments {
  private val statusPattern = "^([ADSCX])$".r
  private val methodPattern = "^([ROM])$".r
  private val tcTypePattern = "^(ETC|ICC)$".r

  implicit val format: Format[Payments] = Format(
    (
      (JsPath \ "periodStartDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "periodEndDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "startDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "endDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "status").readNullable[String](pattern(statusPattern, "invalid status")) and
        (JsPath \ "postedDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "nextDueDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "frequency").readNullable[Int](min[Int](1).keepAnd(max[Int](999))) and
        (JsPath \ "tcType").readNullable[String](pattern(tcTypePattern, "invalid tc type")) and
        (JsPath \ "amount").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "method").readNullable[String](pattern(methodPattern, "invalid method"))
    )(Payments.apply _),
    Json.writes[Payments]
  )
}

case class ChildTaxCredit(
  childCareAmount: Option[Double],
  ctcChildAmount: Option[Double],
  familyAmount: Option[Double],
  babyAmount: Option[Double],
  entitlementYTD: Option[Double],
  paidYTD: Option[Double]
)

object ChildTaxCredit {
  implicit val format: Format[ChildTaxCredit] = Format(
    (
      (JsPath \ "childCareAmount").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "ctcChildAmount").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "familyAmount").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "babyAmount").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "entitlementYTD").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "paidYTD").readNullable[Double](verifying(paymentAmountValidator))
    )(ChildTaxCredit.apply _),
    Json.writes[ChildTaxCredit]
  )
}

case class WorkTaxCredit(amount: Option[Double], entitlementYTD: Option[Double], paidYTD: Option[Double])

object WorkTaxCredit {
  implicit val format: Format[WorkTaxCredit] = Format(
    (
      (JsPath \ "amount").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "entitlementYTD").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "paidYTD").readNullable[Double](verifying(paymentAmountValidator))
    )(WorkTaxCredit.apply _),
    Json.writes[WorkTaxCredit]
  )
}

case class Awards(
  payProfCalcDate: Option[String],
  startDate: Option[String],
  endDate: Option[String],
  totalEntitlement: Option[Double],
  workTaxCredit: Option[WorkTaxCredit],
  childTaxCredit: Option[ChildTaxCredit],
  grossTaxYearAmount: Option[Double],
  payments: Option[Seq[Payments]]
)

object Awards {
  implicit val format: Format[Awards] = Format(
    (
      (JsPath \ "payProfCalcDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "startDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "endDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "totalEntitlement").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "workingTaxCredit").readNullable[WorkTaxCredit] and
        (JsPath \ "childTaxCredit").readNullable[ChildTaxCredit] and
        (JsPath \ "grossYearTaxAmount").readNullable[Double](verifying(paymentAmountValidator)) and
        (JsPath \ "payments").readNullable[Seq[Payments]]
    )(Awards.apply _),
    Json.writes[Awards]
  )
}

case class Application(
  id: Option[Double],
  ceasedDate: Option[String],
  entStartDate: Option[String],
  entEndDate: Option[String],
  awards: Option[Seq[Awards]]
)

object Application {
  private def isMultipleOfOne(value: Double) = value % 1 == 0

  private def applicationIdValidator =
    min[Double](0) andKeep max[Double](999999999999.0) andKeep verifying[Double](isMultipleOfOne)

  implicit val format: Format[Application] = Format(
    (
      (JsPath \ "id").readNullable[Double](applicationIdValidator) and
        (JsPath \ "ceasedDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "entStartDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "entEndDate").readNullable[String](pattern(datePattern, "invalid date")) and
        (JsPath \ "awards").readNullable[Seq[Awards]]
    )(Application.apply _),
    Json.writes[Application]
  )
}

object TaxCredits {
  val datePattern
    : Regex = ("^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
    "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-]" +
    "(0[1-9]|1[0-9]|2[0-8])))$").r

  val minPaymentValue: Double = 0.0
  val maxPaymentValue: Double = 1000000000000000.0

  private def isMultipleOfPointZeroOne(value: Double) = (BigDecimal(value) * 100.0) % 1 == 0

  private def isInRange(value: Double) = value >= minPaymentValue && value <= maxPaymentValue

  def paymentAmountValidator(value: Double): Boolean =
    isInRange(value) && isMultipleOfPointZeroOne(value)
}
