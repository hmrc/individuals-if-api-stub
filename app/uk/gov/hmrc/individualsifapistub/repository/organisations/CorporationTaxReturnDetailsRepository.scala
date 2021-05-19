package uk.gov.hmrc.individualsifapistub.repository.organisations

import play.api.libs.json.JsObject
import play.api.libs.json.Json.obj
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.individualsifapistub.domain.DuplicateException
import uk.gov.hmrc.individualsifapistub.domain.individuals.Applications
import uk.gov.hmrc.individualsifapistub.domain.organisations.{CTReturnDetailsEntry, CorporationTaxReturnDetailsResponse, CreateCorporationTaxReturnDetailsRequest}
import uk.gov.hmrc.individualsifapistub.repository.MongoConnectionProvider
import uk.gov.hmrc.mongo.ReactiveRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CorporationTaxReturnDetailsRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[CTReturnDetailsEntry, BSONObjectID]("corporation-tax-return-details",
    mongoConnectionProvider.mongoDatabase, CTReturnDetailsEntry.ctReturnDetailsEntryFormat){

    override lazy val indexes = Seq(
        Index(key = List("id" -> IndexType.Ascending), name = Some("id"), unique = true, background = true)
    )

    def create(request: CreateCorporationTaxReturnDetailsRequest) = {
        val response = CorporationTaxReturnDetailsResponse(request.utr, request.taxpayerStartDate, request.taxSolvencyStatus, request.accountingPeriods)
        val entry = CTReturnDetailsEntry(request.utr, response)

        insert(entry) map (_ => response) recover {
            case WriteResult.Code(11000) => throw new DuplicateException
        }
    }

    def find(utr: String) = {
        collection
          .find[JsObject, JsObject](obj("id" -> utr), None)
          .one[CTReturnDetailsEntry]
          .map(x => x.map(_.response))
    }

}
