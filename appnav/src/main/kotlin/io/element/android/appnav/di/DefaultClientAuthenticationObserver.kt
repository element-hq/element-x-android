/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.di

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.ClientAuthenticationObserver
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultClientAuthenticationObserver @Inject constructor() : ClientAuthenticationObserver {
    private var listener: ((MatrixClient) -> Unit)? = null

    fun subscribe(listener: (MatrixClient) -> Unit) {
        this.listener = listener
    }

    override fun onClientAuthenticationSucceeded(matrixClient: MatrixClient) {
        listener?.invoke(matrixClient)
    }
}
