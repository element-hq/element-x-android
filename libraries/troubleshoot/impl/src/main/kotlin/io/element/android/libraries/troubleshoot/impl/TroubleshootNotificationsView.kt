/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncAction
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
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState.Status

@Composable
fun TroubleshootNotificationsView(
    state: TroubleshootNotificationsState,
    onBackClick: () -> Unit,
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
        onBackClick = onBackClick,
        title = stringResource(id = R.string.troubleshoot_notifications_screen_title),
    ) {
        TroubleshootNotificationsContent(state)
    }
}

@Composable
private fun ColumnScope.TroubleshootTestView(
    testState: NotificationTroubleshootTestState,
    onQuickFixClick: () -> Unit,
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
                    text = stringResource(id = R.string.troubleshoot_notifications_screen_quick_fix_action),
                    onClick = onQuickFixClick
                )
            }
        )
    }
}

@Composable
private fun ColumnScope.TroubleshootNotificationsContent(state: TroubleshootNotificationsState) {
    when (state.testSuiteState.mainState) {
        AsyncAction.Loading,
        is AsyncAction.Confirming,
        is AsyncAction.Success,
        is AsyncAction.Failure -> {
            TestSuiteView(
                testSuiteState = state.testSuiteState,
                onQuickFixClick = {
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
                    text = stringResource(id = R.string.troubleshoot_notifications_screen_notice)
                )
            })
            RunTestButton(state = state)
        }
        AsyncAction.Loading -> Unit
        is AsyncAction.Failure -> {
            ListItem(headlineContent = {
                Text(text = stringResource(id = R.string.troubleshoot_notifications_screen_failure))
            })
            RunTestButton(state = state)
        }
        is AsyncAction.Confirming -> {
            ListItem(headlineContent = {
                Text(
                    text = stringResource(id = R.string.troubleshoot_notifications_screen_waiting)
                )
            })
        }
        is AsyncAction.Success -> {
            ListItem(headlineContent = {
                Text(
                    text = stringResource(id = R.string.troubleshoot_notifications_screen_success)
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
                text = stringResource(
                    id = if (state.testSuiteState.mainState is AsyncAction.Failure) {
                        R.string.troubleshoot_notifications_screen_action_again
                    } else {
                        R.string.troubleshoot_notifications_screen_action
                    }
                ),
                onClick = {
                    state.eventSink(TroubleshootNotificationsEvents.StartTests)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    )
}

@Composable
private fun ColumnScope.TestSuiteView(
    testSuiteState: TroubleshootTestSuiteState,
    onQuickFixClick: (Int) -> Unit,
) {
    testSuiteState.tests.forEachIndexed { index, testState ->
        TroubleshootTestView(
            testState = testState,
            onQuickFixClick = {
                onQuickFixClick(index)
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
        onBackClick = {},
    )
}
