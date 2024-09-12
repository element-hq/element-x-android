/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.resolver.network

import retrofit2.http.GET

internal interface WellknownAPI {
    @GET(".well-known/matrix/client")
    suspend fun getWellKnown(): WellKnown
}
