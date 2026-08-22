/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.appupdate.api.AppUpdateStep
import io.element.android.features.privatepush.impl.R
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent

/**
 * The 5 pages of the private-notifications setup. Each page is a [FlowStepPage]; the
 * presenter decides which one is shown.
 */
@Composable
fun PrivatePushView(
    state: PrivatePushState,
    modifier: Modifier = Modifier,
) {
    // Coming back from a store, the package installer or ntfy: re-check what is installed.
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            state.eventSink(PrivatePushEvents.Refresh)
        }
    }
    AnimatedContent(
        targetState = state.page,
        modifier = modifier,
        label = "PrivatePushPage",
    ) { page ->
        when (page) {
            PrivatePushPage.Why -> WhyPage(state)
            PrivatePushPage.Install -> InstallPage(state)
            PrivatePushPage.Configure -> ConfigurePage(state)
            PrivatePushPage.Connect -> ConnectPage(state)
            PrivatePushPage.Done -> DonePage(state)
        }
    }
}

@Composable
private fun WhyPage(state: PrivatePushState) {
    FlowStepPage(
        iconStyle = BigIcon.Style.Default(CompoundIcons.NotificationsSolid()),
        title = stringResource(R.string.feral_privatepush_why_title),
        subTitle = stringResource(R.string.feral_privatepush_why_subtitle),
        isScrollable = true,
        onBackClick = { state.eventSink(PrivatePushEvents.Back) },
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.feral_privatepush_action_continue),
                onClick = { state.eventSink(PrivatePushEvents.Continue) },
            )
            LaterButton(state)
            if (state.canStopAsking) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.feral_privatepush_action_dont_ask_again),
                    onClick = { state.eventSink(PrivatePushEvents.DontAskAgain) },
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BulletRow(stringResource(R.string.feral_privatepush_why_point_1))
            BulletRow(stringResource(R.string.feral_privatepush_why_point_2))
            BulletRow(stringResource(R.string.feral_privatepush_why_point_3))
            Text(
                text = stringResource(R.string.feral_privatepush_why_privacy),
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun InstallPage(state: PrivatePushState) {
    val downloading = state.download is AppUpdateStep.Downloading
    FlowStepPage(
        iconStyle = BigIcon.Style.Default(CompoundIcons.Download()),
        title = stringResource(R.string.feral_privatepush_install_title),
        subTitle = stringResource(R.string.feral_privatepush_install_subtitle),
        isScrollable = true,
        onBackClick = { state.eventSink(PrivatePushEvents.Back) },
        buttons = {
            if (state.ntfyInstalled) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.feral_privatepush_action_next),
                    onClick = { state.eventSink(PrivatePushEvents.Continue) },
                )
            } else {
                if (state.playStoreAvailable) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.feral_privatepush_install_play),
                        enabled = !downloading,
                        onClick = { state.eventSink(PrivatePushEvents.InstallFromPlayStore) },
                    )
                }
                if (state.fdroidAvailable) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.feral_privatepush_install_fdroid),
                        enabled = !downloading,
                        onClick = { state.eventSink(PrivatePushEvents.InstallFromFdroid) },
                    )
                }
                when (state.download) {
                    is AppUpdateStep.ReadyToInstall -> Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.feral_privatepush_install_action_install),
                        onClick = { state.eventSink(PrivatePushEvents.InstallDownloaded) },
                    )
                    AppUpdateStep.Failed -> Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.feral_privatepush_install_action_retry),
                        onClick = { state.eventSink(PrivatePushEvents.DownloadFromFeral) },
                    )
                    AppUpdateStep.Idle,
                    is AppUpdateStep.Downloading -> Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.feral_privatepush_install_feral),
                        enabled = !downloading,
                        showProgress = downloading,
                        onClick = { state.eventSink(PrivatePushEvents.DownloadFromFeral) },
                    )
                }
            }
            LaterButton(state)
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.ntfyInstalled) {
                BulletRow(stringResource(R.string.feral_privatepush_install_already))
            } else {
                when (val download = state.download) {
                    AppUpdateStep.Idle -> SecondaryText(stringResource(R.string.feral_privatepush_install_waiting))
                    is AppUpdateStep.Downloading -> {
                        val percent = download.percent
                        if (percent != null) {
                            LinearProgressIndicator(
                                progress = { percent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            SecondaryText(stringResource(R.string.feral_privatepush_install_downloading_percent, percent))
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            SecondaryText(stringResource(R.string.feral_privatepush_install_downloading))
                        }
                    }
                    is AppUpdateStep.ReadyToInstall -> SecondaryText(stringResource(R.string.feral_privatepush_install_ready))
                    AppUpdateStep.Failed -> Text(
                        text = stringResource(R.string.feral_privatepush_install_failed),
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textCriticalPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigurePage(state: PrivatePushState) {
    FlowStepPage(
        iconStyle = BigIcon.Style.Default(CompoundIcons.Settings()),
        title = stringResource(R.string.feral_privatepush_configure_title),
        subTitle = stringResource(R.string.feral_privatepush_configure_subtitle),
        isScrollable = true,
        onBackClick = { state.eventSink(PrivatePushEvents.Back) },
        buttons = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.feral_privatepush_configure_open_ntfy),
                onClick = { state.eventSink(PrivatePushEvents.OpenNtfy) },
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.feral_privatepush_action_next),
                onClick = { state.eventSink(PrivatePushEvents.Continue) },
            )
            LaterButton(state)
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val wrongServerHost = state.wrongServerHost
            if (wrongServerHost != null) {
                Announcement(
                    title = stringResource(R.string.feral_privatepush_configure_hint_wrong_server, wrongServerHost),
                    description = null,
                    type = AnnouncementType.Informative(isCritical = true),
                )
            }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.serverAddress,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = stringResource(R.string.feral_privatepush_configure_address_label),
                supportingText = if (state.addressCopied) stringResource(R.string.feral_privatepush_configure_copied) else null,
                trailingIcon = {
                    IconButton(onClick = { state.eventSink(PrivatePushEvents.CopyAddress) }) {
                        Icon(
                            imageVector = CompoundIcons.Copy(),
                            contentDescription = stringResource(R.string.feral_privatepush_configure_copy),
                        )
                    }
                },
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.feral_privatepush_configure_copy),
                leadingIcon = IconSource.Vector(CompoundIcons.Copy()),
                onClick = { state.eventSink(PrivatePushEvents.CopyAddress) },
            )
            NumberedStep(1, stringResource(R.string.feral_privatepush_configure_step_1))
            NumberedStep(2, stringResource(R.string.feral_privatepush_configure_step_2))
            NumberedStep(3, stringResource(R.string.feral_privatepush_configure_step_3))
            NumberedStep(4, stringResource(R.string.feral_privatepush_configure_step_4))
            NumberedStep(5, stringResource(R.string.feral_privatepush_configure_step_5))
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryText(stringResource(R.string.feral_privatepush_configure_note_no_subscribe))
            SecondaryText(stringResource(R.string.feral_privatepush_configure_note_login))
            SecondaryText(stringResource(R.string.feral_privatepush_configure_note_other_uses))
        }
    }
}

