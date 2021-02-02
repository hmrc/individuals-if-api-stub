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
import uk.gov.hmrc.individualsifapistub.domain.{Employments, IdType, RecordNotFoundException}
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class EmploymentsService @Inject()(employmentsRepository: EmploymentRepository,
                                   val apiPlatformTestUserConnector: ApiPlatformTestUserConnector,
                                   servicesConfig: ServicesConfig) {

  def create(idType: String,
             idValue: String,
             startDate: String,
             endDate: String,
             useCase: String,
             employments: Employments)
            (implicit ec: ExecutionContext,
             hc: HeaderCarrier): Future[Employments] = {

    if (servicesConfig.getConfBool("verifyNino", true)) verifyNino(idType, idValue)
    employmentsRepository.create(idType, idValue, startDate, endDate, useCase, employments)
  }

  def get(idType: String,
          idValue: String,
          startDate: String,
          endDate: String,
          fields: Option[String]): Future[Option[Employments]] = {
    employmentsRepository.findByIdAndType(idType, idValue, startDate, endDate, fields)
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
