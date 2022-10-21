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

package uk.gov.hmrc.individualsifapistub.controllers.organisations

import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents, PlayBodyParsers }
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.{ NumberOfEmployeesRequest, NumberOfEmployeesResponse }
import uk.gov.hmrc.individualsifapistub.domain.organisations.NumberOfEmployees._
import uk.gov.hmrc.individualsifapistub.services.organisations.NumberOfEmployeesService
import uk.gov.hmrc.individualsifapistub.util.FieldFilter

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NumberOfEmployeesController @Inject()(
                                             loggingAction: LoggingAction,
                                             bodyParsers: PlayBodyParsers,
                                             cc: ControllerComponents,
                                             numberOfEmployeesService: NumberOfEmployeesService)
                                           (implicit val ec: ExecutionContext)
  extends CommonController(cc) {

  val emptyResponse = NumberOfEmployeesResponse("", "", Seq.empty)

  def create(): Action[JsValue] =
    loggingAction.async(bodyParsers.json) { implicit request =>
      withJsonBody[NumberOfEmployeesResponse] { body =>
        numberOfEmployeesService.create(body).map(
          x => Created(Json.toJson(x))
        ) recover recovery
      }
    }

  def retrieve(fields: Option[String] = None): Action[JsValue] = loggingAction.async(bodyParsers.json) { implicit request =>
    withJsonBody[NumberOfEmployeesRequest] { body =>
      numberOfEmployeesService.get(body)
        .map {
          case Some(response) => response
          case None => emptyResponse
        }
        .map(response => Ok(FieldFilter.toFilteredJson(response, fields)))
        .recover(recovery)
    }
  }

}
