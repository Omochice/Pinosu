package io.github.omochice.pinosu.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.core.crypto.TinkKeyManager
import io.github.omochice.pinosu.feature.auth.data.local.AuthData
import io.github.omochice.pinosu.feature.auth.data.local.AuthDataSerializer
import java.io.File
import javax.inject.Singleton

/**
 * Hilt module for DataStore dependency injection
 *
 * Provides encrypted DataStore instances for secure local storage.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

  private const val AUTH_DATA_STORE_FILE = "pinosu_auth_data.pb"

  @Provides
  @Singleton
  fun provideAuthDataStore(
      @ApplicationContext context: Context,
      tinkKeyManager: TinkKeyManager
  ): DataStore<AuthData> =
      DataStoreFactory.create(
          serializer = AuthDataSerializer(tinkKeyManager.getAead()),
          produceFile = { File(context.filesDir, AUTH_DATA_STORE_FILE) })
}
