/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryFirebaseStore(
    private var token: String? = null
) : FirebaseStore {
    override fun getFcmToken(): String? = token

    override fun fcmTokenFlow(): Flow<String?> = flowOf(token)

    override fun storeFcmToken(token: String?) {
        this.token = token
    }
}
