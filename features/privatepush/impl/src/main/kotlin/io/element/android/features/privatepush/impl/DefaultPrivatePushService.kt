/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.appconfig.FeralPushConfig
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.privatepush.api.PrivatePushService
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.features.privatepush.impl.system.InstalledAppsDetector
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.feral.FeralPushFallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * "BuiltIn" == the Feral built-in provider (libraries/pushproviders/feral) is the registered one.
 * "Private" == the ntfy distributor is the current one AND the registered UnifiedPush endpoint
 * (PushProvider.getPushConfig().pushKey) lives on [PrivatePushConfig.SERVER_HOST].
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultPrivatePushService(
    private val pushService: PushService,
    private val installedAppsDetector: InstalledAppsDetector,
    private val feralPushFallback: FeralPushFallback,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : PrivatePushService {
    private val store = preferenceDataStoreFactory.create("feral_privatepush")
    private val requests = MutableStateFlow<Set<SessionId>>(emptySet())

    private fun dismissedKey(sessionId: SessionId) = booleanPreferencesKey("dismissed_${sessionId.value}")

    override suspend fun status(sessionId: SessionId): PrivatePushStatus {
        val currentProvider = pushService.getCurrentPushProvider(sessionId)
        if (currentProvider?.name == FeralPushConfig.NAME && currentProvider.getPushConfig(sessionId) != null) {
            return PrivatePushStatus.BuiltIn
        }
        if (!installedAppsDetector.isInstalled(PrivatePushConfig.NTFY_PACKAGE)) {
            return PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NtfyNotInstalled)
        }
        val provider = currentProvider
            ?: return PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected)
        val distributor = provider.getCurrentDistributor(sessionId)
        val endpoint = provider.getPushConfig(sessionId)?.pushKey
        if (distributor?.value != PrivatePushConfig.NTFY_PACKAGE || endpoint.isNullOrBlank()) {
            return PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected)
        }
        // An endpoint that is not an http(s) URL cannot be a working push target: treat it as not
        // connected rather than printing the raw value as a "server" in the UI.
        val host = endpoint.toHttpUrlOrNull()?.host
            ?: return PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected)
        return if (host.equals(PrivatePushConfig.SERVER_HOST, ignoreCase = true)) {
            PrivatePushStatus.Private
        } else {
            PrivatePushStatus.PublicServer(host)
        }
    }

    override suspend fun fallBackToBuiltIn(matrixClient: MatrixClient): Boolean {
        return feralPushFallback.register(matrixClient)
    }

    override fun isDismissed(sessionId: SessionId): Flow<Boolean> =
        store.data.map { it[dismissedKey(sessionId)] ?: false }

    override suspend fun setDismissed(sessionId: SessionId, dismissed: Boolean) {
        store.edit { it[dismissedKey(sessionId)] = dismissed }
    }

    override fun setupRequested(sessionId: SessionId): Flow<Boolean> =
        requests.map { sessionId in it }.distinctUntilChanged()

    override fun requestSetup(sessionId: SessionId) {
        requests.update { it + sessionId }
    }

    override fun clearSetupRequest(sessionId: SessionId) {
        requests.update { it - sessionId }
    }
}
