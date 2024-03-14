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

package uk.gov.hmrc.individualsifapistub.util

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}

case class Interval(fromDate: LocalDateTime, toDate: LocalDateTime) {
  def contains(date: LocalDateTime): Boolean = !date.isBefore(fromDate) && !date.isAfter(toDate)
  def overlaps(other: Interval): Boolean =
    !other.fromDate.isAfter(toDate) & !other.toDate.isBefore(fromDate)
  override def toString: String = {
    val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    s"${fromDate.format(format)}/${toDate.format(format)}"
  }
}

object Dates {

  val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def asLocalDate(string: String): LocalDate = LocalDate.parse(string)

  def toInterval(from: String, to: String): Interval =
    toInterval(LocalDate.parse(from, format), LocalDate.parse(to, format))

  def toInterval(from: LocalDate, to: LocalDate): Interval =
    Interval(from.atStartOfDay, to.atStartOfDay.plus(1, ChronoUnit.MILLIS))

  def toInterval(from: String, to: Option[String]): Interval =
    toInterval(LocalDate.parse(from), to.map(LocalDate.parse).getOrElse(LocalDate.now))
}
