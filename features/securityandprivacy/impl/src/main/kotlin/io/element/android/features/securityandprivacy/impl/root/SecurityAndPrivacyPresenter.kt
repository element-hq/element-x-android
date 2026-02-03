/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyPermissions
import io.element.android.features.securityandprivacy.api.securityAndPrivacyPermissions
import io.element.android.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import io.element.android.features.securityandprivacy.impl.editroomaddress.matchesServer
import io.element.android.features.securityandprivacy.impl.manageauthorizedspaces.SpaceSelectionState
import io.element.android.features.securityandprivacy.impl.manageauthorizedspaces.SpaceSelectionStateHolder
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.AllowRule
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AssistedInject
class SecurityAndPrivacyPresenter(
    @Assisted private val navigator: SecurityAndPrivacyNavigator,
    private val spaceSelectionStateHolder: SpaceSelectionStateHolder,
    private val matrixClient: MatrixClient,
    private val room: JoinedRoom,
    private val featureFlagService: FeatureFlagService,
) : Presenter<SecurityAndPrivacyState> {
    @AssistedFactory
    interface Factory {
        fun create(
            navigator: SecurityAndPrivacyNavigator,
        ): SecurityAndPrivacyPresenter
    }

    @Composable
    override fun present(): SecurityAndPrivacyState {
        val coroutineScope = rememberCoroutineScope()

        val isKnockEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.Knock)
        }.collectAsState(false)
        val isSpaceSettingsEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.SpaceSettings)
        }.collectAsState(false)

        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        val homeserverName = remember { matrixClient.userIdServerName() }
        val roomInfo by room.roomInfoFlow.collectAsState()

        val savedIsVisibleInRoomDirectory = remember { mutableStateOf<AsyncData<Boolean>>(AsyncData.Uninitialized) }
        LaunchedEffect(Unit) {
            isRoomVisibleInRoomDirectory(savedIsVisibleInRoomDirectory)
        }

        val savedSettings by remember {
            derivedStateOf {
                SecurityAndPrivacySettings(
                    roomAccess = roomInfo.joinRule.map(),
                    isEncrypted = roomInfo.isEncrypted == true,
                    isVisibleInRoomDirectory = savedIsVisibleInRoomDirectory.value,
                    historyVisibility = roomInfo.historyVisibility.map(),
                    address = roomInfo.firstDisplayableAlias(homeserverName)?.value,
                )
            }
        }

        val editedRoomAccess = remember(savedSettings.roomAccess) {
            mutableStateOf(savedSettings.roomAccess)
        }
        var editedHistoryVisibility by remember(savedSettings.historyVisibility) {
            mutableStateOf(savedSettings.historyVisibility)
        }
        var editedIsEncrypted by remember(savedSettings.isEncrypted) {
            mutableStateOf(savedSettings.isEncrypted)
        }
        var editedVisibleInRoomDirectory by remember(savedIsVisibleInRoomDirectory.value) {
            mutableStateOf(savedIsVisibleInRoomDirectory.value)
        }
        val editedSettings = SecurityAndPrivacySettings(
            roomAccess = editedRoomAccess.value,
            isEncrypted = editedIsEncrypted,
            isVisibleInRoomDirectory = editedVisibleInRoomDirectory,
            historyVisibility = editedHistoryVisibility,
            address = savedSettings.address,
        )

        val selectableJoinedSpaces by produceState(initialValue = persistentSetOf(), key1 = savedSettings.roomAccess.spaceIds()) {
            val joinedParentSpaces = matrixClient
                .spaceService
                .joinedParents(room.roomId)
                .getOrDefault(emptyList())

            val nonParentJoinedSpaces = savedSettings.roomAccess
                .spaceIds()
                .mapNotNull { spaceId -> matrixClient.spaceService.getSpaceRoom(spaceId) }

            value = (joinedParentSpaces + nonParentJoinedSpaces).toImmutableSet()
        }

        val spaceSelectionMode by remember {
            derivedStateOf {
                getSpaceSelectionMode(selectableJoinedSpaces, savedSettings.roomAccess)
            }
        }

        LaunchedEffect(selectableJoinedSpaces, savedSettings.roomAccess) {
            val unknownSpaceIds = savedSettings.roomAccess.spaceIds().filter { spaceId ->
                selectableJoinedSpaces.none { it.roomId == spaceId }
            }.toImmutableList()
            spaceSelectionStateHolder.update { state ->
                state.copy(
                    selectableSpaces = selectableJoinedSpaces,
                    unknownSpaceIds = unknownSpaceIds,
                )
            }
        }

        var showEnableEncryptionConfirmation by remember(savedSettings.isEncrypted) { mutableStateOf(false) }
        val permissions by room.permissionsAsState(SecurityAndPrivacyPermissions.DEFAULT) { perms ->
            perms.securityAndPrivacyPermissions()
        }

        fun handleEvent(event: SecurityAndPrivacyEvent) {
            when (event) {
                SecurityAndPrivacyEvent.Save -> {
                    coroutineScope.save(
                        saveAction = saveAction,
                        isVisibleInRoomDirectory = savedIsVisibleInRoomDirectory,
                        savedSettings = savedSettings,
                        editedSettings = editedSettings
                    )
                }
                is SecurityAndPrivacyEvent.ChangeRoomAccess -> {
                    editedRoomAccess.value = event.roomAccess
                }
                is SecurityAndPrivacyEvent.ToggleEncryptionState -> {
                    if (editedIsEncrypted) {
                        editedIsEncrypted = false
                    } else {
                        showEnableEncryptionConfirmation = true
                    }
                }
                is SecurityAndPrivacyEvent.ChangeHistoryVisibility -> {
                    editedHistoryVisibility = event.historyVisibility
                }
                SecurityAndPrivacyEvent.ToggleRoomVisibility -> {
                    editedVisibleInRoomDirectory = when (val edited = editedVisibleInRoomDirectory) {
                        is AsyncData.Success -> AsyncData.Success(!edited.data)
                        else -> edited
                    }
                }
                SecurityAndPrivacyEvent.EditRoomAddress -> navigator.openEditRoomAddress()
                SecurityAndPrivacyEvent.CancelEnableEncryption -> {
                    showEnableEncryptionConfirmation = false
                }
                SecurityAndPrivacyEvent.ConfirmEnableEncryption -> {
                    showEnableEncryptionConfirmation = false
                    editedIsEncrypted = true
                }
                SecurityAndPrivacyEvent.DismissSaveError -> {
                    saveAction.value = AsyncAction.Uninitialized
                }
                SecurityAndPrivacyEvent.Exit -> {
                    saveAction.value = if (savedSettings == editedSettings || saveAction.value == AsyncAction.ConfirmingCancellation) {
                        AsyncAction.Success(Unit)
                    } else {
                        AsyncAction.ConfirmingCancellation
                    }
                }
                SecurityAndPrivacyEvent.DismissExitConfirmation -> {
                    saveAction.value = AsyncAction.Uninitialized
                }
                SecurityAndPrivacyEvent.ManageAuthorizedSpaces -> coroutineScope.launch {
                    handleMultipleSelection(
                        savedAccess = savedSettings.roomAccess,
                        editedRoomAccess = editedRoomAccess,
                        forKnockRestricted = editedRoomAccess.value is SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember
                    )
                }
                SecurityAndPrivacyEvent.SelectSpaceMemberAccess -> coroutineScope.launch {
                    handleSpaceMemberAccessSelection(
                        spaceSelectionMode = spaceSelectionMode,
                        savedAccess = savedSettings.roomAccess,
                        editedAccess = editedRoomAccess,
                    )
                }
                SecurityAndPrivacyEvent.SelectAskToJoinWithSpaceMembersAccess -> coroutineScope.launch {
                    handleAskToJoinWithSpaceMembersAccessSelection(
                        spaceSelectionMode = spaceSelectionMode,
                        savedAccess = savedSettings.roomAccess,
                        editedAccess = editedRoomAccess,
                    )
                }
            }
        }

        LaunchedEffect(saveAction.value.isSuccess()) {
            if (saveAction.value.isSuccess()) {
                navigator.onDone()
            }
        }

        val state = SecurityAndPrivacyState(
            savedSettings = savedSettings,
            editedSettings = editedSettings,
            homeserverName = homeserverName,
            showEnableEncryptionConfirmation = showEnableEncryptionConfirmation,
            isKnockEnabled = isKnockEnabled,
            saveAction = saveAction.value,
            permissions = permissions,
            isSpace = roomInfo.isSpace,
            isSpaceSettingsEnabled = isSpaceSettingsEnabled,
            selectableJoinedSpaces = selectableJoinedSpaces,
            spaceSelectionMode = spaceSelectionMode,
            eventSink = ::handleEvent,
        )

        // Revert changes that the user is not allowed to make anymore
        LaunchedEffect(permissions, state.editedSettings.roomAccess) {
            if (!state.showRoomAccessSection) {
                editedRoomAccess.value = savedSettings.roomAccess
            }
            if (!state.showEncryptionSection) {
                editedIsEncrypted = savedSettings.isEncrypted
            }
            if (!state.showRoomVisibilitySections) {
                editedVisibleInRoomDirectory = savedSettings.isVisibleInRoomDirectory
            }
            if (!state.showHistoryVisibilitySection) {
                editedHistoryVisibility = savedSettings.historyVisibility
            } else if (editedSettings.historyVisibility !in state.availableHistoryVisibilities) {
                editedHistoryVisibility = editedSettings.historyVisibility.fallback()
            }
        }
        return state
    }

    private suspend fun handleSpaceMemberAccessSelection(
        spaceSelectionMode: SpaceSelectionMode,
        savedAccess: SecurityAndPrivacyRoomAccess,
        editedAccess: MutableState<SecurityAndPrivacyRoomAccess>,
    ) {
        if (editedAccess.value is SecurityAndPrivacyRoomAccess.SpaceMember) {
            return
        }
        when (spaceSelectionMode) {
            is SpaceSelectionMode.None -> Unit
            is SpaceSelectionMode.Multiple -> handleMultipleSelection(
                savedAccess = savedAccess,
                editedRoomAccess = editedAccess,
                forKnockRestricted = false,
            )
            is SpaceSelectionMode.Single -> {
                val newRoomAccess = SecurityAndPrivacyRoomAccess.SpaceMember(
                    spaceIds = persistentListOf(spaceSelectionMode.spaceId)
                )
                editedAccess.value = newRoomAccess
            }
        }
    }

    private suspend fun handleAskToJoinWithSpaceMembersAccessSelection(
        spaceSelectionMode: SpaceSelectionMode,
        savedAccess: SecurityAndPrivacyRoomAccess,
        editedAccess: MutableState<SecurityAndPrivacyRoomAccess>,
    ) {
        if (editedAccess.value is SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember) {
            return
        }
        when (spaceSelectionMode) {
            is SpaceSelectionMode.None -> Unit
            is SpaceSelectionMode.Multiple -> handleMultipleSelection(
                savedAccess = savedAccess,
                editedRoomAccess = editedAccess,
                forKnockRestricted = true,
            )
            is SpaceSelectionMode.Single -> {
                val newRoomAccess = SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember(
                    spaceIds = persistentListOf(spaceSelectionMode.spaceId)
                )
                editedAccess.value = newRoomAccess
            }
        }
    }

    private suspend fun handleMultipleSelection(
        savedAccess: SecurityAndPrivacyRoomAccess,
        editedRoomAccess: MutableState<SecurityAndPrivacyRoomAccess>,
        forKnockRestricted: Boolean
    ) {
        val initialSelection = when (val currentRoomAccess = editedRoomAccess.value) {
            is SecurityAndPrivacyRoomAccess.SpaceMember -> currentRoomAccess.spaceIds
            is SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember -> currentRoomAccess.spaceIds
            else -> savedAccess.spaceIds()
        }
        spaceSelectionStateHolder.update { state ->
            state.copy(selectedSpaceIds = initialSelection, completion = SpaceSelectionState.Completion.Initial)
        }
        navigator.openManageAuthorizedSpaces()
        val newState = spaceSelectionStateHolder.state.first { it.completion != SpaceSelectionState.Completion.Initial }
        when (newState.completion) {
            SpaceSelectionState.Completion.Initial -> Unit
            SpaceSelectionState.Completion.Cancelled -> {
                navigator.closeManageAuthorizedSpaces()
            }
            SpaceSelectionState.Completion.Completed -> {
                val selectedIds = newState.selectedSpaceIds
                editedRoomAccess.value = if (forKnockRestricted) {
                    SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember(spaceIds = selectedIds)
                } else {
                    SecurityAndPrivacyRoomAccess.SpaceMember(spaceIds = selectedIds)
                }
                navigator.closeManageAuthorizedSpaces()
            }
        }
    }

    private fun getSpaceSelectionMode(
        selectableJoinedSpaces: Set<SpaceRoom>,
        savedAccess: SecurityAndPrivacyRoomAccess,
    ): SpaceSelectionMode {
        val selectableSpacesCount = (selectableJoinedSpaces.map { it.roomId } + savedAccess.spaceIds()).toSet().size
        return when {
            selectableSpacesCount == 0 -> SpaceSelectionMode.None
            selectableSpacesCount > 1 -> SpaceSelectionMode.Multiple
            else -> {
                val joinedSpace = selectableJoinedSpaces.firstOrNull()
                if (joinedSpace != null) {
                    SpaceSelectionMode.Single(joinedSpace.roomId, joinedSpace)
                } else {
                    val spaceId = savedAccess.spaceIds().firstOrNull()
                    if (spaceId == null) {
                        SpaceSelectionMode.None
                    } else {
                        SpaceSelectionMode.Single(spaceId, null)
                    }
                }
            }
        }
    }

    private fun CoroutineScope.isRoomVisibleInRoomDirectory(isRoomVisible: MutableState<AsyncData<Boolean>>) = launch {
        isRoomVisible.runUpdatingState {
            room.getRoomVisibility().map { it == RoomVisibility.Public }
        }
    }

    private fun CoroutineScope.save(
        saveAction: MutableState<AsyncAction<Unit>>,
        isVisibleInRoomDirectory: MutableState<AsyncData<Boolean>>,
        savedSettings: SecurityAndPrivacySettings,
        editedSettings: SecurityAndPrivacySettings,
    ) = launch {
        suspend {
            val enableEncryption = async {
                if (editedSettings.isEncrypted && !savedSettings.isEncrypted) {
                    room.enableEncryption()
                } else {
                    Result.success(Unit)
                }
            }
            val updateHistoryVisibility = async {
                if (editedSettings.historyVisibility != savedSettings.historyVisibility) {
                    room.updateHistoryVisibility(editedSettings.historyVisibility.map())
                } else {
                    Result.success(Unit)
                }
            }
            val updateJoinRule = async {
                val joinRule = editedSettings.roomAccess.map()
                if (editedSettings.roomAccess != savedSettings.roomAccess && joinRule != null) {
                    room.updateJoinRule(joinRule)
                } else {
                    Result.success(Unit)
                }
            }
            val updateRoomVisibility = async {
                // When a user changes join rules to something other than knock or public,
                // the room should be automatically made invisible (private) in the room directory.
                val editedIsVisibleInRoomDirectory = when (editedSettings.roomAccess) {
                    SecurityAndPrivacyRoomAccess.AskToJoin,
                    is SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember,
                    SecurityAndPrivacyRoomAccess.Anyone -> editedSettings.isVisibleInRoomDirectory.dataOrNull()
                    else -> false
                }
                val savedIsVisibleInRoomDirectory = savedSettings.isVisibleInRoomDirectory.dataOrNull()
                if (editedIsVisibleInRoomDirectory != null && editedIsVisibleInRoomDirectory != savedIsVisibleInRoomDirectory) {
                    val roomVisibility = if (editedIsVisibleInRoomDirectory) RoomVisibility.Public else RoomVisibility.Private
                    room
                        .updateRoomVisibility(roomVisibility)
                        .onSuccess {
                            isVisibleInRoomDirectory.value = AsyncData.Success(editedIsVisibleInRoomDirectory)
                        }
                } else {
                    Result.success(Unit)
                }
            }
            val artificialDelay = async {
                // Artificial delay to make sure the user sees the loading state
                delay(500)
                Result.success(Unit)
            }
            val results = awaitAll(
                enableEncryption,
                updateHistoryVisibility,
                updateJoinRule,
                updateRoomVisibility,
                artificialDelay
            )
            if (results.any { it.isFailure }) {
                throw SecurityAndPrivacyFailures.SaveFailed
            }
        }.runCatchingUpdatingState(saveAction)
    }
}

