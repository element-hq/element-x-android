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

package io.element.android.features.messages.impl.timeline.components

enum class TimestampPosition {
    /**
     * Timestamp should overlay the timeline event content (eg. image).
     */
    Overlay,

    /**
     * Timestamp should be aligned with the timeline event content if this is possible (eg. text).
     */
    Aligned,

    /**
     * Timestamp should always be rendered below the timeline event content (eg. poll).
     */
    Below;

    companion object {
        /**
         * Default timestamp position for timeline event contents.
         */
        val Default: TimestampPosition = Aligned
    }
}
