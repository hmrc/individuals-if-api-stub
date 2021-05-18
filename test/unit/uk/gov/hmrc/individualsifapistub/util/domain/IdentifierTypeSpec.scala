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

package unit.uk.gov.hmrc.individualsifapistub.util.domain

import testUtils.TestHelpers
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType.{Nino, Trn}
import uk.gov.hmrc.individualsifapistub.domain._
import uk.gov.hmrc.individualsifapistub.domain.individuals.IdType
import unit.uk.gov.hmrc.individualsifapistub.util.UnitSpec

class IdentifierTypeSpec extends UnitSpec with TestHelpers {

  "IdType" should {
    "parse 'nino' successfully" in {
      IdType.parse("nino") shouldBe Nino
    }

    "parse 'trn' successfully" in {
      IdType.parse("trn") shouldBe Trn
    }

    "throw exception if parsed value is not 'trn' or 'nino'" in {
      intercept[IllegalArgumentException](IdType.parse("other"))
    }
  }

  "Nino" should {
    "convert to string correctly" in {
      Nino.toString shouldBe "nino"
    }
  }

  "Trn" should {
    "convert to string correctly" in {
      Trn.toString shouldBe "trn"
    }
  }
}
