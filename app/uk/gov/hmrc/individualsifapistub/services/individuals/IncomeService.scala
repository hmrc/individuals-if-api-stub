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
import uk.gov.hmrc.individualsifapistub.domain.individuals.{IncomePaye, IncomeSa}
import uk.gov.hmrc.individualsifapistub.repository.individuals.{IncomePayeRepository, IncomeSaRepository}
import uk.gov.hmrc.individualsifapistub.services.ServiceBase
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncomeService @Inject() (
  incomeSaRepository: IncomeSaRepository,
  incomePayeRepository: IncomePayeRepository,
  apiPlatformTestUserConnector: ApiPlatformTestUserConnector,
  servicesConfig: ServicesConfig
) extends ServiceBase(apiPlatformTestUserConnector) {

  def createSa(
    idType: String,
    idValue: String,
    startYear: Option[String],
    endYear: Option[String],
    useCase: Option[String],
    createSelfAssessmentRequest: IncomeSa
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IncomeSa] =
    if (servicesConfig.getConfBool("verifyNino", true)) {
      verifyNino(idType, idValue) flatMap { _ =>
        incomeSaRepository.create(idType, idValue, startYear, endYear, useCase, createSelfAssessmentRequest)
      }
    } else
      incomeSaRepository.create(idType, idValue, startYear, endYear, useCase, createSelfAssessmentRequest)

  def getSa(
    idType: String,
    idValue: String,
    startYear: String,
    endYear: String,
    fields: Option[String]
  ): Future[Option[IncomeSa]] =
    incomeSaRepository.findByTypeAndId(idType, idValue, startYear, endYear, fields)

  def createPaye(
    idType: String,
    idValue: String,
    startDate: Option[String],
    endDate: Option[String],
    useCase: Option[String],
    createIncomePayeRequest: IncomePaye
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IncomePaye] =
    if (servicesConfig.getConfBool("verifyNino", true)) {
      verifyNino(idType, idValue) flatMap { _ =>
        incomePayeRepository.create(idType, idValue, startDate, endDate, useCase, createIncomePayeRequest)
      }
    } else
      incomePayeRepository.create(idType, idValue, startDate, endDate, useCase, createIncomePayeRequest)

  def getPaye(
    idType: String,
    idValue: String,
    startDate: String,
    endDate: String,
    fields: Option[String]
  ): Future[Option[IncomePaye]] =
    incomePayeRepository.findByTypeAndId(idType, idValue, startDate, endDate, fields)
}
