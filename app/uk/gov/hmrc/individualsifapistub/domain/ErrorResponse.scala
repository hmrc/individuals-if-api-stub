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

package uk.gov.hmrc.individualsifapistub.domain

import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.{Result, Results}

sealed abstract class ErrorResponse(val httpStatusCode: Int, val errorCode: String, val message: String) {

  def toHttpResponse: Result = Results.Status(httpStatusCode)(Json.toJson(this))
}

object ErrorResponse {
  implicit val writes: Writes[ErrorResponse] = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}

case class ErrorInvalidRequest(errorMessage: String) extends ErrorResponse(BAD_REQUEST, "INVALID_REQUEST", errorMessage)

object ErrorInvalidRequest {
  implicit val format: Format[ErrorInvalidRequest] = new Format[ErrorInvalidRequest] {
    def reads(json: JsValue): JsResult[ErrorInvalidRequest] = JsSuccess(
      ErrorInvalidRequest((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidRequest): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }
}

class ValidationException(message: String) extends RuntimeException(message)

class DuplicateException extends RuntimeException

case class RecordNotFoundException(errorMessage: String = "Record not found") extends RuntimeException

case object ErrorInternalServer
    extends ErrorResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error")

case object ErrorDuplicate extends ErrorResponse(CONFLICT, "ALREADY_EXISTS", "A record already exists for this id")

case class RecordNotFound(errorMessage: String) extends ErrorResponse(NOT_FOUND, "NOT_FOUND", errorMessage)
