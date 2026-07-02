/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

interface ElementWellknownStore {
    suspend fun get(domain: String): WellknownRetrieverResult<ElementWellKnown>
    suspend fun update(domain: String, wellknown: String): Result<Unit>
    suspend fun delete(domain: String): Result<Unit>
}
