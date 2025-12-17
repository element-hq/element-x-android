/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.ftue.impl.R
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.background.OnboardingBackground
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun NotificationsOptInView(
    state: NotificationsOptInState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    HeaderFooterPage(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize(),
        background = { OnboardingBackground() },
        header = { NotificationsOptInHeader(modifier = Modifier.padding(top = 60.dp, bottom = 28.dp)) },
        footer = { NotificationsOptInFooter(state) },
    ) {
        NotificationsOptInContent()
    }
}

@Composable
private fun NotificationsOptInHeader(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier,
        title = stringResource(R.string.screen_notification_optin_title),
        subTitle = stringResource(R.string.screen_notification_optin_subtitle),
        iconStyle = BigIcon.Style.Default(CompoundIcons.NotificationsSolid()),
    )
}

@Composable
private fun NotificationsOptInFooter(state: NotificationsOptInState) {
    ButtonColumnMolecule {
        Button(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(CommonStrings.action_ok),
            onClick = {
                state.eventSink(NotificationsOptInEvents.ContinueClicked)
            }
        )
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(CommonStrings.action_not_now),
            onClick = {
                state.eventSink(NotificationsOptInEvents.NotNowClicked)
            }
        )
    }
}

@Composable
private fun NotificationsOptInContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            NotificationRow(
                avatarLetter = "M",
                avatarColorsId = "5",
                firstRowPercent = 1f,
                secondRowPercent = 0.4f
            )

            NotificationRow(
                avatarLetter = "A",
                avatarColorsId = "1",
                firstRowPercent = 1f,
                secondRowPercent = 1f
            )

            NotificationRow(
                avatarLetter = "T",
                avatarColorsId = "4",
                firstRowPercent = 0.65f,
                secondRowPercent = 0f
            )
        }
    }
}

@Composable
private fun NotificationRow(
    avatarLetter: String,
    avatarColorsId: String,
    firstRowPercent: Float,
    secondRowPercent: Float,
) {
    Surface(
        color = ElementTheme.colors.bgCanvasDisabled,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                avatarData = AvatarData(id = avatarColorsId, name = avatarLetter, size = AvatarSize.NotificationsOptIn),
                avatarType = AvatarType.User,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .fillMaxWidth(firstRowPercent)
                        .height(10.dp)
                        .background(ElementTheme.colors.borderInteractiveSecondary)
                )
                if (secondRowPercent > 0f) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxWidth(secondRowPercent)
                            .height(10.dp)
                            .background(ElementTheme.colors.borderInteractiveSecondary)
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun NotificationsOptInViewPreview(
    @PreviewParameter(NotificationsOptInStateProvider::class) state: NotificationsOptInState
) {
    ElementPreview {
        NotificationsOptInView(
            onBack = {},
            state = state,
        )
    }
}
