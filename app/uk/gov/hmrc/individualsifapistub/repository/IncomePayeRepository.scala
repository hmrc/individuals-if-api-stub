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
import uk.gov.hmrc.individualsifapistub.domain.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.IncomePaye.incomePayeEntryFormat
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomePayeRepository  @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[IncomePayeEntry, BSONObjectID]( "incomePaye",
    mongoConnectionProvider.mongoDatabase,
    incomePayeEntryFormat ) {


  override lazy val indexes = Seq(
    Index(key = Seq(("id.nino", Text), ("id.trn", Text)), name = Some("nino-trn"), unique = true, background = true)
  )

  def create(idType: String,
             idValue: String,
             startDate: String,
             endDate: String,
             consumer: String,
             request: IncomePaye): Future[IncomePaye] = {

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, startDate, endDate, Some(consumer))
      case Trn => Identifier(None, Some(idValue), startDate, endDate, Some(consumer))
    }

    val id = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$consumer"

    val incomeSaEntry = IncomePayeEntry(id, request)

    insert(incomeSaEntry) map (_ => incomeSaEntry.incomePaye) recover {
      case WriteResult.Code(11000) => throw new DuplicateException
    }
  }

  def findByTypeAndId(idType: String,
                      idValue: String,
                      startDate: String,
                      endDate: String,
                      fields: Option[String]): Future[Option[IncomePaye]] = {

    // TODO - enhance for other use cases when the IF-Stub ticket is complete
    val fieldsMap = Map("paye(employeeNICs(inPayPeriod,inPayPeriod1,inPayPeriod3,inPayPeriod4,ytd1,ytd2,ytd3,ytd4)," +
      "employeePensionContribs(notPaid,notPaidYTD,paid,paidYTD)," +
      "grossEarningsForNICs(inPayPeriod1,inPayPeriod2,inPayPeriod3,inPayPeriod4)," +
      "monthlyPeriodNumber,paidHoursWorked,payFrequency,paymentDate," +
      "statutoryPayYTD(adoption,maternity,parentalBereavement,paternity)," +
      "taxDeductedOrRefunded,taxYear,taxablePay,taxablePayToDate," +
      "totalEmployerNICs(InPayPeriod1,InPayPeriod2,InPayPeriod3,InPayPeriod4,ytd1,ytd2,ytd3,ytd4)" +
      ",totalTaxToDate,weeklyPeriodNumber)" -> "LAA-C1")

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(
        Some(idValue), None, startDate, endDate, fields.flatMap(value => fieldsMap.get(value))
      )
      case Trn => Identifier(
        None, Some(idValue), startDate, endDate, fields.flatMap(value => fieldsMap.get(value))
      )
    }

    collection.find[JsObject, JsObject](obj("id" -> ident), None)
      .one[IncomePayeEntry].map(value => value.map(_.incomePaye))
  }
}
