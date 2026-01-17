package com.colabear754.kbo_scraper.api.scrapers

import com.microsoft.playwright.Browser
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.ElementState
import kotlin.use

internal fun <R> launchChromium(action: Browser.() -> R): R =
    Playwright.create().use { playwright ->
        playwright.chromium().launch().use { browser ->
            browser.action()
        }
    }

internal fun <R> Browser.navigateAndBlock(url: String, block: Page.() -> R) =
    newPage().use { page ->
        page.navigate(url)
        page.block()
    }

/**
 * 셀렉트 박스 조작 후 DOM이 사라질 때까지 대기한다.
 *
 * @receiver 사라질 요소의 Locator
 * @param selectBoxSelector 셀렉트 박스 선택자
 * @param optionValue 선택할 옵션 값
 */
internal fun Locator.selectOptionAndWaitForDomChange(
    selectBoxSelector: String,
    optionValue: String
) {
    val oldElement = elementHandle()

    page().locator(selectBoxSelector).selectOption(optionValue)

    oldElement?.waitForElementState(
        ElementState.HIDDEN,
        ElementHandle.WaitForElementStateOptions().apply { timeout = 10000.0 }
    )
}