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

import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import reactivemongo.api.indexes.IndexType.Ascending
import testUtils.AddressHelpers
import uk.gov.hmrc.individualsifapistub.domain.{ContactDetail, CreateDetailsRequest, Details, DetailsResponse, Residence}
import uk.gov.hmrc.individualsifapistub.repository.DetailsRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import unit.uk.gov.hmrc.individualsifapistub.util.TestSupport

class DetailsRepositorySpec
    extends TestSupport
    with MongoSpecSupport
    with BeforeAndAfterEach with AddressHelpers{

  override lazy val fakeApplication = buildFakeApplication(
    Configuration("mongodb.uri" -> mongoUri))

  val repository = fakeApplication.injector.instanceOf[DetailsRepository]

  val idValue = "2432552635"
  val request = CreateDetailsRequest(
    Some(Seq(ContactDetail(9, "MOBILE TELEPHONE", "07123 987654"), ContactDetail(9,"MOBILE TELEPHONE", "07123 987655"))),
    Some(Seq(
      Residence(residenceType = Some("BASE"), address = createAddress(2)),
      Residence(residenceType = Some("NOMINATED"), address = createAddress(1))))
  )

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override def afterEach() {
  }

  "collection" should {
    "have a unique index on nino" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("nino") &&
        i.key == Seq("details.nino" -> Ascending) &&
        i.background &&
        i.unique
      }) should not be None
    }
    "have a unique index on trn" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("trn") &&
          i.key == Seq("details.trn" -> Ascending) &&
          i.background &&
          i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a details response with a nino" in {
      val details = Details(Some(idValue), None)
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)

      val result = await(repository.create("nino", idValue, request))

      result shouldBe detailsResponse
    }

    "create a details response with a trn" in {
      val details = Details(None, Some(idValue))
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)

      val result = await(repository.create("trn", idValue, request))

      result shouldBe detailsResponse
    }

    "fail to create duplicate details" in {
      val details = Details(None, Some(idValue))

      await(repository.create("trn", idValue, request))

      intercept[Exception](await(repository.create("trn", idValue, request)))
    }
  }

  "find by id and type" should {
    "return None when there are no details for a given nino" in {
      await(repository.findByIdAndType("nino", idValue)) shouldBe None
    }

    "return None when there are no details for a given trn" in {
      await(repository.findByIdAndType("trn", idValue)) shouldBe None
    }


    "return details when nino found" in {

      val details = Details(Some(idValue), None)
      val detailsResponse = DetailsResponse(details, request.contactDetails, request.residences)

      await(repository.create("nino", idValue, request))

      val result = await(repository.findByIdAndType("nino", idValue))

      result shouldBe Some(detailsResponse)
    }
  }
}
