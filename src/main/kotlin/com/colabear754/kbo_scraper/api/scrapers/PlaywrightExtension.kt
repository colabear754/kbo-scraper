package com.colabear754.kbo_scraper.api.scrapers

import com.microsoft.playwright.*
import com.microsoft.playwright.options.ElementState

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
 * @receiver 셀렉트 박스 [Locator]
 * @param optionValue 선택할 옵션 값
 * @param targetDomLocator 사라질 요소의 [Locator]
 */
internal fun Locator.selectOptionAndWaitForDomChange(
    optionValue: String,
    targetDomLocator: Locator
) {
    val oldElement = targetDomLocator.elementHandle()

    selectOption(optionValue)

    oldElement?.waitForElementState(
        ElementState.HIDDEN,
        ElementHandle.WaitForElementStateOptions().apply { timeout = 10000.0 }
    )
}