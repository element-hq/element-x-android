/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

object UnifiedPushConfig {
    /**
     * It is the push gateway for UnifiedPush.
     * Note: default_push_gateway_http_url should have path '/_matrix/push/v1/notify'
     */
    const val DEFAULT_PUSH_GATEWAY_HTTP_URL: String = "https://matrix.gateway.unifiedpush.org/_matrix/push/v1/notify"

    const val UNIFIED_PUSH_DISTRIBUTORS_URL = "https://unifiedpush.org/users/distributors/"

    const val INDEX = 1
    const val NAME = "UnifiedPush"
}
