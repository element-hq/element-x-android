/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.resolver

data class HomeserverData(
    // The computed homeserver url, for which a wellknown file has been retrieved, or just a valid Url
    val homeserverUrl: String,
)
