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

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{max, maxLength, min, minLength, pattern}
import play.api.libs.json.{Format, JsError, JsPath, JsSuccess, Json, OFormat, Reads}

case class ContactDetail(code: Int, `type`: String, detail: String)

object ContactDetail {
  implicit val format: Format[ContactDetail] = Format(
    (
      (JsPath \ "code").read[Int](using min[Int](1).keepAnd(max[Int](9999))) and
        (JsPath \ "type").read[String](using minLength[String](1).keepAnd(maxLength[String](35))) and
        (JsPath \ "detail").read[String](using minLength[String](3).keepAnd(maxLength[String](72)))
    )(ContactDetail.apply),
    Json.writes[ContactDetail]
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
      (JsPath \ "line1").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line2").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line3").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line4").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "line5").readNullable[String](using minLength[String](0) keepAnd maxLength[String](100)) and
        (JsPath \ "postcode").readNullable[String](using minLength[String](0) keepAnd maxLength[String](10))
    )(Address.apply),
    Json.writes[Address]
  )
}

case class Residence(
  statusCode: Option[String] = None,
  status: Option[String] = None,
  typeCode: Option[Int] = None,
  `type`: Option[String] = None,
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
  noLongerUsed: Option[String] = None
)

object Residence {
  private val statusCodePattern = "^[1-9]$".r
  private val datePattern =
    """^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)
      |[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|
      |(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$""".r

  implicit val format: Format[Residence] = Format(
    (
      (JsPath \ "statusCode").readNullable[String](using pattern(statusCodePattern, "Status code is invalid")) and
        (JsPath \ "status").readNullable[String](using minLength[String](1) andKeep maxLength[String](8)) and
        (JsPath \ "typeCode").readNullable[Int](using min[Int](1) andKeep max[Int](9999)) and
        (JsPath \ "type").readNullable[String](using minLength[String](1) andKeep maxLength[String](35)) and
        (JsPath \ "deliveryInfo").readNullable[String](using minLength[String](1) andKeep maxLength[String](35)) and
        (JsPath \ "retLetterServ").readNullable[String](using minLength[String](1) andKeep maxLength[String](1)) and
        (JsPath \ "addressCode").readNullable[String](using pattern(statusCodePattern, "Address code is invalid")) and
        (JsPath \ "addressType").readNullable[String](using minLength[String](1) andKeep maxLength[String](6)) and
        (JsPath \ "address").readNullable[Address] and
        (JsPath \ "houseId").readNullable[String](using maxLength[String](35)) and
        (JsPath \ "homeCountry").readNullable[String](using maxLength[String](16)) and
        (JsPath \ "otherCountry").readNullable[String](using maxLength[String](35)) and
        (JsPath \ "deadLetterOfficeDate").readNullable[String](using pattern(datePattern, "Date is invalid")) and
        (JsPath \ "startDateTime").readNullable[String] and
        (JsPath \ "noLongerUsed").readNullable[String](using minLength[String](1) andKeep maxLength[String](1))
    )(Residence.apply),
    Json.writes[Residence]
  )
}

case class DetailsResponse(
  details: String,
  contactDetails: Option[Seq[ContactDetail]],
  residences: Option[Seq[Residence]]
)

object DetailsResponse {
  private val reads: Reads[DetailsResponse] = (
    (JsPath \ "details").read[String] and
      (JsPath \ "contactDetails").readNullable[Seq[ContactDetail]] and
      Reads { json =>
        (json \ "residences").validateOpt[Seq[Residence]] match {
          case JsSuccess(Some(residences), _) => JsSuccess(Some(residences))
          case JsSuccess(None, _)             => (json \ "residence").validateOpt[Seq[Residence]]
          case err: JsError                   => err
        }
      }
  )(DetailsResponse.apply)

  implicit val format: OFormat[DetailsResponse] = OFormat(reads, Json.writes[DetailsResponse])
}

case class DetailsResponseNoId(contactDetails: Option[Seq[ContactDetail]], residences: Option[Seq[Residence]])

object DetailsResponseNoId {
  implicit val format: OFormat[DetailsResponseNoId] = Json.format
}

case class CreateDetailsRequest(contactDetails: Option[Seq[ContactDetail]], residences: Option[Seq[Residence]])

object CreateDetailsRequest {
  implicit val format: OFormat[CreateDetailsRequest] = Json.format
}
