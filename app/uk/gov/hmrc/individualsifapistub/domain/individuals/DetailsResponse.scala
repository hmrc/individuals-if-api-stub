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

package uk.gov.hmrc.individualsifapistub.domain.individuals

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps, unlift}
import play.api.libs.json.Reads.{max, maxLength, min, minLength, pattern}
import play.api.libs.json.{Format, JsPath, Json, OFormat}

case class ContactDetail(code: Int, detailType: String, detail: String)

object ContactDetail {
  implicit val format: Format[ContactDetail] = Format(
    (
      (JsPath \ "code").read[Int](min[Int](1).keepAnd(max[Int](9999))) and
        (JsPath \ "type").read[String](minLength[String](1).keepAnd(maxLength[String](35))) and
        (JsPath \ "detail").read[String](minLength[String](3).keepAnd(maxLength[String](72)))
      )(ContactDetail.apply _),
    (
      (JsPath \ "code").write[Int] and
        (JsPath \ "type").write[String] and
        (JsPath \ "detail").write[String]
      )(unlift(ContactDetail.unapply))
  )
}

case class Address(
                    line1: Option[String],
                    line2: Option[String],
                    line3: Option[String],
                    line4: Option[String],
                    line5: Option[String] = None,
                    postcode: Option[String]
                  )

object Address {
    implicit val format: Format[Address] = Format(
        (
            (JsPath \ "line1").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
                (JsPath \ "line2").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
                (JsPath \ "line3").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
                (JsPath \ "line4").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
                (JsPath \ "line5").readNullable[String](minLength[String](0) keepAnd maxLength[String](100)) and
                (JsPath \ "postcode").readNullable[String](minLength[String](0) keepAnd maxLength[String](10))
              ) (Address.apply _),
        (
            (JsPath \ "line1").writeNullable[String] and
                (JsPath \ "line2").writeNullable[String] and
                (JsPath \ "line3").writeNullable[String] and
                (JsPath \ "line4").writeNullable[String] and
                (JsPath \ "line5").writeNullable[String] and
                (JsPath \ "postcode").writeNullable[String]
              ) (unlift(Address.unapply))
        )
}

case class Residence(statusCode: Option[String] = None,
                     status: Option[String] = None,
                     typeCode: Option[Int] = None,
                     residenceType: Option[String] = None,
                     deliveryInfo: Option[String] = None,
                     retLetterServ: Option[String] = None,
                     addressCode: Option[String] = None,
                     addressType: Option[String] = None,
                     address: Option[Address] = None,
                     houseId: Option[String] = None,
                     homeCountry: Option[String] = None,
                     otherCountry: Option[String] = None,
                     deadLetterOfficeDate: Option[String] = None,
                     startDateTime: Option[String] = None,
                     noLongerUsed: Option[String] = None)

object Residence {
  private val statusCodePattern = "^[1-9]$".r
  private val datePattern =
    """^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)
      |[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|
      |(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$""".r

  implicit val format: Format[Residence] = Format(
    (
      (JsPath \ "statusCode").readNullable[String](pattern(statusCodePattern, "Status code is invalid")) and
        (JsPath \ "status").readNullable[String](minLength[String](1) andKeep maxLength[String](8)) and
        (JsPath \ "typeCode").readNullable[Int](min[Int](1) andKeep max[Int](9999)) and
        (JsPath \ "type").readNullable[String](minLength[String](1) andKeep maxLength[String](35)) and
        (JsPath \ "deliveryInfo").readNullable[String](minLength[String](1) andKeep maxLength[String](35)) and
        (JsPath \ "retLetterServ").readNullable[String](minLength[String](1) andKeep maxLength[String](1)) and
        (JsPath \ "addressCode").readNullable[String](pattern(statusCodePattern, "Address code is invalid")) and
        (JsPath \ "addressType").readNullable[String](minLength[String](1) andKeep maxLength[String](6)) and
        (JsPath \ "address").readNullable[Address] and
        (JsPath \ "houseId").readNullable[String](maxLength[String](35)) and
        (JsPath \ "homeCountry").readNullable[String](maxLength[String](16)) and
        (JsPath \ "otherCountry").readNullable[String](maxLength[String](35)) and
        (JsPath \ "deadLetterOfficeDate").readNullable[String](pattern(datePattern, "Date is invalid")) and
        (JsPath \ "startDateTime").readNullable[String] and
        (JsPath \ "noLongerUsed").readNullable[String](minLength[String](1) andKeep maxLength[String](1))
      )(Residence.apply _),
    (
      (JsPath \ "statusCode").writeNullable[String] and
        (JsPath \ "status").writeNullable[String] and
        (JsPath \ "typeCode").writeNullable[Int] and
        (JsPath \ "type").writeNullable[String] and
        (JsPath \ "deliveryInfo").writeNullable[String] and
        (JsPath \ "retLetterServ").writeNullable[String] and
        (JsPath \ "addressCode").writeNullable[String] and
        (JsPath \ "addressType").writeNullable[String] and
        (JsPath \ "address").writeNullable[Address] and
        (JsPath \ "houseId").writeNullable[String] and
        (JsPath \ "homeCountry").writeNullable[String] and
        (JsPath \ "otherCountry").writeNullable[String] and
        (JsPath \ "deadLetterOfficeDate").writeNullable[String] and
        (JsPath \ "startDateTime").writeNullable[String] and
        (JsPath \ "noLongerUsed").writeNullable[String]
      )(unlift(Residence.unapply))
  )
}

case class DetailsResponse(details: String, contactDetails: Option[Seq[ContactDetail]], residences: Option[Seq[Residence]])

object DetailsResponse {
  implicit val format: Format[DetailsResponse] = Format(
    (
      (JsPath \ "details").read[String] and
        (JsPath \ "contactDetails").readNullable[Seq[ContactDetail]] and
        (JsPath \ "residence").readNullable[Seq[Residence]]
      )(DetailsResponse.apply _),
    (
      (JsPath \ "details").write[String] and
        (JsPath \ "contactDetails").writeNullable[Seq[ContactDetail]] and
        (JsPath \ "residence").writeNullable[Seq[Residence]]
      )(unlift(DetailsResponse.unapply))
  )
}

case class DetailsResponseNoId(contactDetails: Option[Seq[ContactDetail]], residences: Option[Seq[Residence]])

object DetailsResponseNoId {
  implicit val format: OFormat[DetailsResponseNoId] = Json.format
}

case class CreateDetailsRequest(contactDetails: Option[Seq[ContactDetail]], residences: Option[Seq[Residence]])

object CreateDetailsRequest {
  implicit val format: OFormat[CreateDetailsRequest] = Json.format
}
