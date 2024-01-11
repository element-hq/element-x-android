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

package io.element.android.libraries.core.log.logger

/**
 * Parent class for custom logger tags. Can be used with Timber :
 *
 * val loggerTag = LoggerTag("MyTag", LoggerTag.VOIP)
 * Timber.tag(loggerTag.value).v("My log message")
 */
open class LoggerTag(name: String, parentTag: LoggerTag? = null) {
    object PushLoggerTag : LoggerTag("Push")
    object NotificationLoggerTag : LoggerTag("Notification", PushLoggerTag)

    val value: String = if (parentTag == null) {
        name
    } else {
        "${parentTag.value}/$name"
    }
}
