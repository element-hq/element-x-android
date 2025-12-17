/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Factory used to create a [DataStore] for preferences.
 *
 * It's a wrapper around AndroidX's `PreferenceDataStoreFactory` to make testing easier.
 */
interface PreferenceDataStoreFactory {
    fun create(name: String): DataStore<Preferences>
}
