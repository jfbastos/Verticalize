package br.com.zamfir.verticalize.sync.data

import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity
import br.com.zamfir.verticalize.sync.domain.ResumoDados

/** Cópia completa do banco local: o que é enviado para a nuvem e o que volta dela. */
data class BackupSnapshot(
    val concursos: List<ConcursoEntity>,
    val conteudos: List<ConteudoEntity>,
    val estudos: List<RegistroEstudoEntity>
) {
    val isEmpty: Boolean
        get() = concursos.isEmpty() && conteudos.isEmpty() && estudos.isEmpty()

    /** Mesma ordem dos dois lados, para comparar aparelho e nuvem com `==`. */
    fun normalizado(): BackupSnapshot = BackupSnapshot(
        concursos = concursos.sortedBy { it.id },
        conteudos = conteudos.sortedBy { it.id },
        estudos = estudos.sortedBy { it.id }
    )

    fun resumo(atualizadoEm: Long? = null): ResumoDados = ResumoDados(
        concursos = concursos.size,
        conteudos = conteudos.size,
        estudos = estudos.size,
        atualizadoEm = atualizadoEm
    )
}

/** Backup lido da nuvem, com o horário (do servidor) em que foi gravado. */
data class BackupRemoto(
    val snapshot: BackupSnapshot,
    val atualizadoEm: Long?
)
