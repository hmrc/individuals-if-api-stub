/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.individualsifapistub.services.organisations

import uk.gov.hmrc.individualsifapistub.domain.organisations.{NumberOfEmployeesRequest, NumberOfEmployeesResponse}
import uk.gov.hmrc.individualsifapistub.repository.organisations.NumberOfEmployeesRepository

import javax.inject.Inject
import scala.concurrent.Future

class NumberOfEmployeesService @Inject()(repository: NumberOfEmployeesRepository) {
  def create(request: NumberOfEmployeesResponse): Future[NumberOfEmployeesResponse] = repository.create(request)

  def get(request : NumberOfEmployeesRequest): Future[Option[NumberOfEmployeesResponse]] = repository.find(request)
}
