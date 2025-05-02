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

package unit.uk.gov.hmrc.individualsifapistub.util

import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import uk.gov.hmrc.domain.{EmpRef, Nino, SaUtr}
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.individualsifapistub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualsifapistub.domain._

class ApiPlatformTestUserConnectorSpec
    extends UnitSpec with WireMockSupport with WireMockMethods with BeforeAndAfterEach with GuiceOneAppPerSuite {
  val empRef = EmpRef("123", "AI45678")
  val testOrganisation = TestOrganisation(
    Some(empRef),
    None,
    None,
    TestOrganisationDetails("Disney Inc", TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ"))
  )

  val nino = Nino("AB123456A")
  val utr = SaUtr("2432552635")

  val testIndividual = TestIndividual(
    saUtr = Some(utr),
    taxpayerType = Some("Individual"),
    organisationDetails = Some(
      TestOrganisationDetails(
        name = "Barry Barryson",
        address = TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ")
      )
    )
  )

  private val config = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |      api-platform-test-user {
         |        host     = $wireMockHost
         |        port     = $wireMockPort
         |    }
         |  }
         |}
         |""".stripMargin
    )
  )

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(config).build()

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val underTest = fakeApplication().injector.instanceOf[ApiPlatformTestUserConnector]
  }

  "get organisation by empRef" should {
    "retrieve a test organisation by empRef" in new Setup {
      when(GET, s"/organisations/empref/${empRef.encodedValue}")
        .thenReturn(
          OK,
          s"""
               {
                 "empRef": "${empRef.value}",
                 "organisationDetails": {
                   "name": "Disney Inc",
                   "address": {
                     "line1": "Capital Tower",
                     "line2": "Aberdeen",
                     "postcode": "SW1 4DQ"
                   }
                 }
               }
             """
        )

      val result = await(underTest.getOrganisationByEmpRef(empRef))

      result shouldBe Some(testOrganisation)
    }

    "return nothing if the organisation cannot be found" in new Setup {
      when(GET, s"/organisations/empref/${empRef.encodedValue}")
        .thenReturn(
          NOT_FOUND
        )

      await(underTest.getOrganisationByEmpRef(empRef)) shouldBe None
    }

    "propagate errors" in new Setup {
      when(GET, s"/organisations/empref/${empRef.encodedValue}")
        .thenReturn(
          BAD_REQUEST
        )

      intercept[UpstreamErrorResponse](await(underTest.getOrganisationByEmpRef(empRef)))
    }
  }

  "getIndividualByNino" should {
    "retrieve the individual" in new Setup {
      when(GET, s"/individuals/nino/$nino")
        .thenReturn(
          OK,
          s"""{
            "saUtr": "${utr.value}",
            "taxpayerType": "Individual",
            "organisationDetails": {
              "name": "Barry Barryson",
              "address": {
                "line1": "Capital Tower",
                "line2": "Aberdeen",
                "postcode": "SW1 4DQ"
              }
            }
            }"""
        )

      val result = await(underTest.getIndividualByNino(nino))

      result shouldBe Some(testIndividual)
    }

    "return nothing if the individual cannot be found" in new Setup {
      when(GET, s"/individuals/nino/$nino")
        .thenReturn(
          NOT_FOUND
        )

      await(underTest.getIndividualByNino(nino)) shouldBe None
    }

    "propagate errors" in new Setup {
      when(GET, s"/individuals/nino/$nino")
        .thenReturn(
          INTERNAL_SERVER_ERROR
        )

      intercept[UpstreamErrorResponse](await(underTest.getIndividualByNino(nino)))
    }
  }
}