@Composable
private fun ConnectPage(state: PrivatePushState) {
    val connecting = state.connect is ConnectState.Connecting
    FlowStepPage(
        iconStyle = if (state.connect is ConnectState.Connected) BigIcon.Style.Success else BigIcon.Style.Default(CompoundIcons.Link()),
        title = stringResource(R.string.feral_privatepush_connect_title),
        subTitle = stringResource(R.string.feral_privatepush_connect_subtitle),
        isScrollable = true,
        onBackClick = if (connecting) null else ({ state.eventSink(PrivatePushEvents.Back) }),
        buttons = {
            // One primary action: while a problem is shown, its Announcement carries the fix
            // (Install ntfy / Check the ntfy settings / Try again) and this button steps aside.
            if (state.connect !is ConnectState.Problem) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.feral_privatepush_connect_action),
                    enabled = !connecting,
                    showProgress = connecting,
                    onClick = { state.eventSink(PrivatePushEvents.Activate) },
                )
            }
            LaterButton(state)
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val connect = state.connect) {
                ConnectState.Idle -> Unit
                ConnectState.Connecting -> {
                    CircularProgressIndicator()
                    SecondaryText(stringResource(R.string.feral_privatepush_connect_in_progress))
                }
                ConnectState.Connected -> BulletRow(stringResource(R.string.feral_privatepush_connect_success))
                is ConnectState.Problem -> ConnectProblemContent(connect.problem, state)
            }
        }
    }
}

