package com.smitestats.datacollectorservice.models

import io.circe.generic.JsonCodec

@JsonCodec
case class GetMatchIdsByQueueResponse(
    `Active_Flag`: String,
    `Match`: String
)
