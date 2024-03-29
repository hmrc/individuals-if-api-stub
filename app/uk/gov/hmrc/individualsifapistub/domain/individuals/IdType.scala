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

package uk.gov.hmrc.individualsifapistub.domain.individuals

object IdType {
  sealed trait IdTypeEnum

  case object Nino extends IdTypeEnum {
    override def toString: String = "nino"
  }
  case object Trn extends IdTypeEnum {
    override def toString: String = "trn"
  }

  def parse(value: String): IdTypeEnum =
    value.toLowerCase match {
      case "nino" => Nino
      case "trn"  => Trn
      case _      => throw new IllegalArgumentException(s"$value is not a valid IdType")
    }
}
