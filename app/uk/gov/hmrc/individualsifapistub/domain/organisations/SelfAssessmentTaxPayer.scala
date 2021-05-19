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

case class Address(
                    line1: Option[String],
                    line2: Option[String],
                    line3: Option[String],
                    line4: Option[String],
                    postcode: Option[String]
                  )
case class TaxPayerDetails(name: String, addressType: String, address: Address)
case class CreateSelfAssessmentTaxPayerRequest(utr: String, taxPayerType: String, taxPayerDetails: Seq[TaxPayerDetails])
case class SelfAssessmentTaxPayerResponse(utr: String, taxPayerType: String, taxPayerDetails: Seq[TaxPayerDetails])

object SelfAssessmentTaxPayer {

  val utrPattern: Regex = "^[0-9]{10}$".r
  val taxPayerTypePattern: Regex = "^[A-Z][a-zA-Z]{3,24}$".r
  val addressTypePattern: Regex = "^[A-Za-z0-9\\s -]{1,24}$".r
  val namePattern: Regex = "^[A-Za-z0-9\\s -]{1,24}$".r


  implicit val addressFormat: Format[Address] = Format(
    (
      (JsPath \ "line1").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line2").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line3").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line4").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "postcode").readNullable[String](minLength[String](0) keepAnd maxLength[String](10))
      )(Address.apply _),
    (
      (JsPath \ "line1").writeNullable[String] and
        (JsPath \ "line2").writeNullable[String] and
        (JsPath \ "line3").writeNullable[String] and
        (JsPath \ "line4").writeNullable[String] and
        (JsPath \ "postcode").writeNullable[String]
      )(unlift(Address.unapply))
  )

  implicit val taxPayerDetailsFormat: Format[TaxPayerDetails] = Format(
    (
      (JsPath \ "name").read[String] and
        (JsPath \ "addressType").read[String](pattern(addressTypePattern, "Address Type does not fit expected pattern")) and
        (JsPath \ "address").read[Address]
      )(TaxPayerDetails.apply _),
    (
      (JsPath \ "name").write[String] and
        (JsPath \ "addressType").write[String] and
        (JsPath \ "address").write[Address]
      )(unlift(TaxPayerDetails.unapply))
  )


  implicit val createSelfAssessmentRequestFormat: Format[CreateSelfAssessmentTaxPayerRequest] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "taxPayerType").read[String](pattern(taxPayerTypePattern, "Invalid taxpayer type")) and
        (JsPath \ "taxPayerDetails").read[Seq[TaxPayerDetails]]
      )(CreateSelfAssessmentTaxPayerRequest.apply _),
    (
      (JsPath \ "utr").write[String] and
        (JsPath \ "taxpayerType").write[String] and
        (JsPath \ "taxPayerDetails").write[Seq[TaxPayerDetails]]
      )(unlift(CreateSelfAssessmentTaxPayerRequest.unapply))
  )

  implicit val selfAssessmentResponseFormat: Format[SelfAssessmentTaxPayerResponse] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "taxPayerType").read[String](pattern(taxPayerTypePattern, "Invalid taxpayer type")) and
        (JsPath \ "taxPayerDetails").read[Seq[TaxPayerDetails]]
      )(SelfAssessmentTaxPayerResponse.apply _),
    (
      (JsPath \ "utr").write[String] and
        (JsPath \ "taxpayerType").write[String] and
        (JsPath \ "taxPayerDetails").write[Seq[TaxPayerDetails]]
      )(unlift(SelfAssessmentTaxPayerResponse.unapply))
  )
}
