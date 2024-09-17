/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sdk

import io.element.android.libraries.matrix.impl.room.member.FakeRoomMembersIterator
import io.element.android.libraries.matrix.test.A_ROOM_ID
import org.matrix.rustcomponents.sdk.ComposerDraft
import org.matrix.rustcomponents.sdk.ImageInfo
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.Mentions
import org.matrix.rustcomponents.sdk.MessageLikeEventType
import org.matrix.rustcomponents.sdk.NotifyType
import org.matrix.rustcomponents.sdk.ReceiptType
import org.matrix.rustcomponents.sdk.RoomHero
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomInfoListener
import org.matrix.rustcomponents.sdk.RoomInterface
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomMembersIterator
import org.matrix.rustcomponents.sdk.RoomMessageEventContentWithoutRelation
import org.matrix.rustcomponents.sdk.RoomPowerLevels
import org.matrix.rustcomponents.sdk.RtcApplicationType
import org.matrix.rustcomponents.sdk.StateEventType
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.Timeline
import org.matrix.rustcomponents.sdk.TypingNotificationsListener
import org.matrix.rustcomponents.sdk.UserPowerLevelUpdate
import uniffi.matrix_sdk.RoomMemberRole
import uniffi.matrix_sdk.RoomPowerLevelChanges

class FakeRoomInterface(
    private val getMembers: () -> RoomMembersIterator = { FakeRoomMembersIterator() },
    private val getMembersNoSync: () -> RoomMembersIterator = { FakeRoomMembersIterator() },
) : RoomInterface {
    var membersCallCount = 0
    var membersNoSyncCallCount = 0
    override fun activeMembersCount(): ULong {
        TODO("Not yet implemented")
    }

    override fun activeRoomCallParticipants(): List<String> {
        TODO("Not yet implemented")
    }

    override fun alternativeAliases(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun applyPowerLevelChanges(changes: RoomPowerLevelChanges) {
        TODO("Not yet implemented")
    }

    override fun avatarUrl(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun banUser(userId: String, reason: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun canUserBan(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserInvite(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserKick(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserPinUnpin(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserRedactOther(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserRedactOwn(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserSendMessage(userId: String, message: MessageLikeEventType): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserSendState(userId: String, stateEvent: StateEventType): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun canUserTriggerRoomNotification(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun canonicalAlias(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun clearComposerDraft() {
        TODO("Not yet implemented")
    }

    override suspend fun discardRoomKey() {
        TODO("Not yet implemented")
    }

    override fun displayName(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun edit(eventId: String, newContent: RoomMessageEventContentWithoutRelation) {
        TODO("Not yet implemented")
    }

    override fun enableSendQueue(enable: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun getPowerLevels(): RoomPowerLevels {
        TODO("Not yet implemented")
    }

    override fun hasActiveRoomCall(): Boolean {
        TODO("Not yet implemented")
    }

    override fun heroes(): List<RoomHero> {
        TODO("Not yet implemented")
    }

    override fun id(): String {
        return A_ROOM_ID.value
    }

    override suspend fun ignoreDeviceTrustAndResend(devices: Map<String, List<String>>, transactionId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun ignoreUser(userId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun inviteUserById(userId: String) {
        TODO("Not yet implemented")
    }

    override fun invitedMembersCount(): ULong {
        TODO("Not yet implemented")
    }

    override suspend fun inviter(): RoomMember? {
        TODO("Not yet implemented")
    }

    override fun isDirect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEncrypted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPublic(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSendQueueEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSpace(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTombstoned(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun join() {
        TODO("Not yet implemented")
    }

    override fun joinedMembersCount(): ULong {
        TODO("Not yet implemented")
    }

    override suspend fun kickUser(userId: String, reason: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun leave() {
        TODO("Not yet implemented")
    }

    override suspend fun loadComposerDraft(): ComposerDraft? {
        TODO("Not yet implemented")
    }

    override suspend fun markAsRead(receiptType: ReceiptType) {
        TODO("Not yet implemented")
    }

    override suspend fun matrixToEventPermalink(eventId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun matrixToPermalink(): String {
        TODO("Not yet implemented")
    }

    override suspend fun member(userId: String): RoomMember {
        TODO("Not yet implemented")
    }

    override suspend fun memberAvatarUrl(userId: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun memberDisplayName(userId: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun members(): RoomMembersIterator {
        membersCallCount++
        return getMembers()
    }

    override suspend fun membersNoSync(): RoomMembersIterator {
        membersNoSyncCallCount++
        return getMembersNoSync()
    }

    override fun membership(): Membership {
        TODO("Not yet implemented")
    }

    override fun ownUserId(): String {
        TODO("Not yet implemented")
    }

    override suspend fun pinnedEventsTimeline(internalIdPrefix: String?, maxEventsToLoad: UShort, maxConcurrentRequests: UShort): Timeline {
        TODO("Not yet implemented")
    }

    override fun rawName(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun redact(eventId: String, reason: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun removeAvatar() {
        TODO("Not yet implemented")
    }

    override suspend fun reportContent(eventId: String, score: Int?, reason: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun resetPowerLevels(): RoomPowerLevels {
        TODO("Not yet implemented")
    }

    override suspend fun roomInfo(): RoomInfo {
        TODO("Not yet implemented")
    }

    override suspend fun saveComposerDraft(draft: ComposerDraft) {
        TODO("Not yet implemented")
    }

    override suspend fun sendCallNotification(callId: String, application: RtcApplicationType, notifyType: NotifyType, mentions: Mentions) {
        TODO("Not yet implemented")
    }

    override suspend fun sendCallNotificationIfNeeded() {
        TODO("Not yet implemented")
    }

    override suspend fun setIsFavourite(isFavourite: Boolean, tagOrder: Double?) {
        TODO("Not yet implemented")
    }

    override suspend fun setIsLowPriority(isLowPriority: Boolean, tagOrder: Double?) {
        TODO("Not yet implemented")
    }

    override suspend fun setName(name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setTopic(topic: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setUnreadFlag(newValue: Boolean) {
        TODO("Not yet implemented")
    }

    override fun subscribeToRoomInfoUpdates(listener: RoomInfoListener): TaskHandle {
        TODO("Not yet implemented")
    }

    override fun subscribeToTypingNotifications(listener: TypingNotificationsListener): TaskHandle {
        TODO("Not yet implemented")
    }

    override suspend fun suggestedRoleForUser(userId: String): RoomMemberRole {
        TODO("Not yet implemented")
    }

    override suspend fun timeline(): Timeline {
        TODO("Not yet implemented")
    }

    override suspend fun timelineFocusedOnEvent(eventId: String, numContextEvents: UShort, internalIdPrefix: String?): Timeline {
        TODO("Not yet implemented")
    }

    override fun topic(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun tryResend(transactionId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun typingNotice(isTyping: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun unbanUser(userId: String, reason: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePowerLevelsForUsers(updates: List<UserPowerLevelUpdate>) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadAvatar(mimeType: String, data: ByteArray, mediaInfo: ImageInfo?) {
        TODO("Not yet implemented")
    }

    override suspend fun withdrawVerificationAndResend(userIds: List<String>, transactionId: String) {
        TODO("Not yet implemented")
    }
}
