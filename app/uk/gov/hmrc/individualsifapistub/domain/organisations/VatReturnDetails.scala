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

package uk.gov.hmrc.individualsifapistub.domain.organisations

import play.api.libs.json.Json

case class VatReturn(calendarMonth: Int, liabilityMonth: Int, numMonthsAssessed: Int, box6Total: Double, returnType: String, source: Option[String])

object VatReturn {
  implicit val format = Json.format[VatReturn]
}

case class VatTaxYear(taxYear: String, vatReturns: List[VatReturn])

object VatTaxYear {
  implicit val format = Json.format[VatTaxYear]
}

case class VatReturnDetails(vrn: String, appDate: Option[String], taxYears: List[VatTaxYear])

object VatReturnDetails {
  implicit val format = Json.format[VatReturnDetails]
}

case class VatReturnDetailsEntry(id: String, vatReturnDetails: VatReturnDetails)

object VatReturnDetailsEntry {
  implicit val format = Json.format[VatReturnDetailsEntry]
}