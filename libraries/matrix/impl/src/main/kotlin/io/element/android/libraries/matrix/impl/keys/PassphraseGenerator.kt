/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.keys

interface PassphraseGenerator {
    /**
     * Generate a passphrase to encrypt the databases of a session.
     * Return null to not encrypt the databases.
     */
    fun generatePassphrase(): String?
}
