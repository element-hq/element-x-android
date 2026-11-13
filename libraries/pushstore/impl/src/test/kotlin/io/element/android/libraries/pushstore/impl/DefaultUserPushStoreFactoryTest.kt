/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import androidx.datastore.preferences.core.PreferenceDataStoreFactory as AndroidPreferenceDataStoreFactory

class DefaultUserPushStoreFactoryTest : RobolectricTest() {
    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun `getOrCreate returns the same store when called in parallel for the same session`() = runTest {
        val sut = DefaultUserPushStoreFactory(
            context = context,
            preferenceDataStoreFactory = FileBackedPreferenceDataStoreFactory(context),
        )
        val threadCount = 10
        val startSignal = CountDownLatch(1)
        val stores = arrayOfNulls<UserPushStore>(threadCount)
        val threads = List(threadCount) { index ->
            thread {
                startSignal.await()
                stores[index] = sut.getOrCreate(A_SESSION_ID)
            }
        }
        // Release all the threads at once to maximise the chances of concurrent calls.
        startSignal.countDown()
        threads.forEach { it.join() }

        assertThat(stores.distinct()).hasSize(1)
        // Reading the store would throw if several DataStores were active for the same file.
        stores.forEach { store ->
            assertThat(store!!.getNotificationEnabledForDevice().first()).isTrue()
        }
    }

    @Test
    fun `getOrCreate returns a different store for each session`() {
        val sut = DefaultUserPushStoreFactory(
            context = context,
            preferenceDataStoreFactory = FileBackedPreferenceDataStoreFactory(context),
        )
        val store1 = sut.getOrCreate(A_SESSION_ID)
        val store2 = sut.getOrCreate(A_SESSION_ID_2)
        assertThat(store1).isNotSameInstanceAs(store2)
        assertThat(sut.getOrCreate(A_SESSION_ID)).isSameInstanceAs(store1)
    }
}

/**
 * Unlike `FakePreferenceDataStoreFactory`, which uses a new temporary file for each store, this factory persists each store
 * to a file derived from its name, like the production one. So creating 2 stores with the same name and using them both
 * fails, as it would in the app.
 */
private class FileBackedPreferenceDataStoreFactory(
    private val context: Context,
) : PreferenceDataStoreFactory {
    override fun create(name: String): DataStore<Preferences> {
        return AndroidPreferenceDataStoreFactory.create { context.preferencesDataStoreFile(name) }
    }
}
