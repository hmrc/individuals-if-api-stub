/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.individualsifapistub.repository.organisations

import play.api.libs.json.JsObject
import play.api.libs.json.Json.obj
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain.DuplicateException
import uk.gov.hmrc.individualsifapistub.domain.organisations.{NumberOfEmployeesEntry, NumberOfEmployeesRequest, NumberOfEmployeesResponse}
import uk.gov.hmrc.individualsifapistub.repository.MongoConnectionProvider
import uk.gov.hmrc.mongo.ReactiveRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NumberOfEmployeesRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[NumberOfEmployeesEntry, BSONObjectID]("number-of-employees",
    mongoConnectionProvider.mongoDatabase, NumberOfEmployeesEntry.numberOfEmployeesFormat){

  override lazy val indexes = Seq(
    Index(key = List("id" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
  )

  def create(request: NumberOfEmployeesResponse): Future[NumberOfEmployeesResponse] = {
    val id = s"${request.startDate}-${request.endDate}-" +
      request.references.map(e => s"${e.payeReference}-${e.districtNumber}").mkString("-")

    val entry = NumberOfEmployeesEntry(id, request)

    insert(entry) map (_ => request) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }
  }

  def find(request: NumberOfEmployeesRequest): Future[Option[NumberOfEmployeesResponse]] = {

    val id = s"${request.startDate}-${request.endDate}-" +
      request.references.map(e => s"${e.payeReference}-${e.districtNumber}").mkString("-")

    collection
      .find[JsObject, JsObject](obj("id" -> id), None)
      .one[NumberOfEmployeesEntry]
      .map(x => x.map(_.response))
  }

}
