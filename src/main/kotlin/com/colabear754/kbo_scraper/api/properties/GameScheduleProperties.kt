package com.colabear754.kbo_scraper.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kbo.scraper.game-schedule")
class GameScheduleProperties(
    val url: String,
    val selectors: Selectors
) {
    class Selectors(
        val year: String,
        val month: String,
        val series: String,
        val gamesTable: String
    )
}