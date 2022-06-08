/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.Employments._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals.{EmploymentEntry, Employments, IdType, Identifier}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class EmploymentRepository @Inject()(mongo: MongoComponent)(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[EmploymentEntry](collectionName = "employment",
    mongoComponent = mongo,
    domainFormat = createEmploymentEntryFormat,
    indexes = Seq(
      IndexModel(ascending("id"), IndexOptions().name("id").unique(true).background(true))
    )
  ) {

  private val logger: Logger = Logger(getClass)

  def create(idType: String,
             idValue: String,
             startDate: String,
             endDate: String,
             useCase: String,
             employments: Employments): Future[Employments] = {

    val useCaseMap = Map(
      "LAA-C1" -> "LAA-C1_LAA-C2",
      "LAA-C2" -> "LAA-C1_LAA-C2",
      "LAA-C3" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "LSANI-C1" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "LSANI-C3" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3",
      "HMCTS-C3" -> "HMCTS-C2_HMCTS-C3",
      "HO-RP2" -> "HO-RP2",
      "HO-ECP" -> "HO-ECP"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))
      case Trn => Identifier(None, Some(idValue), Some(startDate), Some(endDate), Some(useCase))
    }

    val filterKey = useCase match {
      case "HO-RP2" => convertToFilterKey(employments)
      case _ => ""
    }

    val tag = useCaseMap.getOrElse(useCase, useCase)
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$tag$filterKey"

    logger.info(s"Insert for cache key: $id - Employments: ${Json.toJson(employments.employments)}")

    collection
      .insertOne(EmploymentEntry(id, employments.employments))
      .map(_ => employments)
      .head()
      .recover {
        case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
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
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,startDate))" -> "HMCTS-C4",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(startDate))" -> "NICTSEJO-C4",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate),payments(date,paidTaxablePay))" -> "HO-ECP",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate),payments(date,paidTaxablePay))_filtered" -> "HO-RP2"
    )

    val useCase: Option[String] = fields.flatMap(value => fieldsMap.get(value + (if (filter.isDefined) "_filtered" else "")))

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, Some(startDate), Some(endDate), useCase
      )
      case Trn => Identifier(
        None, Some(idValue), Some(startDate), Some(endDate), useCase
      )
    }

    val mappedUseCase = if (filter.isDefined) {
      useCase.map(unmappedUseCase => unmappedUseCase + filter.map(filtered => s"-$filtered").getOrElse(""))
    } else {
      useCase
    }

    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-${mappedUseCase.getOrElse("TEST")}"

    logger.info(s"Fetch employments for cache key: $id")

    collection
      .find(equal("id", id))
      .headOption()
      .map(_.map(entry => Employments(entry.employments)))
  }

  private def convertToFilterKey(employments: Employments): String = {
    val empRef = employments.employments.headOption.flatMap(_.employerRef)
    empRef.map(x => s"-employments[]/employerRef eq '$x'").getOrElse("")
  }
}