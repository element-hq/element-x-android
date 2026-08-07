/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.slashcommands.api.ChatEffect
import io.element.android.libraries.slashcommands.api.MessagePrefix
import io.element.android.libraries.slashcommands.api.SlashCommand
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CommandParserTest {
    @Test
    fun parseSlashCommandEmpty() = runTest {
        test("/", SlashCommand.ErrorEmptySlashCommand("A string/"))
    }

    @Test
    fun parseSlashCommandUnknown() = runTest {
        test("/unknown", SlashCommand.ErrorUnknownSlashCommand("A string/unknown"))
        test("/unknown with param", SlashCommand.ErrorUnknownSlashCommand("A string/unknown"))
    }

    @Test
    fun parseSlashCommandNotACommand() = runTest {
        test("", SlashCommand.NotACommand)
        test("test", SlashCommand.NotACommand)
        test("// test", SlashCommand.NotACommand)
    }

    @Test
    fun parseSlashCommandEmote() = runTest {
        test("/me test", SlashCommand.SendEmote("test"))
        test("/me", SlashCommand.ErrorSyntax("A string/me, /me <message>"))
    }

    @Test
    fun parseSlashCommandRemove() = runTest {
        // Nominal
        test("/remove $A_USER_ID", SlashCommand.RemoveUser(A_USER_ID, null))
        // With a reason
        test("/remove $A_USER_ID a reason", SlashCommand.RemoveUser(A_USER_ID, "a reason"))
        // Trim the reason
        test("/remove $A_USER_ID    a    reason    ", SlashCommand.RemoveUser(A_USER_ID, "a    reason"))
        // Alias
        test("/kick $A_USER_ID", SlashCommand.RemoveUser(A_USER_ID, null))
        // Error
        test("/remove", SlashCommand.ErrorSyntax("A string/remove, /remove <user-id> [reason]"))
    }

    @Test
    fun parseSlashCommandRemoveMarkdown() = runTest {
        // Nominal
        test(
            "/remove [@user:domain.org](https://matrix.to/#/@user:domain.org)",
            SlashCommand.RemoveUser(UserId("@user:domain.org"), null)
        )
        test(
            "/remove [@user:domain.org](https://matrix.to/#/@user:domain.org) reason",
            SlashCommand.RemoveUser(UserId("@user:domain.org"), "reason")
        )
    }

    @Test
    fun parseSlashCommandPlain() = runTest {
        test("/plain hello", SlashCommand.SendPlainText("hello"))
        test("/plain", SlashCommand.ErrorSyntax("A string/plain, /plain <message>"))
    }

    @Test
    fun parseSlashCommandNickAndMyAvatar() = runTest {
        test("/nick John", SlashCommand.ChangeDisplayName("John"))
        test("/nick", SlashCommand.ErrorSyntax("A string/nick, /nick <display-name>"))

        test("/myavatar mxc://matrix.org/abc", SlashCommand.ChangeAvatar("mxc://matrix.org/abc"))
        test("/myavatar http://notmxc", SlashCommand.ErrorSyntax("A string/myavatar, /myavatar <mxc_url>"))
        test("/myavatar", SlashCommand.ErrorSyntax("A string/myavatar, /myavatar <mxc_url>"))
    }

    @Test
    fun parseSlashCommandRoomNickAndAvatars() = runTest {
        test("/myroomnick Roomy", SlashCommand.ChangeDisplayNameForRoom("Roomy"))
        test("/roomavatar mxc://matrix.org/abc", SlashCommand.ChangeRoomAvatar("mxc://matrix.org/abc"))
        test("/roomavatar http://notmxc", SlashCommand.ErrorSyntax("A string/roomavatar, /roomavatar <mxc_url>"))
        test("/myroomavatar mxc://matrix.org/abc", SlashCommand.ChangeAvatarForRoom("mxc://matrix.org/abc"))
    }

    @Test
    fun parseSlashCommandTopicAndRainbow() = runTest {
        test("/topic New topic", SlashCommand.ChangeTopic("New topic"))
        test("/topic", SlashCommand.ErrorSyntax("A string/topic, /topic <topic>"))

        test("/rainbow yay", SlashCommand.SendRainbow("yay"))
        test("/rainbow", SlashCommand.ErrorSyntax("A string/rainbow, /rainbow <message>"))

        test("/rainbowme yay", SlashCommand.SendRainbowEmote("yay"))
        test("/rainbowme", SlashCommand.ErrorSyntax("A string/rainbowme, /rainbowme <message>"))
    }

    @Test
    fun parseSlashCommandJoinAndRoomName() = runTest {
        // valid join
        test(
            "/join !roomId:domain reason",
            SlashCommand.JoinRoom(
                RoomIdOrAlias.Id(RoomId("!roomId:domain")),
                "reason"
            )
        )

        // invalid join
        test("/join notavalid", SlashCommand.ErrorSyntax("A string/join, /join <room-address> [reason]"))

        test("/roomname My Room", SlashCommand.ChangeRoomName("My Room"))
        test("/roomname", SlashCommand.ErrorSyntax("A string/roomname, /roomname <name>"))
    }

    @Test
    fun parseSlashCommandInviteBanEtc() = runTest {
        test("/invite $A_USER_ID", SlashCommand.Invite(A_USER_ID, null))
        test("/invite", SlashCommand.ErrorSyntax("A string/invite, /invite <user-id> [reason]"))

        test("/ban $A_USER_ID bad", SlashCommand.BanUser(A_USER_ID, "bad"))
        test("/unban $A_USER_ID", SlashCommand.UnbanUser(A_USER_ID, null))

        test("/ignore $A_USER_ID", SlashCommand.IgnoreUser(A_USER_ID))
        test("/unignore $A_USER_ID", SlashCommand.UnignoreUser(A_USER_ID))
    }

    @Test
    fun parseSlashCommandPowerLevels() = runTest {
        test("/op $A_USER_ID 50", SlashCommand.SetUserPowerLevel(A_USER_ID, 50))
        test("/op $A_USER_ID notnumber", SlashCommand.ErrorSyntax("A string/op, /op <user-id> [<power-level>]"))
        test("/deop $A_USER_ID", SlashCommand.SetUserPowerLevel(A_USER_ID, null))
    }

    @Test
    fun parseSlashCommandDevtoolsAndSpoiler() = runTest {
        test("/devtools", SlashCommand.DevTools)
        test("/devtools extra", SlashCommand.ErrorSyntax("A string/devtools, /devtools"))

        test("/spoiler secret", SlashCommand.SendSpoiler("secret"))
        test("/spoiler", SlashCommand.ErrorSyntax("A string/spoiler, /spoiler <message>"))
    }

    @Test
    fun parseSlashCommandEmojisAndSession() = runTest {
        test("/shrug hello", SlashCommand.SendWithPrefix(MessagePrefix.Shrug, "hello"))
        test("/shrug", SlashCommand.SendWithPrefix(MessagePrefix.Shrug, ""))

        test("/lenny fun", SlashCommand.SendWithPrefix(MessagePrefix.Lenny, "fun"))
        test("/tableflip wow", SlashCommand.SendWithPrefix(MessagePrefix.TableFlip, "wow"))
        test("/unflip be safe", SlashCommand.SendWithPrefix(MessagePrefix.Unflip, "be safe"))

        test("/discardsession", SlashCommand.DiscardSession)
        test("/discardsession extra", SlashCommand.ErrorSyntax("A string/discardsession, /discardsession"))
    }

    @Test
    fun parseSlashCommandWhoisAndEffectsAndLeave() = runTest {
        test("/whois $A_USER_ID", SlashCommand.ShowUser(A_USER_ID))

        test("/confetti party", SlashCommand.SendChatEffect(ChatEffect.CONFETTI, "party"))
        test("/snowfall snow", SlashCommand.SendChatEffect(ChatEffect.SNOWFALL, "snow"))

        test("/leave", SlashCommand.LeaveRoom)
        test("/leave now", SlashCommand.ErrorSyntax("A string/leave, /leave"))
    }

    @Test
    fun parseSlashCommandUpgradeAndCrashAndFeatureFlagAndThreads() = runTest {
        test("/upgraderoom 9", SlashCommand.UpgradeRoom("9"))
        test("/upgraderoom", SlashCommand.ErrorSyntax("A string/upgraderoom, /upgraderoom newVersion"))

        // Crash only when developer mode enabled
        val cpDev = createCommandParser(appPreferencesStore = InMemoryAppPreferencesStore(isDeveloperModeEnabled = true))
        try {
            cpDev.parseSlashCommand("/crash", null, false)
            org.junit.Assert.fail("Expected crash to throw")
        } catch (_: IllegalStateException) {
            // expected
        }

        // Feature flag disabled
        val cpFF = createCommandParser(featureFlagService = FakeFeatureFlagService(initialState = mapOf(FeatureFlags.SlashCommand.key to false)))
        val res = cpFF.parseSlashCommand("/me test", null, false)
        assertThat(res).isEqualTo(SlashCommand.NotACommand)

        // Not supported in threads (e.g. /join)
        val cpThread = createCommandParser()
        val threadRes = cpThread.parseSlashCommand("/join !roomId:domain", null, true)
        assertThat(threadRes).isInstanceOf(SlashCommand.ErrorCommandNotSupportedInThreads::class.java)
        assertThat((threadRes as SlashCommand.ErrorCommandNotSupportedInThreads).message).isEqualTo("A string/join")
    }

    private suspend fun test(message: String, expectedResult: SlashCommand) {
        val commandParser = createCommandParser()
        val result = commandParser.parseSlashCommand(message, null, false)
        assertThat(result).isEqualTo(expectedResult)
    }
}

internal fun createCommandParser(
    appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    featureFlagService: FeatureFlagService = FakeFeatureFlagService(
        initialState = mapOf(
            FeatureFlags.SlashCommand.key to true,
        ),
    ),
    stringProvider: StringProvider = FakeStringProvider(),
) = CommandParser(
    appPreferencesStore = appPreferencesStore,
    featureFlagService = featureFlagService,
    stringProvider = stringProvider,
)
