package br.com.zamfir.verticalize.conteudo.presentation

import androidx.annotation.StringRes
import br.com.zamfir.verticalize.R

enum class ConteudoAgrupamento(@param:StringRes val labelRes: Int) {
    NENHUM(R.string.conteudo_agrupamento_nenhum),
    EIXO(R.string.conteudo_agrupamento_eixo),
    BLOCO(R.string.conteudo_agrupamento_bloco),
    MATERIA(R.string.conteudo_agrupamento_materia)
}
