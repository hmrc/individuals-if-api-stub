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

package uk.gov.hmrc.individualsifapistub.domain

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.individualsifapistub.domain.Identifier._

case class TaxCreditsEntry(id: String, applications: Seq[Application])

case class Applications(applications: Seq[Application])

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

case class ChildTaxCredit(
                           childCareAmount: Option[Double],
                           ctcChildAmount: Option[Double],
                           familyAmount: Option[Double],
                           babyAmount: Option[Double],
                           entitlementYTD: Option[Double],
                           paidYTD: Option[Double]
                         )

case class WorkTaxCredit(amount: Option[Double], entitlementYTD: Option[Double], paidYTD: Option[Double])

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

case class Application(id: Option[Double], ceasedDate: Option[String], entStartDate: Option[String], entEndDate: Option[String], awards: Option[Seq[Awards]])

object TaxCredits {

  val statusPattern = "^([ADSCX])$".r
  val methodPattern = "^([ROM])$".r
  val tcTypePattern = "^(ETC|ICC)$".r
  val datePattern = ("^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-]" +
    "(0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-]" +
    "(0[1-9]|1[0-9]|2[0-8])))$").r

  val minPaymentValue = 0.0
  val maxPaymentValue = 1000000000000000.0

  def isMultipleOfOne(value: Double): Boolean = value % 1 == 0
  def isMultipleOfPointZeroOne(value: Double): Boolean = (value * 100.0) % 1 == 0

  def isInRange(value: Double): Boolean = value >= minPaymentValue && value <= maxPaymentValue

  def applicationIdValidator:Reads[Double] =
    min[Double](0) andKeep max[Double](999999999999.0) andKeep verifying[Double](isMultipleOfOne)

  def paymentAmountValidator(value: Double):Boolean =
    isInRange(value) && isMultipleOfPointZeroOne(value)

  implicit val paymentsFormat: Format[Payments] = Format(
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
    (
      (JsPath \ "periodStartDate").writeNullable[String] and
      (JsPath \ "periodEndDate").writeNullable[String] and
      (JsPath \ "startDate").writeNullable[String] and
      (JsPath \ "endDate").writeNullable[String] and
      (JsPath \ "status").writeNullable[String] and
      (JsPath \ "postedDate").writeNullable[String] and
      (JsPath \ "nextDueDate").writeNullable[String] and
      (JsPath \ "frequency").writeNullable[Int] and
      (JsPath \ "tcType").writeNullable[String] and
      (JsPath \ "amount").writeNullable[Double] and
      (JsPath \ "method").writeNullable[String]
    )(unlift(Payments.unapply))
  )

  implicit val childTaxCreditFormat: Format[ChildTaxCredit] = Format(
    (
      (JsPath \ "childCareAmount").readNullable[Double](verifying(paymentAmountValidator)) and
      (JsPath \ "ctcChildAmount").readNullable[Double](verifying(paymentAmountValidator)) and
      (JsPath \ "familyAmount").readNullable[Double](verifying(paymentAmountValidator)) and
      (JsPath \ "babyAmount").readNullable[Double](verifying(paymentAmountValidator)) and
      (JsPath \ "entitlementYTD").readNullable[Double](verifying(paymentAmountValidator)) and
      (JsPath \ "paidYTD").readNullable[Double](verifying(paymentAmountValidator))
    )(ChildTaxCredit.apply _),
    (
      (JsPath \ "childCareAmount").writeNullable[Double] and
      (JsPath \ "ctcChildAmount").writeNullable[Double] and
      (JsPath \ "familyAmount").writeNullable[Double] and
      (JsPath \ "babyAmount").writeNullable[Double] and
      (JsPath \ "entitlementYTD").writeNullable[Double] and
      (JsPath \ "paidYTD").writeNullable[Double]
    )(unlift(ChildTaxCredit.unapply))
  )

  implicit val workTaxCreditFormat: Format[WorkTaxCredit] = Format(
    (
      (JsPath \ "amount").readNullable[Double](verifying(paymentAmountValidator)) and
      (JsPath \ "entitlementYTD").readNullable[Double](verifying(paymentAmountValidator)) and
      (JsPath \ "paidYTD").readNullable[Double](verifying(paymentAmountValidator))
    )(WorkTaxCredit.apply _),
    (
      (JsPath \ "amount").writeNullable[Double] and
      (JsPath \ "entitlementYTD").writeNullable[Double] and
      (JsPath \ "paidYTD").writeNullable[Double]
    )(unlift(WorkTaxCredit.unapply))
  )

  implicit val awardsFormat: Format[Awards] = Format(
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
    (
      (JsPath \ "payProfCalcDate").writeNullable[String] and
      (JsPath \ "startDate").writeNullable[String] and
      (JsPath \ "endDate").writeNullable[String] and
      (JsPath \ "totalEntitlement").writeNullable[Double] and
      (JsPath \ "workingTaxCredit").writeNullable[WorkTaxCredit] and
      (JsPath \ "childTaxCredit").writeNullable[ChildTaxCredit] and
      (JsPath \ "grossYearTaxAmount").writeNullable[Double] and
      (JsPath \ "payments").writeNullable[Seq[Payments]]
    )(unlift(Awards.unapply))
  )

  implicit val applicationFormat: Format[Application] = Format(
    (
      (JsPath \ "id").readNullable[Double](applicationIdValidator) and
      (JsPath \ "ceasedDate").readNullable[String](pattern(datePattern, "invalid date")) and
      (JsPath \ "entStartDate").readNullable[String](pattern(datePattern, "invalid date")) and
      (JsPath \ "entEndDate").readNullable[String](pattern(datePattern, "invalid date")) and
      (JsPath \ "awards").readNullable[Seq[Awards]]
    )(Application.apply _),
    (
      (JsPath \ "id").writeNullable[Double] and
      (JsPath \ "ceasedDate").writeNullable[String] and
      (JsPath \ "entStartDate").writeNullable[String] and
      (JsPath \ "entEndDate").writeNullable[String] and
      (JsPath \ "awards").writeNullable[Seq[Awards]]
    )(unlift(Application.unapply))
  )

  implicit val applicationsFormat: Format[Applications] = Format(
    (JsPath \ "applications").read[Seq[Application]].map(x => Applications(x)),
    (JsPath \ "applications").write[Seq[Application]].contramap(x => x.applications)
  )

  val taxCreditsEntryFormat: Format[TaxCreditsEntry] = Format(
    (
      (JsPath \ "id").read[String] and
      (JsPath \ "applications").read[Seq[Application]]
    )(TaxCreditsEntry.apply _),
    (
      (JsPath \ "id").write[String] and
      (JsPath \ "applications").write[Seq[Application]]
    )(unlift(TaxCreditsEntry.unapply))
  )
}
