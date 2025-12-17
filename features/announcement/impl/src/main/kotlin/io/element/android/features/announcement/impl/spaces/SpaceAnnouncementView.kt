/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.announcement.impl.R
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

/**
 * Ref: https://www.figma.com/design/kcnHxunG1LDWXsJhaNuiHz/ER-145--Spaces-on-Element-X?node-id=4593-40181
 */
@Composable
fun SpaceAnnouncementView(
    state: SpaceAnnouncementState,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    fun onContinue() {
        eventSink(SpaceAnnouncementEvents.Continue)
    }

    BackHandler(onBack = ::onContinue)
    HeaderFooterPage(
        modifier = modifier,
        isScrollable = true,
        contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
        header = {
            SpaceAnnouncementHeader()
        },
        content = {
            SpaceAnnouncementContent(
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        },
        footer = {
            SpaceAnnouncementFooter(
                onContinue = ::onContinue,
            )
        }
    )
}

@Composable
private fun SpaceAnnouncementHeader(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = 16.dp, bottom = 16.dp),
        title = stringResource(id = R.string.screen_space_announcement_title),
        showBetaLabel = true,
        subTitle = stringResource(id = R.string.screen_space_announcement_subtitle),
        iconStyle = BigIcon.Style.Default(
            vectorIcon = CompoundIcons.SpaceSolid(),
            usePrimaryTint = true,
        ),
    )
}

@Composable
private fun SpaceAnnouncementContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        InfoListOrganism(
            modifier = Modifier.fillMaxWidth(),
            items = persistentListOf(
                InfoListItem(
                    message = stringResource(id = R.string.screen_space_announcement_item1),
                    iconVector = CompoundIcons.VisibilityOn(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_space_announcement_item2),
                    iconVector = CompoundIcons.Email(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_space_announcement_item3),
                    iconVector = CompoundIcons.Search(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_space_announcement_item4),
                    iconVector = CompoundIcons.Explore(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_space_announcement_item5),
                    iconVector = CompoundIcons.Leave(),
                ),
            ),
            textStyle = ElementTheme.typography.fontBodyLgMedium,
            iconTint = ElementTheme.colors.iconSecondary,
            iconSize = 24.dp
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            text = stringResource(id = R.string.screen_space_announcement_notice),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SpaceAnnouncementFooter(
    onContinue: () -> Unit,
) {
    ButtonColumnMolecule(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Button(
            text = stringResource(id = CommonStrings.action_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SpaceAnnouncementViewPreview(@PreviewParameter(SpaceAnnouncementStateProvider::class) state: SpaceAnnouncementState) = ElementPreview {
    SpaceAnnouncementView(
        state = state,
    )
}
