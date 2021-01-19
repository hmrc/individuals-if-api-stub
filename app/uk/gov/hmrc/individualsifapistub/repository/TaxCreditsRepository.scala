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
import reactivemongo.play.json._
import uk.gov.hmrc.individualsifapistub.domain.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.TaxCredits._
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxCreditsRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[TaxCreditsEntry, BSONObjectID]( "taxCredits",
                                                                mongoConnectionProvider.mongoDatabase,
                                                                TaxCredits.taxCreditsEntryFormat
                                                              ) {

  override lazy val indexes = Seq(
    Index(key = Seq(("id.nino", Text), ("id.trn", Text)), name = Some("nino-trn"), unique = true, background = true)
  )

  def create(idType: String,
             idValue: String,
             startDate: String,
             endDate: String,
             consumer: String,
             applications: Applications): Future[Applications] = {

    val overlapMap = Map(
      "LAA-C1-working-tax-credit"   -> "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "LAA-C2-working-tax-credit"   -> "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "LAA-C3-working-tax-credit"   -> "LAA-C1_LAA-C2_LAA-C3_working-tax-credit",
      "LAA-C1-child-tax-credit"     -> "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "LAA-C2-child-tax-credit"     -> "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "LAA-C3-child-tax-credit"     -> "LAA-C1_LAA-C2_LAA-C3_child-tax-credit",
      "HMCTS-C2-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "HMCTS-C3-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "LSANI-C1-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit",
      "LSANI-C3-working-tax-credit" -> "HMCTS-C2_HMCTS-C3_LSANI-C1_LSANI-C3_working-tax-credit"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, startDate, endDate, Some(consumer))
      case Trn => Identifier(None, Some(idValue), startDate, endDate, Some(consumer))
    }

    val tag = overlapMap.get(consumer).getOrElse("")
    val id  = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$tag"

    insert(TaxCreditsEntry(id, applications.applications)) map (_ => applications) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }

    for {
      _        <- collection.findAndRemove(obj("id" -> id)) map (_.result[TaxCreditsEntry])
      inserted <- insert(TaxCreditsEntry(id, applications.applications)) map (_ => applications)
    } yield inserted

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
        Some(idValue), None, startDate, endDate, fields.flatMap(value => fieldsMap.get(value))
      )
      case Trn => Identifier(
        None, Some(idValue), startDate, endDate, fields.flatMap(value => fieldsMap.get(value))
      )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("")
    val id  = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$tag"

    collection
      .find[JsObject, JsObject](obj("id" -> id), None)
      .one[Applications]

  }
}
