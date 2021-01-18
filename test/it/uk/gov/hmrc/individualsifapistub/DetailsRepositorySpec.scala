/*
 * Copyright 2020 HM Revenue & Customs
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
      val details = Identifier(Some(ninoValue), None)
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)

      val result = await(repository.create("nino", ninoValue, request))

      result shouldBe detailsResponse
    }

    "create a details response with a trn" in {
      val details = Identifier(None, Some(trnValue))
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)

      val result = await(repository.create("trn", trnValue, request))

      result shouldBe detailsResponse
    }

    "fail to create duplicate details" in {
      await(repository.create("trn", trnValue, request))
      intercept[Exception](await(repository.create("trn", trnValue, request)))
    }
  }

  "find by id and type" should {
    "return None when there are no details for a given nino" in {
      await(repository.findByIdAndType("nino", ninoValue)) shouldBe None
    }

    "return None when there are no details for a given trn" in {
      await(repository.findByIdAndType("trn", trnValue)) shouldBe None
    }

    "return details when nino found" in {

      val details = Identifier(Some(ninoValue), None)
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)

      await(repository.create("nino", ninoValue, request))

      val result = await(repository.findByIdAndType("nino", ninoValue))
      result shouldBe Some(detailsResponse)
    }
  }
}
