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

package uk.gov.hmrc.individualsifapistub.util

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

import scala.annotation.tailrec

object FieldFilter {
  def filterFields(json: JsValue, fieldsStr: String): JsValue = {
    val fields = extractFields(fieldsStr, "", List.empty)
    filterFieldsInternal(fields, json)
  }

  def toFilteredJson[A: Writes](a: A, fields: Option[String]): JsValue = {
    val json = Json.toJson(a)
    fields.map(filterFields(json, _)).getOrElse(json)
  }

  private sealed trait Field
  private case class SingleField(name: String) extends Field
  private case class FieldGroup(name: String, fields: List[Field]) extends Field

  @tailrec
  private def getClosingParenthesis(str: String, i: Int = 0, innerScopeCount: Int = 0): Int =
    if (i >= str.length) -1
    else
      str(i) match {
        case '('                        => getClosingParenthesis(str, i + 1, innerScopeCount + 1)
        case ')' if innerScopeCount > 0 => getClosingParenthesis(str, i + 1, innerScopeCount - 1)
        case ')'                        => i
        case _                          => getClosingParenthesis(str, i + 1, innerScopeCount)
      }

  private def extractFields(str: String, fieldNameAcc: String, fieldsAcc: List[Field]): List[Field] =
    str.headOption match {
      case Some('(') =>
        val closingIndex = getClosingParenthesis(str.tail)
        val subfields = extractFields(str.tail.take(closingIndex), "", List.empty)
        val field = FieldGroup(fieldNameAcc, subfields)
        extractFields(str.drop(closingIndex + 2), "", field :: fieldsAcc)
      case Some(')') =>
        fieldsAcc.reverse
      case Some(',') if fieldNameAcc.nonEmpty =>
        extractFields(str.tail, "", SingleField(fieldNameAcc) :: fieldsAcc)
      case Some(',') =>
        extractFields(str.tail, "", fieldsAcc)
      case Some(x) =>
        extractFields(str.tail, fieldNameAcc + x, fieldsAcc)
      case None if fieldNameAcc.nonEmpty =>
        (SingleField(fieldNameAcc) :: fieldsAcc).reverse
      case None =>
        fieldsAcc.reverse
    }

  private def filterFieldsInternal(wantedFields: List[Field], json: JsValue): JsValue =
    json match {
      case JsArray(values) => JsArray(values.map(filterFieldsInternal(wantedFields, _)))
      case _ =>
        val filteredFields = wantedFields.flatMap {
          case SingleField(fieldName) =>
            json \ fieldName match {
              case _: JsUndefined   => None
              case JsDefined(value) => Some(fieldName -> (value: JsValueWrapper))
            }
          case FieldGroup(fieldName, children) =>
            json \ fieldName match {
              case _: JsUndefined   => None
              case JsDefined(value) => Some(fieldName -> (filterFieldsInternal(children, value): JsValueWrapper))
            }
        }
        Json.obj(filteredFields*)
    }
}
