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
import uk.gov.hmrc.individualsifapistub.domain.{IncomePaye, IncomeSa}
import uk.gov.hmrc.individualsifapistub.repository.{IncomePayeRepository, IncomeSaRepository}

import scala.concurrent.Future

class IncomeService @Inject()(incomeSaRepository: IncomeSaRepository, incomePayeRepository: IncomePayeRepository) {

  def createSa(idType: String,
               idValue: String,
               startYear: String,
               endYear: String,
               consumer: String,
               createSelfAssessmentRequest: IncomeSa): Future[IncomeSa] = {
    incomeSaRepository.create(idType, idValue, startYear, endYear, consumer, createSelfAssessmentRequest)
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
                 consumer: String,
                 createIncomePayeRequest: IncomePaye): Future[IncomePaye] = {
    incomePayeRepository.create(idType, idValue, startDate, endDate, consumer, createIncomePayeRequest)
  }

  def getPaye(idType: String,
              idValue: String,
              startDate: String,
              endDate: String,
              fields: Option[String]): Future[Option[IncomePaye]] = {
    incomePayeRepository.findByTypeAndId(idType, idValue, startDate, endDate, fields)
  }
}
