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

package uk.gov.hmrc.individualsifapistub.domain.organisations

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.individualsifapistub.domain.organisations.SelfAssessmentReturnDetail.{datePattern, taxPayerTypePattern, taxSolvencyStatusValidator, utrPattern}

import scala.util.matching.Regex

case class TaxYear(taxyear: String, businessSalesTurnover: Double)

object TaxYear {
  private val taxYearPattern = "^20[0-9]{2}$".r

  implicit val format: Format[TaxYear] = Format(
    (
      (JsPath \ "taxyear").read[String](pattern(taxYearPattern, "Tax Year is in the incorrect Format")) and
        (JsPath \ "businessSalesTurnover").read[Double]
    )(TaxYear.apply _),
    Json.writes[TaxYear]
  )
}

case class CreateSelfAssessmentReturnDetailRequest(
  utr: String,
  startDate: String,
  taxpayerType: String,
  taxSolvencyStatus: String,
  taxyears: Seq[TaxYear]
)

object CreateSelfAssessmentReturnDetailRequest {
  implicit val format: Format[CreateSelfAssessmentReturnDetailRequest] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "startDate").read[String](pattern(datePattern, "Date pattern is incorrect")) and
        (JsPath \ "taxpayerType").read[String](pattern(taxPayerTypePattern, "Invalid taxpayer type")) and
        (JsPath \ "taxSolvencyStatus").read[String](verifying(taxSolvencyStatusValidator)) and
        (JsPath \ "taxyears").read[Seq[TaxYear]]
    )(CreateSelfAssessmentReturnDetailRequest.apply _),
    Json.writes[CreateSelfAssessmentReturnDetailRequest]
  )
}

case class SelfAssessmentReturnDetailResponse(
  utr: String,
  startDate: String,
  taxpayerType: String,
  taxSolvencyStatus: String,
  taxyears: Seq[TaxYear]
)

object SelfAssessmentReturnDetailResponse {
  implicit val format: Format[SelfAssessmentReturnDetailResponse] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "startDate").read[String](pattern(datePattern, "Date pattern is incorrect")) and
        (JsPath \ "taxpayerType").read[String](pattern(taxPayerTypePattern, "Invalid taxpayer type")) and
        (JsPath \ "taxSolvencyStatus").read[String](verifying(taxSolvencyStatusValidator)) and
        (JsPath \ "taxyears").read[Seq[TaxYear]]
    )(SelfAssessmentReturnDetailResponse.apply _),
    Json.writes[SelfAssessmentReturnDetailResponse]
  )
}

object SelfAssessmentReturnDetail {
  val utrPattern: Regex = "^[0-9]{10}$".r
  val taxPayerTypePattern: Regex = "^[A-Z][a-zA-Z]{3,24}$".r
  val datePattern: Regex =
    "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r

  def taxSolvencyStatusValidator(value: String): Boolean = value == "S" || value == "I"
}

case class SelfAssessmentReturnDetailEntry(id: String, response: SelfAssessmentReturnDetailResponse)

object SelfAssessmentReturnDetailEntry {
  implicit val format: OFormat[SelfAssessmentReturnDetailEntry] = Json.format[SelfAssessmentReturnDetailEntry]
}
