package com.colabear754.kbo_scraper.api.domain.game

enum class GameStatus(
    val statusName: String
) {
    SCHEDULED("예정"),
    CANCELLED("취소"),
    PLAYING("경기 중"),
    FINISHED("경기 종료")
}