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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers.individuals

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import testUtils.TestHelpers
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.controllers.individuals.DetailsController
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.repository.individuals.DetailsRepository
import uk.gov.hmrc.individualsifapistub.services.individuals.DetailsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class DetailsControllerSpec extends TestSupport with TestHelpers {

  trait Setup {
    implicit val hc = HeaderCarrier()
    val fakeRequest = FakeRequest()
    val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
    val detailsRepo = mock[DetailsRepository]
    val servicesConfig = mock[ServicesConfig]
    val mockDetailsService = new DetailsService(detailsRepo, apiPlatformTestUserConnector, servicesConfig)
    val underTest = new DetailsController(bodyParsers, controllerComponents, mockDetailsService)


    val idType = "nino"
    val idValue = "XH123456A"
    val startDate = "2020-01-01"
    val endDate = "2020-21-31"
    val useCase = "TEST"
    val fields = "some(values)"
    val utr = SaUtr("2432552635")

    val testIndividual = TestIndividual(
      saUtr = Some(utr),
      taxpayerType = Some("Individual"),
      organisationDetails = TestOrganisationDetails(
        name = "Barry Barryson",
        address = TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ")
      )
    )

    when(apiPlatformTestUserConnector.getIndividualByNino(any())(any())).
      thenReturn(Future.successful(testIndividual))
  }


  val request = CreateDetailsRequest(
    Some(Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9,"MOBILE TELEPHONE", "07123 987655"))),
    Some(Seq(
      Residence(residenceType = Some("BASE"), address = generateAddress(2)),
      Residence(residenceType = Some("NOMINATED"), address = generateAddress(1))))
  )

  "Create details" should {

    "Successfully create a details record and return created record as response" in new Setup {

      val returnVal = DetailsResponseNoId(request.contactDetails, request.residences)

      when(mockDetailsService.create(idType, idValue, useCase, request)).thenReturn(
        Future.successful(returnVal)
      )

      val result = await(underTest.create(idType, idValue, useCase)(
        fakeRequest.withBody(Json.toJson(request)))
      )

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(returnVal)

    }

    "Fail when an invalid nino is provided" in new Setup {

      val returnVal = DetailsResponseNoId(request.contactDetails, request.residences)

      when(apiPlatformTestUserConnector.getIndividualByNino(any())(any())).
        thenReturn(Future.failed(new RecordNotFoundException))

      when(mockDetailsService.create(idType, idValue, useCase, request)).thenReturn(
        Future.successful(returnVal)
      )

      val result = await(underTest.create(idType, idValue, useCase)(
        fakeRequest.withBody(Json.toJson("")))
      )

      status(result) shouldBe BAD_REQUEST

    }

    "Fail when a request is not provided" in new Setup {

      val returnVal = DetailsResponseNoId(None, None)

      when(mockDetailsService.create(idType, idValue, useCase, request)).thenReturn(
        Future.successful(returnVal)
      )

      val response = await(underTest.create(idType, idValue, useCase)(
        fakeRequest.withBody(Json.toJson("")))
      )
      status(response) shouldBe BAD_REQUEST
    }

  }

  "Retrieve Details" should {

    "Return details when successfully retrieved from service" in new Setup {

      val details = Identifier(Some(idValue), None, None, None, Some(useCase))
      val id = s"${details.nino.getOrElse(details.trn)}-$useCase"

      val detailsResponse = DetailsResponse(id, request.contactDetails, request.residences)
      when(mockDetailsService.get(idType, idValue, Some(fields))).thenReturn(
        Future.successful(Some(detailsResponse))
      )

      val result = await(underTest.retrieve(idType, idValue, Some(fields))(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(Some(detailsResponse))

    }

    "Fails when it cannot get from service" in new Setup {

      when(mockDetailsService.create(idType, idValue, useCase, request)).thenReturn(
        Future.failed(new Exception)
      )
      assertThrows[Exception] {
        await(underTest.retrieve(idType, idValue, Some(fields))(fakeRequest))
      }

    }
  }
}
