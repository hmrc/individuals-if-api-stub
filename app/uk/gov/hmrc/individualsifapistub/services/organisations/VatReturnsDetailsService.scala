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

package uk.gov.hmrc.individualsifapistub.services.organisations

import uk.gov.hmrc.individualsifapistub.domain.RecordNotFoundException
import uk.gov.hmrc.individualsifapistub.domain.organisations.{VatReturnsDetails, VatReturnsDetailsEntry}
import uk.gov.hmrc.individualsifapistub.repository.organisations.{VatInformationRepository, VatReturnsDetailsRepository}
import uk.gov.hmrc.individualsifapistub.util.DateTimeProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatReturnsDetailsService @Inject()(
  repository: VatReturnsDetailsRepository,
  vatInformationRepository: VatInformationRepository,
  dateTimeProvider: DateTimeProvider)(implicit ec: ExecutionContext) {
  def retrieve(vrn: String): Future[Option[VatReturnsDetailsEntry]] = repository.retrieve(vrn)

  def create(vrn: String, vatReturnDetails: VatReturnsDetails): Future[VatReturnsDetailsEntry] =
    vatInformationRepository.retrieve(vrn).flatMap {
      case Some(_) =>
        repository.create(VatReturnsDetailsEntry(vrn, vatReturnDetails, dateTimeProvider.now()))
      case None =>
        Future.failed(RecordNotFoundException(s"VAT organisation with VRN $vrn does not exist"))
    }
}
