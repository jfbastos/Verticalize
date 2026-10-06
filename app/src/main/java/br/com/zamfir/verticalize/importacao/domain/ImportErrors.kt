package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.core.domain.Error

/** Falhas que impedem a importação do arquivo como um todo. */
enum class ImportFileError : Error {
    ARQUIVO_ILEGIVEL,
    ARQUIVO_MUITO_GRANDE,
    ARQUIVO_VAZIO,
    NENHUM_CONCURSO,
    FALHA_AO_ANALISAR
}

/** Motivo pelo qual uma linha do arquivo foi ignorada. Não derruba o arquivo inteiro. */
enum class ImportLineReason {
    NOME_OBRIGATORIO,
    NIVEL_OBRIGATORIO,
    NIVEL_INVALIDO,
    DATA_PROVA_INVALIDA,
    VALOR_INSCRICAO_INVALIDO,
    CHAVE_DESCONHECIDA,
    LINHA_INVALIDA,
    LINHA_FORA_DE_SECAO,
    SECAO_DESCONHECIDA,
    CONTEUDO_SEM_CONCURSO,
    CABECALHO_SEM_DESCRICAO,
    MUITOS_CAMPOS,
    DESCRICAO_OBRIGATORIA,
    EIXO_INVALIDO,
    BLOCO_INVALIDO,
    AULAS_INVALIDAS,
    MINUTOS_INVALIDOS,
    CONCLUIDO_INVALIDO,
    REVISAO_INVALIDA,
    QUESTOES_INVALIDAS,
    PRIORIDADE_INVALIDA,
    ESTUDO_SEM_CONCURSO,
    CABECALHO_ESTUDO_INCOMPLETO,
    ESTUDO_MATERIA_OBRIGATORIA,
    ESTUDO_DATA_INVALIDA,
    ESTUDO_HORA_INICIO_INVALIDA,
    ESTUDO_HORA_FIM_INVALIDA,
    ESTUDO_INTERVALO_INVALIDO
}

data class ImportLineError(
    val linha: Int,
    val motivo: ImportLineReason
)
