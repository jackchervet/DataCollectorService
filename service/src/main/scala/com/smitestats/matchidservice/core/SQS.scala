package com.smitestats.matchidservice.core

import cats.effect.IO
import com.amazonaws.services.sqs.model.SendMessageBatchRequest
import com.smitestats.matchidservice.config.AppConfig
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry

import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import com.amazonaws.services.sqs.AmazonSQS

import fs2._
import scala.concurrent.duration._
import cats.effect.Timer
import cats.effect.ContextShift
import cats.effect.Blocker

object SQS {
    val logger: Logger = LoggerFactory.getLogger("SQS")
    
    def sendBatches(matchIds: List[String])(implicit config: AppConfig, sqs: AmazonSQS): IO[Unit] = {
        for {
            url <- IO(sqs.getQueueUrl(config.downstreamSqs).getQueueUrl())
            _ <- Stream.emits(matchIds)
                .chunkN(10)
                .evalMap { chunk => 
                    for {
                        _ <- IO(logger.info(s"Building Request for matchIds: ${chunk.toList}"))
                        request <- buildRequest(url, chunk)
                        _ <- IO(sqs.sendMessageBatch(request)).handleErrorWith { e =>
                            IO(logger.error(s"[ERROR] Failed to send message batch for matchIds ${chunk.toList} to SQS... ${e}"))
                        }
                    } yield ()
                }
                .compile
                .drain
        } yield ()
    }

    def buildRequest(url: String, chunk: Chunk[String])(implicit config: AppConfig, sqs: AmazonSQS): IO[SendMessageBatchRequest] = {
        for {
            matchIds <- IO(chunk.toList)
        } yield {
            new SendMessageBatchRequest()
                .withQueueUrl(url)
                .withEntries(matchIds.zipWithIndex.map(m => new SendMessageBatchRequestEntry(m._2.toString(), m._1)).asJava)
        }
        
    }
        
}
