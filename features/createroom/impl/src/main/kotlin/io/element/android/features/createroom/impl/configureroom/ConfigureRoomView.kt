/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.createroom.impl.R
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtomSize
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.modifiers.clearFocusOnTap
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.components.AvatarActionBottomSheet
import io.element.android.libraries.matrix.ui.components.AvatarPickerState
import io.element.android.libraries.matrix.ui.components.AvatarPickerView
import io.element.android.libraries.matrix.ui.room.address.RoomAddressField
import io.element.android.libraries.permissions.api.PermissionsView
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlin.jvm.optionals.getOrNull

@Composable
fun ConfigureRoomView(
    state: ConfigureRoomState,
    onBackClick: () -> Unit,
    onCreateRoomSuccess: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSpace = state.config.isSpace
    val focusManager = LocalFocusManager.current
    val isAvatarActionsSheetVisible = remember { mutableStateOf(false) }

    fun onAvatarClick() {
        focusManager.clearFocus()
        isAvatarActionsSheetVisible.value = true
    }

    Scaffold(
        modifier = modifier.clearFocusOnTap(focusManager),
        topBar = {
            ConfigureRoomToolbar(
                isSpace = isSpace,
                isNextActionEnabled = state.isValid,
                onBackClick = onBackClick,
                onNextClick = {
                    focusManager.clearFocus()
                    state.eventSink(ConfigureRoomEvents.CreateRoom)
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RoomNameWithAvatar(
                isSpace = isSpace,
                modifier = Modifier.padding(horizontal = 16.dp),
                avatarUri = state.config.avatarUri,
                roomName = state.config.roomName.orEmpty(),
                onAvatarClick = ::onAvatarClick,
                onChangeRoomName = { state.eventSink(ConfigureRoomEvents.RoomNameChanged(it)) },
            )
            RoomTopic(
                modifier = Modifier.padding(horizontal = 16.dp),
                topic = state.config.topic.orEmpty(),
                onTopicChange = { state.eventSink(ConfigureRoomEvents.TopicChanged(it)) },
            )

            if (!state.config.isSpace && state.spaces.isNotEmpty()) {
                SelectParentSpaceOptions(
                    spaces = state.spaces,
                    selectedSpace = state.config.parentSpace,
                    onSelectSpace = { state.eventSink(ConfigureRoomEvents.SetParentSpace(it)) },
                )
            }

            RoomJoinRuleOptions(
                options = state.availableJoinRules,
                selected = state.config.visibilityState.joinRuleItem,
                parentSpace = state.config.parentSpace,
                onOptionClick = {
                    focusManager.clearFocus()
                    state.eventSink(ConfigureRoomEvents.JoinRuleChanged(it))
                },
            )

            if (state.config.visibilityState !is RoomVisibilityState.Private) {
                Column {
                    ListSectionHeader(title = stringResource(R.string.screen_create_room_room_address_section_title))
                    RoomAddressField(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        address = state.config.visibilityState.roomAddress().getOrNull().orEmpty(),
                        homeserverName = state.homeserverName,
                        addressValidity = state.roomAddressValidity,
                        onAddressChange = { state.eventSink(ConfigureRoomEvents.RoomAddressChanged(it)) },
                        label = null,
                        supportingText = stringResource(R.string.screen_create_room_room_address_section_footer),
                    )
                }
            }
        }
    }

    AvatarActionBottomSheet(
        actions = state.avatarActions,
        isVisible = isAvatarActionsSheetVisible.value,
        onDismiss = { isAvatarActionsSheetVisible.value = false },
        onSelectAction = { state.eventSink(ConfigureRoomEvents.HandleAvatarAction(it)) }
    )

    AsyncActionView(
        async = state.createRoomAction,
        progressDialog = {
            AsyncActionViewDefaults.ProgressDialog(
                progressText = stringResource(if (isSpace) CommonStrings.common_creating_space else CommonStrings.common_creating_room),
            )
        },
        onSuccess = { onCreateRoomSuccess(it) },
        errorMessage = { stringResource(if (isSpace) R.string.screen_create_room_error_creating_space else R.string.screen_create_room_error_creating_room) },
        onRetry = { state.eventSink(ConfigureRoomEvents.CreateRoom) },
        onErrorDismiss = { state.eventSink(ConfigureRoomEvents.CancelCreateRoom) },
    )

    PermissionsView(
        state = state.cameraPermissionState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigureRoomToolbar(
    isSpace: Boolean,
    isNextActionEnabled: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    TopAppBar(
        titleStr = stringResource(if (isSpace) R.string.screen_create_room_new_space_title else R.string.screen_create_room_new_room_title),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_create),
                enabled = isNextActionEnabled,
                onClick = onNextClick,
            )
        }
    )
}

@Composable
private fun RoomNameWithAvatar(
    isSpace: Boolean,
    avatarUri: String?,
    roomName: String,
    onAvatarClick: () -> Unit,
    onChangeRoomName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.padding(end = 8.dp).size(AvatarSize.EditRoomDetails.dp),
            contentAlignment = Alignment.Center,
        ) {
            val avatarState = remember(avatarUri) {
                if (avatarUri != null) {
                    AvatarPickerState.Selected(
                        avatarData = AvatarData(id = "#", name = null, url = avatarUri, size = AvatarSize.EditRoomDetails),
                        type = if (isSpace) AvatarType.Space() else AvatarType.Room(),
                    )
                } else {
                    val containerSize = 48.dp
                    val padding = PaddingValues((AvatarSize.EditRoomDetails.dp - containerSize) / 2)
                    AvatarPickerState.Pick(buttonSize = 48.dp, iconSize = 24.dp, externalPadding = padding)
                }
            }
            AvatarPickerView(
                state = avatarState,
                onClick = onAvatarClick,
            )
        }

        TextField(
            modifier = Modifier.padding(bottom = 18.dp),
            label = stringResource(CommonStrings.common_name),
            value = roomName,
            placeholder = stringResource(R.string.screen_create_room_name_placeholder),
            singleLine = true,
            onValueChange = onChangeRoomName,
        )
    }
}

