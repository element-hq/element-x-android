/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.api.pollcontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.poll.api.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.progressIndicatorTrackColor
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun PollAnswerView(
    answerItem: PollAnswerItem,
    modifier: Modifier = Modifier,
) {
    val nbVotesText = pluralStringResource(
        id = CommonPlurals.common_poll_votes_count,
        count = answerItem.votesCount,
        answerItem.votesCount,
    )
    val a11yText = buildString {
        val sentenceDelimiter = stringResource(CommonStrings.common_sentence_delimiter)
        append(answerItem.answer.text.removeSuffix("."))
        if (answerItem.showVotes) {
            append(sentenceDelimiter)
            append(nbVotesText)
            if (answerItem.votesCount != 0) {
                append(sentenceDelimiter)
                (answerItem.percentage * 100).toInt().let { percent ->
                    append(pluralStringResource(R.plurals.a11y_polls_percent_of_total, percent, percent))
                }
            }
            if (answerItem.isWinner) {
                append(sentenceDelimiter)
                append(stringResource(R.string.a11y_polls_winning_answer))
            }
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = a11yText
            },
    ) {
        Icon(
            imageVector = if (answerItem.isSelected) {
                CompoundIcons.CheckCircleSolid()
            } else {
                CompoundIcons.Circle()
            },
            contentDescription = null,
            modifier = Modifier
                .padding(0.5.dp)
                .size(22.dp),
            tint = if (answerItem.isEnabled) {
                if (answerItem.isSelected) {
                    ElementTheme.colors.iconPrimary
                } else {
                    ElementTheme.colors.iconSecondary
                }
            } else {
                ElementTheme.colors.iconDisabled
            },
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = answerItem.answer.text,
                    style = if (answerItem.isWinner) ElementTheme.typography.fontBodyLgMedium else ElementTheme.typography.fontBodyLgRegular,
                )
                if (answerItem.showVotes) {
                    Row(
                        modifier = Modifier.align(Alignment.Bottom),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (answerItem.isWinner) {
                            Icon(
                                resourceId = CommonDrawables.ic_winner,
                                contentDescription = null,
                                tint = ElementTheme.colors.iconAccentTertiary,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = nbVotesText,
                                style = ElementTheme.typography.fontBodySmMedium,
                                color = ElementTheme.colors.textPrimary,
                            )
                        } else {
                            Text(
                                text = nbVotesText,
                                style = ElementTheme.typography.fontBodySmRegular,
                                color = ElementTheme.colors.textSecondary,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = if (answerItem.isWinner) ElementTheme.colors.textSuccessPrimary else answerItem.isEnabled.toEnabledColor(),
                progress = {
                    when {
                        answerItem.showVotes -> answerItem.percentage
                        answerItem.isSelected -> 1f
                        else -> 0f
                    }
                },
                trackColor = ElementTheme.colors.progressIndicatorTrackColor,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PollAnswerViewDisclosedNotSelectedPreview() = ElementPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(showVotes = true, isSelected = false),
    )
}

@PreviewsDayNight
@Composable
internal fun PollAnswerViewDisclosedSelectedPreview() = ElementPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(showVotes = true, isSelected = true),
    )
}

@PreviewsDayNight
@Composable
internal fun PollAnswerViewUndisclosedNotSelectedPreview() = ElementPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(showVotes = false, isSelected = false),
    )
}

@PreviewsDayNight
@Composable
internal fun PollAnswerViewUndisclosedSelectedPreview() = ElementPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(showVotes = false, isSelected = true),
    )
}

@PreviewsDayNight
@Composable
internal fun PollAnswerViewEndedWinnerNotSelectedPreview() = ElementPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(showVotes = true, isSelected = false, isEnabled = false, isWinner = true),
    )
}

@PreviewsDayNight
@Composable
internal fun PollAnswerViewEndedWinnerSelectedPreview() = ElementPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(showVotes = true, isSelected = true, isEnabled = false, isWinner = true),
    )
}

@PreviewsDayNight
@Composable
internal fun PollAnswerViewEndedSelectedPreview() = ElementPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(showVotes = true, isSelected = true, isEnabled = false, isWinner = false),
    )
}
