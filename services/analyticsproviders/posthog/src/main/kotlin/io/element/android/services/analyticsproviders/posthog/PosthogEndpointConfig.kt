/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.posthog

data class PosthogEndpointConfig(
    val host: String,
    val apiKey: String,
) {
    val isValid = host.isNotBlank() && apiKey.isNotBlank()
}
