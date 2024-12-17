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

package uk.gov.hmrc.individualsifapistub.repository.organisations

import org.mongodb.scala.MongoWriteException
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.Configuration
import uk.gov.hmrc.individualsifapistub.domain.DuplicateException
import uk.gov.hmrc.individualsifapistub.domain.organisations.{CTReturnDetailsEntry, CorporationTaxReturnDetailsResponse, CreateCorporationTaxReturnDetailsRequest}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc.preservingMdc

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorporationTaxReturnDetailsRepository @Inject() (mongo: MongoComponent, config: Configuration)(implicit
  ec: ExecutionContext
) extends PlayMongoRepository[CTReturnDetailsEntry](
      mongoComponent = mongo,
      collectionName = "corporation-tax-return-details",
      domainFormat = CTReturnDetailsEntry.format,
      indexes = Seq(
        IndexModel(
          ascending("id"),
          IndexOptions()
            .name("id")
            .expireAfter(config.get[FiniteDuration]("mongodb.cache-ttl.expiry-time").toSeconds, TimeUnit.SECONDS)
            .unique(true)
            .background(true)
        )
      ),
      replaceIndexes = true
    ) {
  def create(request: CreateCorporationTaxReturnDetailsRequest): Future[CorporationTaxReturnDetailsResponse] = {
    val response = CorporationTaxReturnDetailsResponse(
      request.utr,
      request.taxpayerStartDate,
      request.taxSolvencyStatus,
      request.accountingPeriods
    )
    val entry = CTReturnDetailsEntry(request.utr, response)

    preservingMdc {
      collection
        .insertOne(entry)
        .map(_ => response)
        .head()
        .recover {
          case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
        }
    }
  }

  def find(utr: String): Future[Option[CorporationTaxReturnDetailsResponse]] =
    preservingMdc {
      collection
        .find(equal("id", utr))
        .headOption()
        .map(x => x.map(_.response))
    }
}
