package br.com.zamfir.verticalize.core.presentation

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @param:StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText
    class PluralResource(
        @param:PluralsRes val id: Int,
        val quantity: Int,
        val args: List<Any> = listOf(quantity)
    ) : UiText
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> stringResource(id, *args.toTypedArray())
        is UiText.PluralResource -> pluralStringResource(id, quantity, *args.toTypedArray())
    }
}

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> context.getString(id, *args.toTypedArray())
        is UiText.PluralResource -> context.resources.getQuantityString(id, quantity, *args.toTypedArray())
    }
}
