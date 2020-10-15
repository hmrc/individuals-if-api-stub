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

package uk.gov.hmrc.individualsifapistub.repository

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json.obj
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualsifapistub.domain.{BenefitsAndCredits, DuplicateSelfAssessmentException, JsonFormatters, SelfAssessment}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class BenefitsAndCreditsRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)
  extends ReactiveRepository[BenefitsAndCredits, BSONObjectID]("benefitsAndCredits", mongoConnectionProvider.mongoDatabase, JsonFormatters.benefitsAndCreditsFormat) {

  override lazy val indexes = Seq(
    Index(key = Seq(("id", Ascending)), name = Some("idIndex"), unique = true, background = true)
  )

  def create(id: String) = {
    val benefitAndCredit = BenefitsAndCredits(id)
    insert(benefitAndCredit) map (_ => benefitAndCredit)
  }

  def findById(id: String) = collection.find(obj("id" -> id)).one[BenefitsAndCredits]
}
