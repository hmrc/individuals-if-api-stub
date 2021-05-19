package it.uk.gov.hmrc.individualsifapistub.organisations

import testUtils.RepositoryTestHelper
import uk.gov.hmrc.individualsifapistub.repository.organisations.CorporationTaxReturnDetailsRepository

class CorporationTaxReturnDetailsRepositorySpec extends RepositoryTestHelper  {
  val repository = fakeApplication.injector.instanceOf[CorporationTaxReturnDetailsRepository]

  "collection" should {
    "have a unique index on a requests utr" in {

    }

    "create" should {
      "create a CT Return Details repsonse with a valid utr" in {

      }

      "fail to create a duplicate" in {

      }
    }

  }

}
