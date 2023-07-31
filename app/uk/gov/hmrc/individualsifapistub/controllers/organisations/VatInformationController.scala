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

import play.api.libs.json.Json
import play.api.mvc.{ ControllerComponents, PlayBodyParsers }
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.VatInformationSimplified
import uk.gov.hmrc.individualsifapistub.services.organisations.VatInformationService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VatInformationController @Inject()(
                                          loggingAction: LoggingAction,
                                          bodyParsers: PlayBodyParsers,
                                          cc: ControllerComponents,
                                          vatInformationService: VatInformationService
                                        )(implicit val ec: ExecutionContext) extends CommonController(cc) {
  def retrieve(vrn: String, fields: Option[String]) = loggingAction.async { _ =>
    logger.info(s"Retrieving VAT information for VRN: $vrn and fields: $fields")
    vatInformationService.retrieve(vrn).map {
      case Some(entry) => Ok(Json.toJson(entry.vatInformation))
      case None => NotFound("NO_DATA_FOUND")
    } recover retrievalRecovery
  }

  def create(vrn: String) = loggingAction.async(bodyParsers.json) { implicit request =>
    withJsonBody[VatInformationSimplified] { vatInformationSimplified =>
      vatInformationService
        .create(vrn, vatInformationSimplified.toVatInformation)
        .map(entry => Created(Json.toJson(VatInformationSimplified.fromVatInformation(entry.vatInformation))))
    } recover recovery
  }
}
