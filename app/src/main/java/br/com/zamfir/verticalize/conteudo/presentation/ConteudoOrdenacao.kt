package br.com.zamfir.verticalize.conteudo.presentation

import androidx.annotation.StringRes
import br.com.zamfir.verticalize.R

enum class ConteudoOrdenacao(@param:StringRes val labelRes: Int) {
    PADRAO(R.string.conteudo_ordenacao_padrao),
    PRIORIDADE_ASC(R.string.conteudo_ordenacao_prioridade_asc),
    PRIORIDADE_DESC(R.string.conteudo_ordenacao_prioridade_desc),
    EIXO_ASC(R.string.conteudo_ordenacao_eixo_asc),
    EIXO_DESC(R.string.conteudo_ordenacao_eixo_desc),
    BLOCO_ASC(R.string.conteudo_ordenacao_bloco_asc),
    BLOCO_DESC(R.string.conteudo_ordenacao_bloco_desc),
    REVISAO_ASC(R.string.conteudo_ordenacao_revisao_asc),
    REVISAO_DESC(R.string.conteudo_ordenacao_revisao_desc),
    TEMPO_AULA_ASC(R.string.conteudo_ordenacao_tempo_aula_asc),
    TEMPO_AULA_DESC(R.string.conteudo_ordenacao_tempo_aula_desc)
}
