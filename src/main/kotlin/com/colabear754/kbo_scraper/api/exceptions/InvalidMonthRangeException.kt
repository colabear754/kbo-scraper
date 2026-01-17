package com.colabear754.kbo_scraper.api.exceptions

class InvalidMonthRangeException(startMonth: Int, endMonth: Int) : RuntimeException(
    when {
        startMonth !in 1..12 || endMonth !in 1..12 -> "월 범위는 1부터 12 사이여야 합니다."
        startMonth > endMonth -> "시작 월은 종료 월 이하여야 합니다."
        else -> "유효하지 않은 월 범위입니다."
    }
)