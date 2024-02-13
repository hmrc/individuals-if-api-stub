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

import org.joda.time.Interval

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZoneId}

object Dates {

  private def asJodaLocalDate(string: String) = org.joda.time.LocalDate.parse(string)

  def asLocalDate(string: String): LocalDate = LocalDate.parse(string)

  def toInterval(from: String, to: String): Interval =
    toInterval(asJodaLocalDate(from), asJodaLocalDate(to))

  def toInterval(from: org.joda.time.LocalDate, to: org.joda.time.LocalDate): Interval =
    new Interval(from.toDate.getTime, to.toDateTimeAtStartOfDay.plusMillis(1).toDate.getTime)

  def toInterval(from: java.time.LocalDate, to: java.time.LocalDate): Interval =
    new Interval(from.atStartOfDay(ZoneId.systemDefault()).toInstant.toEpochMilli, to.atStartOfDay(ZoneId.systemDefault()).plus(1, ChronoUnit.MILLIS).toInstant.toEpochMilli)

  def toInterval(from: String, to: Option[String]): Interval =
    toInterval(asJodaLocalDate(from), to.map(asJodaLocalDate).getOrElse(org.joda.time.LocalDate.now))
}
