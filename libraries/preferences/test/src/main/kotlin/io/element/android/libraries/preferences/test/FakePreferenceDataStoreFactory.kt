/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.preferences.test

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import java.io.File
import androidx.datastore.preferences.core.PreferenceDataStoreFactory as AndroidPreferenceDataStoreFactory

class FakePreferenceDataStoreFactory : PreferenceDataStoreFactory {
    override fun create(name: String): DataStore<Preferences> {
        return AndroidPreferenceDataStoreFactory.create { File.createTempFile("test", ".preferences_pb") }
    }
}
