package com.smitestats.datacollectorservice.core

import scala.concurrent.ExecutionContext
import cats.effect.{ ContextShift, Blocker, IO }
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import com.smitestats.datacollectorservice.config.AppConfig
import com.smitestats.datacollectorservice.helpers.TemporalHelpers._
import org.slf4j.{ Logger, LoggerFactory }
import com.smitestats.datacollectorservice.helpers.IOUtils
import com.smitestats.datacollectorservice.models.MatchDataOutput
import software.amazon.awssdk.core.async.AsyncRequestBody

object S3 extends IOUtils {
    private val logger: Logger = LoggerFactory.getLogger("S3")

    def putObject(output: MatchDataOutput)(implicit ec: ExecutionContext, appConfig: AppConfig, cs: ContextShift[IO], blocker: Blocker, s3: S3AsyncClient): IO[Unit] = {
        for {
            _ <- IO { logger.info(s"Writing ${output.fileName} to S3...") }
            _ <- fromJavaFuture(s3.putObject(putObjectRequest(output.fileName), AsyncRequestBody.fromString(output.content))).handleErrorWith { e =>
                IO { logger.error(s"[ERROR] Failed to write file ${output.fileName} to S3 with error: ${e.getMessage()}") } *> IO.unit
            }
        } yield ()
    }

    def putObjectRequest(fileName: String)(implicit appConfig: AppConfig): PutObjectRequest = 
        PutObjectRequest.builder()
        .bucket(appConfig.downstreamS3)
        .key(s"${getDateTwoHoursPrev}/${fileName}")
        .build()
}
