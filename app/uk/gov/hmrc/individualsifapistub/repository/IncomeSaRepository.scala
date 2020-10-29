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
import uk.gov.hmrc.individualsifapistub.domain.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.{DuplicateException, Id, IdType, IncomeSaRecord, IncomeSaResponse}
import uk.gov.hmrc.individualsifapistub.domain.JsonFormatters._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeSaRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[IncomeSaRecord, BSONObjectID]( "incomeSa",
                                                            mongoConnectionProvider.mongoDatabase,
                                                            incomeSaRecordFormat ) {

  override lazy val indexes = Seq(
    Index(key = Seq(("id.nino", Ascending)), name = Some("nino"), unique = true, background = true),
    Index(key = Seq(("id.trn", Ascending)), name = Some("trn"), unique = true, background = true)
  )

  def create(idType: String, idValue: String, request: IncomeSaResponse): Future[IncomeSaResponse] = {

    val id = IdType.parse(idType) match {
      case Nino => Id(Some(idValue), None)
      case Trn => Id(None, Some(idValue))
    }

    val incomeSaRecord = IncomeSaRecord(id, request)

    insert(incomeSaRecord) map (_ => incomeSaRecord.incomeSaResponse) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }
  }

  def findById(idType: String, idValue: String): Future[Option[IncomeSaResponse]] = {
    collection.find[JsObject, JsObject](obj("id" -> obj(idType -> idValue)), None)
      .one[IncomeSaResponse]

  }
}
