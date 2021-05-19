package uk.gov.hmrc.individualsifapistub.controllers.organisations

import play.api.mvc.ControllerComponents
import uk.gov.hmrc.individualsifapistub.controllers.CommonController
import uk.gov.hmrc.individualsifapistub.domain.organisations.CorporationTaxReturnDetails._
import uk.gov.hmrc.individualsifapistub.domain.organisations.CreateCorporationTaxReturnDetailsRequest

class CorporationTaxReturnDetailsController(cc: ControllerComponents) extends CommonController(cc) {

  def create() = {
    withJsonBody[CreateCorporationTaxReturnDetailsRequest]{
      //TODO Add in service for CreateCorpTax
      false
    }
  }

  def retrieve() = {
    //TODO Add in service for CreateCorpTax
    false
  }

}
