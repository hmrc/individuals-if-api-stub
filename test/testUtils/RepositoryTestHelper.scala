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

package testUtils

import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import uk.gov.hmrc.individualsifapistub.repository.individuals.{DetailsRepository, EmploymentRepository, IncomePayeRepository, IncomeSaRepository, TaxCreditsRepository}
import uk.gov.hmrc.individualsifapistub.repository.organisations.{CorporationTaxCompanyDetailsRepository, CorporationTaxReturnDetailsRepository, NumberOfEmployeesRepository, SelfAssessmentReturnDetailRepository, SelfAssessmentTaxPayerRepository, VatInformationRepository, VatReturnsDetailsRepository}
import uk.gov.hmrc.mongo.test.MongoSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

trait RepositoryTestHelper extends TestSupport
  with MongoSupport
  with BeforeAndAfterEach {

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  lazy private val allRepos = Seq(
    fakeApplication.injector.instanceOf[TaxCreditsRepository],
    fakeApplication.injector.instanceOf[EmploymentRepository],
    fakeApplication.injector.instanceOf[DetailsRepository],
    fakeApplication.injector.instanceOf[IncomePayeRepository],
    fakeApplication.injector.instanceOf[IncomeSaRepository],
    fakeApplication.injector.instanceOf[CorporationTaxReturnDetailsRepository],
    fakeApplication.injector.instanceOf[CorporationTaxCompanyDetailsRepository],
    fakeApplication.injector.instanceOf[SelfAssessmentReturnDetailRepository],
    fakeApplication.injector.instanceOf[SelfAssessmentTaxPayerRepository],
    fakeApplication.injector.instanceOf[VatReturnsDetailsRepository],
    fakeApplication.injector.instanceOf[VatInformationRepository],
    fakeApplication.injector.instanceOf[NumberOfEmployeesRepository]
  )

  override def beforeEach(): Unit = {
    allRepos.foreach { r =>
      await(r.collection.drop().headOption())
      await(r.ensureIndexes)
    }
  }

  override def afterEach(): Unit = {
    allRepos.foreach { r =>
      await(r.collection.drop().headOption())
    }
  }
}
