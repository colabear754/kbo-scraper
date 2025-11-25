package com.colabear754.kbo_scraper.api.handlers

import com.colabear754.kbo_scraper.api.dto.GlobalResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice(basePackages = ["com.colabear754.kbo_scraper.api.controllers"])
class GlobalResponseHandler(
    private val objectMapper: ObjectMapper
) : ResponseBodyAdvice<Any?> {
    companion object {
        private val unSupportedTypes = setOf(
            GlobalResponse::class.java,
            ErrorResponse::class.java
        )
    }

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>?>
    ): Boolean {
        return returnType.parameterType !in unSupportedTypes
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>?>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        if (returnType.parameterType == String::class.java) {
            response.headers.contentType = MediaType.APPLICATION_JSON
            return objectMapper.writeValueAsString(GlobalResponse.success(body))
        }

        return GlobalResponse.success(body)
    }
}