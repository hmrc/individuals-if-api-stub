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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain.{CreateDetailsRequest, DetailsResponse, DetailsResponseNoId}
import uk.gov.hmrc.individualsifapistub.repository.DetailsRepository
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class DetailsService @Inject()(detailsRepository: DetailsRepository,
                               apiPlatformTestUserConnector: ApiPlatformTestUserConnector,
                               servicesConfig: ServicesConfig) extends ServiceBase(apiPlatformTestUserConnector) {

  def create(idType: String,
             idValue:String,
             useCase: String,
             createDetailsRequest: CreateDetailsRequest)
            (implicit ec: ExecutionContext,
             hc: HeaderCarrier) : Future[DetailsResponseNoId] = {

    if (servicesConfig.getConfBool("verifyNino", true)) verifyNino(idType, idValue)
    detailsRepository.create(idType, idValue, useCase, createDetailsRequest)
  }

  def get(idType: String,
          idValue:String,
          fields: Option[String]): Future[Option[DetailsResponse]] = {
    detailsRepository.findByIdAndType(idType, idValue, fields)
  }
}
