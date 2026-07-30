/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

/**
 * Retrieves the well-known configuration for a given session, either from a local cache or from a remote source.
 *
 * Note: This should only be used from an implementation of `SessionRemoteEnterpriseConfigProvider`, and be moved in the future to
 * the `features/enterprise/api` module.
 */
interface SessionWellknownRetriever {
    /**
     * Retrieves the well-known configuration for the given [source].
     */
    suspend fun getElementWellKnown(source: ElementWellKnownSource): WellknownRetrieverResult<ElementWellKnown>
}

/**
 * The source from which the well-known configuration is retrieved.
 */
enum class ElementWellKnownSource {
    /**
     * Retrieve the well-known configuration from the Element Server Suite configuration (ESS_CONFIG) endpoint.
     */
    ESS_CONFIG,

    /**
     * Retrieve the well-known configuration from the Element server's well-known endpoint.
     */
    WELLKNOWN_ENDPOINT,
}
