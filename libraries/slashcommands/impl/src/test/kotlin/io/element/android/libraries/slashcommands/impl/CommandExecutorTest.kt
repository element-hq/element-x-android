/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.timeline.MsgType
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.slashcommands.api.ChatEffect
import io.element.android.libraries.slashcommands.api.MessagePrefix
import io.element.android.libraries.slashcommands.api.SlashCommand
import io.element.android.libraries.slashcommands.impl.rainbow.RainbowGenerator
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CommandExecutorTest {
    @Test
    fun `send plain text delegates to timeline with plain flag`() = runTest {
        val timeline = FakeTimeline()
        var capturedBody: String? = null
        var capturedHtml: String? = "initial"
        var capturedAsPlainText = false
        timeline.sendMessageLambda = { body, htmlBody, _, _, asPlainText ->
            capturedBody = body
            capturedHtml = htmlBody
            capturedAsPlainText = asPlainText
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendPlainText("hello"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("hello")
        assertThat(capturedHtml).isNull()
        assertThat(capturedAsPlainText).isTrue()
    }

    @Test
    fun `send emote delegates to timeline as emote`() = runTest {
        val timeline = FakeTimeline()
        var msgType: MsgType? = null
        timeline.sendMessageLambda = { _, _, _, type, _ ->
            msgType = type
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendEmote("yay"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(msgType).isEqualTo(MsgType.MSG_TYPE_EMOTE)
    }

    @Test
    fun `send lenny prefixes message`() = runTest {
        val timeline = FakeTimeline()
        var capturedBody: String? = null
        timeline.sendMessageLambda = { body, _, _, _, _ ->
            capturedBody = body
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendWithPrefix(MessagePrefix.Lenny, "fun"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("( ͡° ͜ʖ ͡°) fun")
    }

    @Test
    fun `send table flip prefixes message`() = runTest {
        val timeline = FakeTimeline()
        var capturedBody: String? = null
        timeline.sendMessageLambda = { body, _, _, _, _ ->
            capturedBody = body
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendWithPrefix(MessagePrefix.TableFlip, "wow"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("(╯°□°）╯︵ ┻━┻ wow")
    }

    @Test
    fun `send unflip prefixes message`() = runTest {
        val timeline = FakeTimeline()
        var capturedBody: String? = null
        timeline.sendMessageLambda = { body, _, _, _, _ ->
            capturedBody = body
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendWithPrefix(MessagePrefix.Unflip, "keep cool"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("┬──┬ ノ( ゜-゜ノ) keep cool")
    }

    @Test
    fun `send shrug prefixes message`() = runTest {
        val timeline = FakeTimeline()
        var capturedBody: String? = null
        timeline.sendMessageLambda = { body, _, _, _, _ ->
            capturedBody = body
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendWithPrefix(MessagePrefix.Shrug, "wow"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("¯\\\\_(ツ)\\_/¯ wow")
    }

    @Test
    fun `send rainbow provides html body`() = runTest {
        val timeline = FakeTimeline()
        var capturedHtml: String? = null
        var capturedBody: String? = null
        var capturedMsgType: MsgType? = null
        timeline.sendMessageLambda = { body, htmlBody, _, msgType, _ ->
            capturedBody = body
            capturedHtml = htmlBody
            capturedMsgType = msgType
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendRainbow("a nice rainbow"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("a nice rainbow")
        assertThat(capturedHtml).isNotNull()
        assertThat(capturedHtml!!.contains("<font") || capturedHtml!!.contains("<span")).isTrue()
        assertThat(capturedMsgType).isEqualTo(MsgType.MSG_TYPE_TEXT)
    }

    @Test
    fun `send rainbow emote provides html body`() = runTest {
        val timeline = FakeTimeline()
        var capturedHtml: String? = null
        var capturedBody: String? = null
        var capturedMsgType: MsgType? = null
        timeline.sendMessageLambda = { body, htmlBody, _, msgType, _ ->
            capturedBody = body
            capturedHtml = htmlBody
            capturedMsgType = msgType
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendRainbowEmote("a nice rainbow"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("a nice rainbow")
        assertThat(capturedHtml).isNotNull()
        assertThat(capturedHtml!!.contains("<font") || capturedHtml!!.contains("<span")).isTrue()
        assertThat(capturedMsgType).isEqualTo(MsgType.MSG_TYPE_EMOTE)
    }

    @Test
    fun `change display name invokes the method of the matrix client`() = runTest {
        val matrixClient = FakeMatrixClient()
        val sut = createCommandExecutor(matrixClient = matrixClient)
        val res = sut.proceedAdmin(SlashCommand.ChangeDisplayName("new name"))
        assertThat(res.isSuccess).isTrue()
        assertThat(matrixClient.setDisplayNameCalled).isTrue()
    }

    @Test
    fun `change room avatar is not supported`() = runTest {
        val sut = createCommandExecutor()
        val res = sut.proceedAdmin(SlashCommand.ChangeRoomAvatar(AN_AVATAR_URL))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `change avatar for room is not supported`() = runTest {
        val sut = createCommandExecutor()
        val res = sut.proceedAdmin(SlashCommand.ChangeAvatarForRoom(AN_AVATAR_URL))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `change display name for room is not supported`() = runTest {
        val sut = createCommandExecutor()
        val res = sut.proceedAdmin(SlashCommand.ChangeDisplayNameForRoom(A_USER_NAME))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `upgrade room is not supported`() = runTest {
        val sut = createCommandExecutor()
        val res = sut.proceedAdmin(SlashCommand.UpgradeRoom("1"))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `set user power level is not supported`() = runTest {
        val sut = createCommandExecutor()
        val res = sut.proceedAdmin(SlashCommand.SetUserPowerLevel(A_USER_ID, 50))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `discard session is not supported`() = runTest {
        val sut = createCommandExecutor()
        val res = sut.proceedAdmin(SlashCommand.DiscardSession)
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `send spoiler sets formatted and body includes spoiler label`() = runTest {
        val timeline = FakeTimeline()
        var capturedBody: String? = null
        var capturedHtml: String? = null
        timeline.sendMessageLambda = { body, htmlBody, _, _, _ ->
            capturedBody = body
            capturedHtml = htmlBody
            Result.success(Unit)
        }
        val stringProvider = FakeStringProvider(defaultResult = "SPOILER")
        val sut = createCommandExecutor(
            stringProvider = stringProvider,
        )
        val res = sut.proceedSendMessage(SlashCommand.SendSpoiler("secret"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("[SPOILER](secret)")
        assertThat(capturedHtml).isEqualTo("<span data-mx-spoiler>secret</span>")
    }

    @Test
    fun `send chat effect is not supported`() = runTest {
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(
            SlashCommand.SendChatEffect(ChatEffect.CONFETTI, A_MESSAGE),
            FakeTimeline()
        )
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `admin commands call underlying client and room APIs`() = runTest {
        var kicked = false
        var banned = false
        var unbanned = false
        var invited = false
        var ignored = false
        var unignored = false
        var left = false
        var topicSet = false
        var nameSet = false
        var joined = false

        val joinedRoom = FakeJoinedRoom(
            kickUserResult = { _, _ ->
                kicked = true
                Result.success(Unit)
            },
            banUserResult = { _, _ ->
                banned = true
                Result.success(Unit)
            },
            unBanUserResult = { _, _ ->
                unbanned = true
                Result.success(Unit)
            },
            inviteUserResult = { _ ->
                invited = true
                Result.success(Unit)
            },
            setTopicResult = { _ ->
                topicSet = true
                Result.success(Unit)
            },
            setNameResult = { _ ->
                nameSet = true
                Result.success(Unit)
            },
            baseRoom = FakeBaseRoom(
                leaveRoomLambda = {
                    left = true
                    Result.success(Unit)
                },
            )
        )
        val matrixClient = FakeMatrixClient(
            ignoreUserResult = { _ ->
                ignored = true
                Result.success(Unit)
            },
            unIgnoreUserResult = { _ ->
                unignored = true
                Result.success(Unit)
            },
        ).apply {
            joinRoomByIdOrAliasLambda = { _, _ ->
                joined = true
                Result.success(null)
            }
        }
        val sut = createCommandExecutor(
            matrixClient = matrixClient,
            joinedRoom = joinedRoom,
        )
        val kickRes = sut.proceedAdmin(SlashCommand.RemoveUser(A_USER_ID, null))
        assertThat(kicked).isTrue()
        assertThat(kickRes.isSuccess).isTrue()
        val banRes = sut.proceedAdmin(SlashCommand.BanUser(A_USER_ID, "reason"))
        assertThat(banned).isTrue()
        assertThat(banRes.isSuccess).isTrue()
        val unbanRes = sut.proceedAdmin(SlashCommand.UnbanUser(A_USER_ID, null))
        assertThat(unbanned).isTrue()
        assertThat(unbanRes.isSuccess).isTrue()
        val inviteRes = sut.proceedAdmin(SlashCommand.Invite(A_USER_ID, null))
        assertThat(invited).isTrue()
        assertThat(inviteRes.isSuccess).isTrue()
        val ignoreRes = sut.proceedAdmin(SlashCommand.IgnoreUser(A_USER_ID))
        assertThat(ignoreRes.isSuccess).isTrue()
        assertThat(ignored).isTrue()
        val unignoreRes = sut.proceedAdmin(SlashCommand.UnignoreUser(A_USER_ID))
        assertThat(unignoreRes.isSuccess).isTrue()
        assertThat(unignored).isTrue()
        val leaveRes = sut.proceedAdmin(SlashCommand.LeaveRoom)
        assertThat(leaveRes.isSuccess).isTrue()
        assertThat(left).isTrue()
        val topicRes = sut.proceedAdmin(SlashCommand.ChangeTopic("t"))
        assertThat(topicRes.isSuccess).isTrue()
        assertThat(topicSet).isTrue()
        val nameRes = sut.proceedAdmin(SlashCommand.ChangeRoomName("n"))
        assertThat(nameRes.isSuccess).isTrue()
        assertThat(nameSet).isTrue()
        val joinRes = sut.proceedAdmin(
            SlashCommand.JoinRoom(
                roomIdOrAlias = RoomIdOrAlias.Id(
                    RoomId("!room:domain")
                ),
                reason = null,
            )
        )
        assertThat(joinRes.isSuccess).isTrue()
        assertThat(joined).isTrue()
    }
}

fun createCommandExecutor(
    matrixClient: FakeMatrixClient = FakeMatrixClient(),
    joinedRoom: FakeJoinedRoom = FakeJoinedRoom(),
    rainbowGenerator: RainbowGenerator = RainbowGenerator(),
    stringProvider: StringProvider = FakeStringProvider(),
) = CommandExecutor(
    matrixClient = matrixClient,
    joinedRoom = joinedRoom,
    rainbowGenerator = rainbowGenerator,
    stringProvider = stringProvider,
)