@Composable
private fun ConnectProblemContent(problem: ConnectProblem, state: PrivatePushState) {
    when (problem) {
        ConnectProblem.NtfyNotInstalled -> {
            Announcement(
                title = stringResource(R.string.feral_privatepush_connect_problem_ntfy_missing),
                description = null,
                type = AnnouncementType.Actionable(
                    actionText = stringResource(R.string.feral_privatepush_connect_problem_ntfy_missing_action),
                    onActionClick = { state.eventSink(PrivatePushEvents.GoToInstall) },
                    onDismissClick = null,
                ),
            )
        }
        is ConnectProblem.WrongServer -> {
            Announcement(
                title = stringResource(R.string.feral_privatepush_connect_problem_wrong_server, problem.host),
                description = null,
                type = AnnouncementType.Actionable(
                    actionText = stringResource(R.string.feral_privatepush_connect_problem_wrong_server_action),
                    onActionClick = { state.eventSink(PrivatePushEvents.GoToConfigure) },
                    onDismissClick = null,
                ),
            )
        }
        is ConnectProblem.RegistrationFailed -> {
            Announcement(
                title = stringResource(R.string.feral_privatepush_connect_problem_registration),
                description = problem.reason?.let { stringResource(R.string.feral_privatepush_connect_problem_registration_reason, it) },
                type = AnnouncementType.Actionable(
                    actionText = stringResource(R.string.feral_privatepush_connect_action_retry),
                    onActionClick = { state.eventSink(PrivatePushEvents.Activate) },
                    onDismissClick = null,
                ),
            )
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.feral_privatepush_connect_action_troubleshoot),
                onClick = { state.eventSink(PrivatePushEvents.OpenTroubleshoot) },
            )
        }
    }
}

@Composable
private fun DonePage(state: PrivatePushState) {
    FlowStepPage(
        iconStyle = BigIcon.Style.SuccessSolid,
        title = stringResource(R.string.feral_privatepush_done_title),
        subTitle = stringResource(R.string.feral_privatepush_done_subtitle),
        isScrollable = true,
        onBackClick = null,
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.feral_privatepush_action_finish),
                onClick = { state.eventSink(PrivatePushEvents.Finish) },
            )
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SecondaryText(stringResource(R.string.feral_privatepush_done_tip))
        }
    }
}

@Composable
private fun LaterButton(state: PrivatePushState) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.feral_privatepush_action_later),
        onClick = { state.eventSink(PrivatePushEvents.Later) },
    )
}

@Composable
private fun SecondaryText(text: String) {
    Text(
        text = text,
        style = ElementTheme.typography.fontBodyMdRegular,
        color = ElementTheme.colors.textSecondary,
    )
}

@Composable
private fun BulletRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = CompoundIcons.CheckCircleSolid(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSuccessPrimary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun NumberedStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$number.",
            style = ElementTheme.typography.fontBodyLgMedium,
            color = ElementTheme.colors.textPrimary,
            modifier = Modifier.width(28.dp),
        )
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PrivatePushViewPreview(
    @PreviewParameter(PrivatePushStateProvider::class) state: PrivatePushState,
) = ElementPreview {
    PrivatePushView(state = state)
}
