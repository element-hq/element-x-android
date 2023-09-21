/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.notifications

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingPresenter
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingStateEvents
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EditDefaultNotificationSettingsPresenterTests {
    @Test
    fun `present - ensures initial state is correct`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = EditDefaultNotificationSettingPresenter(notificationSettingsService = notificationSettingsService, isOneToOne = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.mode).isNull()
            Truth.assertThat(initialState.isOneToOne).isFalse()

            val loadedState = consumeItemsUntilPredicate {
                it.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            Truth.assertThat(loadedState.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
        }
    }

    @Test
    fun `present - edit default notification setting`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = EditDefaultNotificationSettingPresenter(notificationSettingsService = notificationSettingsService, isOneToOne = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(EditDefaultNotificationSettingStateEvents.SetNotificationMode(RoomNotificationMode.ALL_MESSAGES))
            val loadedState = consumeItemsUntilPredicate {
                it.mode == RoomNotificationMode.ALL_MESSAGES
            }.last()
            Truth.assertThat(loadedState.mode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        }
    }
}
