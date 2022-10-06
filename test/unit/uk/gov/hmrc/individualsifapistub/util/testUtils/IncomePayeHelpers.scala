/*
 * Copyright 2022 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsifapistub.util.testUtils

import org.joda.time.LocalDate
import uk.gov.hmrc.individualsifapistub.domain.individuals.{AdditionalFields, Benefits, EmployeeNics, EmployeePensionContribs, GrossEarningsForNics, PayeEntry, PostGradLoan, StatutoryPayYTD, StudentLoan, TotalEmployerNics}

trait IncomePayeHelpers {
  def createValidPayeEntry() = {
    PayeEntry(
      Some("K971"),
      Some("36"),
      Some(19157.5),
      Some(3095.89),
      Some(159228.49),
      Some("345/34678"),
      Some(LocalDate.parse("2020-02-27")),
      Some(16533.95),
      Some("18-19"),
      Some("3"),
      Some("2"),
      Some("W4"),
      Some(198035.8),
      Some(createValidEmployeeNics()),
      Some(createValidEmployeePensionContribs()),
      Some(createValidBenefits()),
      Some(createValidStatutoryPayToDate()),
      Some(createValidStudentLoan()),
      Some(createValidPostGradLoan()),
      Some(createValodIFGrossEarningsForNics()),
      Some(createValidTotalEmployerNics()),
      Some(createValidAdditionalFields())
    )
  }

  def createValidPayeHOV2FieldsEntry() = {
    PayeEntry(
      None,
      None,
      None,
      None,
      None,
      Some("345/34678"),
      Some(LocalDate.parse("2020-02-27")),
      Some(16533.95),
      None,
      Some("3"),
      Some("2"),
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None
    )
  }

  private def createValidEmployeeNics() = {
    EmployeeNics(
      Some(15797.45),
      Some(13170.69),
      Some(16193.76),
      Some(30846.56),
      Some(10633.5),
      Some(15579.18),
      Some(110849.27),
      Some(162081.23)
    )
  }

  private def createValidEmployeePensionContribs() = EmployeePensionContribs(Some(169731.51), Some(173987.07), Some(822317.49), Some(818841.65))

  private def createValidBenefits() = Benefits(Some(506328.1), Some(246594.83))

  private def createValidStudentLoan() = StudentLoan(Some("02"), Some(88478), Some(545))

  private def createValidPostGradLoan() = PostGradLoan(Some(15636), Some(46849))

  def createValodIFGrossEarningsForNics() =
    GrossEarningsForNics(Some(169731.51), Some(173987.07), Some(822317.49), Some(818841.65))

  private def createValidAdditionalFields() =
    AdditionalFields(Some(false), Some("yxz8Lt5?/`/>6]5b+7%>o-y4~W5suW"))

  def createValidTotalEmployerNics() =
    TotalEmployerNics(
      Some(15797.45),
      Some(13170.69),
      Some(16193.76),
      Some(30846.56),
      Some(10633.5),
      Some(15579.18),
      Some(110849.27),
      Some(162081.23)
    )

  def createValidStatutoryPayToDate() =
    StatutoryPayYTD(
      Some(15797.45),
      Some(13170.69),
      Some(16193.76),
      Some(30846.56)
    )
}
