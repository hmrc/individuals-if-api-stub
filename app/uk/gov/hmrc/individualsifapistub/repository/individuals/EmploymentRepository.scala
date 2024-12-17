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
import play.api.{Configuration, Logging}
import play.api.libs.json.Json
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.util.Dates
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc.preservingMdc

import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmploymentRepository @Inject() (mongo: MongoComponent, config: Configuration)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[EmploymentEntry](
      collectionName = "employment",
      mongoComponent = mongo,
      domainFormat = EmploymentEntry.format,
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
    ) with Logging {
  def create(
    idType: String,
    idValue: String,
    startDate: Option[String],
    endDate: Option[String],
    useCase: Option[String],
    employments: Employments
  ): Future[Employments] = {
    val useCaseMap = Map(
      "LAA-C1"   -> "LAA-C1_LAA-C2",
      "LAA-C2"   -> "LAA-C1_LAA-C2",
      "LAA-C3"   -> "LAA-C3_LSANI-C1_LSANI-C3",
      "LSANI-C1" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "LSANI-C3" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "HMCTS-C2" -> "HMCTS-C2_HMCTS-C3",
      "HMCTS-C3" -> "HMCTS-C2_HMCTS-C3",
      "HO-RP2"   -> "HO-RP2",
      "HO-ECP"   -> "HO-ECP",
      "HO-V2"    -> "HO-V2"
    )

    val ident = IdType.parse(idType) match {
      case Nino => Identifier(Some(idValue), None, startDate, endDate, useCase)
      case Trn  => Identifier(None, Some(idValue), startDate, endDate, useCase)
    }

    val filterKey = useCase match {
      case Some("HO-RP2") => convertToFilterKey(employments)
      case _              => ""
    }

    val tag = useCaseMap.getOrElse(useCase.mkString, useCase.mkString)
    val id =
      s"${ident.nino.getOrElse(ident.trn.get)}-${startDate.mkString}-${endDate.mkString}-$tag$filterKey-${UUID.randomUUID()}"

    logger.info(s"Insert for cache key: $id - Employments: ${Json.toJson(employments.employments)}")

    preservingMdc {
      collection
        .insertOne(EmploymentEntry(id, employments.employments, Some(idValue)))
        .map(_ => employments)
        .head()
        .recover {
          case ex: MongoWriteException if ex.getError.getCode == 11000 => throw new DuplicateException
        }
    }
  }

  def findByIdAndType(
    idType: String,
    idValue: String,
    startDateStr: String,
    endDateStr: String,
    fields: Option[String],
    filter: Option[String]
  ): Future[Option[Employments]] = {
    val fieldsMap = Map(
      "employments(employment(endDate,startDate))"                -> "LAA-C1_LAA-C2",
      "employments(employer(name),employment(endDate,startDate))" -> "LAA-C3_LSANI-C1_LSANI-C3",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(endDate,startDate))" -> "LAA-C4",
      "employments(employment(endDate))" -> "HMCTS-C2_HMCTS-C3",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,startDate))" -> "HMCTS-C4",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employment(startDate))" -> "NICTSEJO-C4",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate),payments(date,paidTaxablePay))" -> "HO-ECP",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate),payments(date,paidTaxablePay))_filtered" -> "HO-RP2",
      "employments(employer(address(line1,line2,line3,line4,line5,postcode),name),employerRef,employment(endDate,payFrequency,startDate))" -> "HO-V2"
    )

    val useCase: Option[String] =
      fields.flatMap(value => fieldsMap.get(value + (if (filter.isDefined) "_filtered" else "")))

    val ident = IdType.parse(idType) match {
      case Nino =>
        Identifier(
          Some(idValue),
          None,
          Some(startDateStr),
          Some(endDateStr),
          useCase
        )
      case Trn =>
        Identifier(
          None,
          Some(idValue),
          Some(startDateStr),
          Some(endDateStr),
          useCase
        )
    }

    val mappedUseCase = if (filter.isDefined) {
      useCase.map(unmappedUseCase => unmappedUseCase + filter.map(filtered => s"-$filtered").mkString)
    } else {
      useCase
    }

    val id = s"${ident.nino.getOrElse(ident.trn.get)}-$startDateStr-$endDateStr-${mappedUseCase.getOrElse("TEST")}"

    logger.info(s"Fetch employments for cache key: $id")

    val startDate = Dates.asLocalDate(startDateStr)
    val endDate = Dates.asLocalDate(endDateStr)

    preservingMdc {
      collection
        .find(deepSearch(idValue))
        .toFuture()
        .map { employmentEntries =>
          val filterByEmployerRef = filter.exists(_.contains("employerRef"))
          val employments = employmentEntries
            .flatMap(entry => entry.employments)
            .groupBy(employment => (employment.employer, employment.employerRef, employment.employment))
            .flatMap { case ((employer, employerRef, employmentDetail), employments) =>
              if (!filterByEmployerRef || (employerRef.nonEmpty && filter.exists(_.contains(employerRef.mkString)))) {
                val interval = Dates.toInterval(startDate, endDate)
                val payments = employments
                  .flatMap(_.payments.getOrElse(Seq.empty))
                  .filter(_.date.exists(date => interval.contains(date.atStartOfDay())))
                if (payments.nonEmpty || employmentDateOverlaps(employmentDetail, startDate, endDate))
                  Some(Employment(employer, employerRef, employmentDetail, payments.headOption.map(_ => payments)))
                else
                  None
              } else
                None
            }
            .toSeq

          employments.headOption.map(_ => Employments(employments))
        }
        .flatMap {
          case result @ Some(_) =>
            Future.successful(result)
          case None =>
            collection
              .find(idBasedSearch(id))
              .headOption()
              .map(_.map(entry => Employments(entry.employments)))
        }
    }
  }

  // legacy search
  private def idBasedSearch(id: String) = regex("id", s"^$id")

  private def deepSearch(idValue: String) = equal(s"idValue", idValue)

  private def convertToFilterKey(employments: Employments): String = {
    val empRef = employments.employments.headOption.flatMap(_.employerRef)
    empRef.map(x => s"-employments[]/employerRef eq '$x'").mkString
  }

  // this is a workaround to make sure that an employment overlaps the queried time interval
  private def employmentDateOverlaps(
    employmentDetail: Option[EmploymentDetail],
    startDate: LocalDate,
    endDate: LocalDate
  ): Boolean =
    employmentDetail.exists { detail =>
      val employmentInterval = Dates.toInterval(detail.startDate.getOrElse("1900-01-01"), detail.endDate)
      val queryInterval = Dates.toInterval(startDate, endDate)
      queryInterval.overlaps(employmentInterval)
    }
}
