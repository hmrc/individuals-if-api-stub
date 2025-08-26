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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxCompanyDetails
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxCompanyDetails.*

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CorporationTaxCompanyDetailsController @Inject() (
  loggingAction: LoggingAction,
  cc: ControllerComponents,
  testUserConnector: ApiPlatformTestUserConnector
)(implicit val ec: ExecutionContext)
    extends CommonController(cc) {

  def retrieve(crn: String): Action[AnyContent] = loggingAction.async { implicit request =>
    testUserConnector.getOrganisationByCrn(crn).map {
      case Some(response) => Ok(Json.toJson(CorporationTaxCompanyDetails.fromApiPlatformTestUser(response)))
      case None           => NotFound
    } recover retrievalRecovery
  }
}
