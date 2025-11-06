package com.colabear754.kbo_scraper.api.dto.requests

import com.colabear754.kbo_scraper.api.domain.SeriesType

data class CollectDataRequest(
    val season: Int,
    val seriesType: SeriesType?
)
