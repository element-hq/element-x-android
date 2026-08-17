/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

/**
 * Parses the raw JSON of an Element `.well-known` document.
 */
fun interface ElementWellKnownParser {
    /**
     * Parses the document, failing when it is not valid JSON; unknown fields are ignored so a newer server does not break an older client.
     *
     * @param json the raw document as served by the server.
     */
    fun parse(json: String): Result<ElementWellKnown>
}
