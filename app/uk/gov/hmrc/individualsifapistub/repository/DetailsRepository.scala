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
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import uk.gov.hmrc.individualsifapistub.domain.{CreateDetailsRequest, Details, DetailsResponse, JsonFormatters}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DetailsRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)
  extends ReactiveRepository[DetailsResponse, BSONObjectID]("details",
    mongoConnectionProvider.mongoDatabase,
    JsonFormatters.detailsResponseFormat) {

  override lazy val indexes = Seq(
    Index(key = Seq(("details.nino", Ascending)), name = Some("nino"), unique = true, background = true),
    Index(key = Seq(("details.trn", Ascending)), name = Some("trn"), unique = true, background = true)
  )

  def create(idType: String, idValue: String, createDetailsRequest: CreateDetailsRequest): Future[DetailsResponse] = {

    val details = idType match {
      case "nino" => Details(Some(idValue), None)
      case "trn" => Details(None, Some(idValue))
      case _ => throw new Exception()
    }

    val detailsResponse = DetailsResponse(details, createDetailsRequest.contactDetails, createDetailsRequest.residences)
    insert(detailsResponse) map (_ => detailsResponse)
  }

  def findByIdAndType(idType: String, idValue: String): Future[Option[DetailsResponse]] = {

    collection.find[JsObject, JsObject](obj("details" -> obj(idType -> idValue)), None).one[DetailsResponse]

  }
}
