/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

/**
 * Exposes build information about the bundled `matrix-rust-sdk`.
 *
 * Mostly used to identify the exact SDK revision in bug reports and in the user agent.
 */
interface SdkMetadata {
    /**
     * The full git SHA of the commit the bundled `matrix-rust-sdk` was built from.
     */
    val sdkGitSha: String
}