private fun JoinRule?.map(): SecurityAndPrivacyRoomAccess {
    return when (this) {
        JoinRule.Public -> SecurityAndPrivacyRoomAccess.Anyone
        JoinRule.Knock -> SecurityAndPrivacyRoomAccess.AskToJoin
        is JoinRule.KnockRestricted -> SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember(
            spaceIds = this.rules
                .filterIsInstance<AllowRule.RoomMembership>()
                .map { it.roomId }
                .toImmutableList()
        )
        is JoinRule.Restricted -> SecurityAndPrivacyRoomAccess.SpaceMember(
            spaceIds = this.rules
                .filterIsInstance<AllowRule.RoomMembership>()
                .map { it.roomId }
                .toImmutableList()
        )
        JoinRule.Invite -> SecurityAndPrivacyRoomAccess.InviteOnly
        // All other cases are not supported so we default to InviteOnly
        is JoinRule.Custom,
        null -> SecurityAndPrivacyRoomAccess.InviteOnly
    }
}

private fun SecurityAndPrivacyRoomAccess.map(): JoinRule? {
    return when (this) {
        SecurityAndPrivacyRoomAccess.Anyone -> JoinRule.Public
        SecurityAndPrivacyRoomAccess.AskToJoin -> JoinRule.Knock
        SecurityAndPrivacyRoomAccess.InviteOnly -> JoinRule.Invite
        is SecurityAndPrivacyRoomAccess.SpaceMember -> JoinRule.Restricted(
            rules = this.spaceIds.map { AllowRule.RoomMembership(it) }.toImmutableList()
        )
        is SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember -> JoinRule.KnockRestricted(
            rules = this.spaceIds.map { AllowRule.RoomMembership(it) }.toImmutableList()
        )
    }
}

