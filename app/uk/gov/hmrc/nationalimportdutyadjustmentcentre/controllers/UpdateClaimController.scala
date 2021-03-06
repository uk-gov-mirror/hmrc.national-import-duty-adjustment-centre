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

package uk.gov.hmrc.nationalimportdutyadjustmentcentre.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.connectors.{MicroserviceAuthConnector, UpdateCaseConnector}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.eis.{ApiError, EISUpdateCaseError, EISUpdateCaseSuccess}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.models.{
  UpdateClaimResponse,
  UpdateClaimResult,
  UpdateEISClaimRequest
}
import uk.gov.hmrc.nationalimportdutyadjustmentcentre.services.FileTransferService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class UpdateClaimController @Inject() (
  val authConnector: MicroserviceAuthConnector,
  cc: ControllerComponents,
  updateCaseConnector: UpdateCaseConnector,
  fileTransferService: FileTransferService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with AuthActions with WithCorrelationId {

  def update(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withAuthorised {
      withCorrelationId { correlationId: String =>
        withJsonBody[UpdateEISClaimRequest] { updateClaimRequest: UpdateEISClaimRequest =>
          updateCaseConnector.updateClaim(updateClaimRequest.eisRequest, correlationId) flatMap { eisResponse =>
            val updateClaimResponse: Future[UpdateClaimResponse] = eisResponse match {
              case success: EISUpdateCaseSuccess =>
                fileTransferService.transferFiles(success.CaseID, correlationId, updateClaimRequest.uploadedFiles) map {
                  uploadResults =>
                    UpdateClaimResponse(
                      correlationId = correlationId,
                      processingDate = Some(success.ProcessingDate),
                      result = Some(UpdateClaimResult(success.CaseID, uploadResults))
                    )
                }
              case error: EISUpdateCaseError =>
                Future(
                  UpdateClaimResponse(
                    correlationId = correlationId,
                    processingDate = Some(error.errorDetail.timestamp),
                    error = Some(
                      ApiError(errorCode = error.errorDetail.errorCode, errorMessage = error.errorDetail.errorMessage)
                    )
                  )
                )
            }

            updateClaimResponse map { response =>
              Ok(Json.toJson(response))
            }

          }

        }
      }
    }
  }

}
