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

package uk.gov.hmrc.individualsifapistub.connector

import play.api.Logging
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readOptionOfNotFound}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.individualsifapistub.domain.{TestIndividual, TestOrganisation}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiPlatformTestUserConnector @Inject() (http: HttpClientV2, servicesConfig: ServicesConfig)(implicit
  ec: ExecutionContext
) extends Logging {
  private val serviceUrl = servicesConfig.baseUrl("api-platform-test-user")

  def getOrganisationByEmpRef(empRef: EmpRef)(implicit hc: HeaderCarrier): Future[Option[TestOrganisation]] =
    http.get(url"$serviceUrl/organisations/empref/${empRef.value}").execute[Option[TestOrganisation]]

  def getOrganisationByCrn(crn: String)(implicit hc: HeaderCarrier): Future[Option[TestOrganisation]] =
    http.get(url"$serviceUrl/organisations/crn/$crn").execute[Option[TestOrganisation]]

  def getOrganisationBySaUtr(utr: String)(implicit hc: HeaderCarrier): Future[Option[TestIndividual]] =
    http.get(url"$serviceUrl/organisations/sautr/$utr").execute[Option[TestIndividual]]

  def getIndividualByNino(nino: Nino)(implicit hc: HeaderCarrier): Future[Option[TestIndividual]] =
    http.get(url"$serviceUrl/individuals/nino/${nino.value}").execute[Option[TestIndividual]]
}
