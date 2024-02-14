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
import uk.gov.hmrc.individualsifapistub.domain.organisations._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class VatReturnsDetailsSpec extends UnitSpec {

  "VatReturn reads successfully from json" in {
    val json =
      """
        |{
        | "periodKey": "22AA",
        | "billingPeriodFromDate": "2022-10-01",
        | "billingPeriodToDate": "2022-10-30",
        | "numDaysAssessed": 30,
        | "box6Total": 6542.2,
        | "returnType": "RegularReturn",
        | "source": "VMF"
        |}
        |""".stripMargin

    val expectedResult = VatPeriod(
      Some("22AA"),
      Some("2022-10-01"),
      Some("2022-10-30"),
      Some(30),
      Some(6542.2),
      Some("RegularReturn"),
      Some("VMF"))
    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "VatReturn reads unsuccessfully from json when periodKey is incorrect" in {
    val json =
      """
        |{
        | "periodKey": 2,
        | "billingPeriodFromDate": "2022-10-01",
        | "billingPeriodToDate": "2022-10-30",
        | "numDaysAssessed": 30,
        | "box6Total": 6542.2,
        | "returnType": "RegularReturn",
        | "source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when billingPeriodFromDate is incorrect" in {
    val json =
      """
        |{
        | "periodKey": "22AA",
        | "billingPeriodFromDate": 2,
        | "billingPeriodToDate": "2022-10-30",
        | "numDaysAssessed": 30,
        | "box6Total": 6542.2,
        | "returnType": "RegularReturn",
        | "source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when billingPeriodToDate is incorrect" in {
    val json =
      """
        |{
        | "periodKey": "22AA",
        | "billingPeriodFromDate": "2022-10-01",
        | "billingPeriodToDate": 2,
        | "numDaysAssessed": 30,
        | "box6Total": 6542.2,
        | "returnType": "RegularReturn",
        | "source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when numDaysAssessed is incorrect" in {
    val json =
      """
        |{
        | "periodKey": "22AA",
        | "billingPeriodFromDate": "2022-10-01",
        | "billingPeriodToDate": "2022-10-30",
        | "numDaysAssessed": "30",
        | "box6Total": 6542.2,
        | "returnType": "RegularReturn",
        | "source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when box6Total is incorrect" in {
    val json =
      """
        |{
        | "periodKey": "22AA",
        | "billingPeriodFromDate": "2022-10-01",
        | "billingPeriodToDate": "2022-10-30",
        | "numDaysAssessed": "30",
        | "box6Total": "6542.2",
        | "returnType": "RegularReturn",
        | "source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when returnType is incorrect" in {
    val json =
      """
        |{
        | "periodKey": "22AA",
        | "billingPeriodFromDate": "2022-10-01",
        | "billingPeriodToDate": "2022-10-30",
        | "numDaysAssessed": 30,
        | "box6Total": 6542.2,
        | "returnType": 2,
        | "source": "VMF"
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe false
  }

  "VatReturn reads unsuccessfully from json when source is incorrect" in {
    val json =
      """
        |{
        | "periodKey": "22AA",
        | "billingPeriodFromDate": "2022-10-01",
        | "billingPeriodToDate": "2022-10-30",
        | "numDaysAssessed": 30,
        | "box6Total": 6542.2,
        | "returnType": "RegularReturn",
        | "source": 2
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatPeriod]

    result.isSuccess shouldBe false
  }

  "vatReturnDetails reads successfully from json" in {
    val json =
      """
        |{
        | "vrn": "12345678",
        | "appDate": "20160425",
        | "extractDate": "2023-01-01",
        | "vatPeriods": []
        |}
        |""".stripMargin

    val expectedResult = VatReturnsDetails("12345678", Some("20160425"), Some("2023-01-01"), List.empty)
    val result = Json.parse(json).validate[VatReturnsDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "vatReturnDetails reads unsuccessfully from json when vrn is incorrect" in {
    val json =
      """
        |{
        | "vrn": 12345678,
        | "appDate": "20160425",
        | "extractDate": "2023-01-01",
        | "vatPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturnsDetails]

    result.isSuccess shouldBe false

  }

  "vatReturnDetails reads unsuccessfully from json when appDate is incorrect" in {
    val json =
      """
        |{
        | "vrn": 12345678,
        | "appDate": 20160425,
        | "extractDate": "2023-01-01",
        | "vatPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[VatReturnsDetails]

    result.isSuccess shouldBe false
  }

}
