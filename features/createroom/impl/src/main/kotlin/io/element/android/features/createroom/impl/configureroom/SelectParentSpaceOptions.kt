/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.createroom.impl.R
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectParentSpaceOptions(
    spaces: ImmutableList<SpaceRoom>,
    selectedSpace: SpaceRoom?,
    onSelectSpace: (SpaceRoom?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var displaySelectSpaceBottomSheet by remember { mutableStateOf(false) }
    ConfigureRoomOptions(
        title = stringResource(CommonStrings.common_space),
        modifier = modifier
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = selectedSpace?.displayName
                        ?: stringResource(R.string.screen_create_room_space_selection_no_space_title),
                    maxLines = 1
                )
            },
            supportingContent = {
                Text(
                    text = if (selectedSpace != null) {
                        selectedSpace.canonicalAlias?.value.orEmpty()
                    } else {
                        stringResource(R.string.screen_create_room_space_selection_no_space_description)
                    },
                    maxLines = 1
                )
            },
            leadingContent = if (selectedSpace == null) {
                ListItemContent.Icon(IconSource.Vector(CompoundIcons.Home()))
            } else {
                ListItemContent.Custom({
                    val avatarData = AvatarData(
                        id = selectedSpace.roomId.value,
                        name = selectedSpace.displayName,
                        url = selectedSpace.avatarUrl,
                        size = AvatarSize.SelectParentSpace,
                    )
                    Avatar(avatarData = avatarData, avatarType = AvatarType.Space())
                })
            },
            onClick = { displaySelectSpaceBottomSheet = true }
        )

        if (displaySelectSpaceBottomSheet) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { true },
            )
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    sheetState.hide(coroutineScope) {
                        displaySelectSpaceBottomSheet = false
                    }
                }
            ) {
                SelectParentSpaceBottomSheet(
                    spaces = spaces,
                    selectedSpace = selectedSpace,
                ) {
                    sheetState.hide(coroutineScope) {
                        displaySelectSpaceBottomSheet = false
                    }
                    onSelectSpace(it)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.SelectParentSpaceBottomSheet(
    spaces: ImmutableList<SpaceRoom>,
    selectedSpace: SpaceRoom?,
    onSelectSpace: (SpaceRoom?) -> Unit,
) {
    ListSectionHeader(
        title = stringResource(R.string.screen_create_room_space_selection_sheet_title),
        hasDivider = false
    )
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.screen_create_room_space_selection_no_space_title),
                        maxLines = 1
                    )
                },
                supportingContent = {
                    Text(
                        stringResource(R.string.screen_create_room_space_selection_no_space_description),
                        maxLines = 1
                    )
                },
                leadingContent = ListItemContent.Icon(
                    IconSource.Vector(CompoundIcons.Home())
                ),
                trailingContent = ListItemContent.RadioButton(
                    selected = selectedSpace == null
                ),
                onClick = { onSelectSpace(null) },
            )
        }
        for (space in spaces) {
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            space.displayName,
                            maxLines = 1
                        )
                    },
                    supportingContent = {
                        Text(
                            space.canonicalAlias?.value.orEmpty(),
                            maxLines = 1
                        )
                    },
                    leadingContent = ListItemContent.Custom({
                            val avatarData =
                                AvatarData(
                                    id = space.roomId.value,
                                    name = space.displayName,
                                    url = space.avatarUrl,
                                    size = AvatarSize.SelectParentSpace,
                                )
                            Avatar(
                                avatarData = avatarData,
                                avatarType = AvatarType.Space()
                            )
                        }),
                    trailingContent = ListItemContent.RadioButton(
                        selected = selectedSpace == space
                    ),
                    onClick = { onSelectSpace(space) },
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SelectParentSpaceBottomSheetPreview() =
    ElementPreview {
        Column {
            SelectParentSpaceBottomSheet(
                spaces = persistentListOf(
                    aSpaceRoom(
                        canonicalAlias = RoomAlias(
                            "#a-room-alias:example.org"
                        )
                    )
                ),
                selectedSpace = null,
            ) {}
        }
    }
