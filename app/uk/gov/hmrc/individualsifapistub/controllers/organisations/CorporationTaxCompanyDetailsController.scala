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

import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, PlayBodyParsers}
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxCompanyDetails._
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxCompanyDetails
import uk.gov.hmrc.individualsifapistub.services.organisations.CorporationTaxCompanyDetailsService

import scala.concurrent.ExecutionContext

class CorporationTaxCompanyDetailsController @Inject()(
                                                       bodyParsers: PlayBodyParsers,
                                                       cc: ControllerComponents,
                                                       corporationTaxCompanyDetailsService: CorporationTaxCompanyDetailsService,
                                                       testUserConnector: ApiPlatformTestUserConnector)
                                                      (implicit val ec: ExecutionContext)
  extends CommonController(cc) {

  def create(crn: String): Action[JsValue] = {
    Action.async(bodyParsers.json) { implicit request =>
      withJsonBody[CorporationTaxCompanyDetails] { body =>
        corporationTaxCompanyDetailsService.create(body).map(
          x => Created(Json.toJson(x))
        ) recover recovery
      }
    }
  }

  def retrieve(crn: String): Action[AnyContent] = Action.async { implicit request =>
    testUserConnector.getOrganisationByCrn(crn).map {
      case Some(response) => Ok(Json.toJson(CorporationTaxCompanyDetails.fromApiPlatformTestUser(response)))
      case None => NotFound
    } recover retrievalRecovery
  }
}
