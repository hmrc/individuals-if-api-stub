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
import play.api.libs.json.{JsObject, Json}
import play.api.libs.json.Json.obj
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.indexes.IndexType.Text
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import uk.gov.hmrc.individualsifapistub.domain.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.IncomeSa.incomeSaEntryFormat
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeSaRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[IncomeSaEntry, BSONObjectID]( "incomeSa",
                                                            mongoConnectionProvider.mongoDatabase,
                                                            incomeSaEntryFormat ) {

  override lazy val indexes = Seq(
    Index(key = List("id" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
  )

  def create(idType: String,
             idValue: String,
             startYear: String,
             endYear: String,
             useCase: String,
             request: IncomeSa): Future[IncomeSa] = {

    val useCaseMap = Map(
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3",
      "HMCTS-C3" -> "HMCTS-C2_HMCTS-C3",
      "LSANI-C1" -> "LSANI-C1_LSANI-C3",
      "LSANI-C3" -> "LSANI-C1_LSANI-C3",
      "RP2" -> "RP2_ECP",
      "ECP" -> "RP2_ECP"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, Some(startYear), Some(endYear), Some(useCase))
      case Trn => Identifier(None, Some(idValue), Some(startYear), Some(endYear), Some(useCase))
    }

    val tag = useCaseMap.get(useCase).getOrElse(useCase)
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$startYear-$endYear-$tag"

    val incomeSaRecord = IncomeSaEntry(id, request)

    Logger.info(s"Insert for cache key: $id - Income sa: ${Json.toJson(incomeSaRecord)}")

    insert(incomeSaRecord) map (_ => incomeSaRecord.incomeSa) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }

  }

  def findByTypeAndId(idType: String,
                      idValue: String,
                      startYear: String,
                      endYear: String,
                      fields: Option[String]): Future[Option[IncomeSa]] = {

    Logger.info(s"IncomeSaRepository - findByTypeAndId: fields: $fields")

    val fieldsMap = Map(
      "sa(returnList(address(line1,line2,line3,line4,postcode),busEndDate,busStartDate,caseStartDate,deducts(totalBusExpenses,totalDisallowBusExp),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),otherBusIncome,totalNIC,totalTaxPaid,tradingIncomeAllowance,turnover),taxYear)" -> "LAA-C1",
      "sa(returnList(busEndDate,busStartDate,caseStartDate,deducts(totalBusExpenses,totalDisallowBusExp),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),otherBusIncome,tradingIncomeAllowance,turnover),taxYear)" -> "LAA-C2",
      "sa(returnList(address(line1,line2,line3,line4,postcode),busEndDate,busStartDate,businessDescription,deducts(totalBusExpenses,totalDisallowBusExp),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),otherBusIncome,tradingIncomeAllowance,turnover),taxYear)" -> "LAA-C3",
      "sa(returnList(address(line1,line2,line3,line4,postcode),businessDescription,telephoneNumber),taxYear)" -> "LAA-C4",
      "sa(returnList(caseStartDate,income(foreign,foreignDivs,selfAssessment,selfEmployment,ukDivsAndInterest,ukInterest,ukProperty)),taxYear)" -> "HMCTS-C2_HMCTS-C3",
      "sa(returnList(address(line1,line2,line3,line4,postcode),businessDescription,caseStartDate,telephoneNumber),taxYear)" -> "HMCTS-C4",
      "sa(returnList(busEndDate,busStartDate,deducts(totalBusExpenses),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,selfEmployment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),receivedDate,totalNIC,totalTaxPaid),taxYear)" -> "LSANI-C1_LSANI-C3",
      "sa(returnList(address(line1,line2,line3,line4,postcode),businessDescription,caseStartDate,income(allEmployments,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,selfEmployment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),receivedDate,telephoneNumber),taxYear)" -> "NICTSEJO-C4",
      "sa(returnList(income(allEmployments,other,selfAssessment,selfEmployment),receivedDate,utr),taxYear)" -> "RP2_ECP"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, Some(startYear), Some(endYear), fields.flatMap(value => fieldsMap.get(value))
      )
      case Trn => Identifier(
        None, Some(idValue), Some(startYear), Some(endYear), fields.flatMap(value => fieldsMap.get(value))
      )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("TEST")
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$startYear-$endYear-$tag"

    Logger.info(s"Fetch income sa for cache key: $id")

    collection.find[JsObject, JsObject](obj("id" -> id), None)
      .one[IncomeSaEntry].map(value => value.map(_.incomeSa))
  }
}
