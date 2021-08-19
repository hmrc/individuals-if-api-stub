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

package uk.gov.hmrc.individualsifapistub.connector

import play.api.Logger
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, NotFoundException}
import uk.gov.hmrc.individualsifapistub.domain.individuals.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.domain.{TestIndividual, TestOrganisation}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApiPlatformTestUserConnector @Inject()(http : HttpClient, servicesConfig: ServicesConfig ) {

  val serviceUrl = servicesConfig.baseUrl("api-platform-test-user")

  def getOrganisationByEmpRef(empRef: EmpRef)(implicit hc: HeaderCarrier): Future[Option[TestOrganisation]] = {
    http.GET[TestOrganisation](s"$serviceUrl/organisations/empref/${empRef.encodedValue}") map (Some(_))
  } recover {
    case e: NotFoundException =>
      Logger.warn(s"unable to retrieve employer with empRef: ${empRef.value}. ${e.getMessage}")
      None
  }

  def getOrganisationByCrn(crn: String)(implicit hc: HeaderCarrier): Future[Option[TestOrganisation]] = {
    http.GET[TestOrganisation](s"$serviceUrl/organisations/crn/$crn") map (Some(_))
  } recover {
    case e: NotFoundException =>
      Logger.warn(s"unable to retrieve employer with crn: $crn. ${e.getMessage}")
      None
  }

  def getOrganisationBySaUtr(utr: String)(implicit hc: HeaderCarrier): Future[Option[TestIndividual]] = {
    http.GET[TestIndividual](s"$serviceUrl/organisations/sautr/$utr") map (Some(_))
  } recover {
    case e: NotFoundException =>
      Logger.warn(s"unable to retrieve individual with utr: $utr. ${e.getMessage}")
      None
  }

  def getIndividualByNino(nino: Nino)(implicit hc: HeaderCarrier): Future[TestIndividual] = {
    http.GET[TestIndividual](s"$serviceUrl/individuals/nino/${nino.value}")
  } recover {
    case e: NotFoundException =>
      Logger.warn(s"unable to retrieve individual with nino: ${nino.value}. ${e.getMessage}")
      throw new NotFoundException(e.getMessage)
    case ex: Exception =>
      Logger.warn(s"getIndividualByNino failed: ${nino.value}. ${ex.getMessage}")
      throw new Exception(ex.getMessage)
  }
}
