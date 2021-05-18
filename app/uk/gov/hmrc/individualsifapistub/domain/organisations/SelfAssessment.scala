package uk.gov.hmrc.individualsifapistub.domain.organisations

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.util.matching.Regex

case class TaxYear(taxYear: String, businessSaleTurnover: Double)

object SelfAssessment {
  val utrPattern: Regex = "^[0-9]{10}$".r
  implicit val createSelfAssessmentRequestFormat: Format[CreateSelfAssessmentRequest] = Format(
    (
      (JsPath \ "utr").readNullable[String](pattern(utrPattern, "UTR pattern is incorrect")) and
        (JsPath \ "taxPayerType").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
        (JsPath \ "taxSolvencyStatus").readNullable[String](minLength[String](1).keepAnd(maxLength[String](35))) and
        (JsPath \ "taxYears").readNullable[Seq[TaxYear]]
      )(CreateSelfAssessmentRequest.apply _),
    (
      (JsPath \ "utr").writeNullable[String] and
        (JsPath \ "taxPayerType").writeNullable[String] and
        (JsPath \ "taxSolvencyStatus").writeNullable[String] and
        (JsPath \ "taxYears").writeNullable[Seq[TaxYear]]
      )(unlift(CreateSelfAssessmentRequest.unapply))
  )
}

case class CreateSelfAssessmentRequest(utr: String, taxPayerType: String, taxSolvencyStatus: String, taxYears: Seq[TaxYear])
