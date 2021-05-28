package com.smitestats.DataCollectorService.models

import io.circe.generic.JsonCodec

@JsonCodec
case class GetMatchIdsByQueueResponse(
    matchIdList: List[String]
)
