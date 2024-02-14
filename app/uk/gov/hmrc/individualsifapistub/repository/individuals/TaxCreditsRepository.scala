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
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc.preservingMdc

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxCreditsRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[TaxCreditsEntry](
      mongoComponent = mongo,
      collectionName = "taxCredits",
      domainFormat = TaxCreditsEntry.format,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("id").unique(true).background(true))
      )
    ) with Logging {
  def create(
    idType: String,
    idValue: String,
    startDate: String,
    endDate: String,
    useCase: String,
    applications: Applications): Future[Applications] = {
    val useCaseMap = Map(
      "LAA-C1-working-tax-credit"   -> "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "LAA-C2-working-tax-credit"   -> "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "LAA-C3-working-tax-credit"   -> "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "LAA-C1-child-tax-credit"     -> "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "LAA-C2-child-tax-credit"     -> "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "LAA-C3-child-tax-credit"     -> "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "HMCTS-C2-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "HMCTS-C3-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "LSANI-C1-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "LSANI-C3-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "HMCTS-C2-child-tax-credit"   -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit",
      "HMCTS-C3-child-tax-credit"   -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit",
      "LSANI-C1-child-tax-credit"   -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit",
      "LSANI-C3-child-tax-credit"   -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))
      case Trn  => Identifier(None, Some(idValue), Some(startDate), Some(endDate), Some(useCase))
    }

    val tag = useCaseMap.getOrElse(useCase, useCase)
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$tag"
    val entry = TaxCreditsEntry(id, applications.applications)

    logger.info(s"Insert for cache key: $id - Tax Credits: ${Json.toJson(entry)}")

    preservingMdc {
      collection
        .insertOne(entry)
        .map(_ => applications)
        .head()
        .recover {
          case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
        }
    }
  }

  def findByIdAndType(
    idType: String,
    idValue: String,
    startDate: String,
    endDate: String,
    fields: Option[String]): Future[Option[Applications]] = {
    def fieldsMap = Map(
      "applications(awards(childTaxCredit(childCareAmount),payProfCalcDate,payments(amount,endDate,frequency,startDate,postedDate,tcType),totalEntitlement,workingTaxCredit(amount,paidYTD)))" ->
        "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "applications(awards(childTaxCredit(babyAmount,childCareAmount,ctcChildAmount,familyAmount,paidYTD),payProfCalcDate,payments(amount,endDate,frequency,startDate,postedDate,tcType),totalEntitlement))" ->
        "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "applications(awards(childTaxCredit(childCareAmount),payProfCalcDate,payments(amount,endDate,frequency,postedDate,startDate,tcType),totalEntitlement,workingTaxCredit(amount,paidYTD)),id)" ->
        "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "applications(awards(childTaxCredit(babyAmount,childCareAmount,ctcChildAmount,familyAmount,paidYTD),payProfCalcDate,payments(amount,endDate,frequency,postedDate,startDate,tcType),totalEntitlement),id)" ->
        "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit"
    )

    val ident = IdType.parse(idType) match {
      case Nino =>
        Identifier(
          Some(idValue),
          None,
          Some(startDate),
          Some(endDate),
          fields.flatMap(value => fieldsMap.get(value))
        )
      case Trn =>
        Identifier(
          None,
          Some(idValue),
          Some(startDate),
          Some(endDate),
          fields.flatMap(value => fieldsMap.get(value))
        )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("TEST")
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$tag"

    logger.info(s"Fetch tax credits for cache key: $id")

    preservingMdc {
      collection
        .find(equal("id", id))
        .headOption()
        .map(_.map(entry => Applications(entry.applications)))
    }
  }
}
