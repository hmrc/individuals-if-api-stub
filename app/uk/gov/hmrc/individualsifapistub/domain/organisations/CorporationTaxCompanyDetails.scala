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
import uk.gov.hmrc.individualsifapistub.domain.{TestOrganisation, TestOrganisationDetails}

case class Name(name1: String, name2: String)
case class NameAddressDetails(name: Name, address: Address)
case class CorporationTaxCompanyDetails(utr: String, crn: String, registeredDetails: Option[NameAddressDetails], communicationDetails: Option[NameAddressDetails])

object CorporationTaxCompanyDetails {

  val utrPattern = "^[0-9]{10}$".r
  val crnPattern = "^[A-Z0-9]{1,10}$".r

  def fromApiPlatformTestUser(testUser: TestOrganisation): CorporationTaxCompanyDetails  = CorporationTaxCompanyDetails(
    testUser.ctUtr.mkString,
    testUser.crn.mkString,
    registeredDetails = Some(fromOrganisationDetails(testUser.organisationDetails)),
    communicationDetails = None
  )

  def fromOrganisationDetails(organisationDetails: TestOrganisationDetails): NameAddressDetails = NameAddressDetails(
    Name(organisationDetails.name, ""),
    Address(
      Some(organisationDetails.address.line1),
      Some(organisationDetails.address.line2),
      None,
      None,
      Some(organisationDetails.address.postcode))
  )

  implicit val nameFormat = Format[Name](
    (
      (JsPath \ "name1").read[String] and
        (JsPath \ "name2").read[String]
      )(Name.apply _),
    (
      (JsPath \ "name1").write[String] and
        (JsPath \ "name2").write[String]
      )(unlift(Name.unapply))
  )

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

  implicit val nameAddressDetailsFormat: Format[NameAddressDetails] = Format(
    (
      (JsPath \ "name").read[Name] and
        (JsPath \ "address").read[Address]
      )(NameAddressDetails.apply _),
    (
      (JsPath \ "name").write[Name] and
        (JsPath \ "address").write[Address]
      )(unlift(NameAddressDetails.unapply))
  )

  implicit val corporationTaxCompanyDetailsResponseFormat: Format[CorporationTaxCompanyDetails] = Format(
    (
      (JsPath \ "utr").read[String](pattern(utrPattern, "Invalid UTR format")) and
        (JsPath \ "crn").read[String](pattern(crnPattern, "Inavlid CRN format")) and
        (JsPath \ "registeredDetails").readNullable[NameAddressDetails] and
        (JsPath \ "communicationDetails").readNullable[NameAddressDetails]
      )(CorporationTaxCompanyDetails.apply _),
    (
      (JsPath \ "utr").write[String] and
        (JsPath \ "crn").write[String] and
        (JsPath \ "registeredDetails").writeNullable[NameAddressDetails] and
        (JsPath \ "communicationDetails").writeNullable[NameAddressDetails]
      )(unlift(CorporationTaxCompanyDetails.unapply))
  )
}

case class CTCompanyDetailsEntry(id: String, response :CorporationTaxCompanyDetails)
object CTCompanyDetailsEntry {
  implicit val ctCompanyDetailsEntryFormat = Json.format[CTCompanyDetailsEntry]
}