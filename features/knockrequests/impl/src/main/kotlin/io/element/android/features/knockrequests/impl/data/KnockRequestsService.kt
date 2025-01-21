/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.data

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.supervisorScope

class KnockRequestsService(
    knockRequestsFlow: Flow<List<KnockRequest>>,
    permissionsFlow: Flow<KnockRequestPermissions>,
    isKnockFeatureEnabledFlow: Flow<Boolean>,
    coroutineScope: CoroutineScope,
) {
    // Keep track of the knock requests that have been handled, so we don't have to wait for sync to remove them.
    private val handledKnockRequestIds = MutableStateFlow<Set<EventId>>(emptySet())

    val knockRequestsFlow = combine(
        isKnockFeatureEnabledFlow,
        knockRequestsFlow,
        handledKnockRequestIds,
    ) { isKnockEnabled, knockRequests, handledKnockIds ->
        if (!isKnockEnabled) {
            AsyncData.Success(persistentListOf())
        } else {
            val presentableKnockRequests = knockRequests
                .filter { it.eventId !in handledKnockIds }
                .map { inner -> KnockRequestWrapper(inner) }
                .toImmutableList()
            AsyncData.Success(presentableKnockRequests)
        }
    }.stateIn(coroutineScope, SharingStarted.Lazily, AsyncData.Loading())

    val permissionsFlow = permissionsFlow.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Lazily,
        initialValue = KnockRequestPermissions(canAccept = false, canDecline = false, canBan = false)
    )

    private fun knockRequestsList() = knockRequestsFlow.value.dataOrNull().orEmpty()

    private fun getKnockRequestById(eventId: EventId): KnockRequestWrapper? {
        return knockRequestsList().find { it.eventId == eventId }
    }

    /**
     * Accept a knock request.
     * @param knockRequest The knock request to accept.
     * @param optimistic If true, the request will be marked as handled before the server responds.
     */
    suspend fun acceptKnockRequest(knockRequest: KnockRequestPresentable, optimistic: Boolean = false): Result<Unit> {
        val wrapped = getKnockRequestById(knockRequest.eventId) ?: return knockRequestNotFoundResult()
        return handleKnockRequest(wrapped, optimistic) { accept() }
    }

    /**
     * Decline a knock request.
     * @param knockRequest The knock request to decline.
     * @param optimistic If true, the request will be marked as handled before the server responds.
     */
    suspend fun declineKnockRequest(knockRequest: KnockRequestPresentable, optimistic: Boolean = false): Result<Unit> {
        val wrapped = getKnockRequestById(knockRequest.eventId) ?: return knockRequestNotFoundResult()
        return handleKnockRequest(wrapped, optimistic) { decline(null) }
    }

    /**
     * Decline a knock request by banning the user.
     * @param knockRequest The knock request to decline.
     * @param optimistic If true, the request will be marked as handled before the server responds.
     */
    suspend fun declineAndBanKnockRequest(knockRequest: KnockRequestPresentable, optimistic: Boolean = false): Result<Unit> {
        val wrapped = getKnockRequestById(knockRequest.eventId) ?: return knockRequestNotFoundResult()
        return handleKnockRequest(wrapped, optimistic) { declineAndBan(null) }
    }

    /**
     * Accept all currently known knock requests.
     * @param optimistic If true, the requests will be marked as handled before the server responds.
     */
    suspend fun acceptAllKnockRequests(optimistic: Boolean = false): Result<Unit> = supervisorScope {
        val results = knockRequestsList()
            .map { knockRequest ->
                async {
                    acceptKnockRequest(knockRequest, optimistic = optimistic)
                }
            }
            .awaitAll()
        if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            Result.failure(KnockRequestsException.AcceptAllPartiallyFailed)
        }
    }

    /**
     * Mark all currently known knock requests as seen.
     */
    suspend fun markAllKnockRequestsAsSeen() = supervisorScope {
        knockRequestsList()
            .map { knockRequest ->
                async { knockRequest.markAsSeen() }
            }
            .awaitAll()
    }

    private suspend fun handleKnockRequest(
        knockRequest: KnockRequestWrapper,
        optimistic: Boolean,
        action: suspend (KnockRequestWrapper.() -> Result<Unit>)
    ): Result<Unit> {
        if (optimistic) {
            handledKnockRequestIds.getAndUpdate { it + knockRequest.eventId }
        }
        return action(knockRequest)
            .onFailure {
                if (optimistic) {
                    handledKnockRequestIds.getAndUpdate { it - knockRequest.eventId }
                }
            }
            .onSuccess {
                if (!optimistic) {
                    handledKnockRequestIds.getAndUpdate { it + knockRequest.eventId }
                }
            }
    }
}

private fun knockRequestNotFoundResult() = Result.failure<Unit>(KnockRequestsException.KnockRequestNotFound)
