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
import uk.gov.hmrc.individualsifapistub.domain.organisations.{CreateSelfAssessmentReturnDetailRequest, SelfAssessmentReturnDetailEntry, SelfAssessmentReturnDetailResponse}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mdc.Mdc.preservingMdc

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelfAssessmentReturnDetailRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[SelfAssessmentReturnDetailEntry](
      mongoComponent = mongo,
      collectionName = "self-assessment-return-details",
      domainFormat = SelfAssessmentReturnDetailEntry.format,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("id").unique(true).background(true))
      )
    ) {
  def create(request: CreateSelfAssessmentReturnDetailRequest): Future[SelfAssessmentReturnDetailResponse] = {
    val response = SelfAssessmentReturnDetailResponse(
      request.utr,
      request.startDate,
      request.taxpayerType,
      request.taxSolvencyStatus,
      request.taxyears
    )
    val entry = SelfAssessmentReturnDetailEntry(request.utr, response)

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

  def find(utr: String): Future[Option[SelfAssessmentReturnDetailResponse]] =
    preservingMdc {
      collection
        .find(equal("id", utr))
        .headOption()
        .map(x => x.map(_.response))
    }
}
