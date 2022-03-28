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

package uk.gov.hmrc.individualsifapistub.domain.organisations

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath, Json}
import uk.gov.hmrc.individualsifapistub.domain.{TestIndividual, TestOrganisationDetails}

import scala.util.matching.Regex

case class Address(
                    line1: Option[String],
                    line2: Option[String],
                    line3: Option[String],
                    line4: Option[String],
                    postcode: Option[String]
                  )
case class TaxPayerDetails(name: String, addressType: Option[String], address: Address)
case class SelfAssessmentTaxPayer(utr: String, taxPayerType: String, taxPayerDetails: Seq[TaxPayerDetails])

object SelfAssessmentTaxPayer {

  def fromApiPlatformTestUser(testUser: TestIndividual): SelfAssessmentTaxPayer  = SelfAssessmentTaxPayer(
    testUser.saUtr.map(_.utr).getOrElse(""),
    testUser.taxpayerType.getOrElse(""),
    taxPayerDetails = Seq(fromOrganisationDetails(testUser.organisationDetails))
  )

  def fromOrganisationDetails(taxpayerDetails: Option[TestOrganisationDetails]): TaxPayerDetails = {
    taxpayerDetails match {
      case Some(value) =>{
        TaxPayerDetails(
          name = value.name,
          address = Address(
            Some(value.address.line1),
            Some(value.address.line2),
            None,
            None,
            Some(value.address.postcode)),
          addressType = None
        )
      }
      case None =>throw new Exception("taxpayerDetails are required for this operation")
    }
  }

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
        (JsPath \ "addressType").readNullable[String](pattern(addressTypePattern, "Address Type does not fit expected pattern")) and
        (JsPath \ "address").read[Address]
      )(TaxPayerDetails.apply _),
    (
      (JsPath \ "name").write[String] and
        (JsPath \ "addressType").writeNullable[String] and
        (JsPath \ "address").write[Address]
      )(unlift(TaxPayerDetails.unapply))
  )

  implicit val selfAssessmentResponseFormat: Format[SelfAssessmentTaxPayer] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "taxpayerType").read[String](pattern(taxPayerTypePattern, "Invalid taxpayer type")) and
        (JsPath \ "taxpayerDetails").read[Seq[TaxPayerDetails]]
      )(SelfAssessmentTaxPayer.apply _),
    (
      (JsPath \ "utr").write[String] and
        (JsPath \ "taxpayerType").write[String] and
        (JsPath \ "taxpayerDetails").write[Seq[TaxPayerDetails]]
      )(unlift(SelfAssessmentTaxPayer.unapply))
  )
}

case class SATaxPayerEntry(id: String, response :SelfAssessmentTaxPayer)
object SATaxPayerEntry {
  implicit val saTaxPayerEntryFormat = Json.format[SATaxPayerEntry]
}
