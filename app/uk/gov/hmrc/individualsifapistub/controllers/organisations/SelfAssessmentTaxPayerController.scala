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
import play.api.mvc.{Action, AnyContent, ControllerComponents, PlayBodyParsers}
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.SelfAssessmentTaxPayer._
import uk.gov.hmrc.individualsifapistub.domain.organisations.SelfAssessmentTaxPayer
import uk.gov.hmrc.individualsifapistub.services.organisations.SelfAssessmentTaxPayerService

import javax.inject.Inject
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector

import scala.concurrent.ExecutionContext

class SelfAssessmentTaxPayerController @Inject()(
                                                       loggingAction: LoggingAction,
                                                       bodyParsers: PlayBodyParsers,
                                                       cc: ControllerComponents,
                                                       selfAssessmentTaxPayerService: SelfAssessmentTaxPayerService,
                                                       testUserConnector: ApiPlatformTestUserConnector)
                                                     (implicit val ec: ExecutionContext)
  extends CommonController(cc) {

  def create(utr: String): Action[JsValue] = {
    loggingAction.async(bodyParsers.json) { implicit request =>
      withJsonBody[SelfAssessmentTaxPayer] { body =>
        selfAssessmentTaxPayerService.create(body).map(
          x => Created(Json.toJson(x))
        ) recover recovery
      }
    }
  }

  def retrieve(utr: String): Action[AnyContent] = loggingAction.async { implicit request =>
    testUserConnector.getOrganisationBySaUtr(utr).map {
      case Some(response) => Ok(Json.toJson(SelfAssessmentTaxPayer.fromApiPlatformTestUser(response)))
      case None => NotFound
    } recover retrievalRecovery
  }
}
