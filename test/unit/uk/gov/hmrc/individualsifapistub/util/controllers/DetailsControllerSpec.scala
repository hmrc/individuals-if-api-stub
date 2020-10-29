/*
 * Copyright 2020 HM Revenue & Customs
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
import testUtils.AddressHelpers
import uk.gov.hmrc.individualsifapistub.controllers.DetailsController
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.services.DetailsService
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

import scala.concurrent.Future

class DetailsControllerSpec extends TestSupport with AddressHelpers {

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockDetailsService = mock[DetailsService]
    val underTest = new DetailsController(bodyParsers, controllerComponents, mockDetailsService)
  }

  val idType = "NINO"
  val idValue = "QW1234QW"
  val request = CreateDetailsRequest(
    Some(Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9,"MOBILE TELEPHONE", "07123 987655"))),
    Some(Seq(
      Residence(residenceType = Some("BASE"), address = createAddress(2)),
      Residence(residenceType = Some("NOMINATED"), address = createAddress(1))))
  )

  "Create details" should {
    "Successfully create a details record and return created record as response" in new Setup {
      val details = Details(Some(idValue), None)
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)
      when(mockDetailsService.create(idType, idValue, request)).thenReturn(Future.successful(detailsResponse))

      val result = await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.toJson(detailsResponse)
    }

    "Fail when a request is not provided" in new Setup {
      val details = Details(Some(idValue), None)
      val detailsResponse = DetailsResponse(details, None, None)
      when(mockDetailsService.create(idType, idValue, request)).thenReturn(Future.successful(detailsResponse))
      val response = await(underTest.create(idType, idValue)(fakeRequest.withBody(Json.toJson(""))))
      status(response) shouldBe BAD_REQUEST
    }
  }

  "Retrieve Details" should {
    "Return details when successfully retrieved from service" in new Setup {
      val details = Details(Some(idValue), None)
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)
      when(mockDetailsService.get(idType, idValue)).thenReturn(Future.successful(Some(detailsResponse)))

      val result = await(underTest.retrieve(idType, idValue)(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(Some(detailsResponse))
    }

    "Fails when it cannot get from service" in new Setup {
      when(mockDetailsService.create(idType, idValue, request)).thenReturn(Future.failed(new Exception))
      assertThrows[Exception] { await(underTest.retrieve(idType, idValue)(fakeRequest)) }
    }
  }
}
