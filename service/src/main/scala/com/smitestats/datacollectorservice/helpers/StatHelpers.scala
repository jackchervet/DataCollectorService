package com.smitestats.datacollectorservice.helpers

import com.smitestats.datacollectorservice.models.GetMatchDetailsBatchResponse

object StatHelpers {
  def findMaxValues(matchData: List[GetMatchDetailsBatchResponse]): Map[String, Double] = {
        Map(
            "playerDamage" -> matchData.sortBy(_.Damage_Player.getOrElse(0L).toDouble).reverse.head.Damage_Player.getOrElse(0L).toDouble,
            "damageMitigated" -> matchData.sortBy(_.Damage_Mitigated.getOrElse(0L).toDouble).reverse.head.Damage_Mitigated.getOrElse(0L).toDouble,
            "KDA" -> getKDA(matchData.sortBy(getKDA(_)).reverse.head),
            "goldEarned" -> matchData.sortBy(_.Gold_Earned.getOrElse(0L).toDouble).reverse.head.Gold_Earned.getOrElse(0L).toDouble,
            "structureDamage" -> matchData.sortBy(_.Structure_Damage.getOrElse(0L).toDouble).reverse.head.Structure_Damage.getOrElse(0L).toDouble,
            "autoAttackDamage" -> matchData.sortBy(_.Damage_Done_In_Hand.getOrElse(0L).toDouble).reverse.head.Damage_Done_In_Hand.getOrElse(0L).toDouble,
            "magicalDamage" -> matchData.sortBy(_.Damage_Done_Magical.getOrElse(0L).toDouble).reverse.head.Damage_Done_Magical.getOrElse(0L).toDouble,
            "physicalDamage" -> matchData.sortBy(_.Damage_Done_Physical.getOrElse(0L).toDouble).reverse.head.Damage_Done_Physical.getOrElse(0L).toDouble
        )
    }

    def getKDA(data: GetMatchDetailsBatchResponse): Double = 
        (for {
            kills <- data.Kills_Player
            assists <- data.Assists
            deaths <- data.Deaths
            deathsAdjusted = if (deaths == 0) 1 else deaths
        } yield (kills + (assists * 0.5)) / deathsAdjusted).getOrElse(0)
}
