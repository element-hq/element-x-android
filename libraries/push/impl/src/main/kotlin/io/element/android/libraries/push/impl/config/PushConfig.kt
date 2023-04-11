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

package io.element.android.libraries.push.impl.config

object PushConfig {
    /**
     * It is the push gateway for FCM embedded distributor.
     * Note: pusher_http_url should have path '/_matrix/push/v1/notify' -->
     */
    const val pusher_http_url: String = "https://matrix.org/_matrix/push/v1/notify"

    /**
     * It is the push gateway for UnifiedPush.
     * Note: default_push_gateway_http_url should have path '/_matrix/push/v1/notify'
     */
    const val default_push_gateway_http_url: String = "https://matrix.gateway.unifiedpush.org/_matrix/push/v1/notify"

    /**
     * Note: pusher_app_id cannot exceed 64 chars.
     */
    const val pusher_app_id: String = "im.vector.app.android"

    /**
     * Set to true to allow external push distributor such as Ntfy.
     */
    const val allowExternalUnifiedPushDistributors: Boolean = false
}
