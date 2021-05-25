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

package unit.uk.gov.hmrc.individualsifapistub.util.domain.organisations

import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain.organisations.{NumberOfEmployeesRequest, NumberOfEmployeeCounts, NumberOfEmployeeReferences, NumberOfEmployeesResponse}
import uk.gov.hmrc.individualsifapistub.domain.organisations.NumberOfEmployees._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class NumberOfEmployeesSpec extends UnitSpec {
  "NumberOfEmployeeCounts reads from JSON successfully" in {
    val json =
      """
        |{
        |    "dateTaken": "2019-10",
        |    "employeeCount": 554
        |}
        |""".stripMargin

    val expectedResult = NumberOfEmployeeCounts("2019-10", 554)

    val result = Json.parse(json).validate[NumberOfEmployeeCounts]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "NumberOfEmployeeCounts reads from JSON unsuccessfully when dateTake is invalid" in {
    val json =
      """
        |{
        |    "dateTaken": "20191-10",
        |    "employeeCount": 554
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[NumberOfEmployeeCounts]

    result.isSuccess shouldBe false
  }

  "NumberOfEmployeeReferences reads from JSON Successfully" in {
    val json =
      """
        |{
        |  "districtNumber": "456",
        |  "payeReference": "RT882d",
        |  "counts": []
        |}
        |""".stripMargin

    val expectedresult = NumberOfEmployeeReferences("456", "RT882d", Seq.empty)

    val result = Json.parse(json).validate[NumberOfEmployeeReferences]

    result.isSuccess shouldBe true
    result.get shouldBe expectedresult
  }

  "NumberOfEmployeeReferences reads from JSON unsuccessfully when districtNumber is incorrect" in {
    val json =
      """
        |{
        |  "districtNumber": "ABC",
        |  "payeReference": "RT882d",
        |  "counts": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[NumberOfEmployeeReferences]

    result.isSuccess shouldBe false
  }

  "NumberOfEmployeeReferences reads from JSON unsuccessfully when payeReference is incorrect" in {
    val json =
      """
        |{
        |  "districtNumber": "456",
        |  "payeReference": "12345678901234",
        |  "counts": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[NumberOfEmployeeReferences]

    result.isSuccess shouldBe false
  }

  "CreateNumberOfEmployeesRequest reads from JSON successfully" in {
    val json =
      """
        |{
        |  "startDate": "2019-10-01",
        |  "endDate": "2020-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val expectedResult = NumberOfEmployeesRequest("2019-10-01", "2020-04-05", Seq.empty)

    val result = Json.parse(json).validate[NumberOfEmployeesRequest]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CreateNumberOfEmployeesRequest reads from JSON unsuccessfully when startDate is incorrect" in {
    val json =
      """
        |{
        |  "startDate": "20111-10-01",
        |  "endDate": "2020-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[NumberOfEmployeesRequest]

    result.isSuccess shouldBe false
  }

  "CreateNumberOfEmployeesRequest reads from JSON unsuccessfully when endDate is incorrect" in {
    val json =
      """
        |{
        |  "startDate": "2019-10-01",
        |  "endDate": "20222-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[NumberOfEmployeesRequest]

    result.isSuccess shouldBe false
  }

  /// --------------------

  "NumberOfEmployeesResponse reads from JSON successfully" in {
    val json =
      """
        |{
        |  "startDate": "2019-10-01",
        |  "endDate": "2020-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val expectedResult = NumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq.empty)

    val result = Json.parse(json).validate[NumberOfEmployeesResponse]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "NumberOfEmployeesResponse reads from JSON unsuccessfully when startDate is incorrect" in {
    val json =
      """
        |{
        |  "startDate": "20111-10-01",
        |  "endDate": "2020-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[NumberOfEmployeesResponse]

    result.isSuccess shouldBe false
  }

  "NumberOfEmployeesResponse reads from JSON unsuccessfully when endDate is incorrect" in {
    val json =
      """
        |{
        |  "startDate": "2019-10-01",
        |  "endDate": "20222-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[NumberOfEmployeesResponse]

    result.isSuccess shouldBe false
  }
}
