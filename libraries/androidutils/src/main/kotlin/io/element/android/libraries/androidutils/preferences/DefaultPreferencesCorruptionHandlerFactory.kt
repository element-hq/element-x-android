/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.preferences

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences

object DefaultPreferencesCorruptionHandlerFactory {
    /**
     * Creates a [ReplaceFileCorruptionHandler] that will replace the corrupted preferences file with an empty preferences object.
     */
    fun replaceWithEmpty(): ReplaceFileCorruptionHandler<Preferences> {
        return ReplaceFileCorruptionHandler(
            produceNewData = {
                // If the preferences file is corrupted, we return an empty preferences object
                emptyPreferences()
            },
        )
    }
}
