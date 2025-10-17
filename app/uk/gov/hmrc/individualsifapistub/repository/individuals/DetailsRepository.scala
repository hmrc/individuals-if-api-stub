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

package uk.gov.hmrc.individualsifapistub.repository.individuals

import org.mongodb.scala.MongoWriteException
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mdc.Mdc.preservingMdc

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DetailsRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[DetailsResponse](
      mongoComponent = mongo,
      collectionName = "details",
      domainFormat = DetailsResponse.format,
      indexes = Seq(
        IndexModel(ascending("details"), IndexOptions().name("id").unique(true).background(true))
      )
    ) with Logging {
  def create(
    idType: String,
    idValue: String,
    useCase: String,
    createDetailsRequest: CreateDetailsRequest
  ): Future[DetailsResponseNoId] = {
    val useCaseMap = Map(
      "LAA-C3-residences"        -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "LAA-C4-residences"        -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "HMCTS-C3-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "HMCTS-C4-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "LSANI-C1-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "LSANI-C3-residences"      -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "NICTSEJO-C4-residences"   -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "SCTS-residences"          -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "LAA-C4-contact-details"   -> "LAA-C4_HMCTS-C4_SCTS-contact-details",
      "HMCTS-C4-contact-details" -> "LAA-C4_HMCTS-C4_SCTS-contact-details",
      "SCTS-contact-details"     -> "LAA-C4_HMCTS-C4_SCTS-contact-details"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, None, None, Some(useCase))
      case Trn  => Identifier(None, Some(idValue), None, None, Some(useCase))
    }

    val tag = useCaseMap.getOrElse(useCase, useCase)
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$tag"

    val detailsResponse = if (useCase.contains("residences")) {
      DetailsResponse(id, None, createDetailsRequest.residences)
    } else {
      DetailsResponse(id, createDetailsRequest.contactDetails, None)
    }

    logger.info(s"Insert for cache key: $id - Details: ${Json.toJson(detailsResponse)}")

    preservingMdc {
      collection
        .insertOne(detailsResponse)
        .map(_ => DetailsResponseNoId(detailsResponse.contactDetails, detailsResponse.residence))
        .head()
        .recover {
          case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
        }
    }
  }

  def findByIdAndType(idType: String, idValue: String, fields: Option[String]): Future[Option[DetailsResponse]] = {
    def fieldsMap = Map(
      "residences(address(line1,line2,line3,line4,line5,postcode),noLongerUsed,type)" -> "LAA-C3_LAA-C4_HMCTS-C3_HMCTS-C4_LSANI-C1_LSANI-C3_NICTSEJO-C4_SCTS-residences",
      "contactDetails(code,detail,type)" -> "LAA-C4_HMCTS-C4_SCTS-contact-details"
    )

    val ident = IdType.parse(idType) match {
      case Nino =>
        Identifier(
          Some(idValue),
          None,
          None,
          None,
          fields.flatMap(value => fieldsMap.get(value))
        )
      case Trn =>
        Identifier(
          None,
          Some(idValue),
          None,
          None,
          fields.flatMap(value => fieldsMap.get(value))
        )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("TEST")
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$tag"

    logger.info(s"Fetch details for cache key: $id")

    preservingMdc {
      collection.find(equal("details", id)).headOption()
    }
  }
}
