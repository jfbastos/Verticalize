package br.com.zamfir.verticalize.sync.data

import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity

/** O que gravar no aparelho para refletir o estado da nuvem. */
data class PlanoAplicacao(
    val concursosGravar: List<ConcursoEntity> = emptyList(),
    val conteudosGravar: List<ConteudoEntity> = emptyList(),
    val estudosGravar: List<RegistroEstudoEntity> = emptyList(),
    val concursosExcluir: List<Long> = emptyList(),
    val conteudosExcluir: List<Long> = emptyList(),
    val estudosExcluir: List<Long> = emptyList()
) {
    val isVazio: Boolean
        get() = concursosGravar.isEmpty() && conteudosGravar.isEmpty() && estudosGravar.isEmpty() &&
            concursosExcluir.isEmpty() && conteudosExcluir.isEmpty() && estudosExcluir.isEmpty()
}

/** Registro local identificado pela tabela e pelo id, como em `sync_pendencias`. */
data class RegistroRef(val tabela: String, val id: Long)

/**
 * Compara aparelho e nuvem registro a registro. Registros com alteração local ainda não enviada
 * ([pendentes]) ficam como estão: a versão local é a mais recente e vai sobrescrever a da nuvem.
 *
 * Conteúdos e estudos cujo concurso não vai existir no aparelho (ex.: concurso excluído aqui e ainda
 * não enviado) são deixados de fora, para não violar a foreign key.
 */
fun planejarAplicacao(
    local: BackupSnapshot,
    remoto: BackupSnapshot,
    pendentes: Set<RegistroRef>
): PlanoAplicacao {
    val concursos = diferenca(TABELA_CONCURSOS, local.concursos, remoto.concursos, pendentes) { it.id }
    val conteudos = diferenca(TABELA_CONTEUDOS, local.conteudos, remoto.conteudos, pendentes) { it.id }
    val estudos = diferenca(TABELA_ESTUDOS, local.estudos, remoto.estudos, pendentes) { it.id }

    val concursosExcluidos = concursos.excluir.toSet()
    val concursosFinais = local.concursos.map { it.id }.filterNot { it in concursosExcluidos }.toSet() +
        concursos.gravar.map { it.id }

    return PlanoAplicacao(
        concursosGravar = concursos.gravar,
        conteudosGravar = conteudos.gravar.filter { it.concursoId in concursosFinais },
        estudosGravar = estudos.gravar.filter { it.concursoId in concursosFinais },
        concursosExcluir = concursos.excluir,
        conteudosExcluir = conteudos.excluir,
        estudosExcluir = estudos.excluir
    )
}

private class Diferenca<T>(val gravar: List<T>, val excluir: List<Long>)

private fun <T> diferenca(
    tabela: String,
    local: List<T>,
    remoto: List<T>,
    pendentes: Set<RegistroRef>,
    id: (T) -> Long
): Diferenca<T> {
    val localPorId = local.associateBy(id)
    val remotoIds = remoto.map(id).toSet()
    fun pendente(registroId: Long) = RegistroRef(tabela, registroId) in pendentes

    return Diferenca(
        gravar = remoto.filter { !pendente(id(it)) && localPorId[id(it)] != it },
        excluir = local.map(id).filter { it !in remotoIds && !pendente(it) }
    )
}
