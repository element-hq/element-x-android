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
package io.element.android.features.verifysession.impl.emoji

import androidx.annotation.StringRes
import io.element.android.features.verifysession.impl.R

@StringRes
internal fun Int.toEmojiStringRes(): Int {
    return when (this % 64) {
        0 -> R.string.verification_emoji_dog
        1 -> R.string.verification_emoji_cat
        2 -> R.string.verification_emoji_lion
        3 -> R.string.verification_emoji_horse
        4 -> R.string.verification_emoji_unicorn
        5 -> R.string.verification_emoji_pig
        6 -> R.string.verification_emoji_elephant
        7 -> R.string.verification_emoji_rabbit
        8 -> R.string.verification_emoji_panda
        9 -> R.string.verification_emoji_rooster
        10 -> R.string.verification_emoji_penguin
        11 -> R.string.verification_emoji_turtle
        12 -> R.string.verification_emoji_fish
        13 -> R.string.verification_emoji_octopus
        14 -> R.string.verification_emoji_butterfly
        15 -> R.string.verification_emoji_flower
        16 -> R.string.verification_emoji_tree
        17 -> R.string.verification_emoji_cactus
        18 -> R.string.verification_emoji_mushroom
        19 -> R.string.verification_emoji_globe
        20 -> R.string.verification_emoji_moon
        21 -> R.string.verification_emoji_cloud
        22 -> R.string.verification_emoji_fire
        23 -> R.string.verification_emoji_banana
        24 -> R.string.verification_emoji_apple
        25 -> R.string.verification_emoji_strawberry
        26 -> R.string.verification_emoji_corn
        27 -> R.string.verification_emoji_pizza
        28 -> R.string.verification_emoji_cake
        29 -> R.string.verification_emoji_heart
        30 -> R.string.verification_emoji_smiley
        31 -> R.string.verification_emoji_robot
        32 -> R.string.verification_emoji_hat
        33 -> R.string.verification_emoji_glasses
        34 -> R.string.verification_emoji_spanner
        35 -> R.string.verification_emoji_santa
        36 -> R.string.verification_emoji_thumbs_up
        37 -> R.string.verification_emoji_umbrella
        38 -> R.string.verification_emoji_hourglass
        39 -> R.string.verification_emoji_clock
        40 -> R.string.verification_emoji_gift
        41 -> R.string.verification_emoji_light_bulb
        42 -> R.string.verification_emoji_book
        43 -> R.string.verification_emoji_pencil
        44 -> R.string.verification_emoji_paperclip
        45 -> R.string.verification_emoji_scissors
        46 -> R.string.verification_emoji_lock
        47 -> R.string.verification_emoji_key
        48 -> R.string.verification_emoji_hammer
        49 -> R.string.verification_emoji_telephone
        50 -> R.string.verification_emoji_flag
        51 -> R.string.verification_emoji_train
        52 -> R.string.verification_emoji_bicycle
        53 -> R.string.verification_emoji_aeroplane
        54 -> R.string.verification_emoji_rocket
        55 -> R.string.verification_emoji_trophy
        56 -> R.string.verification_emoji_ball
        57 -> R.string.verification_emoji_guitar
        58 -> R.string.verification_emoji_trumpet
        59 -> R.string.verification_emoji_bell
        60 -> R.string.verification_emoji_anchor
        61 -> R.string.verification_emoji_headphones
        62 -> R.string.verification_emoji_folder
        /* 63 */ else -> R.string.verification_emoji_pin
    }
}
