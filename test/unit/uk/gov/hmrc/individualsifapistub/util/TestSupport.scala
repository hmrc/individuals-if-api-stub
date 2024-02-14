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

package unit.uk.gov.hmrc.individualsifapistub.util

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ControllerComponents, PlayBodyParsers}
import play.api.{Application, Configuration, Play}
import uk.gov.hmrc.individualsifapistub.config.LoggingAction

import scala.concurrent.ExecutionContext

trait TestSupport extends UnitSpec with BeforeAndAfterAll {

  lazy val additionalConfig = Configuration()

  def buildFakeApplication(extraConfig: Configuration): Application =
    new GuiceApplicationBuilder()
      .configure(
        Configuration(
          ConfigFactory.parseString(
            """
              | metrics.jvm = false
              |""".stripMargin
          )
        ) withFallback extraConfig)
      .build()

  lazy val fakeApplication: Application = buildFakeApplication(additionalConfig)

  implicit lazy val materializer: Materializer = fakeApplication.materializer

  lazy val controllerComponents: ControllerComponents = fakeApplication.injector.instanceOf[ControllerComponents]

  implicit lazy val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]

  lazy val bodyParsers: PlayBodyParsers = fakeApplication.injector.instanceOf[PlayBodyParsers]

  lazy val loggingAction = fakeApplication.injector.instanceOf[LoggingAction]

  override def beforeAll(): Unit = {
    Play.start(fakeApplication)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    Play.stop(fakeApplication)
    super.afterAll()
  }

}
