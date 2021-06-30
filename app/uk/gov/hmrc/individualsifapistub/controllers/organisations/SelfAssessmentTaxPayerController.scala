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

package uk.gov.hmrc.individualsifapistub.controllers.organisations

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.SelfAssessmentTaxPayer._
import uk.gov.hmrc.individualsifapistub.domain.organisations.SelfAssessmentTaxPayerResponse
import uk.gov.hmrc.individualsifapistub.services.organisations.SelfAssessmentTaxPayerService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SelfAssessmentTaxPayerController @Inject()(
                                                  cc: ControllerComponents,
                                                       selfAssessmentTaxPayerService: SelfAssessmentTaxPayerService)
                                                     (implicit val ec: ExecutionContext)
  extends CommonController(cc) {

  val emptyResponse = SelfAssessmentTaxPayerResponse("", "" , Seq.empty)

  def retrieve(utr: String): Action[AnyContent] = Action.async { implicit request =>
    selfAssessmentTaxPayerService.get(utr).map {
      case Some(response) => Ok(Json.toJson(response))
      case None => Ok(Json.toJson(emptyResponse))
    } recover recovery
  }
}
