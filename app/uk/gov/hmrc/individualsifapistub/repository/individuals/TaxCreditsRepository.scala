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
import reactivemongo.play.json._
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals.TaxCredits._
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.repository.MongoConnectionProvider
import uk.gov.hmrc.mongo.ReactiveRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxCreditsRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[TaxCreditsEntry, BSONObjectID]( "taxCredits",
                                                                mongoConnectionProvider.mongoDatabase,
                                                                TaxCredits.taxCreditsEntryFormat
                                                              ) {

  override lazy val indexes = Seq(
    Index(key = List("id" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
  )

  def create(idType: String,
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
      "HMCTS-C2-child-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit",
      "HMCTS-C3-child-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit",
      "LSANI-C1-child-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit",
      "LSANI-C3-child-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))
      case Trn => Identifier(None, Some(idValue), Some(startDate), Some(endDate), Some(useCase))
    }

    val tag = useCaseMap.get(useCase).getOrElse(useCase)
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$tag"
    val entry = TaxCreditsEntry(id, applications.applications)

    Logger.info(s"Insert for cache key: $id - Tax Credits: ${Json.toJson(entry)}")

    insert(entry) map (_ => applications) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }

  }

  def findByIdAndType(idType: String,
                      idValue: String,
                      startDate: String,
                      endDate: String,
                      fields: Option[String]): Future[Option[Applications]] = {

    def fieldsMap = Map(
      "applications(awards(childTaxCredit(childCareAmount),payProfCalcDate,payments(amount,endDate,frequency,startDate,tcType),totalEntitlement,workingTaxCredit(amount,paidYTD)))" ->
        "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "applications(awards(childTaxCredit(babyAmount,childCareAmount,ctcChildAmount,familyAmount,paidYTD),payProfCalcDate,payments(amount,endDate,frequency,startDate,tcType),totalEntitlement))" ->
        "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "applications(awards(childTaxCredit(childCareAmount),payProfCalcDate,payments(amount,endDate,frequency,startDate,tcType),totalEntitlement,workingTaxCredit(amount,paidYTD)),id)" ->
        "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "applications(awards(childTaxCredit(babyAmount,childCareAmount,ctcChildAmount,familyAmount,paidYTD),payProfCalcDate,payments(amount,endDate,frequency,startDate,tcType),totalEntitlement),id)" ->
        "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_child-tax-credit"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, Some(startDate), Some(endDate), fields.flatMap(value => fieldsMap.get(value))
      )
      case Trn => Identifier(
        None, Some(idValue), Some(startDate), Some(endDate), fields.flatMap(value => fieldsMap.get(value))
      )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("TEST")
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$tag"

    Logger.info(s"Fetch tax credits for cache key: $id")

    collection
      .find[JsObject, JsObject](obj("id" -> id), None)
      .one[Applications]

  }
}
