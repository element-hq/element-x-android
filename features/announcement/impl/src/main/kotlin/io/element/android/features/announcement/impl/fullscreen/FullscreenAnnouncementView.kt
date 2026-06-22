/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.fullscreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.AnnouncementEvent
import io.element.android.features.announcement.impl.AnnouncementState
import io.element.android.features.announcement.impl.AnnouncementStateProvider
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Ref: https://www.figma.com/design/kcnHxunG1LDWXsJhaNuiHz/ER-145--Spaces-on-Element-X?node-id=4593-40181
 */
@Composable
fun FullscreenAnnouncementView(
    state: AnnouncementState,
    modifier: Modifier = Modifier,
) {
    // Ensure that the content stays visible during the exit animation
    var fullscreenAnnouncement by remember { mutableStateOf<Announcement.Fullscreen?>(null) }
    if (state.announcement != null) {
        fullscreenAnnouncement = state.announcement
    }
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.announcement != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            fullscreenAnnouncement?.let {
                FullscreenAnnouncementView(
                    announcement = it,
                    eventSink = state.eventSink,
                )
            }
        }
    }
}

@Composable
private fun FullscreenAnnouncementView(
    announcement: Announcement.Fullscreen,
    eventSink: (AnnouncementEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    fun onContinue() {
        eventSink(AnnouncementEvent.Continue(announcement))
    }

    BackHandler(onBack = ::onContinue)
    HeaderFooterPage(
        modifier = modifier,
        isScrollable = true,
        contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
        header = {
            FullscreenAnnouncementHeader(announcement)
        },
        content = {
            FullscreenAnnouncementContent(
                modifier = Modifier.padding(horizontal = 8.dp),
                announcement = announcement,
            )
        },
        footer = {
            FullscreenAnnouncementFooter(
                onContinue = ::onContinue,
            )
        }
    )
}

@Composable
private fun FullscreenAnnouncementHeader(
    announcement: Announcement.Fullscreen,
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(top = 16.dp, bottom = 16.dp),
        title = announcement.title(),
        showBetaLabel = true,
        subTitle = announcement.subtitle(),
        iconStyle = BigIcon.Style.Default(
            vectorIcon = announcement.icon(),
            usePrimaryTint = true,
        ),
    )
}

@Composable
private fun FullscreenAnnouncementContent(
    announcement: Announcement.Fullscreen,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        InfoListOrganism(
            modifier = Modifier.fillMaxWidth(),
            items = announcement.items(),
            textStyle = ElementTheme.typography.fontBodyLgMedium,
            iconTint = ElementTheme.colors.iconSecondary,
            iconSize = 24.dp
        )
        announcement.notice()?.let { notice ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                text = notice,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FullscreenAnnouncementFooter(
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

@Composable
private fun Announcement.Fullscreen.title() = when (this) {
    Announcement.Fullscreen.Space -> "Introducing Spaces"
}

@Composable
private fun Announcement.Fullscreen.subtitle() = when (this) {
    Announcement.Fullscreen.Space -> "Welcome to the beta version of Spaces! With this first version you can:"
}

@Composable
private fun Announcement.Fullscreen.icon() = when (this) {
    Announcement.Fullscreen.Space -> CompoundIcons.SpaceSolid()
}

@Composable
private fun Announcement.Fullscreen.items(): ImmutableList<InfoListItem> = when (this) {
    Announcement.Fullscreen.Space -> persistentListOf(
        InfoListItem(
            message = "View spaces you\'ve created or joined",
            iconVector = CompoundIcons.VisibilityOn(),
        ),
        InfoListItem(
            message = "Accept or decline invites to spaces",
            iconVector = CompoundIcons.Email(),
        ),
        InfoListItem(
            message = "Discover any rooms you can join in your spaces",
            iconVector = CompoundIcons.Search(),
        ),
        InfoListItem(
            message = "Join public spaces",
            iconVector = CompoundIcons.Explore(),
        ),
        InfoListItem(
            message = "Leave any spaces you’ve joined",
            iconVector = CompoundIcons.Leave(),
        ),
    )
}

@Composable
private fun Announcement.Fullscreen.notice(): String? = when (this) {
    Announcement.Fullscreen.Space -> "Filtering, creating and managing spaces is coming soon."
}

@PreviewsDayNight
@Composable
internal fun FullscreenAnnouncementViewPreview(@PreviewParameter(AnnouncementStateProvider::class) state: AnnouncementState) = ElementPreview {
    FullscreenAnnouncementView(
        state = state,
    )
}
