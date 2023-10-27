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

package io.element.android.features.messages.impl.voicemessages

/**
 * Resizes the given [0;1024] int list as per unstable MSC3246 spec
 * to a [0;1] range float list to be used for waveform rendering.
 */
internal fun List<Int>.fromMSC3246range(): List<Float> = map { it / 1024f }

/**
 * Resizes the given [0;1] float list to [0;1024] int list as per unstable MSC3246 spec.
 */
internal fun List<Float>.toMSC3246range(): List<Int> = map { (it * 1024).toInt() }
