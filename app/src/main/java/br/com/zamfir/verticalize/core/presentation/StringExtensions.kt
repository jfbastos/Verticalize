package br.com.zamfir.verticalize.core.presentation

fun String.filterDigits(maxLength: Int): String {
    return filter(Char::isDigit).take(maxLength)
}
