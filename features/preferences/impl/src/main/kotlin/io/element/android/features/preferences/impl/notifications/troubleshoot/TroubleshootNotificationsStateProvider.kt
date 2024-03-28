/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.preferences.impl.notifications.troubleshoot

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import kotlinx.collections.immutable.toImmutableList

open class TroubleshootNotificationsStateProvider : PreviewParameterProvider<TroubleshootNotificationsState> {
    override val values: Sequence<TroubleshootNotificationsState>
        get() = sequenceOf(
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateIdle(),
                    aTroubleshootTestStateIdle(),
                    aTroubleshootTestStateIdle(visible = false),
                )
            ),
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateInProgress(),
                    aTroubleshootTestStateIdle(),
                    aTroubleshootTestStateIdle(),
                )
            ),
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateSuccess(),
                    aTroubleshootTestStateInProgress(),
                    aTroubleshootTestStateIdle(),
                )
            ),
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateSuccess(),
                    aTroubleshootTestStateWaitingForUser(),
                    aTroubleshootTestStateIdle(),
                )
            ),
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateSuccess(),
                    aTroubleshootTestStateFailure(hasQuickFix = true),
                    aTroubleshootTestStateInProgress(),
                )
            ),
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateSuccess(),
                    aTroubleshootTestStateFailure(hasQuickFix = true),
                    aTroubleshootTestStateFailure(hasQuickFix = false),
                )
            ),
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateSuccess(),
                    aTroubleshootTestStateSuccess(),
                    aTroubleshootTestStateSuccess(),
                )
            ),
            aTroubleshootNotificationsState(
                listOf(
                    aTroubleshootTestStateWaitingForUser(),
                )
            ),
        )
}

fun aTroubleshootNotificationsState(
    tests: List<NotificationTroubleshootTestState> = emptyList(),
    eventSink: (TroubleshootNotificationsEvents) -> Unit = {},
) = TroubleshootNotificationsState(
    eventSink = eventSink,
    testSuiteState = TroubleshootTestSuiteState(
        mainState = tests.computeMainState(),
        tests = tests.toImmutableList(),
    ),
)

fun aTroubleshootTestState(
    status: NotificationTroubleshootTestState.Status,
    name: String = "Test",
    description: String = "Description",
): NotificationTroubleshootTestState {
    return NotificationTroubleshootTestState(
        name = name,
        description = description,
        status = status,
    )
}

fun aTroubleshootTestStateIdle(visible: Boolean = true) =
    aTroubleshootTestState(status = NotificationTroubleshootTestState.Status.Idle(visible = visible))

fun aTroubleshootTestStateInProgress() =
    aTroubleshootTestState(status = NotificationTroubleshootTestState.Status.InProgress)

fun aTroubleshootTestStateWaitingForUser() =
    aTroubleshootTestState(status = NotificationTroubleshootTestState.Status.WaitingForUser)

fun aTroubleshootTestStateSuccess() =
    aTroubleshootTestState(status = NotificationTroubleshootTestState.Status.Success)

fun aTroubleshootTestStateFailure(hasQuickFix: Boolean) =
    aTroubleshootTestState(status = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = hasQuickFix))
