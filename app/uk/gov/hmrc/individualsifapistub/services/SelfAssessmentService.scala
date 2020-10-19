/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.individualsifapistub.domain.{CreateEmploymentRequest, CreateSelfAssessmentRequest, Employment, SelfAssessment}
import uk.gov.hmrc.individualsifapistub.repository.{EmploymentRepository, SelfAssessmentRepository}

import scala.concurrent.Future

class SelfAssessmentService @Inject()(selfAssessmentRepository: SelfAssessmentRepository) {

  def create( incomeType: String,
              idType: String,
              idValue: String,
              createSelfAssessmentRequest: CreateSelfAssessmentRequest): Future[SelfAssessment] = {
    selfAssessmentRepository.create(s"$incomeType-$idType-$idValue", createSelfAssessmentRequest)
  }

  def get(incomeType: String, idType: String, idValue: String): Future[Option[SelfAssessment]] = {
    selfAssessmentRepository.findById(s"$incomeType-$idType-$idValue")
  }
}
