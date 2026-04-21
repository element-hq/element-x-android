/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.LiveLocationShare
import org.junit.Test

class LiveLocationShareComparatorTest {
    private val currentUser = UserId("@me:matrix.org")
    private val comparator = LiveLocationShareComparator(currentUser)

    @Test
    fun `compare returns zero when comparing the same current user share`() {
        val share = aLiveLocationShare(userId = currentUser, startTimestamp = 123L)

        val result = comparator.compare(share, share)

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `compare orders current user share before another user share`() {
        val otherShare = aLiveLocationShare(userId = UserId("@alice:matrix.org"), startTimestamp = 200L)
        val currentUserShare = aLiveLocationShare(userId = currentUser, startTimestamp = 100L)

        val sortedShares = listOf(otherShare, currentUserShare).sortedWith(comparator)

        assertThat(sortedShares).containsExactly(currentUserShare, otherShare).inOrder()
    }

    @Test
    fun `compare orders current user shares by newest start timestamp first`() {
        val newerShare = aLiveLocationShare(userId = currentUser, startTimestamp = 200L)
        val olderShare = aLiveLocationShare(userId = currentUser, startTimestamp = 100L)

        val sortedShares = listOf(olderShare, newerShare).sortedWith(comparator)

        assertThat(sortedShares).containsExactly(newerShare, olderShare).inOrder()
    }

    @Test
    fun `compare orders non current user shares by newest start timestamp first`() {
        val newerShare = aLiveLocationShare(userId = UserId("@alice:matrix.org"), startTimestamp = 200L)
        val olderShare = aLiveLocationShare(userId = UserId("@bob:matrix.org"), startTimestamp = 100L)

        val sortedShares = listOf(olderShare, newerShare).sortedWith(comparator)

        assertThat(sortedShares).containsExactly(newerShare, olderShare).inOrder()
    }
}

private fun aLiveLocationShare(
    userId: UserId,
    startTimestamp: Long,
): LiveLocationShare {
    return LiveLocationShare(
        userId = userId,
        lastLocation = null,
        startTimestamp = startTimestamp,
        endTimestamp = startTimestamp + 1_000L,
    )
}
