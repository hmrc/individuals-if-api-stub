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

  def createSa(idType: String, idValue: String, createSelfAssessmentRequest: IncomeSa): Future[IncomeSa] = {
    incomeSaRepository.create(idType, idValue, createSelfAssessmentRequest)
  }

  def getSa(idType: String, idValue: String): Future[Option[IncomeSa]] = {
    incomeSaRepository.findByTypeAndId(idType, idValue)
  }

  def createPaye(idType: String, idValue: String, createIncomePayeRequest: IncomePaye): Future[IncomePaye] = {
    incomePayeRepository.create(idType, idValue, createIncomePayeRequest)
  }

  def getPaye(idType: String, idValue: String): Future[Option[IncomePaye]] = {
    incomePayeRepository.findByTypeAndId(idType, idValue)
  }
}
