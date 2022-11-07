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

package uk.gov.hmrc.individualsifapistub.controllers.individuals

import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, AnyContent, ControllerComponents, PlayBodyParsers }
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.individuals.Employments
import uk.gov.hmrc.individualsifapistub.domain.individuals.Employments._
import uk.gov.hmrc.individualsifapistub.services.individuals.EmploymentsService
import uk.gov.hmrc.individualsifapistub.util.FieldFilter

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmploymentsController @Inject()(loggingAction: LoggingAction,
                                      bodyParser: PlayBodyParsers,
                                      cc: ControllerComponents,
                                      employmentsService: EmploymentsService)
                                     (implicit val ec: ExecutionContext) extends CommonController(cc) {


  def create(idType: String,
             idValue: String,
             startDate: Option[String],
             endDate: Option[String],
             useCase: Option[String]): Action[JsValue] = {
    loggingAction.async(bodyParser.json) { implicit request =>

      withJsonBodyAndValidId[Employments](idType, idValue, startDate, endDate, useCase) {
        jsonBody =>
          employmentsService.create(
            idType,
            idValue,
            startDate,
            endDate,
            useCase,
            jsonBody
          ) map (e => Created(Json.toJson(e)))
      } recover recovery
    }
  }

  def retrieve(idType: String,
               idValue: String,
               startDate: String,
               endDate: String,
               fields: Option[String],
               filter: Option[String]): Action[AnyContent] = loggingAction.async { implicit request =>
    employmentsService
      .get(idType, idValue, startDate, endDate, fields, filter)
      .map {
        case Some(value) => value
        case None => Employments(Seq.empty)
      }
      .map { employments =>
        Ok(FieldFilter.toFilteredJson(employments, fields))
      }
      .recover(recovery)
  }
}
