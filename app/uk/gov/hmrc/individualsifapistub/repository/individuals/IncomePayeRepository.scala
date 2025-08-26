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
import uk.gov.hmrc.individualsifapistub.domain.individuals.{IdType, Identifier, IncomePaye, IncomePayeEntry}
import uk.gov.hmrc.individualsifapistub.util.Dates
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mdc.Mdc.preservingMdc

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomePayeRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[IncomePayeEntry](
      mongoComponent = mongo,
      collectionName = "incomePaye",
      domainFormat = IncomePayeEntry.format,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("id").unique(true).background(true)),
        IndexModel(ascending("idValue"), IndexOptions().background(true))
      )
    ) with Logging {
  def create(
    idType: String,
    idValue: String,
    startDate: Option[String],
    endDate: Option[String],
    useCase: Option[String],
    request: IncomePaye
  ): Future[IncomePaye] = {
    val useCaseMap = Map(
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3",
      "HMCTS-C3" -> "HMCTS-C2_HMCTS-C3",
      "LSANI-C1" -> "LSANI-C1_LSANI-C3",
      "LSANI-C3" -> "LSANI-C1_LSANI-C3",
      "HO-V2"    -> "HO-V2"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, startDate, endDate, useCase)
      case Trn  => Identifier(None, Some(idValue), startDate, endDate, useCase)
    }

    val tag = useCaseMap.getOrElse(useCase.mkString, useCase.mkString)
    val id =
      s"${ident.nino.getOrElse(ident.trn.get)}-${startDate.mkString}-${endDate.mkString}-$tag-${UUID.randomUUID()}"

    val incomePayeEntry = IncomePayeEntry(id, request, idValue)

    logger.info(s"Insert for cache key: $id - Income paye: ${Json.toJson(incomePayeEntry)}")

    preservingMdc {
      collection
        .insertOne(incomePayeEntry)
        .map(_ => incomePayeEntry.incomePaye)
        .head()
        .recover {
          case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
        }
    }
  }

  def findByTypeAndId(
    idType: String,
    idValue: String,
    startDate: String,
    endDate: String,
    fields: Option[String]
  ): Future[Option[IncomePaye]] = {
    val fieldsMap = Map(
      "paye(employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,paidHoursWorked,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxDeductedOrRefunded,taxYear,taxablePay,taxablePayToDate,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),totalTaxToDate,weeklyPeriodNumber)" -> "LAA-C1",
      "paye(dednsFromNetPay,grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,paidHoursWorked,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxYear,taxablePayToDate,totalTaxToDate,weeklyPeriodNumber)" -> "LAA-C2",
      "paye(employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,paidHoursWorked,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxCode,taxDeductedOrRefunded,taxYear,taxablePay,taxablePayToDate,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),totalTaxToDate,weeklyPeriodNumber)" -> "LAA-C3",
      "paye(grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),paymentDate,payroll(id))" -> "LAA-C4",
      "paye(employee(hasPartner),employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),payFrequency,paymentDate,taxablePay)" -> "HMCTS-C2_HMCTS-C3",
      "paye(employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),employerPayeRef,grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),paymentDate,payroll(id),taxablePay)" -> "HMCTS-C4",
      "paye(dednsFromNetPay,employee(hasPartner),employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxCode,taxDeductedOrRefunded,taxYear,taxablePay,taxablePayToDate,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),totalTaxToDate,weeklyPeriodNumber)" -> "LSANI-C1_LSANI-C3",
      "paye(employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),paymentDate,payroll(id),statutoryPayYTD(adoption,maternity,paternity),taxDeductedOrRefunded,taxablePay,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4))" -> "NICTSEJO-C4",
      "paye(employerPayeRef,monthlyPeriodNumber,paymentDate,taxablePay,weeklyPeriodNumber)" -> "HO-V2"
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

    logger.info(s"Fetch income paye for cache key: $id")

    val interval = Dates.toInterval(startDate, Option(endDate).filter(_.nonEmpty))
    val query = queryPaye(id, idValue)

    preservingMdc {
      collection
        .find(query)
        .toFuture()
        .map {
          case Seq() => None
          case nonEmpty =>
            val payeEntries = nonEmpty
              .flatMap(_.incomePaye.paye.getOrElse(Seq.empty))
              .filter(_.paymentDate.forall(pd => interval.contains(pd.atStartOfDay())))
            Some(IncomePaye(Some(payeEntries)))
        }
    }
  }

  private def queryPaye(id: String, idValue: String) =
    or(
      idBasedSearch(id),
      deepSearch(idValue)
    )

  // legacy search
  private def idBasedSearch(id: String) = regex("id", s"^$id/")

  // deep search with nino
  private def deepSearch(idValue: String) =
    and(equal(s"idValue", idValue))
}
