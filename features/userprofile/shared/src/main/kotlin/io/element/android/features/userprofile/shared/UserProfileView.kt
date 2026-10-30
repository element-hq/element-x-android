/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.userprofile.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.startchat.api.ConfirmingStartDmWithMatrixUser
import io.element.android.features.userprofile.api.UserProfileEvent
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.api.UserProfileVerificationState
import io.element.android.features.userprofile.shared.blockuser.BlockUserDialogs
import io.element.android.features.userprofile.shared.blockuser.BlockUserSection
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.matrix.ui.components.CreateDmConfirmationBottomSheet
import io.element.android.libraries.ui.strings.CommonStrings
import android.os.Build
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.widget.Toast

@Composable
fun UserProfileView(
    state: UserProfileState,
    onShareUser: () -> Unit,
    onOpenDm: (RoomId) -> Unit,
    onStartCall: (RoomId, CallIntent) -> Unit,
    goBack: () -> Unit,
    openAvatarPreview: (username: String, url: String) -> Unit,
    onVerifyClick: (UserId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    val context = LocalContext.current;
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { }, navigationIcon = { BackButton(onClick = goBack) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .verticalScroll(rememberScrollState())
        ) {
            UserProfileHeaderSection(
                avatarUrl = state.avatarUrl,
                userId = state.userId,
                userName = state.userName,
                verificationState = state.verificationState,
                displayedStatus = state.displayedStatus,
                openAvatarPreview = { avatarUrl ->
                    openAvatarPreview(state.userName ?: state.userId.value, avatarUrl)
                },
                onUserIdClick = {
                    state.eventSink(UserProfileEvent.CopyToClipboard(state.userId.value))
                },
                withdrawVerificationClick = { state.eventSink(UserProfileEvent.WithdrawVerification) },
            )
            UserProfileMainActionsSection(
                isCurrentUser = state.isCurrentUser,
                canCall = state.canCall,
                onShareUser = onShareUser,
                onStartDM = { state.eventSink(UserProfileEvent.StartDM) },
                onCall = { intent -> state.dmRoomId?.let { onStartCall(it, intent) } }
            )
            Spacer(modifier = Modifier.height(26.dp))
            if (!state.isCurrentUser) {
                VerifyUserSection(state, onVerifyClick = { onVerifyClick(state.userId) })
                AddUserShortcutSection(state, onAddUserShortcutClick = {
                    // 1. Проверяем, поддерживает ли устройство создание ярлыков на рабочем столе
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        // Для старых версий Android (ниже 8.0) логика была другой (через Broadcast-намерения)
                    }

                    Toast.makeText(context, "UserId: ${state.userId}", Toast.LENGTH_SHORT).show()
                    // val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
                    //
                    // if (shortcutManager.isRequestPinShortcutSupported) {
                    //
                    //     // 2. Создаем Intent (ссылку), который сработает при клике на ярлык
                    //     // Используем явный Intent для безопасности, чтобы открывалась именно наша Activity
                    //     val shortcutIntent = Intent(context, ChatActivity::class.java).apply {
                    //         action = Intent.ACTION_VIEW
                    //         // Передаем ID чата/пользователя внутри Uri или через extra-параметры
                    //         data = Uri.parse("https://matrix.to/#${state.userId}")
                    //         putExtra("EXTRA_USER_ID", userId)
                    //         // Флаги, чтобы очистить стек и запустить Activity как новую задачу, если приложение закрыто
                    //         flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    //     }
                    //
                    //     // 3. Собираем сам ярлык
                    //     val shortcut = ShortcutInfo.Builder(context, "shortcut_chat_$userId") // Уникальный ID ярлыка в системе
                    //         .setShortLabel(userName) // Имя под иконкой (например, "Иван")
                    //         .setLongLabel("Чат с $userName") // Полное имя (используется системой в списках)
                    //         .setIcon(Icon.createWithResource(context, avatarResId)) // Аватарка (можно из ресурсов или Bitmap)
                    //         .setIntent(shortcutIntent)
                    //         .build()
                    //
                    //     // 4. Запрашиваем у системы вывод на рабочий стол
                    //     // Система покажет стандартный диалог подтверждения пользователю
                    //     shortcutManager.requestPinShortcut(shortcut, null)
                    // }

                })
                BlockUserSection(state)
                BlockUserDialogs(state)
            }
            AsyncActionView(
                async = state.startDmActionState,
                progressDialog = {
                    AsyncActionViewDefaults.ProgressDialog(
                        progressText = stringResource(CommonStrings.common_starting_chat),
                    )
                },
                onSuccess = onOpenDm,
                errorMessage = { stringResource(R.string.screen_start_chat_error_starting_chat) },
                onRetry = { state.eventSink(UserProfileEvent.StartDM) },
                onErrorDismiss = { state.eventSink(UserProfileEvent.ClearStartDMState) },
                confirmationDialog = { data ->
                    if (data is ConfirmingStartDmWithMatrixUser) {
                        CreateDmConfirmationBottomSheet(
                            matrixUser = data.matrixUser,
                            isUserIdentityUnknown = data.isUserIdentityUnknown,
                            onSendInvite = {
                                state.eventSink(UserProfileEvent.StartDM)
                            },
                            onDismiss = {
                                state.eventSink(UserProfileEvent.ClearStartDMState)
                            },
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun VerifyUserSection(
    state: UserProfileState,
    onVerifyClick: () -> Unit,
) {
    if (state.verificationState == UserProfileVerificationState.UNVERIFIED) {
        ListItem(
            content = { Text(stringResource(CommonStrings.common_verify_user)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Lock())),
            onClick = onVerifyClick,
        )
    }
}


@Composable
private fun AddUserShortcutSection(
    state: UserProfileState,
    onAddUserShortcutClick: () -> Unit,
) {
    ListItem(
        content = { Text("Add user shortcut") },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Lock())),
        onClick =  onAddUserShortcutClick,
    )
}

@PreviewsDayNight
@Composable
internal fun UserProfileViewPreview(
    @PreviewParameter(UserProfileStatePreviewParam::class) state: UserProfileState
) = ElementPreview {
    UserProfileView(
        state = state,
        onShareUser = {},
        goBack = {},
        onOpenDm = {},
        onStartCall = { _, _ -> },
        openAvatarPreview = { _, _ -> },
        onVerifyClick = {},
    )
}
