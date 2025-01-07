/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.resolver.network

class FakeWellknownRequest : WellknownRequest {
    private var resultMap: Map<String, WellKnown> = emptyMap()
    fun givenResultMap(map: Map<String, WellKnown>) {
        resultMap = map
    }

    override suspend fun execute(baseUrl: String): WellKnown {
        return resultMap[baseUrl] ?: error("No result provided for $baseUrl")
    }
}
