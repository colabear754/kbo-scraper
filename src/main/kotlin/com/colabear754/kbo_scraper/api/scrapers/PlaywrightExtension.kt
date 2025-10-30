package com.colabear754.kbo_scraper.api.scrapers

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page

internal fun <R> Browser.navigateAndBlock(url: String, block: Page.() -> R) =
    newPage().use { page ->
        page.navigate(url)
        page.block()
    }