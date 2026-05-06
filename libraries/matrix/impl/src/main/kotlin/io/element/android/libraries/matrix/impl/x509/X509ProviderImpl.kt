/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import android.app.Activity
import android.content.Context
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class X509ProviderImpl(
    @ApplicationContext
    private val context: Context,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
): X509Provider {
    private val store = preferenceDataStoreFactory.create("x509_preferences")
    private val keyAliasKey = stringPreferencesKey("key_alias")

    suspend fun getKeyAlias(): String? {
        return store.data.map { prefs -> prefs[keyAliasKey] }.first()
    }

    override suspend fun initKeyAlias(parentActivity: Activity) {
        val keyAlias = getKeyAlias()
        if (keyAlias != null) {
            Timber.i("X509: using key alias %s", keyAlias)
        }
        if (keyAlias == null) {
            val chosen = chooseKeyAlias(parentActivity)
            if (chosen != null) {
                Timber.i("X509: using key alias %s", chosen)
                store.edit { prefs -> prefs[keyAliasKey] = chosen }
            }
        }
    }

    suspend fun chooseKeyAlias(activity: Activity): String? = suspendCoroutine { continuation ->
        KeyChain.choosePrivateKeyAlias(
            activity,
            KeyChainAliasCallback { alias: String? ->
                Timber.d("X509: key granted: %s", alias)
                continuation.resume(alias)
            },
            null, null, null, null
        )
    }



    override suspend fun getX509KeyPair(): X509KeyPair? {
        val alias = this.getKeyAlias()
        if (alias == null) {
            Timber.w("X509: No key granted (yet)")
            return null
        }

        var key = KeyChain.getPrivateKey(context, alias)
        var certificateChain = KeyChain.getCertificateChain(context, alias)

        if (key == null || certificateChain == null) {
            Timber.w("X509: Key %s not found", alias)
            return null
        }

        return X509KeyPair(key, certificateChain)
    }

    override suspend fun getX509TustRoot(): X509TrustRoot? {
        return X509TrustRoot()
    }
}
