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

package uk.gov.hmrc.individualsifapistub.services.individuals

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain.individuals.Applications
import uk.gov.hmrc.individualsifapistub.repository.individuals.TaxCreditsRepository
import uk.gov.hmrc.individualsifapistub.services.ServiceBase
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxCreditsService @Inject() (
  repository: TaxCreditsRepository,
  apiPlatformTestUserConnector: ApiPlatformTestUserConnector,
  servicesConfig: ServicesConfig
) extends ServiceBase(apiPlatformTestUserConnector) {

  def create(
    idType: String,
    idValue: String,
    startDate: String,
    endDate: String,
    useCase: String,
    applications: Applications
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Applications] =
    if (servicesConfig.getConfBool("verifyNino", true)) {
      verifyNino(idType, idValue) flatMap { _ =>
        repository.create(idType, idValue, startDate, endDate, useCase, applications)
      }
    } else
      repository.create(idType, idValue, startDate, endDate, useCase, applications)

  def get(
    idType: String,
    idValue: String,
    startDate: String,
    endDate: String,
    fields: Option[String]
  ): Future[Option[Applications]] =
    repository.findByIdAndType(idType, idValue, startDate, endDate, fields)
}
