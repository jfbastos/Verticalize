package br.com.zamfir.verticalize

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import br.com.zamfir.verticalize.auth.data.authDataModule
import br.com.zamfir.verticalize.concurso.data.concursoDataModule
import br.com.zamfir.verticalize.concurso.presentation.concursoPresentationModule
import br.com.zamfir.verticalize.conteudo.data.conteudoDataModule
import br.com.zamfir.verticalize.core.di.coreModule
import br.com.zamfir.verticalize.importacao.data.importacaoDataModule
import br.com.zamfir.verticalize.importacao.presentation.importacaoPresentationModule
import br.com.zamfir.verticalize.registroestudo.data.registroEstudoDataModule
import br.com.zamfir.verticalize.registroestudo.presentation.registroEstudoPresentationModule
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerService
import br.com.zamfir.verticalize.sync.data.FirebaseCloudSyncRepository
import br.com.zamfir.verticalize.sync.data.syncDataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class VerticalizeApp : Application() {

    /** Vive enquanto o processo viver; usado pela sincronização contínua com a nuvem. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@VerticalizeApp)
            modules(
                coreModule,
                authDataModule,
                concursoDataModule,
                conteudoDataModule,
                registroEstudoDataModule,
                importacaoDataModule,
                syncDataModule,
                concursoPresentationModule,
                registroEstudoPresentationModule,
                importacaoPresentationModule
            )
        }
        criarCanalDoCronometro()
        // No Application, e não numa tela: alterações feitas pelo cronômetro ou pelo widget, com o app
        // fechado, também precisam chegar à nuvem, e as de outros aparelhos chegam com qualquer tela aberta.
        get<FirebaseCloudSyncRepository>().iniciarSincronizacaoContinua(appScope)
    }

    /**
     * DEFAULT (não LOW): canais de importância baixa caem no grupo "Silenciosas" da bandeja, que em
     * vários aparelhos (Samsung, Xiaomi) renderiza de forma condensada e esconde as ações até expandir.
     * O som fica desligado explicitamente, então a troca de estado continua silenciosa.
     */
    private fun criarCanalDoCronometro() {
        val channel = NotificationChannel(
            EstudoTimerService.ESTUDO_TIMER_CHANNEL_ID,
            getString(R.string.estudo_timer_canal_nome),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.estudo_timer_canal_descricao)
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
