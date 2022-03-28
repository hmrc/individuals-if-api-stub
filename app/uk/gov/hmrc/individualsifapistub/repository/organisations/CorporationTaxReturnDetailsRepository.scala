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

package uk.gov.hmrc.individualsifapistub.repository.organisations

import play.api.libs.json.JsObject
import play.api.libs.json.Json.obj
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain.DuplicateException
import uk.gov.hmrc.individualsifapistub.domain.organisations.{CTReturnDetailsEntry, CorporationTaxReturnDetailsResponse, CreateCorporationTaxReturnDetailsRequest}
import uk.gov.hmrc.individualsifapistub.repository.MongoConnectionProvider
import uk.gov.hmrc.mongo.ReactiveRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CorporationTaxReturnDetailsRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[CTReturnDetailsEntry, BSONObjectID]("corporation-tax-return-details",
    mongoConnectionProvider.mongoDatabase, CTReturnDetailsEntry.ctReturnDetailsEntryFormat){

    override lazy val indexes = Seq(
        Index(key = List("id" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
    )

    def create(request: CreateCorporationTaxReturnDetailsRequest) = {
        val response = CorporationTaxReturnDetailsResponse(request.utr, request.taxpayerStartDate, request.taxSolvencyStatus, request.accountingPeriods)
        val entry = CTReturnDetailsEntry(request.utr, response)

        insert(entry) map (_ => response) recover {
            case WriteResult.Code(11000) => throw new DuplicateException
        }
    }

    def find(utr: String) = {
        collection
          .find[JsObject, JsObject](obj("id" -> utr), None)
          .one[CTReturnDetailsEntry]
          .map(x => x.map(_.response))
    }

}
