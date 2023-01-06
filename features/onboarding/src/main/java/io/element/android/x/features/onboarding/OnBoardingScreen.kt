@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import io.element.android.x.designsystem.components.VectorButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    onPageChanged: (Int) -> Unit = {},
    onSignUp: () -> Unit = {},
    onSignIn: () -> Unit = {},
) {
    val carrouselState = remember { SplashCarouselStateFactory().create() }
    val nbOfPages = carrouselState.items.size
    var key by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
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
                    OnBoardingPage(carrouselState.items[page])
                }
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(16.dp),
                )
                /*
                VectorButton(
                    text = "CREATE ACCOUNT",
                    onClick = {
                        onSignUp()
                    },
                    enabled = true,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(top = 16.dp)
                )
                 */
                VectorButton(
                    text = "Sign in",
                    onClick = {
                        onSignIn()
                    },
                    enabled = true,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun OnBoardingPage(
    item: SplashCarouselState.Item,
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
                fontSize = 24.sp,
            )
            Text(
                text = stringResource(id = item.body),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally),
                textAlign = TextAlign.Center,
            )
        }
    }
}
