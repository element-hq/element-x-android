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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState.Status
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent

/**
 * A view that allows a user edit their global notification settings.
 */
@Composable
fun TroubleshootNotificationsView(
    state: TroubleshootNotificationsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (state.hasFailedTests) {
                    state.eventSink(TroubleshootNotificationsEvents.RetryFailedTests)
                }
            }
            else -> Unit
        }
    }

    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = "Troubleshoot notifications",
    ) {
        TroubleshootNotificationsContent(state)
    }
}

@Composable
private fun TroubleshootTestView(
    testState: NotificationTroubleshootTestState,
    onQuickFixClicked: () -> Unit,
) {
    if ((testState.status as? Status.Idle)?.visible == false) return
    ListItem(
        headlineContent = { Text(text = testState.name) },
        supportingContent = { Text(text = testState.description) },
        trailingContent = when (testState.status) {
            is Status.Idle -> null
            Status.InProgress -> ListItemContent.Custom {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .size(20.dp),
                    strokeWidth = 2.dp
                )
            }
            Status.WaitingForUser -> ListItemContent.Custom {
                Icon(
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    imageVector = CompoundIcons.Info(),
                    tint = ElementTheme.colors.iconAccentTertiary
                )
            }
            Status.Success -> ListItemContent.Custom {
                Icon(
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    imageVector = CompoundIcons.Check(),
                    tint = ElementTheme.colors.iconAccentTertiary
                )
            }
            is Status.Failure -> ListItemContent.Custom {
                Icon(
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    imageVector = CompoundIcons.Error(),
                    tint = ElementTheme.colors.textCriticalPrimary
                )
            }
        }
    )
    if ((testState.status as? Status.Failure)?.hasQuickFix == true) {
        ListItem(
            headlineContent = {
            },
            trailingContent = ListItemContent.Custom {
                Button(
                    text = "Attempt to fix",
                    onClick = onQuickFixClicked
                )
            }
        )
    }
}

@Composable
private fun TroubleshootNotificationsContent(state: TroubleshootNotificationsState) {
    when (state.testSuiteState.mainState) {
        AsyncAction.Loading,
        AsyncAction.Confirming,
        is AsyncAction.Success,
        is AsyncAction.Failure -> {
            TestSuiteView(
                testSuiteState = state.testSuiteState,
                onQuickFixClicked = {
                    state.eventSink(TroubleshootNotificationsEvents.QuickFix(it))
                }
            )
        }
        AsyncAction.Uninitialized -> Unit
    }
    when (state.testSuiteState.mainState) {
        AsyncAction.Uninitialized -> {
            ListItem(headlineContent = {
                Text(
                    text = "Run the tests to detect any issue in your configuration " +
                        "that may make notifications not behave as expected."
                )
            })
            RunTestButton(state = state)
        }
        AsyncAction.Loading -> Unit
        is AsyncAction.Failure -> {
            ListItem(headlineContent = {
                Text(text = "Some tests failed, please check the details.")
            })
            RunTestButton(state = state)
        }
        AsyncAction.Confirming -> {
            ListItem(headlineContent = {
                Text(
                    text = "Some tests require your attention. Please check the details."
                )
            })
        }
        is AsyncAction.Success -> {
            ListItem(headlineContent = {
                Text(
                    text = "All tests passed successfully."
                )
            })
        }
    }
}

@Composable
private fun RunTestButton(state: TroubleshootNotificationsState) {
    ListItem(
        headlineContent = {
            Button(
                text = if (state.testSuiteState.mainState is AsyncAction.Failure) "Run tests again" else "Run tests",
                onClick = {
                    state.eventSink(TroubleshootNotificationsEvents.StartTests)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    )
}

@Composable
private fun TestSuiteView(
    testSuiteState: TroubleshootTestSuiteState,
    onQuickFixClicked: (Int) -> Unit,
) {
    testSuiteState.tests.forEachIndexed { index, testState ->
        TroubleshootTestView(
            testState = testState,
            onQuickFixClicked = {
                onQuickFixClicked(index)
            },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TroubleshootNotificationsViewPreview(
    @PreviewParameter(TroubleshootNotificationsStateProvider::class) state: TroubleshootNotificationsState,
) = ElementPreview {
    TroubleshootNotificationsView(
        state = state,
        onBackPressed = {},
    )
}
