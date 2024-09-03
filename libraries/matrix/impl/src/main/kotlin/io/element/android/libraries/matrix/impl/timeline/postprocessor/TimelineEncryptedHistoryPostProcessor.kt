/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

internal val encryptedHistoryBannerId = UniqueId("EncryptedHistoryBannerId")

class TimelineEncryptedHistoryPostProcessor(
    private val dispatcher: CoroutineDispatcher,
    private val lastLoginTimestamp: Date?,
    private val isRoomEncrypted: Boolean,
    private val isKeyBackupEnabled: Boolean,
) {
    suspend fun process(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> = withContext(dispatcher) {
        Timber.d("Process on Thread=${Thread.currentThread()}")
        if (!isRoomEncrypted || isKeyBackupEnabled || lastLoginTimestamp == null) return@withContext items
        replaceWithEncryptionHistoryBannerIfNeeded(items)
    }

    private fun replaceWithEncryptionHistoryBannerIfNeeded(list: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        var lastEncryptedHistoryBannerIndex = -1
        for ((i, item) in list.withIndex()) {
            if (isItemEncryptionHistory(item)) {
                lastEncryptedHistoryBannerIndex = i
            }
        }
        return if (lastEncryptedHistoryBannerIndex >= 0) {
            val sublist = list.drop(lastEncryptedHistoryBannerIndex + 1).toMutableList()
            sublist.add(0, MatrixTimelineItem.Virtual(encryptedHistoryBannerId, VirtualTimelineItem.EncryptedHistoryBanner))
            sublist
        } else {
            list
        }
    }

    private fun isItemEncryptionHistory(item: MatrixTimelineItem): Boolean {
        if ((item as? MatrixTimelineItem.Virtual)?.virtual is VirtualTimelineItem.EncryptedHistoryBanner) {
            return true
        }
        val timestamp = (item as? MatrixTimelineItem.Event)?.event?.timestamp ?: return false
        return timestamp <= lastLoginTimestamp!!.time
    }
}
