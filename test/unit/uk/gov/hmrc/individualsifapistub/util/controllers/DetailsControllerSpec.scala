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

package unit.uk.gov.hmrc.individualsifapistub.util.controllers

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import testUtils.TestHelpers
import uk.gov.hmrc.individualsifapistub.controllers.DetailsController
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.services.DetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class DetailsControllerSpec extends TestSupport with TestHelpers {

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockDetailsService = mock[DetailsService]
    val underTest = new DetailsController(bodyParsers, controllerComponents, mockDetailsService)
  }

  val idType = "nino"
  val idValue = "XH123456A"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"

  val request = CreateDetailsRequest(
    Some(Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9,"MOBILE TELEPHONE", "07123 987655"))),
    Some(Seq(
      Residence(residenceType = Some("BASE"), address = generateAddress(2)),
      Residence(residenceType = Some("NOMINATED"), address = generateAddress(1))))
  )

  "Create details" should {

    "Successfully create a details record and return created record as response" in new Setup {

      val ident = Identifier(Some(idValue), None, None, None, Some(useCase))
      val id = s"${ident.nino.getOrElse(ident.trn.get)}-$useCase"
      val detailsResponse = DetailsResponse(id, request.contactDetails, request.residences)
      val returnVal = DetailsResponseNoId(detailsResponse.contactDetails, detailsResponse.residences)

      when(mockDetailsService.create(idType, idValue, useCase, request)).thenReturn(
        Future.successful(returnVal)
      )

      val result = await(underTest.create(idType, idValue, useCase)(
        fakeRequest.withBody(Json.toJson(request)))
      )

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(returnVal)

    }

    "Fail when a request is not provided" in new Setup {

      val details = Identifier(Some(idValue), None, None, None, Some(useCase))
      val id = s"${details.nino.getOrElse(details.trn)}-$useCase"
      val detailsResponse = DetailsResponse(id, None, None)
      val returnVal = DetailsResponseNoId(detailsResponse.contactDetails, detailsResponse.residences)

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
