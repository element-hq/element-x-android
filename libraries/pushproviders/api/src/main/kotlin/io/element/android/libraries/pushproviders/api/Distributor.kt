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

package io.element.android.libraries.pushproviders.api

/**
 * Firebase does not have the concept of distributor. So for Firebase, there will be one distributor:
 * Distributor("Firebase", "Firebase").
 *
 * For UnifiedPush, for instance, the Distributor can be:
 * Distributor("io.heckel.ntfy", "ntfy").
 * But other values are possible.
 */
data class Distributor(
    val value: String,
    val name: String,
)
