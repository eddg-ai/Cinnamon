package com.sosauce.cinnamon.core.ui.components.searchbars

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.app.navigation.Screen
import com.sosauce.cinnamon.core.ui.barsContentTransform
import com.sosauce.cinnamon.core.ui.components.ScreenSelection
import com.sosauce.cinnamon.core.utils.SharedTransitionKeys
import com.sosauce.cinnamon.core.utils.selfAlignHorizontally
import com.sosauce.nekobites.animations.AnimatedFab

val MainTabs: List<Screen> = listOf(Screen.Conversations, Screen.Calls, Screen.Contacts)

@Composable
fun SharedTransitionScope.MainSearchbar(
    textFieldState: TextFieldState,
    sortingMenu: @Composable () -> Unit,
    fab: @Composable () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val navAnimatedScope = LocalNavAnimatedContentScope.current

    CuteSearchbar2(
        modifier = Modifier.selfAlignHorizontally(),
        barModifier = Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(SharedTransitionKeys.MAIN_SEARCHBAR),
            animatedVisibilityScope = navAnimatedScope
        ),
        fabRowModifier = Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(SharedTransitionKeys.MAIN_SEARCHBAR_FAB),
            animatedVisibilityScope = navAnimatedScope
        ),
        textFieldState = textFieldState,
        sortingMenu = sortingMenu,
        fab = fab,
        onNavigate = onNavigate
    )
}

@Composable
fun MainNavigationBar(
    isInSelectionMode: Boolean,
    selectedBar: @Composable () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    AnimatedContent(
        targetState = isInSelectionMode,
        transitionSpec = { barsContentTransform }
    ) { selecting ->
        if (selecting) {
            selectedBar()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedFab(
                        onClick = { onNavigate(Screen.Dialer()) },
                        icon = R.drawable.dialpad
                    )
                }
                ScreenSelection(
                    onNavigate = onNavigate,
                    dismiss = {}
                )
            }
        }
    }
}
