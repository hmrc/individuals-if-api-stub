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

import org.joda.time.DateTime
import play.api.libs.json.JodaWrites._
import play.api.libs.json._

import scala.util.{Failure, Try}

object JsonFormatters {
  implicit val dateFormatDefault = new Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = JodaReads.DefaultJodaDateTimeReads.reads(json)
    override def writes(o: DateTime): JsValue = JodaDateTimeNumberWrites.writes(o)
  }

  implicit val contactDetailFormat = Json.format[ContactDetail]
  implicit val addressFormat = Json.format[Address]
  implicit val residenceFormat = Json.format[Residence]
  implicit val detailsResponseFormat = Json.format[DetailsResponse]
  implicit val createDetailsRequestFormat = Json.format[CreateDetailsRequest]

  implicit val testAddressFormat = Json.format[TestAddress]
  implicit val testIndividualFormat = Json.format[TestIndividual]
  implicit val testOrganisationDetailsFormat = Json.format[TestOrganisationDetails]
  implicit val testOrganisationFormat = Json.format[TestOrganisation]

  implicit val incomeFormat = Json.format[Income]
  implicit val createIncomeRequestFormat = Json.format[CreateIncomeRequest]

  implicit val errorInvalidRequestFormat = new Format[ErrorInvalidRequest] {
    def reads(json: JsValue): JsResult[ErrorInvalidRequest] = JsSuccess(
      ErrorInvalidRequest((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidRequest): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}

object EnumJson {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        Try(JsSuccess(enum.withName(s))) recoverWith {
          case _: NoSuchElementException => Failure(new InvalidEnumException(enum.getClass.getSimpleName, s))
        } get
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}

class InvalidEnumException(className: String, input:String) extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")
