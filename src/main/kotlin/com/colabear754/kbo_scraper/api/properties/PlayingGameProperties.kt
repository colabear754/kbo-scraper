package com.colabear754.kbo_scraper.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kbo.scraper.playing-game")
class PlayingGameProperties(
    val url: String,
    val selectors: Selectors
) {
    class Selectors(
        val gameList: String,
        val gameStatus: String,
        val gameScore: String,
        val calendarTrigger: String,
        val yearPicker: String,
        val monthPicker: String,
        val dayPicker: String
    )
}