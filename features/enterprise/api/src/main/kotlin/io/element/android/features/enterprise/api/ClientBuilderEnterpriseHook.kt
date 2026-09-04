/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import io.element.android.libraries.matrix.api.MatrixClientBuilder
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * A hook that can be used to customize the [MatrixClientBuilder] for enterprise features.
 */
interface ClientBuilderEnterpriseHook {
    /**
     * Customize the [MatrixClientBuilder] for enterprise features.
     * This method is invoked everytime a new [MatrixClientBuilder] is created.
     *
     * @param clientBuilder The [MatrixClientBuilder] to customize.
     * @return The customized [MatrixClientBuilder].
     */
    suspend fun tweakClientBuilder(clientBuilder: MatrixClientBuilder): MatrixClientBuilder

    /**
     * Customize the [MatrixClientBuilder] for enterprise features.
     * This method is invoked when a new [MatrixClientBuilder] is created to build a client for a specific session.
     *
     * @param clientBuilder The [MatrixClientBuilder] to customize.
     * @param sessionId The [SessionId] for which the [MatrixClientBuilder] is being created.
     * @return The customized [MatrixClientBuilder].
     */
    suspend fun tweakClientBuilder(clientBuilder: MatrixClientBuilder, sessionId: SessionId): MatrixClientBuilder
}
