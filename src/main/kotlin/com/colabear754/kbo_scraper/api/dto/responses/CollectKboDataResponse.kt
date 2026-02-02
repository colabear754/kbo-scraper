package com.colabear754.kbo_scraper.api.dto.responses

data class CollectKboDataResponse(
    val collectedCount: Int,
    val savedCount: Int,
    val modifiedCount: Int
)
