/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.individualsifapistub.domain

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.util.matching.Regex

case class ContactDetail(code: Int, detailType: String, detail: String)

case class Address(
                    line1: Option[String],
                    line2: Option[String],
                    line3: Option[String],
                    line4: Option[String],
                    line5: Option[String],
                    postcode: Option[String]
                  )

case class Residence(residenceType: Option[String], address: Option[Address])

case class Details(nino: Option[String], trn: Option[String])

case class DetailsResponse(details: Details, contactDetails: Option[Seq[ContactDetail]], residences: Option[Seq[Residence]])

object DetailsResponse {
  val ninoPattern: Regex = "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D\\s]?$".r
  val trnPattern: Regex = "^[0-9]{8}$".r

  implicit val detailsRead: Reads[Details] = (
    (JsPath \ "nino").readNullable[String](pattern(ninoPattern, "InvalidNino")) and
      (JsPath \ "trn").readNullable[String](pattern(trnPattern, "InvalidTrn"))
    )(Details.apply _)

  implicit val contactDetailsRead: Reads[ContactDetail] = (
    (JsPath \ "code").read[Int](min[Int](1).keepAnd(max[Int](9999))) and
      (JsPath \ "type").read[String](minLength[String](1).keepAnd(maxLength[String](35))) and
      (JsPath \ "detail").read[String](minLength[String](3).keepAnd(maxLength[String](72)))
    )(ContactDetail.apply _)

  implicit val addressRead: Reads[Address] = (
    (JsPath \ "line1").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
      (JsPath \ "line2").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
      (JsPath \ "line3").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
      (JsPath \ "line4").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
      (JsPath \ "line5").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
      (JsPath \ "postcode").readNullable[String](minLength[String](1).keepAnd(maxLength[String](10)))
    )(Address.apply _)

  implicit val residencesRead: Reads[Residence] = (
    (JsPath \ "residenceType").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
      (JsPath \ "address").readNullable[Address]
    )(Residence.apply _)

  implicit val detailsResponseRead: Reads[DetailsResponse] = (
    (JsPath \ "details").read[Details] and
      (JsPath \ "contactDetails").readNullable[Seq[ContactDetail]] and
      (JsPath \ "residence").readNullable[Seq[Residence]]
    )(DetailsResponse.apply _)


  implicit val detailsWrite: Writes[Details] = implicitly

  implicit val contactDetailsWrite: Writes[ContactDetail] = (
    (JsPath \ "code").write[Int] and
      (JsPath \ "type").write[String] and
      (JsPath \ "detail").write[String]
    )(unlift(ContactDetail.unapply))

  implicit val addressWrite: Writes[Address] = implicitly

  implicit val residencesWrite: Writes[Residence] = implicitly

  implicit val detailsResponseWrite: Writes[DetailsResponse] = implicitly
}

case class CreateDetailsRequest(contactDetails: Option[Seq[ContactDetail]], residences: Option[Seq[Residence]])