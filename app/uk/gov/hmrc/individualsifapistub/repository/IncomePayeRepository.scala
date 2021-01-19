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

package uk.gov.hmrc.individualsifapistub.repository

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsObject
import play.api.libs.json.Json.obj
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Text
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.IncomePaye.incomePayeEntryFormat
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomePayeRepository  @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[IncomePayeEntry, BSONObjectID]( "incomePaye",
    mongoConnectionProvider.mongoDatabase,
    incomePayeEntryFormat ) {


  override lazy val indexes = Seq(
    Index(key = Seq(("id.nino", Text), ("id.trn", Text)), name = Some("nino-trn"), unique = true, background = true)
  )

  def create(idType: String,
             idValue: String,
             startDate: String,
             endDate: String,
             consumer: String,
             request: IncomePaye): Future[IncomePaye] = {

    val overlapMap = Map(
      "TODO" -> "TODO"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, startDate, endDate, Some(consumer))
      case Trn => Identifier(None, Some(idValue), startDate, endDate, Some(consumer))
    }

    val tag = overlapMap.get(consumer).getOrElse("")
    val id  = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$tag"

    val incomePayeEntry = IncomePayeEntry(id, request)

    for {
      _        <- collection.findAndRemove(obj("id" -> id)) map (_.result[IncomePayeEntry])
      inserted <- insert(incomePayeEntry) map (_ => incomePayeEntry.incomePaye)
    } yield inserted

  }

  def findByTypeAndId(idType: String,
                      idValue: String,
                      startDate: String,
                      endDate: String,
                      fields: Option[String]): Future[Option[IncomePaye]] = {

    val fieldsMap = Map(
      "TODO" -> "TODO"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, startDate, endDate, fields.flatMap(value => fieldsMap.get(value))
      )
      case Trn => Identifier(
        None, Some(idValue), startDate, endDate, fields.flatMap(value => fieldsMap.get(value))
      )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("")
    val id  = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$tag"

    collection.find[JsObject, JsObject](obj("id" -> id), None)
      .one[IncomePayeEntry].map(value => value.map(_.incomePaye))

  }
}
