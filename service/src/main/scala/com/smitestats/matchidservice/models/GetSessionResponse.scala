package com.smitestats.matchidservice.models

import io.circe.generic.JsonCodec

@JsonCodec
case class GetSessionResponse(
    session_id: String
)