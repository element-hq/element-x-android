/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.resolver

data class HomeserverData(
    // The computed homeserver url, for which a wellknown file has been retrieved, or just a valid Url
    val homeserverUrl: String,
    // True if a wellknown file has been found and is valid. If false, it means that the [homeserverUrl] is valid
    val isWellknownValid: Boolean,
)
