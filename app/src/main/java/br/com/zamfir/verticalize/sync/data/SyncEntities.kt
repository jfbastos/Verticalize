package br.com.zamfir.verticalize.sync.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro alterado localmente que ainda não foi confirmado pela nuvem. Preenchida pelos triggers de
 * [SYNC_TRIGGERS_SQL] (nunca pelo código do app) e esvaziada pelo envio.
 */
@Entity(tableName = "sync_pendencias", primaryKeys = ["tabela", "registroId"])
data class SyncPendenciaEntity(
    /** Nome da tabela local: concursos, conteudos ou registros_estudo. */
    val tabela: String,
    val registroId: Long,
    val excluido: Boolean,
    /** Cresce a cada alteração; o envio só apaga a pendência se ela não mudou enquanto ia para a nuvem. */
    val seq: Long
)

/**
 * Linha única que os triggers consultam: com [aplicandoRemoto] ligado, o que veio da nuvem é gravado
 * sem virar pendência (senão voltaria para a nuvem).
 */
@Entity(tableName = "sync_controle")
data class SyncControleEntity(
    @PrimaryKey val id: Int = SYNC_CONTROLE_ID,
    val aplicandoRemoto: Boolean = false
)

const val SYNC_CONTROLE_ID = 1
