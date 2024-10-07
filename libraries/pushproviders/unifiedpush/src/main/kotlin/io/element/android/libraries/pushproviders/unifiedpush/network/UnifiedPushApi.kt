/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.network

import retrofit2.http.GET

interface UnifiedPushApi {
    @GET("_matrix/push/v1/notify")
    suspend fun discover(): DiscoveryResponse
}
