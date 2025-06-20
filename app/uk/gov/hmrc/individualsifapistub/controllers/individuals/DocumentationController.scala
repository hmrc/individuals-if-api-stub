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

import controllers.Assets
import org.apache.pekko.stream.Materializer
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.filters.cors.CORSActionBuilder
import uk.gov.hmrc.individualsifapistub.config.LoggingAction
import uk.gov.hmrc.individualsifapistub.views._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DocumentationController @Inject() (
  loggingAction: LoggingAction,
  configuration: Configuration,
  cc: ControllerComponents,
  assets: Assets
)(implicit materializer: Materializer, executionContext: ExecutionContext)
    extends BackendController(cc) {

  private lazy val whitelistedApplicationIds = configuration
    .getOptional[Seq[String]]("api.access.version-1.0.whitelistedApplicationIds")
    .getOrElse(Seq.empty)

  private lazy val endpointsEnabled: Boolean = configuration
    .getOptional[Boolean]("api.access.version-1.0.endpointsEnabled")
    .getOrElse(true)

  private lazy val status: String = configuration
    .getOptional[String]("api.access.version-1.0.status")
    .getOrElse("BETA")

  def definition(): Action[AnyContent] = loggingAction {
    Ok(txt.definition(whitelistedApplicationIds, endpointsEnabled, status)).withHeaders(CONTENT_TYPE -> JSON)
  }

  def documentation(version: String, endpointName: String): Action[AnyContent] =
    assets.at(s"/public/api/documentation/$version", s"${endpointName.replaceAll(" ", "-")}.xml")

  def yaml(version: String, file: String): Action[AnyContent] =
    CORSActionBuilder(configuration).async { implicit request =>
      assets.at(s"/public/api/conf/$version", file)(request)
    }
}
