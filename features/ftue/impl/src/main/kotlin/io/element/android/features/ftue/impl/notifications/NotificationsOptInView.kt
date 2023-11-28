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
import androidx.compose.foundation.layout.systemBarsPadding
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
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
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
            .systemBarsPadding()
            .fillMaxSize(),
        header = { NotificationsOptInHeader(modifier = Modifier.padding(top = 60.dp, bottom = 12.dp)) },
        footer = { NotificationsOptInFooter(state) },
    ) {
        NotificationsOptInContent(modifier = Modifier.fillMaxWidth())
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
        iconImageVector = CompoundIcons.NotificationsSolid,
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
private fun NotificationsOptInContent(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
