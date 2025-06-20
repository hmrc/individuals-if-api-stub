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

package uk.gov.hmrc.individualsifapistub.controllers.individuals

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.individuals.Applications
import uk.gov.hmrc.individualsifapistub.services.individuals.TaxCreditsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TaxCreditsController @Inject() (
  loggingAction: LoggingAction,
  cc: ControllerComponents,
  taxCreditsService: TaxCreditsService
)(implicit val ec: ExecutionContext)
    extends CommonController(cc) {

  def create(idType: String, idValue: String, startDate: String, endDate: String, useCase: String): Action[JsValue] =
    loggingAction.async(parse.json) { implicit request =>
      withJsonBodyAndValidId[Applications](idType, idValue, Some(startDate), Some(endDate), Some(useCase)) {
        applications =>
          taxCreditsService.create(idType, idValue, startDate, endDate, useCase, applications) map (e =>
            Created(Json.toJson(e))
          )
      } recover recovery
    }

  def retrieve(
    idType: String,
    idValue: String,
    startDate: String,
    endDate: String,
    fields: Option[String]
  ): Action[AnyContent] = loggingAction.async { _ =>
    taxCreditsService.get(idType, idValue, startDate, endDate, fields) map {
      case Some(value) => Ok(Json.toJson(value))
      case None        => Ok(Json.toJson(Applications(Seq.empty)))
    } recover recovery
  }
}
