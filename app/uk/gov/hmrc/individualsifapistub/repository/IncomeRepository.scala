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
import play.api.libs.json.JsObject
import play.api.libs.json.Json.obj
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import uk.gov.hmrc.individualsifapistub.domain.{CreateIncomeRequest, DuplicateException, JsonFormatters, Income}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IncomeRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)
  extends ReactiveRepository[Income, BSONObjectID]( "Income",
                                                            mongoConnectionProvider.mongoDatabase,
                                                            JsonFormatters.incomeFormat ) {

  override lazy val indexes = Seq(
    Index(key = Seq(("id", Ascending)), name = Some("idIndex"), unique = true, background = true)
  )

  def create(id: String, request: CreateIncomeRequest): Future[Income] = {
    val income = Income(id, request.body)
    insert(income) map (_ => income) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }
  }

  def findById(id: String): Future[Option[Income]] = collection.find[JsObject, JsObject](obj("id" -> id), None).one[Income]
}