package br.com.zamfir.verticalize.sync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity
import kotlinx.coroutines.flow.Flow

/** Acesso ao banco usado pela sincronização com a nuvem. */
@Dao
abstract class SyncDao {
    @Query("SELECT * FROM concursos ORDER BY id")
    abstract suspend fun getAllConcursos(): List<ConcursoEntity>

    @Query("SELECT * FROM conteudos ORDER BY id")
    abstract suspend fun getAllConteudos(): List<ConteudoEntity>

    @Query("SELECT * FROM registros_estudo ORDER BY id")
    abstract suspend fun getAllEstudos(): List<RegistroEstudoEntity>

    @Query("SELECT * FROM concursos WHERE id = :id")
    abstract suspend fun getConcurso(id: Long): ConcursoEntity?

    @Query("SELECT * FROM conteudos WHERE id = :id")
    abstract suspend fun getConteudo(id: Long): ConteudoEntity?

    @Query("SELECT * FROM registros_estudo WHERE id = :id")
    abstract suspend fun getEstudo(id: Long): RegistroEstudoEntity?

    // --- Pendências (alimentadas pelos triggers, ver SyncTriggers.kt) ---

    @Query("SELECT * FROM sync_pendencias ORDER BY seq")
    abstract fun observePendencias(): Flow<List<SyncPendenciaEntity>>

    @Query("SELECT * FROM sync_pendencias ORDER BY seq")
    abstract suspend fun getPendencias(): List<SyncPendenciaEntity>

    /** Só apaga se a pendência não mudou ([seq] igual) enquanto era enviada. */
    @Query("DELETE FROM sync_pendencias WHERE tabela = :tabela AND registroId = :registroId AND seq = :seq")
    abstract suspend fun deletePendencia(tabela: String, registroId: Long, seq: Long)

    @Query("DELETE FROM sync_pendencias")
    abstract suspend fun limparPendencias()

    @Transaction
    open suspend fun confirmarEnvio(enviadas: List<SyncPendenciaEntity>) {
        enviadas.forEach { deletePendencia(it.tabela, it.registroId, it.seq) }
    }

    // --- Gravação do que veio da nuvem ---

    @Query("UPDATE sync_controle SET aplicandoRemoto = :aplicando WHERE id = $SYNC_CONTROLE_ID")
    abstract suspend fun setAplicandoRemoto(aplicando: Boolean)

    @Query("DELETE FROM registros_estudo")
    abstract suspend fun deleteAllEstudos()

    @Query("DELETE FROM conteudos")
    abstract suspend fun deleteAllConteudos()

    @Query("DELETE FROM concursos")
    abstract suspend fun deleteAllConcursos()

    @Query("DELETE FROM registros_estudo WHERE id IN (:ids)")
    abstract suspend fun deleteEstudos(ids: List<Long>)

    @Query("DELETE FROM conteudos WHERE id IN (:ids)")
    abstract suspend fun deleteConteudos(ids: List<Long>)

    @Query("DELETE FROM concursos WHERE id IN (:ids)")
    abstract suspend fun deleteConcursos(ids: List<Long>)

    @Insert
    abstract suspend fun insertConcursos(entities: List<ConcursoEntity>)

    @Insert
    abstract suspend fun insertConteudos(entities: List<ConteudoEntity>)

    @Insert
    abstract suspend fun insertEstudos(entities: List<RegistroEstudoEntity>)

    // Upsert (e não INSERT OR REPLACE): REPLACE apaga a linha antes, o que dispararia o CASCADE e
    // levaria junto os conteúdos e estudos do concurso.
    @Upsert
    abstract suspend fun upsertConcursos(entities: List<ConcursoEntity>)

    @Upsert
    abstract suspend fun upsertConteudos(entities: List<ConteudoEntity>)

    @Upsert
    abstract suspend fun upsertEstudos(entities: List<RegistroEstudoEntity>)

    @Transaction
    open suspend fun getSnapshot(): BackupSnapshot = BackupSnapshot(
        concursos = getAllConcursos(),
        conteudos = getAllConteudos(),
        estudos = getAllEstudos()
    )

    /**
     * Troca todo o conteúdo local por [snapshot], mantendo os ids (assim as referências de conteúdos e
     * estudos aos concursos continuam válidas), e zera as pendências: depois disso aparelho e nuvem
     * estão iguais. Numa transação: se algo falhar, nada muda.
     */
    @Transaction
    open suspend fun substituirTudo(snapshot: BackupSnapshot) {
        setAplicandoRemoto(true)
        try {
            deleteAllEstudos()
            deleteAllConteudos()
            deleteAllConcursos()
            insertConcursos(snapshot.concursos)
            insertConteudos(snapshot.conteudos)
            insertEstudos(snapshot.estudos)
        } finally {
            setAplicandoRemoto(false)
        }
        limparPendencias()
    }

    /**
     * Aplica uma mudança vinda da nuvem. Lê o estado local, as pendências e grava na mesma transação,
     * para nenhuma alteração local feita no meio do caminho ser sobrescrita.
     */
    @Transaction
    open suspend fun aplicarRemoto(remoto: BackupSnapshot) {
        val pendentes = getPendencias().map { RegistroRef(it.tabela, it.registroId) }.toSet()
        val plano = planejarAplicacao(getSnapshot().normalizado(), remoto, pendentes)
        if (plano.isVazio) return

        setAplicandoRemoto(true)
        try {
            // Filhos antes ao excluir; concursos antes ao gravar: respeita as foreign keys.
            // Em lotes: o SQLite limita a quantidade de parâmetros do IN (999 em versões mais antigas).
            plano.estudosExcluir.chunked(EXCLUSAO_LOTE).forEach { deleteEstudos(it) }
            plano.conteudosExcluir.chunked(EXCLUSAO_LOTE).forEach { deleteConteudos(it) }
            plano.concursosExcluir.chunked(EXCLUSAO_LOTE).forEach { deleteConcursos(it) }
            upsertConcursos(plano.concursosGravar)
            upsertConteudos(plano.conteudosGravar)
            upsertEstudos(plano.estudosGravar)
        } finally {
            setAplicandoRemoto(false)
        }
    }

    private companion object {
        const val EXCLUSAO_LOTE = 500
    }
}
