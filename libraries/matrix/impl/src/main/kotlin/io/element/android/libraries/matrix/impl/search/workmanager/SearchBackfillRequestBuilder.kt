/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.workmanager

import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.workDataOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.search.workmanager.SearchBackfillWorker.Companion.SESSION_ID_PARAM
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

interface SearchBackfillRequestBuilder : WorkManagerRequestBuilder {
    fun interface Factory {
        fun create(sessionId: SessionId): SearchBackfillRequestBuilder
    }
}

/**
 * Builds a work request that runs the search backfill (back paginations) only when there is network connectivity and the device is charging.
 */
@AssistedInject
class DefaultSearchBackfillRequestBuilder(
    @Assisted private val sessionId: SessionId,
    private val networkMonitor: NetworkMonitor,
) : SearchBackfillRequestBuilder {
    @AssistedFactory
    @ContributesBinding(AppScope::class)
    interface Factory : SearchBackfillRequestBuilder.Factory {
        override fun create(sessionId: SessionId): DefaultSearchBackfillRequestBuilder
    }

    override suspend fun build(): Result<List<WorkManagerRequestWrapper>> {
        val tag = workManagerTag(sessionId, WorkManagerRequestType.SEARCH_BACKFILL)
        val type = WorkManagerWorkerType.Default

        val networkRequestBuilder = NetworkRequest.Builder()
            // Only allow unmetered networks to avoid using mobile data for backfilling.
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            // Some customers use a VPN to connect to their homeserver, so VPN networks must be allowed.
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)

        // In an air-gapped environment, validating internet connectivity would fail and the worker would never run.
        if (networkMonitor.isInAirGappedEnvironment.first()) {
            Timber.d("In an air-gapped environment, not adding NET_CAPABILITY_VALIDATED to the network request")
            networkRequestBuilder.removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkRequest(networkRequestBuilder.build(), NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val request = PeriodicWorkRequestBuilder<SearchBackfillWorker>(1.hours.toJavaDuration())
            // Add a delay before the first execution to avoid running the backfill immediately after the app starts
            .setInitialDelay(5.minutes.toJavaDuration())
            .setInputData(workDataOf(SESSION_ID_PARAM to sessionId.value))
            .addTag(tag)
            .setConstraints(constraints)
            .build()

        return Result.success(listOf(WorkManagerRequestWrapper(request, type)))
    }
}
