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
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxReturnDetails.{taxpayerStartDatePattern, utrPattern, validTaxSolvencyStatus}

import scala.util.matching.Regex

case class AccountingPeriod(apStartDate: String, apEndDate: String, turnover: Int)

object AccountingPeriod {
  private val apDatePattern =
    "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r

  implicit val format: Format[AccountingPeriod] = Format[AccountingPeriod](
    (
      (JsPath \ "apStartDate").read[String](using pattern(apDatePattern, "apStartDate not in correct format")) and
        (JsPath \ "apEndDate").read[String](using pattern(apDatePattern, "apEndDate not in correct format")) and
        (JsPath \ "turnover").read[Int]
    )(AccountingPeriod.apply),
    Json.writes[AccountingPeriod]
  )
}

case class CreateCorporationTaxReturnDetailsRequest(
  utr: String,
  taxpayerStartDate: String,
  taxSolvencyStatus: String,
  accountingPeriods: Seq[AccountingPeriod]
)

object CreateCorporationTaxReturnDetailsRequest {
  implicit val format: Format[CreateCorporationTaxReturnDetailsRequest] =
    Format[CreateCorporationTaxReturnDetailsRequest](
      (
        (JsPath \ "utr").read[String](using pattern(utrPattern, "Invalid UTR format")) and
          (JsPath \ "taxpayerStartDate")
            .read[String](using pattern(taxpayerStartDatePattern, "Invalid taxpayer start date")) and
          (JsPath \ "taxSolvencyStatus").read[String](using verifying(validTaxSolvencyStatus)) and
          (JsPath \ "accountingPeriods").read[Seq[AccountingPeriod]]
      )(CreateCorporationTaxReturnDetailsRequest.apply),
      Json.writes[CreateCorporationTaxReturnDetailsRequest]
    )
}

case class CorporationTaxReturnDetailsResponse(
  utr: String,
  taxpayerStartDate: String,
  taxSolvencyStatus: String,
  accountingPeriods: Seq[AccountingPeriod]
)

object CorporationTaxReturnDetailsResponse {
  implicit val format: Format[CorporationTaxReturnDetailsResponse] = Format[CorporationTaxReturnDetailsResponse](
    (
      (JsPath \ "utr").read[String](using pattern(utrPattern, "Invalid UTR format")) and
        (JsPath \ "taxpayerStartDate")
          .read[String](using pattern(taxpayerStartDatePattern, "Invalid taxpayer start date")) and
        (JsPath \ "taxSolvencyStatus").read[String](using verifying(validTaxSolvencyStatus)) and
        (JsPath \ "accountingPeriods").read[Seq[AccountingPeriod]]
    )(CorporationTaxReturnDetailsResponse.apply),
    Json.writes[CorporationTaxReturnDetailsResponse]
  )
}

object CorporationTaxReturnDetails {
  var utrPattern: Regex = "^[0-9]{10}$".r
  val taxpayerStartDatePattern: Regex =
    "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  def validTaxSolvencyStatus(value: String): Boolean = Seq("V", "S", "I", "A").contains(value)
}

case class CTReturnDetailsEntry(id: String, response: CorporationTaxReturnDetailsResponse)

object CTReturnDetailsEntry {
  implicit val format: OFormat[CTReturnDetailsEntry] = Json.format[CTReturnDetailsEntry]
}
