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
import uk.gov.hmrc.individualsifapistub.domain.DuplicateException
import uk.gov.hmrc.individualsifapistub.domain.organisations.VatInformationEntry
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc.preservingMdc

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DAYS
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatInformationRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[VatInformationEntry](
      mongoComponent = mongo,
      collectionName = "vat-information",
      domainFormat = VatInformationEntry.format,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("id").unique(true).background(true)),
        IndexModel(ascending("createdAt"), IndexOptions().background(true).expireAfter(14, DAYS))
      )
    ) {
  def create(entry: VatInformationEntry): Future[VatInformationEntry] =
    preservingMdc {
      collection
        .insertOne(entry)
        .map(_ => entry)
        .head()
        .recover {
          case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
        }
    }

  def retrieve(vrn: String): Future[Option[VatInformationEntry]] =
    preservingMdc {
      collection
        .find(equal("id", vrn))
        .headOption()
    }
}
