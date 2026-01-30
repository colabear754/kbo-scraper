package com.colabear754.kbo_scraper.api.repositories.cache

import org.springframework.cache.Cache
import org.springframework.cache.get

@Suppress("UNCHECKED_CAST") fun <T> Cache.getTyped(key: Any): T? = this.get<Any>(key) as? T?
@Suppress("UNCHECKED_CAST") fun <T> Cache.getTypedList(key: Any): List<T>? = this.get<Any>(key) as? List<T>?