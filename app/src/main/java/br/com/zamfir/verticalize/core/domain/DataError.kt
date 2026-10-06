package br.com.zamfir.verticalize.core.domain

sealed interface DataError : Error {
    enum class Local : DataError {
        NOT_FOUND,
        UNKNOWN
    }
}
