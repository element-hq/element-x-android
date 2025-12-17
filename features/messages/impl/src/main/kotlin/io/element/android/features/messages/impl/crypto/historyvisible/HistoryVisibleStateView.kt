/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.molecules.ComposerAlertLevel
import io.element.android.libraries.designsystem.atomic.molecules.ComposerAlertMolecule
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun HistoryVisibleStateView(
    state: HistoryVisibleState,
    onLinkClick: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.showAlert) {
        return
    }

    ComposerAlertMolecule(
        modifier = modifier,
        avatar = null,
        showIcon = true,
        level = ComposerAlertLevel.Info,
        content = buildAnnotatedString {
            val learnMoreStr = stringResource(CommonStrings.action_learn_more)
            val fullText = stringResource(CommonStrings.crypto_history_visible, learnMoreStr)
            append(fullText)
            val learnMoreStartIndex = fullText.lastIndexOf(learnMoreStr)
            addStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold,
                    color = ElementTheme.colors.textPrimary
                ),
                start = learnMoreStartIndex,
                end = learnMoreStartIndex + learnMoreStr.length,
            )
            addLink(
                url = LinkAnnotation.Url(
                    url = LearnMoreConfig.HISTORY_VISIBLE_URL,
                    linkInteractionListener = {
                        onLinkClick(LearnMoreConfig.HISTORY_VISIBLE_URL, true)
                    }
                ),
                start = learnMoreStartIndex,
                end = learnMoreStartIndex + learnMoreStr.length,
            )
        },
        submitText = stringResource(CommonStrings.action_dismiss),
        onSubmitClick = { state.eventSink(HistoryVisibleEvent.Acknowledge) },
    )
}

@PreviewsDayNight
@Composable
internal fun HistoryVisibleStateViewPreview(
    @PreviewParameter(HistoryVisibleStateProvider::class) state: HistoryVisibleState,
) = ElementPreview {
    HistoryVisibleStateView(
        state = state,
        onLinkClick = { _, _ -> },
    )
}
