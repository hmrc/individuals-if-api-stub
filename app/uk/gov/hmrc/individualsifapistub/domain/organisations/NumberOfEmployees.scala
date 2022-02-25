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

case class NumberOfEmployeeCounts(dateTaken: String, employeeCount: Int)
case class NumberOfEmployeeReferences(districtNumber: String, payeReference: String, counts: Seq[NumberOfEmployeeCounts])
case class NumberOfEmployeeReferencesRequest(districtNumber: String, payeReference: String)
case class NumberOfEmployeesRequest(startDate: String, endDate: String, references: Seq[NumberOfEmployeeReferencesRequest])
case class NumberOfEmployeesResponse(startDate: String, endDate: String, references: Seq[NumberOfEmployeeReferences])

object NumberOfEmployees {

  val dateTakenPattern = "^[1-2]{1}[0-9]{3}-[0-9]{2}$".r
  val districtNumberPattern = "^[0-9]{3}$".r
  val payeRefPattern = "^[a-zA-Z0-9]{1,10}$".r
  val datePattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r

  implicit val numberOfEmployeeCountsFormat = Format[NumberOfEmployeeCounts](
    (
      (JsPath \ "dateTaken").read[String](pattern(dateTakenPattern, "Invalid dateTaken format")) and
        (JsPath \ "employeeCount").read[Int]
      )(NumberOfEmployeeCounts.apply _),
    (
      (JsPath \ "dateTaken").write[String] and
        (JsPath \ "employeeCount").write[Int]
      )(unlift(NumberOfEmployeeCounts.unapply))
  )

  implicit val numberOfEmployeeReferencesFormat = Format(
    (
      (JsPath \ "districtNumber").read[String](pattern(districtNumberPattern, "District number is invalid")) and
        (JsPath \ "payeReference").read[String](pattern(payeRefPattern, "payeReference is invalid")) and
        (JsPath \ "counts").read[Seq[NumberOfEmployeeCounts]]
      )(NumberOfEmployeeReferences.apply _),
    (
      (JsPath \ "districtNumber").write[String] and
        (JsPath \ "payeReference").write[String] and
        (JsPath \ "counts").write[Seq[NumberOfEmployeeCounts]]
      )(unlift(NumberOfEmployeeReferences.unapply))
  )

  implicit val numberOfEmployeeReferencesRequestFormat = Format(
    (
      (JsPath \ "districtNumber").read[String](pattern(districtNumberPattern, "District number is invalid")) and
        (JsPath \ "payeReference").read[String](pattern(payeRefPattern, "payeReference is invalid"))
      )(NumberOfEmployeeReferencesRequest.apply _),
    (
      (JsPath \ "districtNumber").write[String] and
        (JsPath \ "payeReference").write[String]
      )(unlift(NumberOfEmployeeReferencesRequest.unapply))
  )

  implicit val createNumberOfEmployeesRequestFormat = Format(
    (
      (JsPath \ "startDate").read[String](pattern(datePattern, "startDate is invalid")) and
        (JsPath \ "endDate").read[String](pattern(datePattern, "endDate is invalid")) and
        (JsPath \ "references").read[Seq[NumberOfEmployeeReferencesRequest]]
      )(NumberOfEmployeesRequest.apply _),
    (
      (JsPath \ "startDate").write[String] and
        (JsPath \ "endDate").write[String] and
        (JsPath \ "references").write[Seq[NumberOfEmployeeReferencesRequest]]
      )(unlift(NumberOfEmployeesRequest.unapply))
  )

  implicit val numberOfEmployeesResponseFormat = Format(
    (
      (JsPath \ "startDate").read[String](pattern(datePattern, "startDate is invalid")) and
        (JsPath \ "endDate").read[String](pattern(datePattern, "endDate is invalid")) and
        (JsPath \ "references").read[Seq[NumberOfEmployeeReferences]]
      )(NumberOfEmployeesResponse.apply _),
    (
      (JsPath \ "startDate").write[String] and
        (JsPath \ "endDate").write[String] and
        (JsPath \ "references").write[Seq[NumberOfEmployeeReferences]]
      )(unlift(NumberOfEmployeesResponse.unapply))
  )
}

case class NumberOfEmployeesEntry(id: String, response: NumberOfEmployeesResponse)
object NumberOfEmployeesEntry{
  import NumberOfEmployees._
  implicit val numberOfEmployeesFormat = Json.format[NumberOfEmployeesEntry]
}