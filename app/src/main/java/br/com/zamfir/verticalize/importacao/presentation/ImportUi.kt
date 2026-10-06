package br.com.zamfir.verticalize.importacao.presentation

import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.importacao.domain.ImportLineError
import br.com.zamfir.verticalize.importacao.domain.ImportPlan
import br.com.zamfir.verticalize.importacao.domain.PlannedConcurso

data class ImportResumoUi(
    val concursosNovos: Int = 0,
    val concursosExistentes: Int = 0,
    val conteudosNovos: Int = 0,
    val conteudosDuplicados: Int = 0,
    val estudosNovos: Int = 0,
    val estudosDuplicados: Int = 0
) {
    val temNovidades: Boolean get() = concursosNovos > 0 || conteudosNovos > 0 || estudosNovos > 0
    val temEstudos: Boolean get() = estudosNovos > 0 || estudosDuplicados > 0
}

data class ImportConcursoUi(
    val nome: String,
    val nivel: Nivel,
    val isNovo: Boolean,
    val conteudosNovos: Int,
    val conteudosDuplicados: Int,
    val estudosNovos: Int = 0
)

data class ImportLineErrorUi(
    val linha: Int,
    val mensagem: UiText
)

fun ImportPlan.toImportResumoUi(): ImportResumoUi {
    return ImportResumoUi(
        concursosNovos = concursosNovos,
        concursosExistentes = concursosExistentes,
        conteudosNovos = conteudosNovos,
        conteudosDuplicados = conteudosDuplicados,
        estudosNovos = estudosNovos,
        estudosDuplicados = estudosDuplicados
    )
}

fun PlannedConcurso.toImportConcursoUi(): ImportConcursoUi {
    return ImportConcursoUi(
        nome = nome,
        nivel = nivel,
        isNovo = isNovo,
        conteudosNovos = novosConteudos.size,
        conteudosDuplicados = conteudosDuplicados,
        estudosNovos = novosEstudos.size
    )
}

fun ImportLineError.toImportLineErrorUi(): ImportLineErrorUi {
    return ImportLineErrorUi(
        linha = linha,
        mensagem = UiText.StringResource(motivo.messageRes())
    )
}
