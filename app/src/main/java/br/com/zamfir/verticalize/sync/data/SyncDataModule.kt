package br.com.zamfir.verticalize.sync.data

import android.content.Context
import br.com.zamfir.verticalize.sync.domain.CloudSyncRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

private const val SYNC_PREFS_NAME = "cloud_sync"

val syncDataModule = module {
    single { FirebaseBackupDataSource() }
    single {
        FirebaseCloudSyncRepository(
            dao = get(),
            remote = get(),
            authRepository = get(),
            prefs = androidContext().getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)
        )
    } bind CloudSyncRepository::class
}
