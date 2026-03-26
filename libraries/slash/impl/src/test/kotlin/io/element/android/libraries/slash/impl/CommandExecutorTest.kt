/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slash.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.slash.api.SlashCommand
import io.element.android.libraries.slash.impl.rainbow.RainbowGenerator
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
        var asEmote = false
        timeline.sendMessageLambda = { _, _, _, emote, _ ->
            asEmote = emote
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendEmote("yay"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(asEmote).isTrue()
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
        val res = sut.proceedSendMessage(SlashCommand.SendLenny("fun"), timeline)
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
        val res = sut.proceedSendMessage(SlashCommand.SendTableFlip("wow"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("(╯°□°）╯︵ ┻━┻ wow")
    }

    @Test
    fun `send rainbow provides html body`() = runTest {
        val timeline = FakeTimeline()
        var capturedHtml: String? = null
        var capturedBody: String? = null
        timeline.sendMessageLambda = { body, htmlBody, _, _, _ ->
            capturedBody = body
            capturedHtml = htmlBody
            Result.success(Unit)
        }
        val sut = createCommandExecutor()
        val res = sut.proceedSendMessage(SlashCommand.SendRainbow("party"), timeline)
        assertThat(res.isSuccess).isTrue()
        assertThat(capturedBody).isEqualTo("party")
        assertThat(capturedHtml).isNotNull()
        assertThat(capturedHtml!!.contains("<font") || capturedHtml!!.contains("<span")).isTrue()
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
