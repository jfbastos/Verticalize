package br.com.zamfir.verticalize.sync.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Leitura e gravação dos dados de cada usuário em users/{uid} no Realtime Database. */
class FirebaseBackupDataSource {

    private val database by lazy { FirebaseDatabase.getInstance() }

    private fun usuarioRef(uid: String): DatabaseReference = database.getReference("users").child(uid)

    /** null quando o usuário ainda não tem nada salvo na nuvem. */
    suspend fun baixar(uid: String): BackupRemoto? {
        val snapshot = usuarioRef(uid).get().await()
        return if (snapshot.exists()) snapshot.toBackupRemoto() else null
    }

    /** Troca todo o nó `dados` por [snapshot] (escolha de manter os dados do aparelho). */
    suspend fun substituirTudo(uid: String, snapshot: BackupSnapshot) {
        usuarioRef(uid).updateChildren(metadadosDeAlteracao() + (NO_DADOS to snapshot.toFirebaseDados())).await()
    }

    /**
     * Grava só os registros alterados, numa única operação atômica. As chaves são caminhos dentro de
     * `dados` (ver [caminhoDoRegistro]); valor null exclui o registro.
     */
    suspend fun enviarAlteracoes(uid: String, alteracoes: Map<String, Any?>) {
        val updates = metadadosDeAlteracao() + alteracoes.mapKeys { (caminho, _) -> "$NO_DADOS/$caminho" }
        usuarioRef(uid).updateChildren(updates).await()
    }

    /**
     * Emite o conteúdo de `dados` agora e a cada mudança, feita por qualquer aparelho. Um backup em
     * formato inválido é ignorado (com log) em vez de encerrar o fluxo.
     */
    fun observarDados(uid: String): Flow<BackupSnapshot> = callbackFlow {
        val ref = usuarioRef(uid).child(NO_DADOS)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    trySend(snapshot.toBackupSnapshot())
                } catch (e: BackupInvalidoException) {
                    Log.w(TAG, "Dados da nuvem em formato inválido; alteração ignorada", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Ex.: regras de acesso negando a leitura. Encerra o fluxo; o repositório registra o erro.
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private companion object {
        const val TAG = "CloudSync"
    }
}
