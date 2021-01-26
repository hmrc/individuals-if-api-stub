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
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.Json.obj
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Text
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain.Employments._
import uk.gov.hmrc.individualsifapistub.domain.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class EmploymentRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[EmploymentEntry, BSONObjectID]( "employment",
                                                        mongoConnectionProvider.mongoDatabase,
                                                        createEmploymentEntryFormat) {

  override lazy val indexes = Seq(
    Index(key = Seq(("id", Text)), name = Some("cache-key"), unique = true, background = true)
  )

  def create(idType: String,
             idValue: String,
             startDate: String,
             endDate: String,
             useCase: String,
             employments: Employments): Future[Employments] = {

    val useCaseMap = Map(
      "LAA-C1"   -> "LAA-C1_LAA-C2",
      "LAA-C2"   -> "LAA-C1_LAA-C2",
      "LAA-C3"   -> "LAA-C3_LSANI-C1_LSANI-C3",
      "LSANI-C1" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "LSANI-C3" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3",
      "HMCTS-C3" -> "HMCTS-C2_HMCTS-C3"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))
      case Trn => Identifier(None, Some(idValue), Some(startDate), Some(endDate), Some(useCase))
    }

    val tag = useCaseMap.get(useCase).getOrElse(useCase)
    val id  = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$tag"

    for {
      _        <- collection.findAndRemove(obj("id" -> id)) map (_.result[EmploymentEntry])
      inserted <- insert(EmploymentEntry(id, employments.employments)) map (_ => employments)
    } yield inserted

  }

  def findByIdAndType(idType: String,
                      idValue: String,
                      startDate: String,
                      endDate: String,
                      fields: Option[String]): Future[Option[Employments]] = {

    val fieldsMap = Map(
      "employments(employment(endDate,startDate))" -> "LAA-C1_LAA-C2",
      "employments(employer(name),employment(endDate,startDate))" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(endDate,startDate))" -> "LAA-C4",
      "employments(employment(endDate))" -> "HMCTS-C2_HMCTS-C3",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),districtNumber,name,schemeRef),employment(endDate,startDate))" -> "HMCTS-C4",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(startDate))" -> "NICTSEJO-C4"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, Some(startDate), Some(endDate), fields.flatMap(value => fieldsMap.get(value))
      )
      case Trn => Identifier(
        None, Some(idValue), Some(startDate), Some(endDate), fields.flatMap(value => fieldsMap.get(value))
      )
    }

    Logger.debug(s"fields: ${fields}")

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("TEST")
    val id  = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$tag"

    Logger.debug(s"key: ${id}")

    collection
      .find[JsObject, JsObject](obj("id" -> id), None)
      .one[Employments]
  }
}