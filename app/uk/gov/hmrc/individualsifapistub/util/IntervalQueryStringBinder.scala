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

import uk.gov.hmrc.individualsifapistub.util.Dates.toInterval

import java.time.LocalDate
import scala.util.Try

class IntervalQueryStringBinder extends AbstractQueryStringBindable[Interval] {

  private val dateFormat = Dates.format

  override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Interval]] =
    (getParam(params, "from"), getParam(params, "to", Some(LocalDate.now()))) match {
      case (Right(from), Right(to)) if from isBefore to => Some(interval(from, to))
      case (Right(_), Right(_))                         => Some(Left(errorResponse("Invalid time period requested")))
      case (_, Left(msg))                               => Some(Left(msg))
      case (Left(msg), _)                               => Some(Left(msg))
    }

  private def interval(from: LocalDate, to: LocalDate): Either[String, Interval] =
    Try(Right(toInterval(from, to))) getOrElse Left(errorResponse("Invalid time period requested"))

  private def getParam(
    params: Map[String, Seq[String]],
    paramName: String,
    default: Option[LocalDate] = None): Either[String, LocalDate] =
    Try(params.get(paramName).flatMap(_.headOption) match {
      case Some(date) => Right(LocalDate.parse(date, dateFormat))
      case None =>
        default
          .map(Right(_))
          .getOrElse(Left(errorResponse(s"$paramName is required")))
    }) getOrElse Left(errorResponse(s"$paramName: invalid date format"))

  override def unbind(key: String, dateRange: Interval): String =
    s"from=${dateRange.fromDate.format(dateFormat)}&to=${dateRange.toDate.format(dateFormat)}"

}
