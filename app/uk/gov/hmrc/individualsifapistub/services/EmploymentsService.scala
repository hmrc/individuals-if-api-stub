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
import uk.gov.hmrc.individualsifapistub.domain.{CreateEmploymentRequest, Employment}
import uk.gov.hmrc.individualsifapistub.repository.EmploymentRepository

import scala.concurrent.Future

class EmploymentsService @Inject()(employmentsRepository: EmploymentRepository) {

  def create( idType: String,
              idValue: String,
              createEmploymentRequest: CreateEmploymentRequest): Future[Seq[Employment]] = {
    employmentsRepository.create(s"$idType-$idValue", createEmploymentRequest)
  }

  def get(idType: String, idValue: String): Future[Option[Seq[Employment]]] = {
    employmentsRepository.findById(s"$idType-$idValue")
  }
}
