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
import uk.gov.hmrc.play.http.logging.Mdc.preservingMdc

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeSaRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[IncomeSaEntry](
      mongoComponent = mongo,
      collectionName = "incomeSa",
      domainFormat = IncomeSaEntry.format,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("id").unique(true).background(true)),
        IndexModel(ascending("idValue"), IndexOptions().name("idValue").unique(false).background(true))
      )
    ) with Logging {
  def create(
    idType: String,
    idValue: String,
    startYear: Option[String],
    endYear: Option[String],
    useCase: Option[String],
    request: IncomeSa
  ): Future[IncomeSa] = {
    val useCaseMap = Map(
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3",
      "HMCTS-C3" -> "HMCTS-C2_HMCTS-C3",
      "LSANI-C1" -> "LSANI-C1_LSANI-C3",
      "LSANI-C3" -> "LSANI-C1_LSANI-C3",
      "HO-ECP"   -> "HO-ECP",
      "HO-V2"    -> "HO-V2"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, startYear, endYear, useCase)
      case Trn  => Identifier(None, Some(idValue), startYear, endYear, useCase)
    }

    val tag = useCaseMap.getOrElse(useCase.mkString, useCase.mkString)
    val id =
      s"${ident.nino.getOrElse(ident.trn.get)}-${startYear.mkString}-${endYear.mkString}-$tag-${UUID.randomUUID()}"

    val incomeSaRecord = IncomeSaEntry(id, request, Some(idValue))

    logger.info(s"Insert for cache key: $id - Income sa: ${Json.toJson(incomeSaRecord)}")

    preservingMdc {
      collection
        .insertOne(incomeSaRecord)
        .map(_ => incomeSaRecord.incomeSa)
        .head()
        .recover {
          case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
        }
    }
  }

  def findByTypeAndId(
    idType: String,
    idValue: String,
    startYear: String,
    endYear: String,
    fields: Option[String]
  ): Future[Option[IncomeSa]] = {
    logger.info(s"IncomeSaRepository - findByTypeAndId: fields: $fields")

    val fieldsMap = Map(
      "sa(returnList(address(line1,line2,line3,line4,postcode),busEndDate,busStartDate,caseStartDate,deducts(totalBusExpenses,totalDisallowBusExp),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),otherBusIncome,totalNIC,totalTaxPaid,tradingIncomeAllowance,turnover),taxYear)" -> "LAA-C1",
      "sa(returnList(busEndDate,busStartDate,caseStartDate,deducts(totalBusExpenses,totalDisallowBusExp),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),otherBusIncome,tradingIncomeAllowance,turnover),taxYear)" -> "LAA-C2",
      "sa(returnList(address(line1,line2,line3,line4,postcode),busEndDate,busStartDate,businessDescription,deducts(totalBusExpenses,totalDisallowBusExp),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),otherBusIncome,tradingIncomeAllowance,turnover),taxYear)" -> "LAA-C3",
      "sa(returnList(address(line1,line2,line3,line4,postcode),businessDescription,telephoneNumber),taxYear)" -> "LAA-C4",
      "sa(returnList(caseStartDate,income(foreign,foreignDivs,selfAssessment,selfEmployment,ukDivsAndInterest,ukInterest,ukProperty)),taxYear)" -> "HMCTS-C2_HMCTS-C3",
      "sa(returnList(address(line1,line2,line3,line4,postcode),businessDescription,caseStartDate,telephoneNumber),taxYear)" -> "HMCTS-C4",
      "sa(returnList(busEndDate,busStartDate,deducts(totalBusExpenses),income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,selfEmployment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),receivedDate,totalNIC,totalTaxPaid),taxYear)" -> "LSANI-C1_LSANI-C3",
      "sa(returnList(address(line1,line2,line3,line4,postcode),businessDescription,caseStartDate,income(allEmployments,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,selfEmployment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),receivedDate,telephoneNumber),taxYear)" -> "NICTSEJO-C4",
      "sa(returnList(income(allEmployments,other,selfAssessment,selfEmployment),receivedDate,utr),taxYear)" -> "HO-ECP",
      "sa(returnList(address(line1,line2,line3,line4,postcode),businessDescription,caseStartDate,income(allEmployments,foreign,foreignDivs,lifePolicies,other,partnerships,pensions,selfAssessment,selfEmployment,shares,trusts,ukDivsAndInterest,ukInterest,ukProperty),receivedDate,telephoneNumber,utr),taxYear)" -> "HO-V2"
    )

    val ident = IdType.parse(idType) match {
      case Nino =>
        Identifier(
          Some(idValue),
          None,
          Some(startYear),
          Some(endYear),
          fields.flatMap(value => fieldsMap.get(value))
        )
      case Trn =>
        Identifier(
          None,
          Some(idValue),
          Some(startYear),
          Some(endYear),
          fields.flatMap(value => fieldsMap.get(value))
        )
    }

    val tag = fields.flatMap(value => fieldsMap.get(value)).getOrElse("TEST")
    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startYear-$endYear-$tag"

    logger.info(s"Fetch income sa for cache key: $id")

    preservingMdc {
      collection
        .find(
          deepSearch(
            idValue,
            Option(startYear).filter(_.nonEmpty).map(_.toInt).getOrElse(1000),
            Option(endYear).filter(_.nonEmpty).map(_.toInt).getOrElse(3000)
          )
        )
        .map(_.incomeSa.sa.getOrElse(Seq.empty))
        .foldLeft(Seq.empty[SaTaxYearEntry])(_ ++ _)
        .toFuture()
        .flatMap {
          case entries if entries.nonEmpty =>
            Future.successful(Some(IncomeSa(Some(entries))))
          case _ =>
            // fallback to legacy search
            collection
              .find(idBasedSearch(id))
              .headOption()
              .map(_.map(_.incomeSa))
        }
    }
  }

  private def idBasedSearch(id: String) = regex("id", s"^$id")

  // deep search with nino and paymentDate range
  private def deepSearch(idValue: String, startYear: Int, endYear: Int) =
    and(
      equal(s"idValue", idValue),
      elemMatch(
        "incomeSaResponse.sa",
        and(
          gte("taxYear", startYear),
          lte("taxYear", endYear)
        )
      )
    )
}