private fun RoomHistoryVisibility?.map(): SecurityAndPrivacyHistoryVisibility {
    return when (this) {
        RoomHistoryVisibility.Joined,
        RoomHistoryVisibility.Invited -> SecurityAndPrivacyHistoryVisibility.Invited
        RoomHistoryVisibility.Shared -> SecurityAndPrivacyHistoryVisibility.Shared
        RoomHistoryVisibility.WorldReadable -> SecurityAndPrivacyHistoryVisibility.WorldReadable
        // All other cases are not supported so we default to Shared
        is RoomHistoryVisibility.Custom,
        null -> SecurityAndPrivacyHistoryVisibility.Shared
    }
}

private fun SecurityAndPrivacyHistoryVisibility.map(): RoomHistoryVisibility {
    return when (this) {
        SecurityAndPrivacyHistoryVisibility.Invited -> RoomHistoryVisibility.Invited
        SecurityAndPrivacyHistoryVisibility.Shared -> RoomHistoryVisibility.Shared
        SecurityAndPrivacyHistoryVisibility.WorldReadable -> RoomHistoryVisibility.WorldReadable
    }
}

private fun RoomInfo.firstDisplayableAlias(serverName: String): RoomAlias? {
    return aliases.firstOrNull { it.matchesServer(serverName) } ?: aliases.firstOrNull()
}
