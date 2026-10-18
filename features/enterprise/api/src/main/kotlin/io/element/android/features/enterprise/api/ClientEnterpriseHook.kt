/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import io.element.android.libraries.matrix.api.MatrixClient

/**
 * A hook that can be used to customize the [MatrixClient] for enterprise features.
 */
fun interface ClientEnterpriseHook {
    suspend operator fun invoke(client: MatrixClient)
}
