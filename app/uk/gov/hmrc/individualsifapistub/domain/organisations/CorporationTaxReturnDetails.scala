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
import play.api.libs.json.{Format, JsPath, Json}

case class AccountingPeriod(apStartDate: String, apEndDate: String, turnover: Int)
case class CreateCorporationTaxReturnDetailsRequest(utr: String, taxpayerStartDate: String, taxSolvencyStatus: String, accountingPeriods: Seq[AccountingPeriod])

case class CorporationTaxReturnDetailsResponse(utr: String, taxpayerStartDate: String, taxSolvencyStatus: String, accountingPeriods: Seq[AccountingPeriod])

object CorporationTaxReturnDetails {

  val apDatePattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  var utrPattern = "^[0-9]{10}$".r
  val taxpayerStartDatePattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  def validTaxSolvencyStatus(value: String) = Seq("V", "S", "I", "A").contains(value)

  implicit val accountingPeriodFormat = Format[AccountingPeriod](
    (
      (JsPath \ "apStartDate").read[String](pattern(apDatePattern, "apStartDate not in correct format")) and
        (JsPath \ "apEndDate").read[String](pattern(apDatePattern, "apEndDate not in correct format")) and
        (JsPath \ "turnover").read[Int]
      )(AccountingPeriod.apply _),
    (
      (JsPath \ "apStartDate").write[String] and
        (JsPath \ "apEndDate").write[String] and
        (JsPath \ "turnover").write[Int]
      )(unlift(AccountingPeriod.unapply))
  )

  implicit val createCorporationTaxReturnDetailsRequestFormat = Format[CreateCorporationTaxReturnDetailsRequest](
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "Invalid UTR format")) and
        (JsPath \ "taxpayerStartDate").read[String](pattern(taxpayerStartDatePattern, "Invalid taxpayer start date")) and
        (JsPath \ "taxSolvencyStatus").read[String](verifying(validTaxSolvencyStatus)) and
        (JsPath \ "accountingPeriods").read[Seq[AccountingPeriod]]
      )(CreateCorporationTaxReturnDetailsRequest.apply _),
    (
      (JsPath \ "utr").write[String] and
        (JsPath \ "taxpayerStartDate").write[String] and
        (JsPath \ "taxSolvencyStatus").write[String] and
        (JsPath \ "accountingPeriods").write[Seq[AccountingPeriod]]
      )(unlift(CreateCorporationTaxReturnDetailsRequest.unapply))
  )

  implicit val corporationTaxReturnDetailsResponseFormat = Format[CorporationTaxReturnDetailsResponse](
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "Invalid UTR format")) and
        (JsPath \ "taxpayerStartDate").read[String](pattern(taxpayerStartDatePattern, "Invalid taxpayer start date")) and
        (JsPath \ "taxSolvencyStatus").read[String](verifying(validTaxSolvencyStatus)) and
        (JsPath \ "accountingPeriods").read[Seq[AccountingPeriod]]
      )(CorporationTaxReturnDetailsResponse.apply _),
    (
      (JsPath \ "utr").write[String] and
        (JsPath \ "taxpayerStartDate").write[String] and
        (JsPath \ "taxSolvencyStatus").write[String] and
        (JsPath \ "accountingPeriods").write[Seq[AccountingPeriod]]
      )(unlift(CorporationTaxReturnDetailsResponse.unapply))
  )
}


case class CTReturnDetailsEntry(id: String, response :CorporationTaxReturnDetailsResponse)
object CTReturnDetailsEntry {
  import CorporationTaxReturnDetails._
  implicit val ctReturnDetailsEntryFormat = Json.format[CTReturnDetailsEntry]
}