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
import uk.gov.hmrc.individualsifapistub.domain.organisations.VatReturnsDetails
import uk.gov.hmrc.individualsifapistub.services.organisations.VatReturnsDetailsService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VatReturnDetailsController @Inject() (
  loggingAction: LoggingAction,
  cc: ControllerComponents,
  vatReturnsDetailsService: VatReturnsDetailsService
)(implicit val ec: ExecutionContext)
    extends CommonController(cc) {

  def retrieve(vrn: String, fields: Option[String]): Action[AnyContent] = loggingAction.async { _ =>
    logger.info(s"Retrieving VAT return details for VRN: $vrn and fields: $fields")
    vatReturnsDetailsService.retrieve(vrn).map {
      case Some(entry) => Ok(Json.toJson(entry.vatReturnsDetails))
      case None        => NotFound("NO_VAT_RETURNS_DETAIL_FOUND")
    } recover retrievalRecovery
  }

  def create(vrn: String): Action[JsValue] = loggingAction.async(parse.json) { implicit request =>
    withJsonBody[VatReturnsDetails] { vatReturnDetails =>
      vatReturnsDetailsService
        .create(vrn, vatReturnDetails)
        .map(_ => Created(Json.toJson(vatReturnDetails)))
    } recover recovery
  }

}
