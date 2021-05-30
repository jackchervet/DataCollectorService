package com.smitestats.DataCollectorService.models

import io.circe.generic.JsonCodec

@JsonCodec
case class GetSessionResponse(
    session_id: String
)