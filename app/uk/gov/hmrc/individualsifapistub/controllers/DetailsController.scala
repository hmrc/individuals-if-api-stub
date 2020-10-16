/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{BodyParsers, ControllerComponents}
import uk.gov.hmrc.individualsifapistub.domain.CreateDetailsRequest
import uk.gov.hmrc.individualsifapistub.services.DetailsService
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._

import scala.concurrent.ExecutionContext

@Singleton
class DetailsController @Inject()(cc: ControllerComponents, detailsService: DetailsService)(implicit val ec: ExecutionContext) extends CommonController(cc) {
  def create(idType: String, idValue: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    withJsonBody[CreateDetailsRequest] { createRequest =>
      detailsService.create(idType, idValue, createRequest) map (e => Created(Json.toJson(e)))
    } recover recovery
  }

  def retrieve(idType: String, idValue: String) = Action.async { implicit request =>
    detailsService.get(idType, idValue) map { detailsOption =>
      detailsOption match {
        case Some(value) => Ok(Json.toJson(value))
        case None => NotFound
      }
    } recover recovery
  }
}
