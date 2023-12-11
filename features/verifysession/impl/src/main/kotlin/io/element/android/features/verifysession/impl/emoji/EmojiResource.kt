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
import androidx.annotation.StringRes
import io.element.android.features.verifysession.impl.R

internal data class EmojiResource(
    @DrawableRes val drawableRes: Int,
    @StringRes val nameRes: Int
)

internal fun Int.toEmojiResource(): EmojiResource {
    return when (this % 64) {
        0 -> EmojiResource(R.drawable.ic_verification_dog, R.string.verification_emoji_dog)
        1 -> EmojiResource(R.drawable.ic_verification_cat, R.string.verification_emoji_cat)
        2 -> EmojiResource(R.drawable.ic_verification_lion, R.string.verification_emoji_lion)
        3 -> EmojiResource(R.drawable.ic_verification_horse, R.string.verification_emoji_horse)
        4 -> EmojiResource(R.drawable.ic_verification_unicorn, R.string.verification_emoji_unicorn)
        5 -> EmojiResource(R.drawable.ic_verification_pig, R.string.verification_emoji_pig)
        6 -> EmojiResource(R.drawable.ic_verification_elephant, R.string.verification_emoji_elephant)
        7 -> EmojiResource(R.drawable.ic_verification_rabbit, R.string.verification_emoji_rabbit)
        8 -> EmojiResource(R.drawable.ic_verification_panda, R.string.verification_emoji_panda)
        9 -> EmojiResource(R.drawable.ic_verification_rooster, R.string.verification_emoji_rooster)
        10 -> EmojiResource(R.drawable.ic_verification_penguin, R.string.verification_emoji_penguin)
        11 -> EmojiResource(R.drawable.ic_verification_turtle, R.string.verification_emoji_turtle)
        12 -> EmojiResource(R.drawable.ic_verification_fish, R.string.verification_emoji_fish)
        13 -> EmojiResource(R.drawable.ic_verification_octopus, R.string.verification_emoji_octopus)
        14 -> EmojiResource(R.drawable.ic_verification_butterfly, R.string.verification_emoji_butterfly)
        15 -> EmojiResource(R.drawable.ic_verification_flower, R.string.verification_emoji_flower)
        16 -> EmojiResource(R.drawable.ic_verification_tree, R.string.verification_emoji_tree)
        17 -> EmojiResource(R.drawable.ic_verification_cactus, R.string.verification_emoji_cactus)
        18 -> EmojiResource(R.drawable.ic_verification_mushroom, R.string.verification_emoji_mushroom)
        19 -> EmojiResource(R.drawable.ic_verification_globe, R.string.verification_emoji_globe)
        20 -> EmojiResource(R.drawable.ic_verification_moon, R.string.verification_emoji_moon)
        21 -> EmojiResource(R.drawable.ic_verification_cloud, R.string.verification_emoji_cloud)
        22 -> EmojiResource(R.drawable.ic_verification_fire, R.string.verification_emoji_fire)
        23 -> EmojiResource(R.drawable.ic_verification_banana, R.string.verification_emoji_banana)
        24 -> EmojiResource(R.drawable.ic_verification_apple, R.string.verification_emoji_apple)
        25 -> EmojiResource(R.drawable.ic_verification_strawberry, R.string.verification_emoji_strawberry)
        26 -> EmojiResource(R.drawable.ic_verification_corn, R.string.verification_emoji_corn)
        27 -> EmojiResource(R.drawable.ic_verification_pizza, R.string.verification_emoji_pizza)
        28 -> EmojiResource(R.drawable.ic_verification_cake, R.string.verification_emoji_cake)
        29 -> EmojiResource(R.drawable.ic_verification_heart, R.string.verification_emoji_heart)
        30 -> EmojiResource(R.drawable.ic_verification_smiley, R.string.verification_emoji_smiley)
        31 -> EmojiResource(R.drawable.ic_verification_robot, R.string.verification_emoji_robot)
        32 -> EmojiResource(R.drawable.ic_verification_hat, R.string.verification_emoji_hat)
        33 -> EmojiResource(R.drawable.ic_verification_glasses, R.string.verification_emoji_glasses)
        34 -> EmojiResource(R.drawable.ic_verification_spanner, R.string.verification_emoji_spanner)
        35 -> EmojiResource(R.drawable.ic_verification_santa, R.string.verification_emoji_santa)
        36 -> EmojiResource(R.drawable.ic_verification_thumbs_up, R.string.verification_emoji_thumbs_up)
        37 -> EmojiResource(R.drawable.ic_verification_umbrella, R.string.verification_emoji_umbrella)
        38 -> EmojiResource(R.drawable.ic_verification_hourglass, R.string.verification_emoji_hourglass)
        39 -> EmojiResource(R.drawable.ic_verification_clock, R.string.verification_emoji_clock)
        40 -> EmojiResource(R.drawable.ic_verification_gift, R.string.verification_emoji_gift)
        41 -> EmojiResource(R.drawable.ic_verification_light_bulb, R.string.verification_emoji_light_bulb)
        42 -> EmojiResource(R.drawable.ic_verification_book, R.string.verification_emoji_book)
        43 -> EmojiResource(R.drawable.ic_verification_pencil, R.string.verification_emoji_pencil)
        44 -> EmojiResource(R.drawable.ic_verification_paperclip, R.string.verification_emoji_paperclip)
        45 -> EmojiResource(R.drawable.ic_verification_scissors, R.string.verification_emoji_scissors)
        46 -> EmojiResource(R.drawable.ic_verification_lock, R.string.verification_emoji_lock)
        47 -> EmojiResource(R.drawable.ic_verification_key, R.string.verification_emoji_key)
        48 -> EmojiResource(R.drawable.ic_verification_hammer, R.string.verification_emoji_hammer)
        49 -> EmojiResource(R.drawable.ic_verification_phone, R.string.verification_emoji_telephone)
        50 -> EmojiResource(R.drawable.ic_verification_flag, R.string.verification_emoji_flag)
        51 -> EmojiResource(R.drawable.ic_verification_train, R.string.verification_emoji_train)
        52 -> EmojiResource(R.drawable.ic_verification_bicycle, R.string.verification_emoji_bicycle)
        53 -> EmojiResource(R.drawable.ic_verification_aeroplane, R.string.verification_emoji_aeroplane)
        54 -> EmojiResource(R.drawable.ic_verification_rocket, R.string.verification_emoji_rocket)
        55 -> EmojiResource(R.drawable.ic_verification_trophy, R.string.verification_emoji_trophy)
        56 -> EmojiResource(R.drawable.ic_verification_ball, R.string.verification_emoji_ball)
        57 -> EmojiResource(R.drawable.ic_verification_guitar, R.string.verification_emoji_guitar)
        58 -> EmojiResource(R.drawable.ic_verification_trumpet, R.string.verification_emoji_trumpet)
        59 -> EmojiResource(R.drawable.ic_verification_bell, R.string.verification_emoji_bell)
        60 -> EmojiResource(R.drawable.ic_verification_anchor, R.string.verification_emoji_anchor)
        61 -> EmojiResource(R.drawable.ic_verification_headphones, R.string.verification_emoji_headphones)
        62 -> EmojiResource(R.drawable.ic_verification_folder, R.string.verification_emoji_folder)
        /* 63 */ else -> EmojiResource(R.drawable.ic_verification_pin, R.string.verification_emoji_pin)
    }
}