@Composable
private fun RoomTopic(
    topic: String,
    onTopicChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        modifier = modifier,
        label = stringResource(R.string.screen_create_room_topic_label),
        value = topic,
        onValueChange = onTopicChange,
        maxLines = 3,
        placeholder = stringResource(R.string.screen_create_room_topic_placeholder),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
    )
}

@Composable
internal fun ConfigureRoomOptions(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.selectableGroup()
    ) {
        ListSectionHeader(title = title)
        content()
    }
}

@Composable
private fun RoomJoinRuleOptions(
    options: ImmutableList<JoinRuleItem>,
    selected: JoinRuleItem,
    onOptionClick: (JoinRuleItem) -> Unit,
    parentSpace: SpaceRoom?,
    modifier: Modifier = Modifier,
) {
    ConfigureRoomOptions(
        title = stringResource(R.string.screen_create_room_room_access_section_title),
        modifier = modifier,
    ) {
        options.forEach { item ->
            val isSelected = item == selected
            ListItem(
                leadingContent = ListItemContent.Custom {
                    RoundedIconAtom(
                        size = RoundedIconAtomSize.Big,
                        imageVector = when (item) {
                            JoinRuleItem.PublicVisibility.Public -> CompoundIcons.Public()
                            is JoinRuleItem.PublicVisibility.Restricted -> CompoundIcons.Space()
                            JoinRuleItem.PublicVisibility.AskToJoin,
                            is JoinRuleItem.PublicVisibility.AskToJoinRestricted -> CompoundIcons.UserAdd()
                            JoinRuleItem.Private -> CompoundIcons.Lock()
                        },
                        tint = if (isSelected) ElementTheme.colors.iconPrimary else ElementTheme.colors.iconSecondary,
                        backgroundTint = Color.Transparent,
                    )
                },
                headlineContent = {
                    val title = when (item) {
                        JoinRuleItem.PublicVisibility.Public -> stringResource(R.string.screen_create_room_room_access_section_public_option_title)
                        is JoinRuleItem.PublicVisibility.Restricted -> stringResource(R.string.screen_create_room_room_access_section_restricted_option_title)
                        JoinRuleItem.PublicVisibility.AskToJoin -> stringResource(R.string.screen_create_room_room_access_section_knocking_option_title)
                        is JoinRuleItem.PublicVisibility.AskToJoinRestricted -> stringResource(
                            R.string.screen_create_room_room_access_section_knocking_restricted_option_title
                        )
                        JoinRuleItem.Private -> stringResource(R.string.screen_create_room_room_access_section_private_option_title)
                    }
                    Text(text = title)
                },
                supportingContent = {
                    val description = when (item) {
                        JoinRuleItem.PublicVisibility.Public -> stringResource(R.string.screen_create_room_room_access_section_public_option_description)
                        is JoinRuleItem.PublicVisibility.Restricted -> stringResource(
                            R.string.screen_create_room_room_access_section_restricted_option_description,
                            parentSpace?.displayName.orEmpty()
                        )
                        JoinRuleItem.PublicVisibility.AskToJoin -> stringResource(R.string.screen_create_room_room_access_section_knocking_option_description)
                        is JoinRuleItem.PublicVisibility.AskToJoinRestricted -> stringResource(
                            R.string.screen_create_room_room_access_section_knocking_restricted_option_description,
                            parentSpace?.displayName.orEmpty()
                        )
                        JoinRuleItem.Private -> stringResource(R.string.screen_create_room_room_access_section_private_option_description)
                    }
                    Text(text = description)
                },
                trailingContent = ListItemContent.RadioButton(selected = isSelected),
                onClick = { onOptionClick(item) },
            )
        }
    }
}

@PreviewWithLargeHeight
@Composable
internal fun ConfigureRoomViewLightPreview(@PreviewParameter(ConfigureRoomStateProvider::class) state: ConfigureRoomState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun ConfigureRoomViewDarkPreview(@PreviewParameter(ConfigureRoomStateProvider::class) state: ConfigureRoomState) =
    ElementPreviewDark { ContentToPreview(state) }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(state: ConfigureRoomState) {
    ConfigureRoomView(
        state = state,
        onBackClick = {},
        onCreateRoomSuccess = {},
    )
}
