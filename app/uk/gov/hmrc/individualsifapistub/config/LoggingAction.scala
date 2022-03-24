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

package uk.gov.hmrc.individualsifapistub.config

import akka.stream.Materializer
import play.api.Logging
import play.api.mvc.{ActionBuilderImpl, BodyParsers, Request, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LoggingAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext, mat: Materializer)
  extends ActionBuilderImpl(parser) with Logging {
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    val result = block(request)

    val requestLog =
      s"""Request to ${request.method} ${request.uri}
         |CorrelationId: ${request.headers.get("CorrelationId").getOrElse("")}
         |Request Body: ${request.body}""".stripMargin

    result.foreach { result =>
      responseLog(result).foreach { responseBody =>
        logger.info(
          s"""$requestLog
             |Response Status: ${result.header.status}
             |Response Content-Type: ${getContentType(result)}
             |Response Body: $responseBody""".stripMargin
        )
      }
    }
    result.failed.foreach { t =>
      logger.info(
        s"""$requestLog
           |
           |Exception: ${t.getMessage}""".stripMargin, t)
    }

    result
  }

  private def responseLog(result: Result): Future[String] = {
    val contentType = getContentType(result)
    val isLoggable = contentType == "application/json" || contentType == "plain/text"
    if (isLoggable) result.body.consumeData.map(_.utf8String) else Future.successful("")
  }

  private def getContentType(result: Result): String = {
    result
      .header
      .headers
      .get("Content-Type")
      .orElse(result.body.contentType)
      .map(_.toLowerCase)
      .flatMap(_.split(";").headOption)
      .getOrElse("")
  }
}