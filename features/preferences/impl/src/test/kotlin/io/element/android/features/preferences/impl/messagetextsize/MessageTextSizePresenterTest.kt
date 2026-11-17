/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.messagetextsize

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.MessageTextSize
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.lambdaRecorder
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
    fun `present - moving the slider does not store anything until it is let go`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore()
        createPresenter(appPreferencesStore).test {
            awaitItem().eventSink(MessageTextSizeEvent.SetTextSize(20))

            val moved = consumeItemsUntilPredicate { it.textSizeSp == 20 }.last()
            assertThat(appPreferencesStore.getMessageTextSizeFlow().first()).isEqualTo(MessageTextSize.DEFAULT)

            moved.eventSink(MessageTextSizeEvent.CommitTextSize)
            runCurrent()
            assertThat(appPreferencesStore.getMessageTextSizeFlow().first()).isEqualTo(20)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - letting go without having moved the slider stores nothing`() = runTest {
        val setMessageTextSizeResult = lambdaRecorder<Int, Unit> { }
        val appPreferencesStore = object : AppPreferencesStore by InMemoryAppPreferencesStore(messageTextSize = 18) {
            override suspend fun setMessageTextSize(sizeSp: Int) = setMessageTextSizeResult(sizeSp)
        }
        createPresenter(appPreferencesStore).test {
            val state = consumeItemsUntilPredicate { it.textSizeSp == 18 }.last()
            state.eventSink(MessageTextSizeEvent.CommitTextSize)
            cancelAndIgnoreRemainingEvents()
        }

        setMessageTextSizeResult.assertions().isNeverCalled()
    }

    private fun TestScope.createPresenter(
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    ) = MessageTextSizePresenter(
        appPreferencesStore = appPreferencesStore,
        sessionCoroutineScope = this,
    )
}
