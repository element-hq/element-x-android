@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import io.element.android.x.designsystem.components.VectorButton

@Composable
fun OnBoardingScreen(
    viewModel: OnBoardingViewModel = mavericksViewModel(),
    onSignUp: () -> Unit = { },
    onSignIn: () -> Unit = { },
) {
    val state: OnBoardingViewState by viewModel.collectAsState()
    OnBoardingContent(
        state,
        onSignUp = onSignUp,
        onSignIn = onSignIn,
    )
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardingContent(
    state: OnBoardingViewState,
    onSignUp: () -> Unit,
    onSignIn: () -> Unit,
) {
    val carrouselState = remember { SplashCarouselStateFactory().create() }
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                val pagerState = rememberPagerState()
                // pagerState.scrollToPage(state.currentPage)
                HorizontalPager(
                    modifier = Modifier.weight(1f),
                    count = carrouselState.items.size,
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
                VectorButton(
                    text = "I ALREADY HAVE AN ACCOUNT",
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
) {
    Box {
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