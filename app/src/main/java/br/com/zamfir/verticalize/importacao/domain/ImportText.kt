package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import java.math.BigDecimal
import java.math.BigInteger
import java.text.Normalizer
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

private val MARCAS_DIACRITICAS = Regex("\\p{M}+")
private val ESPACOS = Regex("\\s+")
private val SEPARADORES_DE_CHAVE = Regex("[\\s\\-]+")
private val VALOR_PERMITIDO = Regex("^[0-9.,]+$")

// "d/M/uuuu" aceita "5/3/2027" e "05/03/2027"; STRICT rejeita datas que não existem (31/02).
private val FORMATO_DATA: DateTimeFormatter = DateTimeFormatter
    .ofPattern("d/M/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

// A UI limita os campos numéricos a 4 dígitos (ver ConcursoDetailViewModel) e o valor da inscrição a 10 dígitos.
internal const val LIMITE_INTEIRO = 9999
private const val LIMITE_CENTAVOS = 9_999_999_999L

/** Minúsculas, sem acentos e com espaços colapsados; usada para comparar textos e detectar duplicados. */
internal fun String.normalizado(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(MARCAS_DIACRITICAS, "")
        .lowercase()
        .trim()
        .replace(ESPACOS, " ")
}

/** Como [normalizado], mas com "_" no lugar de espaços/hífens (ex.: "Data da prova" -> "data_da_prova"). */
internal fun String.paraChave(): String = normalizado().replace(SEPARADORES_DE_CHAVE, "_")

internal fun parseNivel(texto: String): Nivel? {
    return when (texto.normalizado()) {
        "medio" -> Nivel.MEDIO
        "superior" -> Nivel.SUPERIOR
        else -> null
    }
}

internal fun parsePrioridade(texto: String): Prioridade? {
    return when (texto.normalizado()) {
        "alta" -> Prioridade.ALTA
        "media" -> Prioridade.MEDIA
        "baixa" -> Prioridade.BAIXA
        "opcional" -> Prioridade.OPCIONAL
        else -> null
    }
}

/** dd/MM/yyyy -> meia-noite UTC em millis, o mesmo formato que o DatePicker e o `formatDate` usam. */
internal fun parseDataEmMillis(texto: String): Long? {
    return try {
        LocalDate.parse(texto.trim(), FORMATO_DATA).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}

/**
 * Converte "120", "120,00", "R$ 1.200,50" ou "120.50" em centavos, sem passar por ponto flutuante.
 * Retorna null se o texto não for um valor monetário válido (mais de 2 casas decimais, letras, etc.).
 */
internal fun parseValorEmCentavos(texto: String): Long? {
    val limpo = texto.replace("R$", "", ignoreCase = true).replace(ESPACOS, "")
    if (limpo.isEmpty() || !VALOR_PERMITIDO.matches(limpo)) return null

    val normalizado = when {
        // Com vírgula, ela é o separador decimal e os pontos são milhares: 1.200,50
        ',' in limpo -> limpo.replace(".", "").replace(',', '.')
        // Só pontos: um único ponto seguido de 1-2 dígitos é decimal (120.50); senão são milhares (1.200).
        limpo.count { it == '.' } == 1 && limpo.substringAfter('.').length in 1..2 -> limpo
        else -> limpo.replace(".", "")
    }

    return try {
        // toBigIntegerExact lança ArithmeticException se sobrar fração de centavo (ex.: "120,505").
        val centavos = BigDecimal(normalizado).movePointRight(2).toBigIntegerExact()
        if (centavos > BigInteger.valueOf(LIMITE_CENTAVOS)) null else centavos.toLong()
    } catch (_: NumberFormatException) {
        null
    } catch (_: ArithmeticException) {
        null
    }
}

/** "08:30" ou "8:30" -> 510 (minutos desde 00:00). Só 00:00 a 23:59, como o seletor de hora do app. */
internal fun parseHoraEmMinutos(texto: String): Int? {
    val partes = texto.trim().split(':')
    if (partes.size != 2) return null
    val horas = partes[0].toIntOrNull()?.takeIf { partes[0].length in 1..2 && it in 0..23 } ?: return null
    val minutos = partes[1].toIntOrNull()?.takeIf { partes[1].length == 2 && it in 0..59 } ?: return null
    return horas * 60 + minutos
}

/** true para sim/s/true/1/x, false para nao/n/false/0 ou vazio; null se não for reconhecido. */
internal fun parseBooleano(texto: String): Boolean? {
    return when (texto.normalizado()) {
        "", "nao", "n", "false", "0", "no" -> false
        "sim", "s", "true", "1", "x", "yes", "y" -> true
        else -> null
    }
}

internal fun String.toIntNoIntervalo(minimo: Int, maximo: Int = LIMITE_INTEIRO): Int? {
    return toIntOrNull()?.takeIf { it in minimo..maximo }
}
