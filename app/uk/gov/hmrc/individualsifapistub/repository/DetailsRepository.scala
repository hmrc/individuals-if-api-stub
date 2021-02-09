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
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.indexes.IndexType.Text
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import uk.gov.hmrc.individualsifapistub.domain.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DetailsRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[DetailsResponse, BSONObjectID]("details",
    mongoConnectionProvider.mongoDatabase,
    JsonFormatters.detailsResponseFormat) {

  override lazy val indexes = Seq(
    Index(key = List("details" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
  )

  def create(idType: String,
             idValue: String,
             useCase: String,
             createDetailsRequest: CreateDetailsRequest): Future[DetailsResponseNoId] = {

    val useCaseMap = Map(
      "LAA-C3-residences"        -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "LAA-C4-residences"        -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "HMCTS-C3-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "HMCTS-C4-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "LSANI-C1-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "LSANI-C4-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "NICTSEJO-C4-residences"   -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "LAA-C4-contact-details"   -> "LAA-C4_HMCTS-C4-contact-details",
      "HMCTS-C4-contact-details" -> "LAA-C4_HMCTS-C4-contact-details"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, None, None, Some(useCase))
      case Trn => Identifier(None, Some(idValue), None, None, Some(useCase))
    }

    val tag = useCaseMap.get(useCase).getOrElse(useCase)
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$tag"

    val detailsResponse = useCase.contains("residences") match {
      case true => {
        DetailsResponse(id, None, createDetailsRequest.residences)
      }
      case _    => {
        DetailsResponse(id, createDetailsRequest.contactDetails, None)
      }
    }

    insert(detailsResponse) map (_ => DetailsResponseNoId(detailsResponse.contactDetails, detailsResponse.residences)) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }

  }

  def findByIdAndType(idType: String,
                      idValue: String,
                      fields: Option[String]): Future[Option[DetailsResponse]] = {

    def fieldsMap = Map(
      "residences(address(line1,line2,line3,line4,line5,postcode),noLongerUsed,type)" -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4-residences",
      "contactDetails(code,detail,type)" -> "LAA-C4_HMCTS-C4-contact-details"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, None, None, fields.flatMap(value => fieldsMap.get(value))
      )
      case Trn => Identifier(
        None, Some(idValue), None, None, fields.flatMap(value => fieldsMap.get(value))
      )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("TEST")
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$tag"

    collection.find[JsObject, JsObject](obj("details" ->id), None).one[DetailsResponse]

  }
}
