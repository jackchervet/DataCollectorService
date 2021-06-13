package com.smitestats

import org.scalatest._
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import com.smitestats.datacollectorservice.core.DDB
import com.smitestats.datacollectorservice.models.GetMatchDetailsBatchResponse
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.smitestats.datacollectorservice.config.AppConfig

class DDBSpec extends AnyWordSpec with Matchers {

   lazy val matchDetail1 = GetMatchDetailsBatchResponse(
      Match = 123L,
      Entry_Datetime = "6/8/2021 5:40:32 PM",
      Item_Purch_1 = Some("Bluestone Pendant"),
      Item_Purch_2 = Some("Stone of Gaia"),
      Item_Purch_3 = Some("Shifter's Shield"),
      Item_Purch_4 = Some("Spectral Armor"),
      Item_Purch_5 = Some("Witchblade"),
      Item_Purch_6 = Some("Mystical Mail")
  )

  lazy val matchDetail2 = GetMatchDetailsBatchResponse(
      Match = 234L,
      Entry_Datetime = "6/8/2021 5:45:33 PM",
      Item_Purch_1 = Some("Ornate Arrow"),
      Item_Purch_2 = Some("Devourer's Gauntlet"),
      Item_Purch_3 = Some("Ninja Tabi"),
      Item_Purch_4 = Some("The Executioner"),
      Item_Purch_5 = Some("Wind Demon"),
      Item_Purch_6 = Some("Deathbringer")
  )

  lazy val matchDetail3 = GetMatchDetailsBatchResponse(
      Match = 345L,
      Entry_Datetime = "6/8/2021 2:22:54 AM",
      Item_Purch_1 = Some("Bumba's Hammer"),
      Item_Purch_2 = Some("The Crusher"),
      Item_Purch_3 = Some("Warrior Tabi"),
      Item_Purch_4 = Some("Brawler's Beatstick"),
      Item_Purch_5 = Some("Mantle of Discord"),
      Item_Purch_6 = Some("Heartseeker")
  )

  "DDB" should {
      "Create attributes" in {
        val map = DDB.matchDetailTransformer(GetMatchDetailsBatchResponse(Entry_Datetime = "6/8/2021 3:02:32 AM"))
        val timestamp = map.get("timestamp").get.n
        assert(timestamp == "1623121352000")
      }

      "Build valid BatchWriteRequest" in {
        val req = DDB.buildBatchWriteItemRequest("test", List(matchDetail1, matchDetail2, matchDetail3)).unsafeRunSync
        val items = req.requestItems()
        println(items)
        assert(true)
      }
  }
}
