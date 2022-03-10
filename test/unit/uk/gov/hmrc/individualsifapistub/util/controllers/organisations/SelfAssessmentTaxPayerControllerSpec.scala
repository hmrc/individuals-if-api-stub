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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers.organisations

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.organisations.SelfAssessmentTaxPayerController
import uk.gov.hmrc.individualsifapistub.domain.{TestAddress, TestIndividual, TestOrganisationDetails}
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, SelfAssessmentTaxPayer, TaxPayerDetails}
import uk.gov.hmrc.individualsifapistub.domain.organisations.SelfAssessmentTaxPayer._
import uk.gov.hmrc.individualsifapistub.domain.individuals.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.services.organisations.SelfAssessmentTaxPayerService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class SelfAssessmentTaxPayerControllerSpec extends TestSupport {

  val mockService = mock[SelfAssessmentTaxPayerService]
  val mockConnector = mock[ApiPlatformTestUserConnector]

  val controller = new SelfAssessmentTaxPayerController(bodyParsers, controllerComponents, mockService, mockConnector)

  val exampleAddress = Address(Some("Alfie House"),
    Some("Main Street"),
    Some("Birmingham"),
    Some("West midlands"),
    Some("B14 6JH"))

  val utr = SaUtr("2432552635")

  val taxPayerDetails = Seq(TaxPayerDetails("John Smith II", Some("Registered"), exampleAddress))
  val selfAssessmentTaxPayer = SelfAssessmentTaxPayer(utr.utr, "Individual", taxPayerDetails)
  val testIndividual = TestIndividual(
    saUtr = Some(utr),
    taxpayerType = Some("Individual"),
    organisationDetails = Some(TestOrganisationDetails(
      name = "Barry Barryson",
      address = TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ")
    ))
  )

  "create" should {
    "return response with created status when successful" in {
      when(mockService.create(selfAssessmentTaxPayer)).thenReturn(Future.successful(selfAssessmentTaxPayer))

      val httpRequest =
        FakeRequest()
          .withMethod("Post")
          .withBody(Json.toJson(selfAssessmentTaxPayer))

      val result = controller.create(utr.utr)(httpRequest)

      result.map(x => {
        x.header.status shouldBe CREATED
        jsonBodyOf(x) shouldBe Json.toJson(testIndividual)
      })
    }

    "fail when invalid request provided" in {
      when(mockService.create(selfAssessmentTaxPayer)).thenReturn(Future.successful(selfAssessmentTaxPayer))

      val httpRequest =
        FakeRequest()
          .withMethod("POST")
          .withBody(Json.parse("{}"))

      val result = controller.create(utr.utr)(httpRequest)

      result.map(x => {
        x.header.status shouldBe BAD_REQUEST
      })
    }
  }

  "retrieve" should {
    "return response when entry found by service" in {

      when(mockConnector.getOrganisationBySaUtr(any())(any())).thenReturn(Future.successful(Some(testIndividual)))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      val result = controller.retrieve(selfAssessmentTaxPayer.utr)(httpRequest)

      result.map(x => {
        x.header.status shouldBe OK
        jsonBodyOf(x) shouldBe Json.toJson(testIndividual)
      })
    }

    "fails when an exception is thrown" in {

      when(mockConnector.getOrganisationBySaUtr(any())(any())).thenReturn(Future.failed(new Exception))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      val result = await(controller.retrieve(selfAssessmentTaxPayer.utr)(httpRequest))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
