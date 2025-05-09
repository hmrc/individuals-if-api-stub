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
import uk.gov.hmrc.individualsifapistub.domain.{TestOrganisation, TestOrganisationDetails}

case class Name(name1: String, name2: String)

object Name {
  implicit val format: Format[Name] = Json.format
}

case class NameAddressDetails(name: Name, address: Address)

object NameAddressDetails {
  implicit val format: Format[NameAddressDetails] = Json.format
}

case class CorporationTaxCompanyDetails(
  utr: String,
  crn: String,
  registeredDetails: Option[NameAddressDetails],
  communicationDetails: Option[NameAddressDetails]
)

object CorporationTaxCompanyDetails {
  private val utrPattern = "^[0-9]{10}$".r
  private val crnPattern = "^[A-Z0-9]{1,10}$".r

  def fromApiPlatformTestUser(testUser: TestOrganisation): CorporationTaxCompanyDetails = CorporationTaxCompanyDetails(
    testUser.ctUtr.mkString,
    testUser.crn.mkString,
    registeredDetails = Some(fromOrganisationDetails(testUser.organisationDetails)),
    communicationDetails = None
  )

  private def fromOrganisationDetails(organisationDetails: TestOrganisationDetails) = NameAddressDetails(
    Name(organisationDetails.name, ""),
    Address(
      Some(organisationDetails.address.line1),
      Some(organisationDetails.address.line2),
      None,
      None,
      Some(organisationDetails.address.postcode)
    )
  )

  implicit val format: Format[CorporationTaxCompanyDetails] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "Invalid UTR format")) and
        (JsPath \ "crn").read[String](pattern(crnPattern, "Inavlid CRN format")) and
        (JsPath \ "registeredDetails").readNullable[NameAddressDetails] and
        (JsPath \ "communicationDetails").readNullable[NameAddressDetails]
    )(CorporationTaxCompanyDetails.apply _),
    Json.writes[CorporationTaxCompanyDetails]
  )
}

case class CTCompanyDetailsEntry(id: String, response: CorporationTaxCompanyDetails)

object CTCompanyDetailsEntry {
  implicit val format: OFormat[CTCompanyDetailsEntry] = Json.format[CTCompanyDetailsEntry]
}
