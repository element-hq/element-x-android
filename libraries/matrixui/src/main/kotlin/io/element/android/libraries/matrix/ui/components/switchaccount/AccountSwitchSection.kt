/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components.switchaccount

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.CounterAtom
import io.element.android.libraries.designsystem.components.avatar.AvatarRow
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Display the other accounts, which can be expanded to switch to one of them.
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=5414-4759
 *
 * @param otherAccounts the accounts which are not the current one.
 * @param isExpanded true to display the list of the other accounts.
 * @param canAddAccount true to display an item to add an account. If false, the list of accounts is read only.
 * @param onToggleExpand invoked when the user wants to expand or collapse the list of accounts.
 * @param onSelectAccount invoked when the user selects one of the other accounts.
 * @param onAddAccountClick invoked when the user clicks on the item to add an account. Only used if [canAddAccount] is true.
 * @param modifier the modifier to apply to this layout.
 */
@Composable
fun AccountSwitchSection(
    otherAccounts: ImmutableList<OtherAccount>,
    isExpanded: Boolean,
    canAddAccount: Boolean,
    onToggleExpand: () -> Unit,
    onSelectAccount: (SessionId) -> Unit,
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (otherAccounts.isEmpty()) {
            if (canAddAccount) {
                AddAccountItem(onAddAccountClick)
            }
        } else {
            val expandedStateDescription = if (isExpanded) {
                stringResource(CommonStrings.a11y_state_expanded)
            } else {
                stringResource(CommonStrings.a11y_state_collapsed)
            }
            ListItem(
                modifier = Modifier.semantics {
                    stateDescription = expandedStateDescription
                },
                content = { Text(stringResource(CommonStrings.common_switch_account)) },
                onClick = onToggleExpand,
                trailingContent = ListItemContent.Custom { _ ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnimatedVisibility(
                            visible = !isExpanded,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            AvatarRow(
                                avatarDataList = otherAccounts
                                    .take(3)
                                    .map { it.user.getAvatarData(AvatarSize.OtherAccountItem) }
                                    .toImmutableList(),
                                avatarType = AvatarType.User,
                                lastOnTop = true,
                            )
                        }
                        // Animate the chevron icon to rotate when the section is expanded/collapsed
                        val rotation: Float by animateFloatAsState(
                            targetValue = if (isExpanded) -180f else 0f,
                            animationSpec = tween(
                                delayMillis = 0,
                                durationMillis = 300,
                            ),
                            label = "chevron"
                        )
                        Icon(
                            modifier = Modifier.rotate(rotation),
                            imageVector = CompoundIcons.ChevronDown(),
                            contentDescription = null,
                        )
                    }
                },
            )
            AnimatedVisibility(
                visible = isExpanded,
            ) {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = ElementTheme.colors.bgSubtleSecondary,
                    )
                    otherAccounts.forEach { otherAccount ->
                        MatrixUserRow(
                            modifier = Modifier
                                .clickable {
                                    onSelectAccount(otherAccount.user.userId)
                                }
                                .padding(top = 2.dp, bottom = 2.dp, end = 8.dp),
                            matrixUser = otherAccount.user,
                            avatarSize = AvatarSize.AccountItem,
                            verticalSpaceWidth = 16.dp,
                            trailingContent = if (otherAccount.unreadCount > 0) {
                                { CounterAtom(count = otherAccount.unreadCount) }
                            } else {
                                null
                            },
                        )
                    }
                    if (canAddAccount) {
                        AddAccountItem(onAddAccountClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAccountItem(onAddAccountClick: () -> Unit) {
    ListItem(
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Plus())),
        content = {
            Text(stringResource(CommonStrings.common_add_another_account))
        },
        onClick = onAddAccountClick,
    )
}

@PreviewsDayNight
@Composable
internal fun AccountSwitchSectionPreview(@PreviewParameter(AccountSwitchSectionStatePreviewParam::class) state: AccountSwitchSectionState) =
    ElementPreview {
        AccountSwitchSection(
            otherAccounts = state.otherAccounts,
            isExpanded = state.isExpanded,
            canAddAccount = state.canAddAccount,
            onToggleExpand = {},
            onSelectAccount = {},
            onAddAccountClick = {},
        )
    }
