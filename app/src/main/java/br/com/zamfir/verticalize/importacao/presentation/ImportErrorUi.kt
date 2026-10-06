package br.com.zamfir.verticalize.importacao.presentation

import androidx.annotation.StringRes
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.importacao.domain.ExportError
import br.com.zamfir.verticalize.importacao.domain.ImportFileError
import br.com.zamfir.verticalize.importacao.domain.ImportLineReason

@StringRes
fun ImportFileError.messageRes(): Int {
    return when (this) {
        ImportFileError.ARQUIVO_ILEGIVEL -> R.string.import_file_error_ilegivel
        ImportFileError.ARQUIVO_MUITO_GRANDE -> R.string.import_file_error_muito_grande
        ImportFileError.ARQUIVO_VAZIO -> R.string.import_file_error_vazio
        ImportFileError.NENHUM_CONCURSO -> R.string.import_file_error_nenhum_concurso
        ImportFileError.FALHA_AO_ANALISAR -> R.string.import_file_error_falha_analise
    }
}

@StringRes
fun ExportError.messageRes(): Int {
    return when (this) {
        ExportError.SEM_DADOS -> R.string.export_error_sem_dados
        ExportError.FALHA_AO_LER -> R.string.export_error_falha_ler
        ExportError.FALHA_AO_GRAVAR -> R.string.export_error_falha_gravar
    }
}

@StringRes
fun ImportLineReason.messageRes(): Int {
    return when (this) {
        ImportLineReason.NOME_OBRIGATORIO -> R.string.import_line_nome_obrigatorio
        ImportLineReason.NIVEL_OBRIGATORIO -> R.string.import_line_nivel_obrigatorio
        ImportLineReason.NIVEL_INVALIDO -> R.string.import_line_nivel_invalido
        ImportLineReason.DATA_PROVA_INVALIDA -> R.string.import_line_data_prova_invalida
        ImportLineReason.VALOR_INSCRICAO_INVALIDO -> R.string.import_line_valor_inscricao_invalido
        ImportLineReason.CHAVE_DESCONHECIDA -> R.string.import_line_chave_desconhecida
        ImportLineReason.LINHA_INVALIDA -> R.string.import_line_linha_invalida
        ImportLineReason.LINHA_FORA_DE_SECAO -> R.string.import_line_fora_de_secao
        ImportLineReason.SECAO_DESCONHECIDA -> R.string.import_line_secao_desconhecida
        ImportLineReason.CONTEUDO_SEM_CONCURSO -> R.string.import_line_conteudo_sem_concurso
        ImportLineReason.CABECALHO_SEM_DESCRICAO -> R.string.import_line_cabecalho_sem_descricao
        ImportLineReason.MUITOS_CAMPOS -> R.string.import_line_muitos_campos
        ImportLineReason.DESCRICAO_OBRIGATORIA -> R.string.import_line_descricao_obrigatoria
        ImportLineReason.EIXO_INVALIDO -> R.string.import_line_eixo_invalido
        ImportLineReason.BLOCO_INVALIDO -> R.string.import_line_bloco_invalido
        ImportLineReason.AULAS_INVALIDAS -> R.string.import_line_aulas_invalidas
        ImportLineReason.MINUTOS_INVALIDOS -> R.string.import_line_minutos_invalidos
        ImportLineReason.CONCLUIDO_INVALIDO -> R.string.import_line_concluido_invalido
        ImportLineReason.REVISAO_INVALIDA -> R.string.import_line_revisao_invalida
        ImportLineReason.QUESTOES_INVALIDAS -> R.string.import_line_questoes_invalidas
        ImportLineReason.PRIORIDADE_INVALIDA -> R.string.import_line_prioridade_invalida
        ImportLineReason.ESTUDO_SEM_CONCURSO -> R.string.import_line_estudo_sem_concurso
        ImportLineReason.CABECALHO_ESTUDO_INCOMPLETO -> R.string.import_line_cabecalho_estudo_incompleto
        ImportLineReason.ESTUDO_MATERIA_OBRIGATORIA -> R.string.import_line_estudo_materia_obrigatoria
        ImportLineReason.ESTUDO_DATA_INVALIDA -> R.string.import_line_estudo_data_invalida
        ImportLineReason.ESTUDO_HORA_INICIO_INVALIDA -> R.string.import_line_estudo_hora_inicio_invalida
        ImportLineReason.ESTUDO_HORA_FIM_INVALIDA -> R.string.import_line_estudo_hora_fim_invalida
        ImportLineReason.ESTUDO_INTERVALO_INVALIDO -> R.string.import_line_estudo_intervalo_invalido
    }
}
