/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

const val A_FIREBASE_GATEWAY = "aGateway"

class FakeFirebaseGatewayProvider(
    private val firebaseGatewayResult: () -> String = { A_FIREBASE_GATEWAY }
) : FirebaseGatewayProvider {
    override fun getFirebaseGateway() = firebaseGatewayResult()
}
