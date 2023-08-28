package featuremanagement.api

import featuremanagement.grpc.EvaluationServiceGrpc.EvaluationService
import featuremanagement.grpc.Feature._
import featuremanagement.grpc.{AvailableFeatures, EvaluationServiceGrpc, Feature, ShowStateRequest}
import io.grpc.{Server, ServerBuilder}
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

object Main {
  def main(args: Array[String]): Unit = {
    val ec = scala.concurrent.ExecutionContext.global
    val server = new ApiServer(9001, ec)
    server.start()
    server.awaitTermination()

    sys.addShutdownHook {
      server.stop()
    }
  }
}

class ApiServer(port: Int, ec: ExecutionContext) extends LogSupport { self =>
  private var server: Server = _

  def start(): Unit = {
    server = ServerBuilder
      .forPort(port)
      .addService(EvaluationService.bindService(EvaluationServiceImpl, ec))
      .build()
      .start()
    info(s"starting server at port ${server.getPort}")
    info("--------------------------------------")
    info(
      "grpcurl -plaintext -d '{\"token\": \"partial\"}' -proto ./api_schema/api.proto 127.0.0.1:50051 featuremanagement.grpc.EvaluationService/ListAvailableFeature"
    )
  }

  def awaitTermination(): Unit =
    if (server != null) {
      server.awaitTermination()
    }

  def stop(): Unit =
    if (server != null) {
      info("stopping Server...")
      server.shutdown()
    }
}

object EvaluationServiceImpl extends EvaluationServiceGrpc.EvaluationService {

  /** 渡されたトークンをもとに、利用可能な機能のコードを列挙する
    */
  override def listAvailableFeature(request: ShowStateRequest): Future[AvailableFeatures] = {
    val features = request.token match {
      case "all"     => Feature.values
      case "partial" => Seq(DOC, SEARCH, SPREADSHEET)
      case _         => Seq.empty
    }
    Future.successful(AvailableFeatures(features))
  }
}
