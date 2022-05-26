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

import play.api.libs.json.Json.obj
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals.IncomePaye.incomePayeEntryFormat
import uk.gov.hmrc.individualsifapistub.domain.individuals.{IdType, Identifier, IncomePaye, IncomePayeEntry}
import uk.gov.hmrc.individualsifapistub.repository.MongoConnectionProvider
import uk.gov.hmrc.mongo.ReactiveRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomePayeRepository  @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[IncomePayeEntry, BSONObjectID]( "incomePaye",
    mongoConnectionProvider.mongoDatabase,
    incomePayeEntryFormat ) {


  override lazy val indexes = Seq(
    Index(key = List("id" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
  )

  def create(idType: String,
             idValue: String,
             startDate: String,
             endDate: String,
             useCase: String,
             request: IncomePaye): Future[IncomePaye] = {

    val useCaseMap = Map(
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3",
      "HMCTS-C3" -> "HMCTS-C2_HMCTS-C3",
      "LSANI-C1" -> "LSANI-C1_LSANI-C3",
      "LSANI-C3" -> "LSANI-C1_LSANI-C3"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, Some(startDate), Some(endDate), Some(useCase))
      case Trn => Identifier(None, Some(idValue), Some(startDate), Some(endDate), Some(useCase))
    }

    val tag = useCaseMap.get(useCase).getOrElse(useCase)
    val id  = s"${ident.nino.getOrElse(ident.trn.get)}-$startDate-$endDate-$tag"

    val incomePayeEntry = IncomePayeEntry(id, request)

    logger.info(s"Insert for cache key: $id - Income paye: ${Json.toJson(incomePayeEntry)}")

    insert(incomePayeEntry) map (_ => incomePayeEntry.incomePaye) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }

  }

  def findByTypeAndId(idType: String,
                      idValue: String,
                      startDate: String,
                      endDate: String,
                      fields: Option[String]): Future[Option[IncomePaye]] = {

    val fieldsMap = Map(
      "paye(employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,paidHoursWorked,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxDeductedOrRefunded,taxYear,taxablePay,taxablePayToDate,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),totalTaxToDate,weeklyPeriodNumber)" -> "LAA-C1",
      "paye(dednsFromNetPay,grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,paidHoursWorked,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxYear,taxablePayToDate,totalTaxToDate,weeklyPeriodNumber)" -> "LAA-C2",
      "paye(employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,paidHoursWorked,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxCode,taxDeductedOrRefunded,taxYear,taxablePay,taxablePayToDate,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),totalTaxToDate,weeklyPeriodNumber)" -> "LAA-C3",
      "paye(grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),paymentDate,payroll(id))" -> "LAA-C4",
      "paye(employee(hasPartner),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),payFrequency,paymentDate)" -> "HMCTS-C2_HMCTS-C3",
      "paye(employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),employerPayeRef,grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),paymentDate,payroll(id),taxablePay)" -> "HMCTS-C4",
      "paye(dednsFromNetPay,employee(hasPartner),employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),monthlyPeriodNumber,payFrequency,paymentDate,statutoryPayYTD(adoption,maternity,parentalBereavement,paternity),taxCode,taxDeductedOrRefunded,taxYear,taxablePay,taxablePayToDate,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),totalTaxToDate,weeklyPeriodNumber)" -> "LSANI-C1_LSANI-C3",
      "paye(employeeNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4),employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD),grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4),paymentDate,payroll(id),statutoryPayYTD(adoption,maternity,paternity),taxDeductedOrRefunded,taxablePay,totalEmployerNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4))" -> "NICTSEJO-C4"
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

    logger.info(s"Fetch income paye for cache key: $id")

    collection.find[JsObject, JsObject](obj("id" -> id), None)
      .one[IncomePayeEntry].map(value => value.map(_.incomePaye))

  }
}
