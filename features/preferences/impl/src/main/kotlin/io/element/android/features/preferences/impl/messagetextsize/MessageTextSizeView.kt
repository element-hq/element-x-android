/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.messagetextsize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.api.timeline.TimelineSampleRenderer
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Slider
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.preferences.api.store.MessageTextSize
import kotlin.math.roundToInt

@Composable
fun MessageTextSizeView(
    state: MessageTextSizeState,
    timelineSampleRenderer: TimelineSampleRenderer,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onClick = onBackClick) },
                title = {
                    Text(
                        modifier = Modifier.semantics { heading() },
                        text = stringResource(R.string.screen_message_text_size_title),
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
            )
        },
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                timelineSampleRenderer.Render(
                    textSizeSp = state.textSizeSp,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            ListSectionHeader(
                title = stringResource(R.string.screen_message_text_size_adjust_size),
                hasDivider = true,
                description = {
                    Text(
                        text = stringResource(R.string.screen_message_text_size_default_description, MessageTextSize.DEFAULT),
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                },
            )
            MessageTextSizeSlider(
                textSizeSp = state.textSizeSp,
                onTextSizeChange = { state.eventSink(MessageTextSizeEvent.SetTextSize(it)) },
                onTextSizeCommit = { state.eventSink(MessageTextSizeEvent.CommitTextSize) },
            )
        }
    }
}

@Composable
private fun MessageTextSizeSlider(
    textSizeSp: Int,
    onTextSizeChange: (Int) -> Unit,
    onTextSizeCommit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Slider(
            modifier = Modifier.weight(1f),
            value = textSizeSp.toFloat(),
            onValueChange = { onTextSizeChange(it.roundToInt()) },
            onValueChangeFinish = onTextSizeCommit,
            valueRange = MessageTextSize.MIN.toFloat()..MessageTextSize.MAX.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = ElementTheme.colors.iconAccentPrimary,
                activeTrackColor = ElementTheme.colors.iconAccentPrimary,
                inactiveTrackColor = ElementTheme.colors.bgBadgeAccent,
                inactiveTickColor = ElementTheme.colors.iconAccentPrimary,
            )
        )
        Text(
            text = textSizeSp.toString(),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MessageTextSizeViewPreview(
    @PreviewParameter(MessageTextSizeStatePreviewParam::class) state: MessageTextSizeState
) = ElementPreview {
    MessageTextSizeView(
        state = state,
        timelineSampleRenderer = NoOpTimelineSampleRenderer,
        onBackClick = {},
    )
}

/** The sample is rendered by the messages feature, which previews here cannot reach. */
private object NoOpTimelineSampleRenderer : TimelineSampleRenderer {
    @Composable
    override fun Render(textSizeSp: Int, modifier: Modifier) = Unit
}
