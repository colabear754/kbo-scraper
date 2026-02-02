package com.colabear754.kbo_scraper.api.domain.game

enum class CancellationReason(
    val reasonName: String
) {
    GROUND_CONDITION("그라운드 사정"),
    RAIN("우천"),
    HEATWAVE("폭염"),
    FINE_DUST("미세먼지"),
    STRONG_WIND("강풍"),
    YELLOW_DUST("황사"),
    ETC("기타");

    companion object {
        fun fromString(reason: String) = when {
            "그라운드" in reason -> GROUND_CONDITION
            "우천" in reason -> RAIN
            "폭염" in reason -> HEATWAVE
            "미세먼지" in reason -> FINE_DUST
            "강풍" in reason -> STRONG_WIND
            "황사" in reason -> YELLOW_DUST
            "-" == reason -> null
            else -> ETC
        }
    }
}