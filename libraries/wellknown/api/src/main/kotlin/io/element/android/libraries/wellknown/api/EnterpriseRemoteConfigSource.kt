/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

/**
 * The source from which the enterprise remote configuration is retrieved.
 */
enum class EnterpriseRemoteConfigSource {
    /**
     * Retrieve the enterprise remote configuration from the Element Server Suite configuration (ESS_CONFIG) endpoint.
     */
    ESS_CONFIG,

    /**
     * Retrieve the enterprise remote configuration from the Element server's well-known endpoint.
     */
    WELLKNOWN_ENDPOINT,
}
