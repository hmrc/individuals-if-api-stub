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

package uk.gov.hmrc.individualsifapistub.controllers.organisations

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.CreateSelfAssessmentReturnDetailRequest
import uk.gov.hmrc.individualsifapistub.services.organisations.SelfAssessmentReturnDetailService
import uk.gov.hmrc.individualsifapistub.util.FieldFilter

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SelfAssessmentReturnDetailController @Inject() (
  loggingAction: LoggingAction,
  cc: ControllerComponents,
  selfAssessmentReturnDetailService: SelfAssessmentReturnDetailService
)(implicit val ec: ExecutionContext)
    extends CommonController(cc) {

  def create(utr: String): Action[JsValue] =
    loggingAction.async(parse.json) { implicit request =>
      withJsonBody[CreateSelfAssessmentReturnDetailRequest] { body =>
        selfAssessmentReturnDetailService
          .create(body)
          .map(x => Created(Json.toJson(x))) recover recovery
      }
    }

  def retrieve(utr: String, fields: Option[String] = None): Action[AnyContent] = loggingAction.async { _ =>
    selfAssessmentReturnDetailService
      .get(utr)
      .map {
        case Some(response) => Ok(FieldFilter.toFilteredJson(response, fields))
        case None           => NotFound
      }
      .recover(recovery)
  }

}
