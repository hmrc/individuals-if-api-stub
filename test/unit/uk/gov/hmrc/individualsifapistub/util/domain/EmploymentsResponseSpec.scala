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

package unit.uk.gov.hmrc.individualsifapistub.util.domain

import play.api.libs.json.Json
import testUtils.AddressHelpers
import uk.gov.hmrc.individualsifapistub.domain.{Address, Employer, Employment, EmploymentDetail, EmploymentsResponse, Id, Payment}
import uk.gov.hmrc.individualsifapistub.domain.EmploymentsResponse._
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class EmploymentsResponseSpec extends UnitSpec with AddressHelpers {

  val ninoDetails = Id(Some("XH123456A"), None)
  val trnDetails = Id(None, Some("12345678"))

  val employmentsResponse = EmploymentsResponse(
    Seq(
      Employment(
        employer = Some(Employer(
          name = Some("Name"),
          address = Some(Address(
            Some("line1"),
            Some("line2"),
            Some("line3"),
            Some("line4"),
            Some("line5"),
            Some("postcode")
          )),
          districtNumber = Some("ABC"),
          schemeRef = Some("ABC")
        )),
        employment = Some(EmploymentDetail(
          startDate = Some("2001-12-31"),
          endDate = Some("2002-05-12"),
          payFrequency = Some("W2"),
          payrollId = Some("12341234"),
          address = Some(Address(
            Some("line1"),
            Some("line2"),
            Some("line3"),
            Some("line4"),
            Some("line5"),
            Some("postcode")
          )))),
        payments = Some(Seq(Payment(
          date = Some("2001-12-31"),
          ytdTaxablePay = Some(120.02),
          paidTaxablePay = Some(112.75),
          paidNonTaxOrNICPayment = Some(123123.32),
          week = Some(52),
          month = Some(12)
        )
      )
    )
  )))

  "Employment Response" should {
    "Write to JSON when only nino provided" in {
      val result = Json.toJson(employmentsResponse)
      println(Json.prettyPrint(result))
    }
  }
}
