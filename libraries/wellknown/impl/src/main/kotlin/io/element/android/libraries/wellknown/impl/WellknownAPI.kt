/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import retrofit2.http.GET

internal interface WellknownAPI {
    @GET(".well-known/matrix/client")
    suspend fun getWellKnown(): InternalWellKnown

    @GET(".well-known/element/element.json")
    suspend fun getElementWellKnown(): InternalElementWellKnown
}
