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
import play.api.libs.json.{Format, JsObject, Reads, Writes}
import play.api.libs.json.Json.obj
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain.{CreateEmploymentRequest, Employment}
import uk.gov.hmrc.individualsifapistub.domain.EmploymentsResponse._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class EmploymentRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)
  extends ReactiveRepository[Seq[Employment], BSONObjectID]( "employment",
                                                        mongoConnectionProvider.mongoDatabase,
                                                        Format(Reads.seq[Employment], Writes.seq[Employment])) {

  override lazy val indexes = Seq(
    Index(key = Seq(("id", Ascending)), name = Some("idIndex"), unique = true, background = true)
  )

  def create(id: String, request: CreateEmploymentRequest): Future[Seq[Employment]] = {
    val employment = request.employments
    insert(employment) map (_ => employment)
  }

  def findById(id: String): Future[Option[Seq[Employment]]] = collection.find[JsObject, JsObject](obj("id" -> id), None).one[Seq[Employment]]
}