package com.smitestats.matchidservice.models

import io.circe.generic.JsonCodec

@JsonCodec
case class GetMatchIdsByQueueResponse(
    matchIdList: List[MatchIdResponse]
)

@JsonCodec
case class MatchIdResponse(
    `Active_Flag`: String,
    `Match`: String
)
