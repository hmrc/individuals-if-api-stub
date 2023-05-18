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

package unit.uk.gov.hmrc.individualsifapistub.util.domain.organisations

import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain.organisations.{VatReturn, VatReturnDetails, VatTaxYear}
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class VatReturnDetailsSpec extends UnitSpec {

  "VatReturn reads successfully from json" in {
    val json =
      """
        |{
        |"calendarMonth": 1,
        |"liabilityMonth": 10,
        |"numMonthsAssessed": 5,
        |"box6Total": 6542,
        |"returnType": "RegularReturn",
        |"source": "VMF"
        |}
        |""".stripMargin


    val expectedResult = VatReturn(1, 10, 5, 6542, "RegularReturn", Some("VMF"))
    val result = Json.parse(json).validate[VatReturn]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "VatReturn reads unsuccessfully from json when calendarMonth is incorrect" in {

    val json =
      """
        |{
        |"calendarMonth": "",
        |"liabilityMonth": 10,
        |"numMonthsAssessed": 5,
        |"box6Total": 6542,
        |"returnType": "RegularReturn",
        |"source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturn]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when liabilityMonth is incorrect" in {

    val json =
      """
        |{
        |"calendarMonth": 1,
        |"liabilityMonth": "",
        |"numMonthsAssessed": 5,
        |"box6Total": 6542,
        |"returnType": "RegularReturn",
        |"source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturn]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when numMonthsAssessed is incorrect" in {

    val json =
      """
        |{
        |"calendarMonth": 1,
        |"liabilityMonth": 10,
        |"numMonthsAssessed": "",
        |"box6Total": 6542,
        |"returnType": "RegularReturn",
        |"source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturn]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when box6Total is incorrect" in {

    val json =
      """
        |{
        |"calendarMonth": 1,
        |"liabilityMonth": 10,
        |"numMonthsAssessed": 5,
        |"box6Total": "",
        |"returnType": "RegularReturn",
        |"source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturn]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when returnType is incorrect" in {

    val json =
      """
        |{
        |"calendarMonth": "",
        |"liabilityMonth": 10,
        |"numMonthsAssessed": 5,
        |"box6Total": 6542,
        |"returnType": 1,
        |"source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturn]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when source is incorrect" in {

    val json =
      """
        |{
        |"calendarMonth": "",
        |"liabilityMonth": 10,
        |"numMonthsAssessed": 5,
        |"box6Total": 6542,
        |"returnType": "RegularReturn",
        |"source": 1
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturn]

    result.isSuccess shouldBe false
  }

  "VatTaxYear reads successfully from json" in {
    val json =
      """{
        |"taxYear": "2019",
        |"vatReturns": []
        |}
        |""".stripMargin

    val expectedResult = VatTaxYear("2019", List.empty)
    val result = Json.parse(json).validate[VatTaxYear]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "VatTaxYear reads unsuccessfully from json when taxYear is incorrect" in {
    val json =
      """{
        |"taxYear": 2019,
        |"vatReturns": []
        |}
        |""".stripMargin


    val result = Json.parse(json).validate[VatTaxYear]

    result.isSuccess shouldBe false

  }

  "vatReturnDetails reads successfully from json" in {
    val json =
      """
        |{
        |"vrn": "12345678",
        |"appDate": "20160425",
        |"taxYears": []
        |}
        |""".stripMargin

    val expectedResult = VatReturnDetails("12345678", Some("20160425"), List.empty)
    val result = Json.parse(json).validate[VatReturnDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "vatReturnDetails reads unsuccessfully from json when vrn is incorrect" in {
    val json =
      """
        |{
        |"vrn": 12345678,
        |"appDate": "20160425",
        |"taxYears": []
        |}
        |""".stripMargin


    val result = Json.parse(json).validate[VatReturnDetails]

    result.isSuccess shouldBe false

  }

  "vatReturnDetails reads unsuccessfully from json when appDate is incorrect" in {
    val json =
      """
        |{
        |"vrn": 12345678,
        |"appDate": 20160425,
        |"taxYears": []
        |}
        |""".stripMargin


    val result = Json.parse(json).validate[VatReturnDetails]

    result.isSuccess shouldBe false

  }

}