package com.smitestats.datacollectorservice.core

import scala.concurrent.ExecutionContext
import com.smitestats.datacollectorservice.config.AppConfig
import cats.effect.ContextShift
import org.http4s.client.Client
import scalacache.Cache
import cats.effect.Blocker
import cats.effect.IO
import com.smitestats.datacollectorservice.helpers.SessionHelper
import com.smitestats.datacollectorservice.clients.SmiteApiClient
import com.smitestats.datacollectorservice.models.QueueType
import fs2.Stream
import com.smitestats.datacollectorservice.models.GetMatchDetailsBatchResponse
import java.nio.file.Paths
import fs2.Pipe
import org.slf4j.{Logger, LoggerFactory}
import java.nio.file.StandardOpenOption
import com.smitestats.datacollectorservice.helpers.StatHelpers

object FileWriter {
    val logger: Logger = LoggerFactory.getLogger("FileWriteHelpers")
    val headerRow: String = "Match_Id,God_Name,God_Id,Role,Item_1_Id,Item_1_Name,Item_2_Id,Item_2_Name,Item_3_Id,Item_3_Name,Item_4_Id,Item_4_Name,Item_5_Id,Item_5_Name,Item_6_Id,Item_6_Name,Active_1_Id,Active_1_Name,Active_2_Id,Active_2_Name,Active_3_Id,Active_3_Name,Active_4_Id,Active_4_Name,Player_Damage,Max_Player_Damage,Player_Damage_Score,Damage_Mitigated,Max_Damage_Mitigated,Damage_Mitigated_Score,KDA,Max_KDA,KDA_Score,Gold_Earned,Max_Gold_Earned,Gold_Earned_Score,Structure_Damage,Max_Structure_Damage,Structure_Damage_Score,Auto_Attack_Damage,Max_Auto_Attack_Damage,Auto_Attack_Damage_Score,Magical_Damage,Max_Magical_Damage,Magical_Damage_Score,Physical_Damage,Max_Physical_Damage,Physical_Damage_Score,Win\n"

    def writeFile(items: List[List[GetMatchDetailsBatchResponse]])(implicit config: AppConfig, cs: ContextShift[IO], blocker: Blocker): IO[Unit] = {
        for {
            _ <- Stream.emit(headerRow)
                .through(fs2.text.utf8Encode)
                .through(fs2.io.file.writeAll[IO](Paths.get("matchData_large.csv"), blocker))
                .compile
                .drain  
            _ <- Stream.emits(items)
                .through(toCSVRow)
                .through(fs2.text.utf8Encode)
                .through(fs2.io.file.writeAll[IO](Paths.get("matchData_large.csv"), blocker, Seq(StandardOpenOption.APPEND)))
                .compile
                .drain               
        } yield ()
    }

    def toCSVRow: Pipe[IO, List[GetMatchDetailsBatchResponse], String] = stream => {
        stream
            .evalMap { matchData => IO { (matchData, StatHelpers.findMaxValues(matchData)) }  }
            .evalMap { tuple => 
                IO {
                    tuple._1.map { playerData =>
                        List(s"${playerData.Match},",
                            s"${playerData.Reference_Name.getOrElse("na")},",
                            s"${playerData.GodId.getOrElse(-1L)},",
                            s"${playerData.Role.getOrElse("na")},",
                            s"${playerData.ItemId1.getOrElse(-1L)},",
                            s"${playerData.Item_Purch_1.getOrElse("na")},",
                            s"${playerData.ItemId2.getOrElse(-1L)},",
                            s"${playerData.Item_Purch_2.getOrElse("na")},",
                            s"${playerData.ItemId3.getOrElse(-1L)},",
                            s"${playerData.Item_Purch_3.getOrElse("na")},",
                            s"${playerData.ItemId4.getOrElse(-1L)},",
                            s"${playerData.Item_Purch_4.getOrElse("na")},",
                            s"${playerData.ItemId5.getOrElse(-1L)},",
                            s"${playerData.Item_Purch_5.getOrElse("na")},",
                            s"${playerData.ItemId6.getOrElse(-1L)},",
                            s"${playerData.Item_Purch_6.getOrElse("na")},",
                            s"${playerData.ActiveId1.getOrElse(-1L)},",
                            s"${playerData.Item_Active_1.getOrElse("na")},",
                            s"${playerData.ActiveId2.getOrElse(-1L)},",
                            s"${playerData.Item_Active_2.getOrElse("na")},",
                            s"${playerData.ActiveId3.getOrElse(-1L)},",
                            s"${playerData.Item_Active_3.getOrElse("na")},",
                            s"${playerData.ActiveId4.getOrElse(-1L)},",
                            s"${playerData.Item_Active_4.getOrElse("na")},",
                            s"${playerData.Damage_Player.getOrElse(-1L)},",
                            s"${tuple._2("playerDamage")},",
                            s"${playerData.Damage_Player.getOrElse(0L)/tuple._2("playerDamage")},",
                            s"${playerData.Damage_Mitigated.getOrElse(-1L)},",
                            s"${tuple._2("damageMitigated")},",
                            s"${playerData.Damage_Mitigated.getOrElse(0L)/tuple._2("damageMitigated")},",
                            s"${StatHelpers.getKDA(playerData)},",
                            s"${tuple._2("KDA")},",
                            s"${StatHelpers.getKDA(playerData)/tuple._2("KDA")},",
                            s"${playerData.Gold_Earned.getOrElse(-1L)},",
                            s"${tuple._2("goldEarned")},",
                            s"${playerData.Gold_Earned.getOrElse(0L)/tuple._2("goldEarned")},",
                            s"${playerData.Structure_Damage.getOrElse(-1L)},",
                            s"${tuple._2("structureDamage")},",
                            s"${playerData.Structure_Damage.getOrElse(0L)/tuple._2("structureDamage")},",
                            s"${playerData.Damage_Done_In_Hand.getOrElse(-1L)},",
                            s"${tuple._2("autoAttackDamage")},",
                            s"${playerData.Damage_Done_In_Hand.getOrElse(0L)/tuple._2("autoAttackDamage")},",
                            s"${playerData.Damage_Done_Magical.getOrElse(-1L)},",
                            s"${tuple._2("magicalDamage")},",
                            s"${playerData.Damage_Done_Magical.getOrElse(0L)/tuple._2("magicalDamage")},",
                            s"${playerData.Damage_Done_Physical.getOrElse(-1L)},",
                            s"${tuple._2("physicalDamage")},",
                            s"${playerData.Damage_Done_Physical.getOrElse(0L)/tuple._2("physicalDamage")},",
                            s"${playerData.Win_Status.map(_ match { case "Winner" => 1; case _ => 0 }).getOrElse("na")}\n").fold[String]("")(_ + _)
                    }
                    .fold[String]("")(_ + _)
                }
            }

    }
}