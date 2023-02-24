/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    onPageChanged: (Int) -> Unit = {},
    onSignUp: () -> Unit = {},
    onSignIn: () -> Unit = {},
) {
    val carrouselData = remember { SplashCarouselDataFactory().create() }
    val nbOfPages = carrouselData.items.size
    var key by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val pagerState = rememberPagerState()
            LaunchedEffect(key) {
                launch {
                    delay(3_000)
                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % nbOfPages)
                    // https://stackoverflow.com/questions/73714228/accompanist-pager-animatescrolltopage-doesnt-scroll-to-next-page-correctly
                    key = !key
                }
            }
            LaunchedEffect(pagerState) {
                // Collect from the pager state a snapshotFlow reading the currentPage
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    onPageChanged(page)
                }
            }
            HorizontalPager(
                modifier = Modifier.weight(1f),
                count = nbOfPages,
                state = pagerState,
            ) { page ->
                // Our page content
                OnBoardingPage(carrouselData.items[page])
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(16.dp),
            )
            Button(
                onClick = {
                    onSignIn()
                },
                enabled = true,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .testTag(TestTags.onBoardingSignIn)
                    .padding(top = 16.dp)
            ) {
                Text(text = stringResource(id = StringR.string.login_splash_submit))
            }
        }
    }
}

@Composable
fun OnBoardingPage(
    item: SplashCarouselData.Item,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        /*
        Image(
            painterResource(id = item.pageBackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
         */
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp)
        ) {
            Image(
                painterResource(id = item.image),
                contentDescription = null,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .size(192.dp)
                    .padding(16.dp)
            )
            Text(
                text = stringResource(id = item.title),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally)
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
            )
            Text(
                text = stringResource(id = item.body),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview
@Composable
internal fun OnBoardingScreenLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun OnBoardingScreenDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    OnBoardingScreen()
}
