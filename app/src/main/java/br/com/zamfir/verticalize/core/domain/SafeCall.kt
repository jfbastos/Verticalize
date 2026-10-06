package br.com.zamfir.verticalize.core.domain

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeCall(action: suspend () -> T): Result<T, DataError.Local> {
    return try {
        Result.Success(action())
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
