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

package uk.gov.hmrc.individualsifapistub.repository.organisations

import org.mongodb.scala.MongoWriteException
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.individualsifapistub.domain.DuplicateException
import uk.gov.hmrc.individualsifapistub.domain.organisations.{NumberOfEmployeesEntry, NumberOfEmployeesRequest, NumberOfEmployeesResponse}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NumberOfEmployeesRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[NumberOfEmployeesEntry](
      mongoComponent = mongo,
      collectionName = "number-of-employees",
      domainFormat = NumberOfEmployeesEntry.format,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("id").unique(true).background(true))
      )
    ) {

  def create(request: NumberOfEmployeesResponse): Future[NumberOfEmployeesResponse] = {
    val id = s"${request.startDate}-${request.endDate}-" +
      request.references.map(e => s"${e.payeReference}-${e.districtNumber}").mkString("-")

    val entry = NumberOfEmployeesEntry(id, request)

    collection
      .insertOne(entry)
      .map(_ => request)
      .head()
      .recover {
        case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
      }
  }

  def find(request: NumberOfEmployeesRequest): Future[Option[NumberOfEmployeesResponse]] = {

    val id = s"${request.startDate}-${request.endDate}-" +
      request.references.map(e => s"${e.payeReference}-${e.districtNumber}").mkString("-")

    collection
      .find(equal("id", id))
      .headOption()
      .map(x => x.map(_.response))
  }

}
