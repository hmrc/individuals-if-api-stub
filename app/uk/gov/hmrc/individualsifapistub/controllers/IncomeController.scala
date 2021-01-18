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

import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, PlayBodyParsers}
import uk.gov.hmrc.individualsifapistub.domain.{IncomePaye, IncomeSa}
import uk.gov.hmrc.individualsifapistub.services.IncomeService
import uk.gov.hmrc.individualsifapistub.domain.IncomePaye._
import uk.gov.hmrc.individualsifapistub.domain.IncomeSa._

import scala.concurrent.ExecutionContext

class IncomeController @Inject()( bodyParser: PlayBodyParsers,
                                  cc: ControllerComponents,
                                  incomeService: IncomeService
                                ) (implicit val ec: ExecutionContext) extends CommonController(cc) {

  def createSa(idType: String,
               idValue: String,
               startYear: String,
               endYear: String,
               consumer: String): Action[JsValue] = Action.async(bodyParser.json) { implicit request =>
    withJsonBody[IncomeSa] { createRequest =>
      incomeService.createSa(idType, idValue, createRequest) map (e => Created(Json.toJson(e)))
    } recover recovery
  }

  def retrieveSa(idType: String,
                 idValue: String,
                 startYear: String,
                 endYear: String,
                 fields: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    incomeService.getSa(idType, idValue) map {
      case Some(value) => Ok(Json.toJson(value))
      case None => NotFound
    } recover recovery
  }

  def createPaye(idType: String,
                 idValue: String,
                 startDate: String,
                 endDate: String,
                 consumer: String): Action[JsValue] = Action.async(bodyParser.json) { implicit request =>
    withJsonBody[IncomePaye] { createRequest =>
      incomeService.createPaye(idType, idValue, createRequest) map (e => Created(Json.toJson(e)))
    } recover recovery
  }

  def retrievePaye(idType: String,
                   idValue: String,
                   startDate: String,
                   endDate: String,
                   fields: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    incomeService.getPaye(idType, idValue) map {
      case Some(value) => Ok(Json.toJson(value))
      case None => NotFound
    } recover recovery
  }

}
