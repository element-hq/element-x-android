/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api.remoteconfig

import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

/**
 * Wrapper to fetch the remote enterprise configuration for a given session, either from a local cache or from a remote source.
 */
interface SessionRemoteEnterpriseConfigProvider {
    suspend fun get(): WellknownRetrieverResult<RemoteEnterpriseConfig>
}
