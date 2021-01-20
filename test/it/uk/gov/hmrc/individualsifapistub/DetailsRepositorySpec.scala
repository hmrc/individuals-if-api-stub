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

package it.uk.gov.hmrc.individualsifapistub

import reactivemongo.api.indexes.IndexType.Text
import testUtils.{RepositoryTestHelper, TestHelpers}
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.repository.DetailsRepository

class DetailsRepositorySpec extends RepositoryTestHelper with TestHelpers {

  val repository = fakeApplication.injector.instanceOf[DetailsRepository]

  val ninoValue = "XH123456A"
  val trnValue = "2432552635"
  val startDate = "2020-01-01"
  val endDate = "2020-21-31"
  val useCase = "TEST"
  val fields = "some(values)"

  val request = CreateDetailsRequest(
    Some(Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9,"MOBILE TELEPHONE", "07123 987655"))),
    Some(Seq(
      Residence(residenceType = Some("BASE"), address = generateAddress(2)),
      Residence(residenceType = Some("NOMINATED"), address = generateAddress(1))))
  )

  "collection" should {

    "have a unique index on nino and trn" in {

      await(repository.collection.indexesManager.list()).find({ i =>
      {
        i.name.contains("nino-trn") &&
          i.key.exists(key => key._1 == "id.nino") &&
          i.key.exists(key => key._1 == "id.trn")
          i.background &&
          i.unique
      }
      }) should not be None
    }

  }

  "create" should {

    "create a details response with a nino" in {

      val ident = Identifier(Some(ninoValue), None, startDate, endDate, Some(useCase))
      val id = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$useCase"

      val detailsResponse = DetailsResponse(id, request.contactDetails, request.residences)

      val result = await(repository.create("nino", ninoValue, startDate, endDate, useCase, request))

      result shouldBe detailsResponse

    }

    "create a details response with a trn" in {

      val ident = Identifier(None, Some(trnValue), startDate, endDate, Some(useCase))
      val id = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$useCase"

      val detailsResponse = DetailsResponse(id, request.contactDetails, request.residences)

      val result = await(repository.create("trn", trnValue, startDate, endDate, useCase, request))

      result shouldBe detailsResponse

    }

    "fail to create duplicate details" in {

      await(repository.create("trn", trnValue, startDate, endDate, useCase, request))
      intercept[Exception](await(repository.create("trn", trnValue, startDate, endDate, useCase, request)))

    }
  }

  "find by id and type" should {

    "return None when there are no details for a given nino" in {

      await {
        repository.findByIdAndType("nino", ninoValue, startDate, endDate, Some(fields))
      } shouldBe None

    }

    "return None when there are no details for a given trn" in {

      await {
        repository.findByIdAndType("trn", trnValue, startDate, endDate, Some(fields))
      } shouldBe None

    }

    "return details when nino found" in {

      val ident = Identifier(Some(ninoValue), None, startDate, endDate, Some(useCase))
      val id = s"${ident.nino.getOrElse(ident.trn)}-$startDate-$endDate-$useCase"

      val detailsResponse = DetailsResponse(id, request.contactDetails, request.residences)

      await(repository.create("nino", ninoValue, startDate, endDate, useCase, request))

      val result = await(repository.findByIdAndType("nino", ninoValue, startDate, endDate, Some(fields)))

      result shouldBe Some(detailsResponse)

    }
  }
}
