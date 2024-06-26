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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class VatPeriod(
  periodKey: Option[String],
  billingPeriodFromDate: Option[String],
  billingPeriodToDate: Option[String],
  numDaysAssessed: Option[Int],
  box6Total: Option[Double],
  returnType: Option[String],
  source: Option[String]
)

object VatPeriod {
  implicit val format: OFormat[VatPeriod] = Json.format[VatPeriod]
}

case class VatReturnsDetails(
  vrn: String,
  appDate: Option[String],
  extractDate: Option[String],
  vatPeriods: List[VatPeriod]
)

object VatReturnsDetails {
  implicit val format: OFormat[VatReturnsDetails] = Json.format[VatReturnsDetails]
}

case class VatReturnsDetailsEntry(id: String, vatReturnsDetails: VatReturnsDetails, createdAt: LocalDateTime)

object VatReturnsDetailsEntry {
  implicit val format: OFormat[VatReturnsDetailsEntry] = Json.format[VatReturnsDetailsEntry]
}
