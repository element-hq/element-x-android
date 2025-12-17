/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveRoomMemberIdentityStateChangeTest {
    private val aliceRoomMember = aRoomMember(A_USER_ID, displayName = "Alice")
    private val bobRoomMember = aRoomMember(A_USER_ID_2, displayName = "Bob")
    private val carolRoomMember = aRoomMember(A_USER_ID_3, displayName = "Carol")

    @Test
    fun `roomMemberIdentityStateChange emits empty list for non-encrypted room with no identity changes`() =
        runTest {
            val identityStateChangesFlow = MutableStateFlow<List<IdentityStateChange>>(emptyList())

            val joinedRoom = FakeJoinedRoom(
                identityStateChangesFlow = identityStateChangesFlow,
                baseRoom = FakeJoinedRoom().baseRoom.apply {
                    givenRoomInfo(aRoomInfo(isEncrypted = false))
                    givenRoomMembersState(
                        RoomMembersState.Ready(
                            persistentListOf(
                                aliceRoomMember,
                                bobRoomMember
                            )
                        )
                    )
                }
            )

            joinedRoom.roomMemberIdentityStateChange(waitForEncryption = false).test {
                val result = awaitItem()
                assertThat(result).isEmpty()
            }
        }

    @Test
    fun `roomMemberIdentityStateChange emits identity changes for non-encrypted room when waitForEncryption is false`() =
        runTest {
            val identityStateChangesFlow = MutableStateFlow(
                listOf(
                    IdentityStateChange(bobRoomMember.userId, IdentityState.Verified),
                    IdentityStateChange(carolRoomMember.userId, IdentityState.PinViolation)
                )
            )

            val joinedRoom = FakeJoinedRoom(
                identityStateChangesFlow = identityStateChangesFlow,
                baseRoom = FakeJoinedRoom().baseRoom.apply {
                    givenRoomInfo(aRoomInfo(isEncrypted = false))
                    givenRoomMembersState(
                        RoomMembersState.Ready(
                            persistentListOf(
                                aliceRoomMember,
                                bobRoomMember,
                                carolRoomMember
                            )
                        )
                    )
                }
            )

            joinedRoom.roomMemberIdentityStateChange(waitForEncryption = false).test {
                val result = awaitItem()
                assertThat(result).hasSize(2)

                val bobChange = result.find { it.identityRoomMember.userId == bobRoomMember.userId }
                assertThat(bobChange).isNotNull()
                assertThat(bobChange?.identityState).isEqualTo(IdentityState.Verified)
                assertThat(bobChange?.identityRoomMember?.displayNameOrDefault).isEqualTo("Bob")

                val carolChange = result.find { it.identityRoomMember.userId == carolRoomMember.userId }
                assertThat(carolChange).isNotNull()
                assertThat(carolChange?.identityState).isEqualTo(IdentityState.PinViolation)
                assertThat(carolChange?.identityRoomMember?.displayNameOrDefault).isEqualTo("Carol")
            }
        }

    @Test
    fun `roomMemberIdentityStateChange emits identity changes for already encrypted room`() =
        runTest {
            val identityStateChangesFlow = MutableStateFlow(
                listOf(
                    IdentityStateChange(bobRoomMember.userId, IdentityState.VerificationViolation)
                )
            )

            val joinedRoom = FakeJoinedRoom(
                identityStateChangesFlow = identityStateChangesFlow,
                baseRoom = FakeJoinedRoom().baseRoom.apply {
                    givenRoomInfo(aRoomInfo(isEncrypted = true))
                    givenRoomMembersState(
                        RoomMembersState.Ready(
                            persistentListOf(
                                aliceRoomMember,
                                bobRoomMember
                            )
                        )
                    )
                }
            )

            joinedRoom.roomMemberIdentityStateChange(waitForEncryption = true).test {
                val result = awaitItem()
                assertThat(result).hasSize(1)

                val bobChange = result.first()
                assertThat(bobChange.identityRoomMember.userId).isEqualTo(bobRoomMember.userId)
                assertThat(bobChange.identityState).isEqualTo(IdentityState.VerificationViolation)
                assertThat(bobChange.identityRoomMember.displayNameOrDefault).isEqualTo("Bob")
            }
        }

    @Test
    fun `roomMemberIdentityStateChange waits for encryption before emitting when waitForEncryption is true`() =
        runTest {
            val identityStateChangesFlow = MutableStateFlow(
                listOf(IdentityStateChange(bobRoomMember.userId, IdentityState.Pinned))
            )
            val joinedRoom = FakeJoinedRoom(
                identityStateChangesFlow = identityStateChangesFlow,
                baseRoom = FakeJoinedRoom().baseRoom.apply {
                    givenRoomInfo(aRoomInfo(isEncrypted = false))
                    givenRoomMembersState(
                        RoomMembersState.Ready(
                            persistentListOf(
                                aliceRoomMember,
                                bobRoomMember
                            )
                        )
                    )
                }
            )

            joinedRoom.roomMemberIdentityStateChange(waitForEncryption = true).test {
                // Should not emit anything yet since room is not encrypted
                expectNoEvents()

                // Enable encryption
                joinedRoom.baseRoom.givenRoomInfo(aRoomInfo(isEncrypted = true))

                val result = awaitItem()
                assertThat(result).hasSize(1)
                assertThat(result.first().identityRoomMember.userId).isEqualTo(bobRoomMember.userId)
                assertThat(result.first().identityState).isEqualTo(IdentityState.Pinned)
            }
        }

    @Test
    fun `roomMemberIdentityStateChange creates default member when room member not found`() =
        runTest {
            val identityStateChangesFlow = MutableStateFlow(
                listOf(IdentityStateChange(carolRoomMember.userId, IdentityState.PinViolation))
            )

            val joinedRoom = FakeJoinedRoom(
                identityStateChangesFlow = identityStateChangesFlow,
                baseRoom = FakeJoinedRoom().baseRoom.apply {
                    givenRoomInfo(aRoomInfo(isEncrypted = true))
                    // Only include aliceRoomMember and bobRoomMember, not carolRoomMember
                    givenRoomMembersState(
                        RoomMembersState.Ready(
                            persistentListOf(
                                aliceRoomMember,
                                bobRoomMember
                            )
                        )
                    )
                }
            )

            joinedRoom.roomMemberIdentityStateChange(waitForEncryption = false).test {
                val result = awaitItem()
                assertThat(result).hasSize(1)

                val carolChange = result.first()
                assertThat(carolChange.identityRoomMember.userId).isEqualTo(carolRoomMember.userId)
                assertThat(carolChange.identityState).isEqualTo(IdentityState.PinViolation)
                // Should use extracted display name from user ID since member not found
                assertThat(carolChange.identityRoomMember.displayNameOrDefault).isEqualTo(
                    carolRoomMember.userId.extractedDisplayName
                )
            }
        }

    @Test
    fun `roomMemberIdentityStateChange updates when identity state changes`() = runTest {
        val identityStateChangesFlow = MutableStateFlow(
            listOf(IdentityStateChange(bobRoomMember.userId, IdentityState.Pinned))
        )

        val joinedRoom = FakeJoinedRoom(
            identityStateChangesFlow = identityStateChangesFlow,
            baseRoom = FakeJoinedRoom().baseRoom.apply {
                givenRoomInfo(aRoomInfo(isEncrypted = true))
                givenRoomMembersState(
                    RoomMembersState.Ready(
                        persistentListOf(
                            aliceRoomMember,
                            bobRoomMember
                        )
                    )
                )
            }
        )

        joinedRoom.roomMemberIdentityStateChange(waitForEncryption = false).test {
            val firstResult = awaitItem()
            assertThat(firstResult).hasSize(1)
            assertThat(firstResult.first().identityState).isEqualTo(IdentityState.Pinned)

            // Update identity state
            identityStateChangesFlow.value = listOf(
                IdentityStateChange(bobRoomMember.userId, IdentityState.VerificationViolation)
            )

            val secondResult = awaitItem()
            assertThat(secondResult).hasSize(1)
            assertThat(secondResult.first().identityState).isEqualTo(IdentityState.VerificationViolation)
        }
    }

    @Test
    fun `roomMemberIdentityStateChange updates when members state changes`() = runTest {
        val identityStateChangesFlow = MutableStateFlow(
            listOf(IdentityStateChange(bobRoomMember.userId, IdentityState.Verified))
        )

        val joinedRoom = FakeJoinedRoom(
            identityStateChangesFlow = identityStateChangesFlow,
            baseRoom = FakeJoinedRoom().baseRoom.apply {
                givenRoomInfo(aRoomInfo(isEncrypted = true))
                givenRoomMembersState(
                    RoomMembersState.Ready(
                        persistentListOf(
                            aliceRoomMember,
                            bobRoomMember
                        )
                    )
                )
            }
        )

        joinedRoom.roomMemberIdentityStateChange(waitForEncryption = false).test {
            val firstResult = awaitItem()
            assertThat(firstResult).hasSize(1)
            assertThat(firstResult.first().identityRoomMember.displayNameOrDefault).isEqualTo("Bob")

            // Update room member with different display name
            val updatedMember2 = bobRoomMember.copy(displayName = "Bobby")
            joinedRoom.baseRoom.givenRoomMembersState(
                RoomMembersState.Ready(persistentListOf(aliceRoomMember, updatedMember2))
            )

            val secondResult = awaitItem()
            assertThat(secondResult).hasSize(1)
            assertThat(secondResult.first().identityRoomMember.displayNameOrDefault).isEqualTo("Bobby")
        }
    }

    @Test
    fun `roomMemberIdentityStateChange handles multiple identity states`() = runTest {
        val identityStateChangesFlow = MutableStateFlow(
            listOf(
                IdentityStateChange(aliceRoomMember.userId, IdentityState.Verified),
                IdentityStateChange(bobRoomMember.userId, IdentityState.PinViolation),
                IdentityStateChange(carolRoomMember.userId, IdentityState.VerificationViolation)
            )
        )

        val joinedRoom = FakeJoinedRoom(
            identityStateChangesFlow = identityStateChangesFlow,
            baseRoom = FakeJoinedRoom().baseRoom.apply {
                givenRoomInfo(aRoomInfo(isEncrypted = true))
                givenRoomMembersState(
                    RoomMembersState.Ready(
                        persistentListOf(
                            aliceRoomMember,
                            bobRoomMember,
                            carolRoomMember
                        )
                    )
                )
            }
        )

        joinedRoom.roomMemberIdentityStateChange(waitForEncryption = false).test {
            val result = awaitItem()
            assertThat(result).hasSize(3)

            val verifiedUser = result.find { it.identityState == IdentityState.Verified }
            assertThat(verifiedUser?.identityRoomMember?.userId).isEqualTo(aliceRoomMember.userId)

            val pinViolationUser = result.find { it.identityState == IdentityState.PinViolation }
            assertThat(pinViolationUser?.identityRoomMember?.userId).isEqualTo(bobRoomMember.userId)

            val verificationViolationUser =
                result.find { it.identityState == IdentityState.VerificationViolation }
            assertThat(verificationViolationUser?.identityRoomMember?.userId).isEqualTo(carolRoomMember.userId)
        }
    }

    @Test
    fun `roomMemberIdentityStateChange handles room becoming encrypted scenario`() = runTest {
        val identityStateChangesFlow = MutableStateFlow(
            listOf(IdentityStateChange(bobRoomMember.userId, IdentityState.Pinned))
        )

        val joinedRoom = FakeJoinedRoom(
            identityStateChangesFlow = identityStateChangesFlow,
            baseRoom = FakeJoinedRoom().baseRoom.apply {
                givenRoomInfo(aRoomInfo(isEncrypted = false))
                givenRoomMembersState(
                    RoomMembersState.Ready(
                        persistentListOf(
                            aliceRoomMember,
                            bobRoomMember
                        )
                    )
                )
            }
        )

        joinedRoom.roomMemberIdentityStateChange(waitForEncryption = true).test {
            // Should not emit anything initially as room is not encrypted
            expectNoEvents()

            // Room becomes encrypted
            joinedRoom.baseRoom.givenRoomInfo(aRoomInfo(isEncrypted = true))

            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result.first().identityRoomMember.userId).isEqualTo(bobRoomMember.userId)
            assertThat(result.first().identityState).isEqualTo(IdentityState.Pinned)

            // Add more identity changes after encryption is enabled
            identityStateChangesFlow.value = listOf(
                IdentityStateChange(bobRoomMember.userId, IdentityState.Pinned),
                IdentityStateChange(aliceRoomMember.userId, IdentityState.VerificationViolation)
            )

            val updatedResult = awaitItem()
            assertThat(updatedResult).hasSize(2)
        }
    }

    @Test
    fun `roomMemberIdentityStateChange does not emit duplicates for same state`() = runTest {
        val identityStateChangesFlow = MutableSharedFlow<List<IdentityStateChange>>()
        val identityStateChanges = listOf(
            IdentityStateChange(bobRoomMember.userId, IdentityState.Verified)
        )

        val joinedRoom = FakeJoinedRoom(
            identityStateChangesFlow = identityStateChangesFlow,
            baseRoom = FakeJoinedRoom().baseRoom.apply {
                givenRoomInfo(aRoomInfo(isEncrypted = true))
                givenRoomMembersState(
                    RoomMembersState.Ready(
                        persistentListOf(
                            aliceRoomMember,
                            bobRoomMember
                        )
                    )
                )
            }
        )

        joinedRoom.roomMemberIdentityStateChange(waitForEncryption = false).test {
            identityStateChangesFlow.emit(identityStateChanges)

            val firstResult = awaitItem()
            assertThat(firstResult).hasSize(1)

            // Emit the same state again
            identityStateChangesFlow.emit(identityStateChanges)

            // Should not emit a new item due to distinctUntilChanged
            expectNoEvents()
        }
    }
}
