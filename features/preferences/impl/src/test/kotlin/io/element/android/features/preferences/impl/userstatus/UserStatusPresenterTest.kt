/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UserStatusPresenterTest {
    private fun createPresenter(client: FakeMatrixClient = FakeMatrixClient()) =
        UserStatusPresenter(matrixClient = client)

    @Test
    fun `initial state - no status set`() = runTest {
        createPresenter().test {
            val state = awaitItem()
            assertThat(state.displayedStatus).isNull()
            assertThat(state.pickerState).isEqualTo(UserStatusPickerState.Hidden)
        }
    }

    @Test
    fun `Open event transitions to ShowingPicker`() = runTest {
        createPresenter().test {
            val state = awaitItem()
            state.eventSink(UserStatusEvent.OpenPicker)
            assertThat(awaitItem().pickerState).isEqualTo(UserStatusPickerState.ShowingPicker)
        }
    }

    @Test
    fun `Dismiss event from ShowingPicker goes back to Hidden`() = runTest {
        createPresenter().test {
            val state = awaitItem()
            state.eventSink(UserStatusEvent.OpenPicker)
            awaitItem() // ShowingPicker
            state.eventSink(UserStatusEvent.DismissPicker)
            assertThat(awaitItem().pickerState).isEqualTo(UserStatusPickerState.Hidden)
        }
    }

    @Test
    fun `Set event calls setUserStatus and transitions to Hidden with updated status`() = runTest {
        val client = FakeMatrixClient()
        createPresenter(client).test {
            val status = UserStatus(emoji = "💬", text = "In a meeting")
            awaitItem().eventSink(UserStatusEvent.SetStatus(status))
            val newState = awaitItem()
            assertThat(newState.pickerState).isEqualTo(UserStatusPickerState.Hidden)
            assertThat(newState.displayedStatus).isEqualTo(DisplayedStatus.UserSet(status))
            assertThat(client.setUserStatusCalled).isTrue()
        }
    }

    @Test
    fun `OpenCustomInput with no existing status uses defaults`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(UserStatusEvent.OpenCustomInput)
            val state = awaitItem()
            val pickerState = state.pickerState as UserStatusPickerState.CustomInput
            assertThat(pickerState.emoji).isEqualTo("😀")
            assertThat(pickerState.textFieldState.text.toString()).isEqualTo("")
        }
    }

    @Test
    fun `OpenCustomInput with existing rawStatus pre-fills values`() = runTest {
        val client = FakeMatrixClient()
        createPresenter(client).test {
            val status = UserStatus(emoji = "🌴", text = "Away")
            awaitItem().eventSink(UserStatusEvent.SetStatus(status))
            // Single emission: pickerState=Hidden and displayedStatus both set in one recomposition
            awaitItem().eventSink(UserStatusEvent.OpenCustomInput)
            val state = awaitItem()
            val pickerState = state.pickerState as UserStatusPickerState.CustomInput
            assertThat(pickerState.emoji).isEqualTo("🌴")
            assertThat(pickerState.textFieldState.text.toString()).isEqualTo("Away")
        }
    }

    @Test
    fun `UpdateCustomEmoji updates emoji in CustomInput state`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(UserStatusEvent.OpenCustomInput)
            awaitItem().eventSink(UserStatusEvent.UpdateCustomEmoji("🚀"))
            val state = awaitItem()
            assertThat((state.pickerState as UserStatusPickerState.CustomInput).emoji).isEqualTo("🚀")
        }
    }

    @Test
    fun `CancelCustomInput goes back to Hidden`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(UserStatusEvent.OpenCustomInput)
            awaitItem().eventSink(UserStatusEvent.CancelCustomInput) // CustomInput state, fire Cancel
            assertThat(awaitItem().pickerState).isEqualTo(UserStatusPickerState.Hidden)
        }
    }

    @Test
    fun `Clear event calls clearUserStatus and clears displayed status`() = runTest {
        val client = FakeMatrixClient()
        createPresenter(client).test {
            val status = UserStatus(emoji = "☕", text = "Be right back")
            awaitItem().eventSink(UserStatusEvent.SetStatus(status))
            // Single emission: pickerState=Hidden and displayedStatus both set in one recomposition
            awaitItem().eventSink(UserStatusEvent.ClearStatus)
            val clearedState = awaitItem()
            assertThat(clearedState.pickerState).isEqualTo(UserStatusPickerState.Hidden)
            assertThat(clearedState.displayedStatus).isNull()
            assertThat(client.clearUserStatusCalled).isTrue()
        }
    }
}
