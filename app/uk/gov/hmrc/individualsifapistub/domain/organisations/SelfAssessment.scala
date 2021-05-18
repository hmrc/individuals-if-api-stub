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

package uk.gov.hmrc.individualsifapistub.domain.organisations

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.util.matching.Regex

case class TaxYear(taxYear: String, businessSalesTurnover: Double)
case class CreateSelfAssessmentRequest(utr: String, taxPayerType: String, taxSolvencyStatus: String, taxYears: Seq[TaxYear])

object SelfAssessment {

  val taxYearPattern: Regex = "^20[0-9]{2}$".r
  val utrPattern: Regex = "^[0-9]{10}$".r
  val taxPayerTypePattern: Regex = "^[A-Z][a-zA-Z]{3,24}$".r

  def taxSolvencyStatusValidator(value: String): Boolean = value == "S" || value == "I"

  implicit val taxYearFormat: Format[TaxYear] = Format(
    (
      (JsPath \ "taxYear").read[String](pattern(taxYearPattern, "Tax Year is in the incorrect Format")) and
        (JsPath \ "businessSalesTurnover").read[Double]
      )(TaxYear.apply _),
    (
      (JsPath \ "taxYear").write[String] and
        (JsPath \ "businessSaleTurnover").write[Double]
      )(unlift(TaxYear.unapply))
  )


  implicit val createSelfAssessmentRequestFormat: Format[CreateSelfAssessmentRequest] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "taxpayerType").read[String](pattern(taxPayerTypePattern, "Invalid taxpayer type")) and
        (JsPath \ "taxSolvencyStatus").read[String](verifying(taxSolvencyStatusValidator)) and
        (JsPath \ "taxyears").read[Seq[TaxYear]]
      )(CreateSelfAssessmentRequest.apply _),
    (
      (JsPath \ "utr").write[String] and
        (JsPath \ "taxpayerType").write[String] and
        (JsPath \ "taxSolvencyStatus").write[String] and
        (JsPath \ "taxyears").write[Seq[TaxYear]]
      )(unlift(CreateSelfAssessmentRequest.unapply))
  )
}
