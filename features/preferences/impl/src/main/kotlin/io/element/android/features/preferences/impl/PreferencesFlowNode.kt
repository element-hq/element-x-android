/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.deactivation.api.AccountDeactivationEntryPoint
import io.element.android.features.licenses.api.OpenSourceLicensesEntryPoint
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.logout.api.LogoutEntryPoint
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.preferences.impl.about.AboutNode
import io.element.android.features.preferences.impl.advanced.AdvancedSettingsNode
import io.element.android.features.preferences.impl.analytics.AnalyticsSettingsNode
import io.element.android.features.preferences.impl.blockedusers.BlockedUsersNode
import io.element.android.features.preferences.impl.developer.DeveloperSettingsNode
import io.element.android.features.preferences.impl.labs.LabsNode
import io.element.android.features.preferences.impl.notifications.NotificationSettingsNode
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingNode
import io.element.android.features.preferences.impl.root.PreferencesRootNode
import io.element.android.features.preferences.impl.user.editprofile.EditUserProfileNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.appyx.canPop
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class PreferencesFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val lockScreenEntryPoint: LockScreenEntryPoint,
    private val notificationTroubleShootEntryPoint: NotificationTroubleShootEntryPoint,
    private val pushHistoryEntryPoint: PushHistoryEntryPoint,
    private val logoutEntryPoint: LogoutEntryPoint,
    private val openSourceLicensesEntryPoint: OpenSourceLicensesEntryPoint,
    private val accountDeactivationEntryPoint: AccountDeactivationEntryPoint,
) : BaseFlowNode<PreferencesFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = plugins.filterIsInstance<PreferencesEntryPoint.Params>().first().initialElement.toNavTarget(),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object DeveloperSettings : NavTarget

        @Parcelize
        data object AdvancedSettings : NavTarget

        @Parcelize
        data object Labs : NavTarget

        @Parcelize
        data object AnalyticsSettings : NavTarget

        @Parcelize
        data object About : NavTarget

        @Parcelize
        data object NotificationSettings : NavTarget

        @Parcelize
        data object TroubleshootNotifications : NavTarget

        @Parcelize
        data object PushHistory : NavTarget

        @Parcelize
        data object LockScreenSettings : NavTarget

        @Parcelize
        data class EditDefaultNotificationSetting(val isOneToOne: Boolean) : NavTarget

        @Parcelize
        data class UserProfile(val matrixUser: MatrixUser) : NavTarget

        @Parcelize
        data object BlockedUsers : NavTarget

        @Parcelize
        data object SignOut : NavTarget

        @Parcelize
        data object AccountDeactivation : NavTarget

        @Parcelize
        data object OssLicenses : NavTarget
    }

    private val callback: PreferencesEntryPoint.Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : PreferencesRootNode.Callback {
                    override fun navigateToAddAccount() {
                        callback.navigateToAddAccount()
                    }

                    override fun navigateToBugReport() {
                        callback.navigateToBugReport()
                    }

                    override fun navigateToSecureBackup() {
                        callback.navigateToSecureBackup()
                    }

                    override fun navigateToAnalyticsSettings() {
                        backstack.push(NavTarget.AnalyticsSettings)
                    }

                    override fun navigateToAbout() {
                        backstack.push(NavTarget.About)
                    }

                    override fun navigateToDeveloperSettings() {
                        backstack.push(NavTarget.DeveloperSettings)
                    }

                    override fun navigateToNotificationSettings() {
                        backstack.push(NavTarget.NotificationSettings)
                    }

                    override fun navigateToLockScreenSettings() {
                        backstack.push(NavTarget.LockScreenSettings)
                    }

                    override fun navigateToAdvancedSettings() {
                        backstack.push(NavTarget.AdvancedSettings)
                    }

                    override fun navigateToLabs() {
                        backstack.push(NavTarget.Labs)
                    }

                    override fun navigateToUserProfile(matrixUser: MatrixUser) {
                        backstack.push(NavTarget.UserProfile(matrixUser))
                    }

                    override fun navigateToBlockedUsers() {
                        backstack.push(NavTarget.BlockedUsers)
                    }

                    override fun startSignOutFlow() {
                        backstack.push(NavTarget.SignOut)
                    }

                    override fun startAccountDeactivationFlow() {
                        backstack.push(NavTarget.AccountDeactivation)
                    }
                }
                createNode<PreferencesRootNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.DeveloperSettings -> {
                val developerSettingsCallback = object : DeveloperSettingsNode.Callback {
                    override fun navigateToPushHistory() {
                        backstack.push(NavTarget.PushHistory)
                    }

                    override fun onDone() {
                        backstack.pop()
                    }
                }
                createNode<DeveloperSettingsNode>(buildContext, listOf(developerSettingsCallback))
            }
            NavTarget.Labs -> {
                val callback = object : LabsNode.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                createNode<LabsNode>(buildContext, listOf(callback))
            }
            NavTarget.About -> {
                val callback = object : AboutNode.Callback {
                    override fun navigateToOssLicenses() {
                        backstack.push(NavTarget.OssLicenses)
                    }
                }
                createNode<AboutNode>(buildContext, listOf(callback))
            }
            NavTarget.AnalyticsSettings -> {
                createNode<AnalyticsSettingsNode>(buildContext)
            }
            NavTarget.NotificationSettings -> {
                val notificationSettingsCallback = object : NotificationSettingsNode.Callback {
                    override fun navigateToEditDefaultNotificationSetting(isOneToOne: Boolean) {
                        backstack.push(NavTarget.EditDefaultNotificationSetting(isOneToOne))
                    }

                    override fun navigateToTroubleshootNotifications() {
                        backstack.push(NavTarget.TroubleshootNotifications)
                    }
                }
                createNode<NotificationSettingsNode>(buildContext, listOf(notificationSettingsCallback))
            }
            NavTarget.TroubleshootNotifications -> {
                notificationTroubleShootEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = object : NotificationTroubleShootEntryPoint.Callback {
                        override fun onDone() {
                            if (backstack.canPop()) {
                                backstack.pop()
                            } else {
                                navigateUp()
                            }
                        }

                        override fun navigateToBlockedUsers() {
                            backstack.push(NavTarget.BlockedUsers)
                        }
                    },
                )
            }
            NavTarget.PushHistory -> {
                pushHistoryEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = object : PushHistoryEntryPoint.Callback {
                        override fun onDone() {
                            if (backstack.canPop()) {
                                backstack.pop()
                            } else {
                                navigateUp()
                            }
                        }

                        override fun navigateToEvent(roomId: RoomId, eventId: EventId) {
                            callback.navigateToEvent(roomId, eventId)
                        }
                    },
                )
            }
            is NavTarget.EditDefaultNotificationSetting -> {
                val callback = object : EditDefaultNotificationSettingNode.Callback {
                    override fun navigateToRoomNotificationSettings(roomId: RoomId) {
                        callback.navigateToRoomNotificationSettings(roomId)
                    }
                }
                val input = EditDefaultNotificationSettingNode.Inputs(navTarget.isOneToOne)
                createNode<EditDefaultNotificationSettingNode>(buildContext, plugins = listOf(input, callback))
            }
            NavTarget.AdvancedSettings -> {
                createNode<AdvancedSettingsNode>(buildContext)
            }
            is NavTarget.UserProfile -> {
                val inputs = EditUserProfileNode.Inputs(navTarget.matrixUser)
                val callback = object : EditUserProfileNode.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                createNode<EditUserProfileNode>(buildContext, listOf(inputs, callback))
            }
            NavTarget.LockScreenSettings -> {
                lockScreenEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    navTarget = LockScreenEntryPoint.Target.Settings,
                    callback = object : LockScreenEntryPoint.Callback {
                        override fun onSetupDone() {
                            // No op
                        }
                    }
                )
            }
            NavTarget.BlockedUsers -> {
                createNode<BlockedUsersNode>(buildContext)
            }
            NavTarget.SignOut -> {
                val callBack: LogoutEntryPoint.Callback = object : LogoutEntryPoint.Callback {
                    override fun navigateToSecureBackup() {
                        callback.navigateToSecureBackup()
                    }
                }
                logoutEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callBack,
                )
            }
            is NavTarget.OssLicenses -> {
                openSourceLicensesEntryPoint.createNode(this, buildContext)
            }
            NavTarget.AccountDeactivation -> {
                accountDeactivationEntryPoint.createNode(this, buildContext)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
