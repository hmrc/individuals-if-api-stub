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

import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.util.matching.Regex

case class Identifier(nino: Option[String],
                      trn: Option[String],
                      from: Option[String],
                      to: Option[String],
                      useCase: Option[String]
                     )

object Identifier {

  val ninoPattern: Regex =
    "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D\\s]?$".r

  val trnPattern: Regex = "^[0-9]{8}$".r

  implicit val idFormat: Format[Identifier] = Format(
    (
      (JsPath \ "nino").readNullable[String](pattern(ninoPattern, "InvalidNino")) and
      (JsPath \ "trn").readNullable[String](pattern(trnPattern, "InvalidTrn")) and
      (JsPath \ "from").readNullable[String] and
      (JsPath \ "to").readNullable[String] and
      (JsPath \ "useCase").readNullable[String]
    )(Identifier.apply _),
    (
      (JsPath \ "nino").writeNullable[String] and
      (JsPath \ "trn").writeNullable[String] and
      (JsPath \ "from").writeNullable[String] and
      (JsPath \ "to").writeNullable[String] and
      (JsPath \ "useCase").writeNullable[String]
    )(unlift(Identifier.unapply))
  )
}


