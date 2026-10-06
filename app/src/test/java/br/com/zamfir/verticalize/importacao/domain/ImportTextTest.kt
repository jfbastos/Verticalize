package br.com.zamfir.verticalize.importacao.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import br.com.zamfir.verticalize.concurso.domain.Nivel
import org.junit.jupiter.api.Test

class ImportTextTest {

    @Test
    fun `valor em reais vira centavos`() {
        assertThat(parseValorEmCentavos("120")).isEqualTo(12000L)
        assertThat(parseValorEmCentavos("120,00")).isEqualTo(12000L)
        assertThat(parseValorEmCentavos("120,5")).isEqualTo(12050L)
        assertThat(parseValorEmCentavos("R$ 120,00")).isEqualTo(12000L)
        assertThat(parseValorEmCentavos("r\$120")).isEqualTo(12000L)
        assertThat(parseValorEmCentavos("R$ 1.200,50")).isEqualTo(120050L)
        assertThat(parseValorEmCentavos("0,99")).isEqualTo(99L)
        assertThat(parseValorEmCentavos("0")).isEqualTo(0L)
    }

    @Test
    fun `ponto sozinho e decimal so quando ha uma ou duas casas`() {
        assertThat(parseValorEmCentavos("120.50")).isEqualTo(12050L)
        assertThat(parseValorEmCentavos("1.200")).isEqualTo(120000L)
        assertThat(parseValorEmCentavos("1.200.000")).isEqualTo(120000000L)
    }

    @Test
    fun `valor invalido retorna nulo`() {
        assertThat(parseValorEmCentavos("")).isNull()
        assertThat(parseValorEmCentavos("R$")).isNull()
        assertThat(parseValorEmCentavos("abc")).isNull()
        assertThat(parseValorEmCentavos("12a")).isNull()
        assertThat(parseValorEmCentavos("120,505")).isNull()
        assertThat(parseValorEmCentavos("1,2,3")).isNull()
        assertThat(parseValorEmCentavos("-5")).isNull()
        // acima do limite de 10 dígitos de centavos aceito pela tela de cadastro
        assertThat(parseValorEmCentavos("999999999999")).isNull()
    }

    @Test
    fun `data valida vira meia noite UTC e datas inexistentes sao rejeitadas`() {
        assertThat(parseDataEmMillis("01/01/1970")).isEqualTo(0L)
        assertThat(parseDataEmMillis("02/01/1970")).isEqualTo(86_400_000L)
        assertThat(parseDataEmMillis("31/02/2027")).isNull()
        assertThat(parseDataEmMillis("15/13/2027")).isNull()
        assertThat(parseDataEmMillis("2027-03-15")).isNull()
        assertThat(parseDataEmMillis("15/03/27")).isNull()
        assertThat(parseDataEmMillis("")).isNull()
    }

    @Test
    fun `hora vira minutos desde meia noite`() {
        assertThat(parseHoraEmMinutos("00:00")).isEqualTo(0)
        assertThat(parseHoraEmMinutos("08:30")).isEqualTo(510)
        assertThat(parseHoraEmMinutos("8:30")).isEqualTo(510)
        assertThat(parseHoraEmMinutos(" 23:59 ")).isEqualTo(1439)
    }

    @Test
    fun `hora invalida retorna nulo`() {
        assertThat(parseHoraEmMinutos("")).isNull()
        assertThat(parseHoraEmMinutos("24:00")).isNull()
        assertThat(parseHoraEmMinutos("12:60")).isNull()
        assertThat(parseHoraEmMinutos("12:5")).isNull()
        assertThat(parseHoraEmMinutos("1230")).isNull()
        assertThat(parseHoraEmMinutos("12:30:00")).isNull()
        assertThat(parseHoraEmMinutos("ab:cd")).isNull()
        assertThat(parseHoraEmMinutos("-1:30")).isNull()
    }

    @Test
    fun `nivel ignora caixa e acento`() {
        assertThat(parseNivel("Médio")).isEqualTo(Nivel.MEDIO)
        assertThat(parseNivel("MEDIO")).isEqualTo(Nivel.MEDIO)
        assertThat(parseNivel(" superior ")).isEqualTo(Nivel.SUPERIOR)
        assertThat(parseNivel("fundamental")).isNull()
    }

    @Test
    fun `booleano reconhece variacoes e devolve nulo para o resto`() {
        assertThat(parseBooleano("")).isEqualTo(false)
        assertThat(parseBooleano("Não")).isEqualTo(false)
        assertThat(parseBooleano("SIM")).isEqualTo(true)
        assertThat(parseBooleano("x")).isEqualTo(true)
        assertThat(parseBooleano("talvez")).isNull()
    }

    @Test
    fun `normalizado remove acentos caixa e espacos repetidos`() {
        assertThat("  Direito   Constitucional ".normalizado()).isEqualTo("direito constitucional")
        assertThat("Português".normalizado()).isEqualTo("portugues")
        assertThat("Data da Prova".paraChave()).isEqualTo("data_da_prova")
        assertThat("min-por aula".paraChave()).isEqualTo("min_por_aula")
    }
}
