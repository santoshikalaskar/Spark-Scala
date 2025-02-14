package services

import controllers.routes
import models.Utility
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Request}

object ClosePricePredictionService {
  /*
  This function converts the InputTuple to Json
  @params:inputTupleMessageAndValue Tuple2[Double,String]
  @return : jsonOutput[JsValue]
   */

  def convertToJson(
      inputTupleMessageAndValue: Tuple2[Double, String]
  ): JsValue = {
    try {
      val resultMap =
        Map(
          "messsage" -> inputTupleMessageAndValue._2,
          "value" -> inputTupleMessageAndValue._1.toString,
          "status" -> "200"
        )
      Json.toJson(resultMap)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        Json.toJson("")
    }
  }

  /*
  This function takes input as request, and using request calls the presentation layer arguments and pass to those business layer.
  @params:request Request
  @return : Tuple2[Double,String]
   */

  def predictPrice(
      request: Request[AnyContent]
  ): Tuple2[Double, String] = {
    try {
      val message = "The predicted result is"
      var closePrice: Double = 0
      //Calling input arguments using request
      val postvals = request.body.asFormUrlEncoded
      //Creating a sparkSessionObj
      val sparkSessionObj =
        Utility.UtilityClass.createSessionObject("Stock Prediction")
      postvals
        .map { args =>
          val openPrice = args("OpenPrice").head.toDouble
          val highPrice = args("HighPrice").head.toDouble
          val lowPrice = args("LowPrice").head.toDouble
          val volume = args("Volume").head.toDouble
          //Calling the
          closePrice = models.StockPricePredictionPythonModel.predictPrice(
            openPrice,
            highPrice,
            lowPrice,
            volume,
            sparkSessionObj
          )
        }
        .getOrElse(Redirect(routes.ClosePricePredictionController.homePage()))

      (closePrice, message)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        (1.0, "")
    }
  }

  /*
This function converts the InputTuple to String for XML reponse
@params:inputTupleMessageAndValue Tuple2[Double,String]
@return : String
   */
  def convertToXML(
      inputTupleMessageAndValue: Tuple2[Double, String]
  ): String = {
    "<xml><data><message>" + inputTupleMessageAndValue._2 + "</message>" + "<value>" + inputTupleMessageAndValue._1 + "</value></data><status>200</status></xml>"
  }

}
