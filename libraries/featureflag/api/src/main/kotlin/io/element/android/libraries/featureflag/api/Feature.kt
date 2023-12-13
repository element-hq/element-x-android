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

package io.element.android.libraries.featureflag.api

interface Feature {
    /**
     * Unique key to identify the feature.
     */
    val key: String

    /**
     * Title to show in the UI. Not needed to be translated as it's only dev accessible.
     */
    val title: String

    /**
     * Optional description to give more context on the feature.
     */
    val description: String?

    /**
     * The default value of the feature (enabled or disabled).
     */
    val defaultValue: Boolean

    /**
     * Whether the feature is finished or not.
     * If false: the feature is still in development, it will appear in the developer options screen to be able to enable it and test it.
     * If true: the feature is finished, it will not appear in the developer options screen.
     */
    val isFinished: Boolean
}
