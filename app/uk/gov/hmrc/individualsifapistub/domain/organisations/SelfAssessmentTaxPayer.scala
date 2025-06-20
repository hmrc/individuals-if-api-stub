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
import uk.gov.hmrc.individualsifapistub.domain.{TestIndividual, TestOrganisationDetails}

case class Address(
  line1: Option[String],
  line2: Option[String],
  line3: Option[String],
  line4: Option[String],
  postcode: Option[String]
)

object Address {
  implicit val format: Format[Address] = Format(
    (
      (JsPath \ "line1").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line2").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line3").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line4").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "postcode").readNullable[String](minLength[String](0) keepAnd maxLength[String](10))
    )(Address.apply _),
    Json.writes[Address]
  )
}

case class TaxPayerDetails(name: String, addressType: Option[String], address: Address)

object TaxPayerDetails {
  private val addressTypePattern = "^[A-Za-z0-9\\s -]{1,24}$".r

  implicit val format: Format[TaxPayerDetails] = Format(
    (
      (JsPath \ "name").read[String] and
        (JsPath \ "addressType")
          .readNullable[String](pattern(addressTypePattern, "Address Type does not fit expected pattern")) and
        (JsPath \ "address").read[Address]
    )(TaxPayerDetails.apply _),
    Json.writes[TaxPayerDetails]
  )
}

case class SelfAssessmentTaxPayer(utr: String, taxpayerType: String, taxpayerDetails: Seq[TaxPayerDetails])

object SelfAssessmentTaxPayer {
  def fromApiPlatformTestUser(testUser: TestIndividual): SelfAssessmentTaxPayer = SelfAssessmentTaxPayer(
    testUser.saUtr.map(_.utr).mkString,
    testUser.taxpayerType.mkString,
    taxpayerDetails = Seq(fromOrganisationDetails(testUser.organisationDetails))
  )

  private def fromOrganisationDetails(taxpayerDetails: Option[TestOrganisationDetails]) =
    taxpayerDetails match {
      case Some(value) =>
        TaxPayerDetails(
          name = value.name,
          address =
            Address(Some(value.address.line1), Some(value.address.line2), None, None, Some(value.address.postcode)),
          addressType = None
        )
      case None => throw new Exception("taxpayerDetails are required for this operation")
    }

  private val utrPattern = "^[0-9]{10}$".r
  private val taxPayerTypePattern = "^[A-Z][a-zA-Z]{3,24}$".r

  implicit val format: Format[SelfAssessmentTaxPayer] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "taxpayerType").read[String](pattern(taxPayerTypePattern, "Invalid taxpayer type")) and
        (JsPath \ "taxpayerDetails").read[Seq[TaxPayerDetails]]
    )(SelfAssessmentTaxPayer.apply _),
    Json.writes[SelfAssessmentTaxPayer]
  )
}

case class SATaxPayerEntry(id: String, response: SelfAssessmentTaxPayer)

object SATaxPayerEntry {
  implicit val format: OFormat[SATaxPayerEntry] = Json.format[SATaxPayerEntry]
}
