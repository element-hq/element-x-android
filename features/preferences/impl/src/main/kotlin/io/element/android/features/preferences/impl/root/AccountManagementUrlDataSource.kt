/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import androidx.annotation.VisibleForTesting
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

interface AccountManagementUrlDataSource {
    /**
     * Return a flow of nullable URLs.
     */
    fun getAccountManagementUrl(action: AccountManagementAction?): Flow<String?>
}

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultAccountManagementUrlDataSource @Inject constructor(
    private val matrixClient: MatrixClient,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : AccountManagementUrlDataSource {
    /**
     * It will first emit the value from cache, then it will fetch it from the SDK
     * (with network request), store the value in cache and emit it.
     */
    override fun getAccountManagementUrl(action: AccountManagementAction?): Flow<String?> {
        val key = action.toKey()
        return flow {
            // emit value from cache if available
            val fromCache = sessionPreferencesStore.getString(key).first()
            emit(fromCache)
            // Then load value from the SDK
            matrixClient.getAccountManagementUrl(action)
                .onFailure {
                    Timber.w(it, "Unable to get account management URL for $action")
                }
                .onSuccess { url ->
                    sessionPreferencesStore.setString(key, url)
                    emit(url)
                }
                .getOrNull()
        }.distinctUntilChanged()
    }
}

@VisibleForTesting
internal fun AccountManagementAction?.toKey(): String {
    return when (this) {
        AccountManagementAction.Profile -> "account_management_url_profile"
        is AccountManagementAction.SessionEnd -> "account_management_url_session_end_$deviceId"
        is AccountManagementAction.SessionView -> "account_management_url_session_view_$deviceId"
        AccountManagementAction.SessionsList -> "account_management_url_sessions_list"
        null -> "account_management_url"
    }
}
