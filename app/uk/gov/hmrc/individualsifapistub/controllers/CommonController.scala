/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.individualsifapistub.controllers

import play.api.{Configuration, Logger}
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.libs.json._
import play.api.mvc.Results.{BadRequest, NotFound, Status}
import play.api.mvc.{ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals.Identifier._
import uk.gov.hmrc.individualsifapistub.domain.individuals.{IdType, Identifier}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.{ErrorResponse, JsonErrorHandler}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent
import javax.inject.Inject
import uk.gov.hmrc.http.NotFoundException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class CustomErrorHandler @Inject()( configuration: Configuration,
                                    auditConnector: AuditConnector,
                                    httpAuditEvent: HttpAuditEvent )
                                  ( implicit ec: ExecutionContext ) extends JsonErrorHandler( auditConnector,
                                                                                              httpAuditEvent,
                                                                                              configuration ) {


  override def onClientError( request: RequestHeader,
                              statusCode: Int,
                              message: String ): Future[Result] = {

    val newMessage = Try {
      Json.parse(message).\\("message").mkString(",").replaceAll("\"", "")
    } match {
      case Success(value) => value
      case Failure(_)     => "Invalid Request"
    }

    statusCode match {
      case NOT_FOUND =>
        Future.successful(
          NotFound(
            Json.toJson(
              ErrorResponse(NOT_FOUND,
                            "URI not found",
                            requested = Some(request.path)))))
      case BAD_REQUEST =>
        Future.successful(
          BadRequest(Json.toJson(ErrorResponse(BAD_REQUEST, newMessage))))
      case _ =>
        Future.successful(
          Status(statusCode)(
            Json.toJson(ErrorResponse(statusCode, newMessage))))
    }
  }
}

abstract class CommonController(controllerComponents: ControllerComponents)
    extends BackendController(controllerComponents) {

  protected val logger: Logger = play.api.Logger(this.getClass)

  override protected def withJsonBody[T](f: (T) => Future[Result])(
      implicit request: Request[JsValue],
      m: Manifest[T],
      reads: Reads[T]): Future[Result] = {
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) =>
        Future.successful(
          ErrorInvalidRequest(s"${fieldName(errs)} is required").toHttpResponse)
      case Failure(e) if e.isInstanceOf[ValidationException] =>
        Future.successful(ErrorInvalidRequest(e.getMessage).toHttpResponse)
      case Failure(_) =>
        Future.successful(
          ErrorInvalidRequest("Unable to process request").toHttpResponse)
    }
  }

  protected def withJsonBodyAndValidId[T](idType: String,
                                          id: String,
                                          from: Option[String],
                                          to: Option[String],
                                          useCase:
                                          Option[String])
                                         (f: (T) => Future[Result])(
    implicit request: Request[JsValue],
    m: Manifest[T],
    reads: Reads[T]): Future[Result] = {
    Try(IdType.parse(idType)) match {
      case Failure(e) => Future.successful(ErrorInvalidRequest(e.getLocalizedMessage).toHttpResponse)
      case Success(idType) =>
        (idType match {
          case Nino => Json.toJson(Identifier(Some(id), None, from, to, useCase))
          case Trn  => Json.toJson(Identifier(None, Some(id), from, to, useCase))
        }).validate[Identifier] match {
          case JsError(errs) => Future.successful(ErrorInvalidRequest(s"${errs.head._1.toString()} is invalid").toHttpResponse)
          case JsSuccess(_, _) => withJsonBody(f)
        }
    }
  }

  private def fieldName[T](errs: Seq[(JsPath, Seq[JsonValidationError])]) = {
    val e = errs.head._1.toString()
    if(!e.isEmpty)
      e.substring(1)
    else
      e
  }

  private[controllers] def recovery: PartialFunction[Throwable, Result] = {
    case e: IllegalArgumentException =>
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    case _: DuplicateException =>
      ErrorDuplicate.toHttpResponse
  }

  private[controllers] def retrievalRecovery: PartialFunction[Throwable, Result] = {
    case e: IllegalArgumentException =>
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    case e: Exception =>
      logger.error(s"Something went wrong: ${e.getMessage}")
      ErrorInternalServer.toHttpResponse
  }
}