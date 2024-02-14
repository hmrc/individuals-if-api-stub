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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class VatCustomerDetails(organisationName: String)

object VatCustomerDetails {
  implicit val format: OFormat[VatCustomerDetails] = Json.format[VatCustomerDetails]
}

case class VatAddress(line1: String, postCode: String)

object VatAddress {
  implicit val format: OFormat[VatAddress] = Json.format[VatAddress]
}

case class VatPPOB(address: VatAddress)

object VatPPOB {
  implicit val format: OFormat[VatPPOB] = Json.format[VatPPOB]
}

case class VatApprovedInformation(customerDetails: VatCustomerDetails, PPOB: VatPPOB)

object VatApprovedInformation {
  implicit val format: OFormat[VatApprovedInformation] = Json.format[VatApprovedInformation]
}

case class VatInformation(approvedInformation: VatApprovedInformation)

object VatInformation {
  implicit val format: OFormat[VatInformation] = Json.format[VatInformation]
}

case class VatInformationSimplified(organisationName: String, addressLine1: String, postcode: String) {
  def toVatInformation: VatInformation =
    VatInformation(
      VatApprovedInformation(
        VatCustomerDetails(organisationName),
        VatPPOB(VatAddress(addressLine1, postcode))
      )
    )
}

object VatInformationSimplified {
  implicit val format: OFormat[VatInformationSimplified] = Json.format[VatInformationSimplified]

  def fromVatInformation(vatInformation: VatInformation): VatInformationSimplified =
    VatInformationSimplified(
      vatInformation.approvedInformation.customerDetails.organisationName,
      vatInformation.approvedInformation.PPOB.address.line1,
      vatInformation.approvedInformation.PPOB.address.postCode
    )
}

case class VatInformationEntry(id: String, vatInformation: VatInformation, createdAt: LocalDateTime)

object VatInformationEntry {
  implicit val format: OFormat[VatInformationEntry] = Json.format[VatInformationEntry]
}
