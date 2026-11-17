/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.messagetextsize

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.api.store.MessageTextSize
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageTextSizePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state is the default size`() = runTest {
        createPresenter().test {
            assertThat(awaitItem().textSizeSp).isEqualTo(MessageTextSize.DEFAULT)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - the stored size reaches the state`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore().apply {
            setMessageTextSize(MessageTextSize.MAX)
        }
        createPresenter(appPreferencesStore).test {
            consumeItemsUntilPredicate { it.textSizeSp == MessageTextSize.MAX }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - changing the size is not stored while the screen is open`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore()
        createPresenter(appPreferencesStore).test {
            awaitItem().eventSink(MessageTextSizeEvent.SetTextSize(20))

            consumeItemsUntilPredicate { it.textSizeSp == 20 }
            assertThat(appPreferencesStore.getMessageTextSizeFlow().first()).isEqualTo(MessageTextSize.DEFAULT)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - the size is stored when leaving the screen`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore()
        createPresenter(appPreferencesStore).test {
            awaitItem().eventSink(MessageTextSizeEvent.SetTextSize(20))
            consumeItemsUntilPredicate { it.textSizeSp == 20 }
            cancelAndIgnoreRemainingEvents()
        }

        runCurrent()
        assertThat(appPreferencesStore.getMessageTextSizeFlow().first()).isEqualTo(20)
    }

    @Test
    fun `present - leaving the screen without touching the slider keeps the stored size`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore(messageTextSize = 18)
        createPresenter(appPreferencesStore).test {
            consumeItemsUntilPredicate { it.textSizeSp == 18 }
            cancelAndIgnoreRemainingEvents()
        }

        runCurrent()
        assertThat(appPreferencesStore.getMessageTextSizeFlow().first()).isEqualTo(18)
    }

    private fun TestScope.createPresenter(
        appPreferencesStore: InMemoryAppPreferencesStore = InMemoryAppPreferencesStore(),
    ) = MessageTextSizePresenter(
        appPreferencesStore = appPreferencesStore,
        sessionCoroutineScope = this,
    )
}
