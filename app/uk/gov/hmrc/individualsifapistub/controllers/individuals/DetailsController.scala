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
import uk.gov.hmrc.individualsifapistub.domain.individuals.CreateDetailsRequest
import uk.gov.hmrc.individualsifapistub.services.individuals.DetailsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DetailsController @Inject() (
  loggingAction: LoggingAction,
  cc: ControllerComponents,
  detailsService: DetailsService
)(implicit val ec: ExecutionContext)
    extends CommonController(cc) {

  def create(idType: String, idValue: String, useCase: String): Action[JsValue] =
    loggingAction.async(parse.json) { implicit request =>
      withJsonBodyAndValidId[CreateDetailsRequest](idType, idValue, None, None, Some(useCase)) { createRequest =>
        detailsService.create(idType, idValue, useCase, createRequest) map (e => Created(Json.toJson(e)))
      } recover recovery
    }

  def retrieve(idType: String, idValue: String, fields: Option[String]): Action[AnyContent] = loggingAction.async { _ =>
    detailsService.get(idType, idValue, fields) map {
      case Some(value) => Ok(Json.toJson(value))
      case None =>
        Ok(Json.parse("""{
                        |"residences": [],
                        |"contactDetails": []
                        |}""".stripMargin))
    } recover recovery
  }
}
