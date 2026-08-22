/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.Inject
import io.element.android.appconfig.AppUpdateConfig
import io.element.android.features.appupdate.api.AvailableUpdate
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

private val lastCheckKey = longPreferencesKey("lastCheckMs")
private val ignoredVersionKey = longPreferencesKey("ignoredVersionCode")
private val pendingUpdateKey = stringPreferencesKey("pendingUpdateJson")

/** Serializable mirror of [AvailableUpdate] for the local pending-update cache. */
@Serializable
private data class StoredUpdate(
    val versionName: String,
    val versionCode: Long,
    val url: String,
    val sha256: String,
) {
    fun toAvailableUpdate() = AvailableUpdate(
        versionName = versionName,
        versionCode = versionCode,
        url = url,
        sha256 = sha256,
    )

    companion object {
        fun from(update: AvailableUpdate) = StoredUpdate(
            versionName = update.versionName,
            versionCode = update.versionCode,
            url = update.url,
            sha256 = update.sha256,
        )
    }
}

/**
 * Checks the public Feral update channel for a newer release.
 *
 * Fail-quiet by design: any network/parsing problem simply yields "no update".
 * The network is hit at most once per [AppUpdateConfig.CHECK_INTERVAL_MS]; in between,
 * the last fetched result is served from the local store so the banner survives app
 * restarts without re-fetching.
 */
@Inject
class AppUpdateChecker(
    private val okHttpClient: OkHttpClient,
    private val buildMeta: BuildMeta,
    private val coroutineDispatchers: CoroutineDispatchers,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) {
    private val store = preferenceDataStoreFactory.create("feral_appupdate")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdate(): AvailableUpdate? = withContext(coroutineDispatchers.io) {
        if (!AppUpdateConfig.ENABLED) return@withContext null
        val prefs = store.data.first()
        val ignored = prefs[ignoredVersionKey]
        val now = System.currentTimeMillis()
        val lastCheck = prefs[lastCheckKey] ?: 0L
        if (now - lastCheck < AppUpdateConfig.CHECK_INTERVAL_MS) {
            return@withContext pendingFromStore(prefs[pendingUpdateKey], ignored)
        }
        val manifest = fetchManifest() ?: return@withContext pendingFromStore(prefs[pendingUpdateKey], ignored)
        val update = manifest.selectUpdate(
            supportedAbis = Build.SUPPORTED_ABIS.orEmpty().toList(),
            currentVersionCode = buildMeta.versionCode,
            ignoredVersionCode = ignored,
        )
        store.edit { editable ->
            editable[lastCheckKey] = now
            if (update != null) {
                editable[pendingUpdateKey] = json.encodeToString(StoredUpdate.serializer(), StoredUpdate.from(update))
            } else {
                editable.remove(pendingUpdateKey)
            }
        }
        update
    }

    suspend fun ignoreVersion(versionCode: Long) {
        store.edit { editable ->
            editable[ignoredVersionKey] = versionCode
            editable.remove(pendingUpdateKey)
        }
    }

    private fun pendingFromStore(encoded: String?, ignored: Long?): AvailableUpdate? {
        if (encoded == null) return null
        val pending = runCatchingExceptions {
            json.decodeFromString(StoredUpdate.serializer(), encoded).toAvailableUpdate()
        }.getOrNull() ?: return null
        if (pending.versionCode.releaseOrdinal() <= buildMeta.versionCode.releaseOrdinal()) return null
        if (pending.versionCode == ignored) return null
        return pending
    }

    private fun fetchManifest(): UpdateManifest? {
        val request = Request.Builder()
            .url(AppUpdateConfig.BASE_URL + "update.json")
            .build()
        return runCatchingExceptions {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatchingExceptions null
                val body = response.body.string()
                json.decodeFromString(UpdateManifest.serializer(), body)
            }
        }.getOrNull()
    }
}
