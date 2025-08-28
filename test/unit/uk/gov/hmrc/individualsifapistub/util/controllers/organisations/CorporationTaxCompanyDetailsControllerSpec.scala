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
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.organisations.CorporationTaxCompanyDetailsController
import uk.gov.hmrc.individualsifapistub.domain.organisations.{Address, CorporationTaxCompanyDetails, Name, NameAddressDetails}
import uk.gov.hmrc.individualsifapistub.domain.{TestAddress, TestOrganisation, TestOrganisationDetails}
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class CorporationTaxCompanyDetailsControllerSpec extends TestSupport {

  val mockConnector: ApiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
  val controller =
    new CorporationTaxCompanyDetailsController(loggingAction, controllerComponents, mockConnector)
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val address: Address =
    Address(Some("Alfie House"), Some("Main Street"), Some("Manchester"), Some("Londonberry"), Some("LN1 1AG"))

  val name: Name = Name("Waitrose", "And Partners")

  val registeredDetails: NameAddressDetails = NameAddressDetails(name, address)
  val communicationDetails: NameAddressDetails = NameAddressDetails(name, address)

  val ctCompanyDetails: CorporationTaxCompanyDetails =
    CorporationTaxCompanyDetails("1234567890", "12345678", Some(registeredDetails), Some(communicationDetails))
  val testOrganisation: TestOrganisation = TestOrganisation(
    Some(EmpRef("1234567890", "")),
    Some("12345678"),
    Some(""),
    TestOrganisationDetails(name.name1, TestAddress(address.line1.get, address.line2.get, address.postcode.get))
  )

  "retrieve" should {
    "return response when entry found by service" in {
      when(mockConnector.getOrganisationByCrn(any())(using any())).thenReturn(Future.successful(Some(testOrganisation)))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      val result = controller.retrieve(ctCompanyDetails.crn)(httpRequest)

      result.map { x =>
        x.header.status shouldBe OK
        jsonBodyOf(x) shouldBe Json.toJson(testOrganisation)
      }
    }

    "fails when an exception is thrown" in {
      when(mockConnector.getOrganisationByCrn(any())(using any())).thenReturn(Future.failed(new Exception))

      val httpRequest =
        FakeRequest()
          .withMethod("GET")

      val result = await(controller.retrieve(ctCompanyDetails.crn)(httpRequest))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

}
