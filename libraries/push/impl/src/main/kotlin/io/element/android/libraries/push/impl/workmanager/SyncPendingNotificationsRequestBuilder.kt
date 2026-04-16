/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.workDataOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.workmanager.SyncPendingNotificationsRequestBuilder.Companion.SESSION_ID
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.flow.first
import timber.log.Timber

interface SyncPendingNotificationsRequestBuilder : WorkManagerRequestBuilder {
    fun interface Factory {
        fun create(sessionId: SessionId): SyncPendingNotificationsRequestBuilder
    }

    companion object {
        const val SESSION_ID = "session_id"
    }
}

@AssistedInject
class DefaultSyncPendingNotificationsRequestBuilder(
    @Assisted private val sessionId: SessionId,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
    private val networkMonitor: NetworkMonitor,
    private val featureFlagService: FeatureFlagService,
) : SyncPendingNotificationsRequestBuilder {
    @AssistedFactory
    @ContributesBinding(AppScope::class)
    interface Factory : SyncPendingNotificationsRequestBuilder.Factory {
        override fun create(sessionId: SessionId): DefaultSyncPendingNotificationsRequestBuilder
    }

    override suspend fun build(): Result<List<WorkManagerRequestWrapper>> {
        val type = WorkManagerWorkerType.Unique(
            name = workManagerTag(sessionId = sessionId, requestType = WorkManagerRequestType.NOTIFICATION_SYNC),
            policy = ExistingWorkPolicy.APPEND_OR_REPLACE,
        )

        val networkRequestBuilder = NetworkRequest.Builder()
            // Allow any kind of network that can have internet connectivity.
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            // By default, the network request will require the device to not be in VPN, but since some customers use a VPN to connect to their homeserver,
            // we need to allow VPN networks.
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)

        // If we're in an air-gapped environment, we shouldn't validate internet connectivity, as the checker will fail and the worker won't run at all.
        if (networkMonitor.isInAirGappedEnvironment.first()) {
            Timber.d("In an air-gapped environment, not adding NET_CAPABILITY_VALIDATED to the network request")
            networkRequestBuilder.removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else if (featureFlagService.isFeatureEnabled(FeatureFlags.ValidateNetworkWhenSchedulingNotificationFetching)) {
            Timber.d("Not in an air-gapped environment, adding NET_CAPABILITY_VALIDATED to the network request")
            networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkRequest(networkRequestBuilder.build(), NetworkType.NOT_REQUIRED)
            .build()

        val request = OneTimeWorkRequestBuilder<FetchPendingNotificationsWorker>()
            .setInputData(workDataOf(SESSION_ID to sessionId.value))
            .apply {
                // Expedited workers aren't needed on Android 12 or lower:
                // They force displaying a foreground sync notification for no good reason, since they sync almost immediately anyway
                // See https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#backwards-compat
                if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
                    setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                }
            }
            .setConstraints(networkConstraints)
            .setTraceTag(workManagerTag(sessionId, WorkManagerRequestType.NOTIFICATION_SYNC))
            .build()

        return Result.success(listOf(WorkManagerRequestWrapper(request, type)))
    }
}
