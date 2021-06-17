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

package uk.gov.hmrc.individualsifapistub.repository.individuals

import play.api.Logger
import play.api.libs.json.Json.obj
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.Employments._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals.{EmploymentEntry, Employments, IdType, Identifier}
import uk.gov.hmrc.individualsifapistub.repository.MongoConnectionProvider
import uk.gov.hmrc.mongo.ReactiveRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class EmploymentRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[EmploymentEntry, BSONObjectID]( "employment",
                                                        mongoConnectionProvider.mongoDatabase,
                                                        createEmploymentEntryFormat) {

  override lazy val indexes = Seq(
    Index(key = List("id" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
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
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))
      case Trn => Identifier(None, Some(idValue), Some(startDate), Some(endDate), Some(useCase))
    }

    val tag = useCaseMap.get(useCase).getOrElse(useCase)
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$tag"

    Logger.info(s"Insert for cache key: $id - Employments: ${Json.toJson(employments.employments)}")

    insert(EmploymentEntry(id, employments.employments)) map (_ => employments) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }

  }

  def findByIdAndType(idType: String,
                      idValue: String,
                      startDate: String,
                      endDate: String,
                      fields: Option[String],
                      filter: Option[String]): Future[Option[Employments]] = {

    val fieldsMap = Map(
      "employments(employment(endDate,startDate))" -> "LAA-C1_LAA-C2",
      "employments(employer(name),employment(endDate,startDate))" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(endDate,startDate))" -> "LAA-C4",
      "employments(employment(endDate))" -> "HMCTS-C2_HMCTS-C3",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),districtNumber,name,schemeRef),employment(endDate,startDate))" -> "HMCTS-C4",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(startDate))" -> "NICTSEJO-C4",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,startDate),payments(date,paidTaxablePay))" -> "ECP",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,startDate),payments(date,paidTaxablePay))_filtered" -> "RP2"
    )

    val useCase:Option[String] = fields.flatMap(value => fieldsMap.get(value + (if (filter.isDefined) "_filtered" else "")))

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, Some(startDate), Some(endDate), useCase
      )
      case Trn => Identifier(
        None, Some(idValue), Some(startDate), Some(endDate), useCase
      )
    }

    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-${useCase.getOrElse("TEST")}"

    Logger.info(s"Fields: ${fields.getOrElse("")}")
    Logger.info(s"Filter: ${filter.getOrElse("")}")

    Logger.info(s"Fetch employments for cache key: $id")

    collection
      .find[JsObject, JsObject](obj("id" -> id), None)
      .one[Employments]
  }
}