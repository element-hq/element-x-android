/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface FirebaseGatewayProvider {
    fun getFirebaseGateway(): String
}

@ContributesBinding(AppScope::class)
class DefaultFirebaseGatewayProvider @Inject constructor(
    private val enterpriseService: EnterpriseService,
) : FirebaseGatewayProvider {
    override fun getFirebaseGateway(): String {
        return enterpriseService.firebasePushGateway() ?: FirebaseConfig.PUSHER_HTTP_URL
    }
}
