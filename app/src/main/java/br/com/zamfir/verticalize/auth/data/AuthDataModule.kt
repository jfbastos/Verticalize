package br.com.zamfir.verticalize.auth.data

import br.com.zamfir.verticalize.auth.domain.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authDataModule = module {
    single { FirebaseAuth.getInstance() }
    single<AuthRepository> { FirebaseAuthRepository(get(), androidContext()) }
    singleOf(::GoogleIdTokenRequester)
}
