/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

const val A_UNIFIED_PUSH_GATEWAY = "aGateway"

class FakeDefaultPushGatewayHttpUrlProvider(
    private val provideResult: () -> String = { A_UNIFIED_PUSH_GATEWAY }
) : DefaultPushGatewayHttpUrlProvider {
    override fun provide(): String {
        return provideResult()
    }
}
