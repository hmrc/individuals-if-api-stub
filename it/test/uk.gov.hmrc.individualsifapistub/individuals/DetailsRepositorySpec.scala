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

package it.uk.gov.hmrc.individualsifapistub.individuals

import testUtils.{RepositoryTestHelper, TestHelpers}
import uk.gov.hmrc.individualsifapistub.domain.individuals._
import uk.gov.hmrc.individualsifapistub.repository.individuals.DetailsRepository

class DetailsRepositorySpec extends RepositoryTestHelper with TestHelpers {

  val repository = fakeApplication.injector.instanceOf[DetailsRepository]

  val ninoValue = "XH123456A"
  val trnValue = "2432552635"
  val useCase = "TEST"
  val fields = "some(values)"

  val request = CreateDetailsRequest(
    Some(
      Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9, "MOBILE TELEPHONE", "07123 987655"))
    ),
    None
  )

  "collection" should {

    "have a unique index on nino and trn" in {

      repository.indexes
        .find { i =>
          i.getOptions.getName.contains("id") &&
          i.getKeys.toBsonDocument.getFirstKey == "details" &&
          i.getOptions.isBackground &&
          i.getOptions.isUnique
        } should not be None
    }

  }

  "create" should {

    "create a details response with a nino" in {
      val returnVal = DetailsResponseNoId(request.contactDetails, request.residences)

      val result = await(repository.create("nino", ninoValue, useCase, request))

      result shouldBe returnVal

    }

    "create a details response with a trn" in {
      val returnVal = DetailsResponseNoId(request.contactDetails, request.residences)

      val result = await(repository.create("trn", trnValue, useCase, request))

      result shouldBe returnVal

    }

    "fail to create duplicate" in {
      await(repository.create("nino", ninoValue, useCase, request))
      intercept[Exception](await(repository.create("nino", ninoValue, useCase, request)))
    }

  }

  "find by id and type" should {

    "return None when there are no details for a given nino" in {

      await {
        repository.findByIdAndType("nino", ninoValue, Some(fields))
      } shouldBe None

    }

    "return None when there are no details for a given trn" in {

      await {
        repository.findByIdAndType("trn", trnValue, Some(fields))
      } shouldBe None

    }

    "return details when nino found" in {

      val ident = Identifier(Some(ninoValue), None, None, None, Some(useCase))
      val id = s"${ident.nino.getOrElse(ident.trn.get)}-$useCase"

      val detailsResponse = DetailsResponse(id, request.contactDetails, request.residences)

      await(repository.create("nino", ninoValue, useCase, request))

      val result = await(repository.findByIdAndType("nino", ninoValue, Some(fields)))

      result shouldBe Some(detailsResponse)

    }
  }
}
