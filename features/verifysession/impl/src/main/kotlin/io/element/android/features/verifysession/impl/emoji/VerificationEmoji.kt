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

import androidx.annotation.DrawableRes
import io.element.android.features.verifysession.impl.R

@DrawableRes
internal fun Int.toEmojiDrawableRes(): Int {
    return when (this % 64) {
        0 -> R.drawable.ic_verification_dog
        1 -> R.drawable.ic_verification_cat
        2 -> R.drawable.ic_verification_lion
        3 -> R.drawable.ic_verification_horse
        4 -> R.drawable.ic_verification_unicorn
        5 -> R.drawable.ic_verification_pig
        6 -> R.drawable.ic_verification_elephant
        7 -> R.drawable.ic_verification_rabbit
        8 -> R.drawable.ic_verification_panda
        9 -> R.drawable.ic_verification_rooster
        10 -> R.drawable.ic_verification_penguin
        11 -> R.drawable.ic_verification_turtle
        12 -> R.drawable.ic_verification_fish
        13 -> R.drawable.ic_verification_octopus
        14 -> R.drawable.ic_verification_butterfly
        15 -> R.drawable.ic_verification_flower
        16 -> R.drawable.ic_verification_tree
        17 -> R.drawable.ic_verification_cactus
        18 -> R.drawable.ic_verification_mushroom
        19 -> R.drawable.ic_verification_globe
        20 -> R.drawable.ic_verification_moon
        21 -> R.drawable.ic_verification_cloud
        22 -> R.drawable.ic_verification_fire
        23 -> R.drawable.ic_verification_banana
        24 -> R.drawable.ic_verification_apple
        25 -> R.drawable.ic_verification_strawberry
        26 -> R.drawable.ic_verification_corn
        27 -> R.drawable.ic_verification_pizza
        28 -> R.drawable.ic_verification_cake
        29 -> R.drawable.ic_verification_heart
        30 -> R.drawable.ic_verification_smiley
        31 -> R.drawable.ic_verification_robot
        32 -> R.drawable.ic_verification_hat
        33 -> R.drawable.ic_verification_glasses
        34 -> R.drawable.ic_verification_spanner
        35 -> R.drawable.ic_verification_santa
        36 -> R.drawable.ic_verification_thumbs_up
        37 -> R.drawable.ic_verification_umbrella
        38 -> R.drawable.ic_verification_hourglass
        39 -> R.drawable.ic_verification_clock
        40 -> R.drawable.ic_verification_gift
        41 -> R.drawable.ic_verification_light_bulb
        42 -> R.drawable.ic_verification_book
        43 -> R.drawable.ic_verification_pencil
        44 -> R.drawable.ic_verification_paperclip
        45 -> R.drawable.ic_verification_scissors
        46 -> R.drawable.ic_verification_lock
        47 -> R.drawable.ic_verification_key
        48 -> R.drawable.ic_verification_hammer
        49 -> R.drawable.ic_verification_phone
        50 -> R.drawable.ic_verification_flag
        51 -> R.drawable.ic_verification_train
        52 -> R.drawable.ic_verification_bicycle
        53 -> R.drawable.ic_verification_aeroplane
        54 -> R.drawable.ic_verification_rocket
        55 -> R.drawable.ic_verification_trophy
        56 -> R.drawable.ic_verification_ball
        57 -> R.drawable.ic_verification_guitar
        58 -> R.drawable.ic_verification_trumpet
        59 -> R.drawable.ic_verification_bell
        60 -> R.drawable.ic_verification_anchor
        61 -> R.drawable.ic_verification_headphones
        62 -> R.drawable.ic_verification_folder
        /* 63 */ else -> R.drawable.ic_verification_pin
    }
}
