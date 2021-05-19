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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType

import javax.inject.Inject

class ServiceBase @Inject()(apiPlatformTestUserConnector: ApiPlatformTestUserConnector) {

  def verifyNino(idType: String, idValue: String)
                (implicit hc: HeaderCarrier) = {
    IdType.parse(idType) match {
      case IdType.Nino => apiPlatformTestUserConnector.getIndividualByNino(Nino(idValue))
      case _           => throw new BadRequestException("Invalid National Insurance Number")
    }
  }

}
