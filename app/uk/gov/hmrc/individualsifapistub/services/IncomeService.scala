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

package uk.gov.hmrc.individualsifapistub.services

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain.{IdType, IncomePaye, IncomeSa, RecordNotFoundException}
import uk.gov.hmrc.individualsifapistub.repository.{IncomePayeRepository, IncomeSaRepository}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class IncomeService @Inject()(incomeSaRepository: IncomeSaRepository,
                              incomePayeRepository: IncomePayeRepository,
                              apiPlatformTestUserConnector: ApiPlatformTestUserConnector,
                              servicesConfig: ServicesConfig) {

  def createSa(idType: String,
               idValue: String,
               startYear: String,
               endYear: String,
               useCase: String,
               createSelfAssessmentRequest: IncomeSa)
              (implicit ec: ExecutionContext,
               hc: HeaderCarrier): Future[IncomeSa] = {

    if (servicesConfig.getConfBool("verifyNino", true)) verifyNino(idType, idValue)
    incomeSaRepository.create(idType, idValue, startYear, endYear, useCase, createSelfAssessmentRequest)
  }

  def getSa(idType: String,
            idValue: String,
            startYear: String,
            endYear: String,
            fields: Option[String]
           ): Future[Option[IncomeSa]] = {
    incomeSaRepository.findByTypeAndId(idType, idValue, startYear, endYear, fields)
  }

  def createPaye(idType: String,
                 idValue: String,
                 startDate: String,
                 endDate: String,
                 useCase: String,
                 createIncomePayeRequest: IncomePaye)
                (implicit ec: ExecutionContext,
                 hc: HeaderCarrier): Future[IncomePaye] = {

    if (servicesConfig.getConfBool("verifyNino", true)) verifyNino(idType, idValue)
    incomePayeRepository.create(idType, idValue, startDate, endDate, useCase, createIncomePayeRequest)
  }

  def getPaye(idType: String,
              idValue: String,
              startDate: String,
              endDate: String,
              fields: Option[String]): Future[Option[IncomePaye]] = {
    incomePayeRepository.findByTypeAndId(idType, idValue, startDate, endDate, fields)
  }

  def verifyNino(idType: String, idValue: String)
                (implicit ec: ExecutionContext,
                 hc: HeaderCarrier) = {
    IdType.parse(idType) match {
      case IdType.Nino => {
        for {
          individual <- apiPlatformTestUserConnector.getIndividualByNino(Nino(idValue))
          utr = individual.saUtr.getOrElse(throw new RecordNotFoundException)
        } yield utr
      }
      case _ => throw new BadRequestException("Invalid National Insurance Number")
    }
  }
}
