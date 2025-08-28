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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers.organisations

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.organisations.SelfAssessmentTaxPayerController
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, SelfAssessmentTaxPayer, TaxPayerDetails}
import uk.gov.hmrc.individualsifapistub.domain.{TestAddress, TestIndividual, TestOrganisationDetails}
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class SelfAssessmentTaxPayerControllerSpec extends TestSupport {

  val mockConnector: ApiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]

  val controller =
    new SelfAssessmentTaxPayerController(loggingAction, controllerComponents, mockConnector)

  val exampleAddress: Address =
    Address(Some("Alfie House"), Some("Main Street"), Some("Birmingham"), Some("West midlands"), Some("B14 6JH"))

  val utr: SaUtr = SaUtr("2432552635")

  val taxPayerDetails: Seq[TaxPayerDetails] = Seq(TaxPayerDetails("John Smith II", Some("Registered"), exampleAddress))
  val selfAssessmentTaxPayer: SelfAssessmentTaxPayer = SelfAssessmentTaxPayer(utr.utr, "Individual", taxPayerDetails)
  val testIndividual: TestIndividual = TestIndividual(
    saUtr = Some(utr),
    taxpayerType = Some("Individual"),
    organisationDetails = Some(
      TestOrganisationDetails(
        name = "Barry Barryson",
        address = TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ")
      )
    )
  )

  "retrieve" should {
    "return response when entry found by service" in {

      when(mockConnector.getOrganisationBySaUtr(any())(using any())).thenReturn(Future.successful(Some(testIndividual)))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      val result = controller.retrieve(selfAssessmentTaxPayer.utr)(httpRequest)

      result.map { x =>
        x.header.status shouldBe OK
        jsonBodyOf(x) shouldBe Json.toJson(testIndividual)
      }
    }

    "fails when an exception is thrown" in {

      when(mockConnector.getOrganisationBySaUtr(any())(using any())).thenReturn(Future.failed(new Exception))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      val result = await(controller.retrieve(selfAssessmentTaxPayer.utr)(httpRequest))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
