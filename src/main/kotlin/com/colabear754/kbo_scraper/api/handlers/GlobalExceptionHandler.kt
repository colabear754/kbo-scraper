package com.colabear754.kbo_scraper.api.handlers

import com.colabear754.kbo_scraper.api.dto.GlobalResponse
import com.colabear754.kbo_scraper.api.exceptions.InvalidMonthRangeException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDate

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<GlobalResponse<*>> {
        val message = when (val cause = e.cause) {
            is InvalidFormatException -> {
                val fieldName = cause.path?.lastOrNull()?.fieldName ?: "Unknown Field"
                if (cause.targetType?.isEnum == true) "'$fieldName'에 유효하지 않은 값이 입력되었습니다."
                else "'$fieldName'에 유효하지 않은 형식의 값이 입력되었습니다."
            }
            else -> "요청 형식이 올바르지 않습니다."
        }
        return ResponseEntity.badRequest().body(GlobalResponse.error(HttpStatus.BAD_REQUEST, message))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<GlobalResponse<*>> {
        val message = when (e.requiredType) {
            LocalDate::class.java -> "날짜 형식은 yyyy-MM-dd이어야 합니다."
            else -> "요청 형식이 올바르지 않습니다."
        }
        return ResponseEntity.badRequest().body(GlobalResponse.error(HttpStatus.BAD_REQUEST, message))
    }

    @ExceptionHandler(InvalidMonthRangeException::class)
    fun handleInvalidMonthRangeException(e: InvalidMonthRangeException): ResponseEntity<GlobalResponse<*>> {
        return ResponseEntity.badRequest()
            .body(GlobalResponse.error(HttpStatus.BAD_REQUEST, e.message ?: "유효하지 않은 월 범위입니다."))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(): ResponseEntity<GlobalResponse<*>> {
        return ResponseEntity.internalServerError()
            .body(GlobalResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."))
    }
}