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

package io.element.android.features.poll.api

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconToggleButton
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonPlurals

@Composable
fun PollAnswerView(
    answerItem: PollAnswerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .selectable(
                selected = answerItem.isSelected,
                enabled = answerItem.isEnabled,
                onClick = onClick,
                role = Role.RadioButton,
            )
    ) {
        IconToggleButton(
            modifier = Modifier.size(22.dp),
            checked = answerItem.isSelected,
            enabled = answerItem.isEnabled,
            colors = IconButtonDefaults.iconToggleButtonColors(
                contentColor = ElementTheme.colors.iconSecondary,
                checkedContentColor = ElementTheme.colors.iconPrimary,
                disabledContentColor = ElementTheme.colors.iconDisabled,
            ),
            onCheckedChange = { onClick() },
        ) {
            Icon(
                imageVector = if (answerItem.isSelected) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = answerItem.answer.text,
                    style = if (answerItem.isWinner) ElementTheme.typography.fontBodyLgMedium else ElementTheme.typography.fontBodyLgRegular,
                )
                if (answerItem.isDisclosed) {
                    Text(
                        modifier = Modifier.align(Alignment.Bottom),
                        text = pluralStringResource(
                            id = CommonPlurals.common_poll_votes_count,
                            count = answerItem.votesCount,
                            answerItem.votesCount
                        ),
                        style = if (answerItem.isWinner) ElementTheme.typography.fontBodySmMedium else ElementTheme.typography.fontBodySmRegular,
                        color = if (answerItem.isWinner) ElementTheme.colors.textPrimary else ElementTheme.colors.textSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = if (answerItem.isWinner) ElementTheme.colors.textSuccessPrimary else answerItem.isEnabled.toEnabledColor(),
                progress = when {
                    answerItem.isDisclosed -> answerItem.percentage
                    answerItem.isSelected -> 1f
                    else -> 0f
                },
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Preview
@Composable
internal fun PollAnswerDisclosedNotSelectedPreview() = ElementThemedPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(isDisclosed = true, isSelected = false),
        onClick = { },
    )
}

@Preview
@Composable
internal fun PollAnswerDisclosedSelectedPreview() = ElementThemedPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(isDisclosed = true, isSelected = true),
        onClick = { }
    )
}

@Preview
@Composable
internal fun PollAnswerUndisclosedNotSelectedPreview() = ElementThemedPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(isDisclosed = false, isSelected = false),
        onClick = { },
    )
}

@Preview
@Composable
internal fun PollAnswerUndisclosedSelectedPreview() = ElementThemedPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(isDisclosed = false, isSelected = true),
        onClick = { }
    )
}

@Preview
@Composable
internal fun PollAnswerEndedWinnerNotSelectedPreview() = ElementThemedPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(isDisclosed = true, isSelected = false, isEnabled = false, isWinner = true),
        onClick = { }
    )
}

@Preview
@Composable
internal fun PollAnswerEndedWinnerSelectedPreview() = ElementThemedPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(isDisclosed = true, isSelected = true, isEnabled = false, isWinner = true),
        onClick = { }
    )
}

@Preview
@Composable
internal fun PollAnswerEndedSelectedPreview() = ElementThemedPreview {
    PollAnswerView(
        answerItem = aPollAnswerItem(isDisclosed = true, isSelected = true, isEnabled = false, isWinner = false),
        onClick = { }
    )
}
