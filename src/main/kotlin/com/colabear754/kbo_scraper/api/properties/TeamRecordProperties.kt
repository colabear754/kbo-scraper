package com.colabear754.kbo_scraper.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kbo.scraper.team-record")
class TeamRecordProperties(
    val url: String,
    val selectors: Selectors
) {
    class Selectors(
        val year: String,
        val series: String,
        val rankTable: String
    )
}