package com.harsh.fittrack.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.harsh.fittrack.navigation.NavTab
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.nav_exercises
import com.harsh.fittrack.resources.nav_home
import com.harsh.fittrack.resources.nav_profile
import com.harsh.fittrack.resources.nav_record
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import org.jetbrains.compose.resources.stringResource

@Composable
fun FitBottomNav(
    currentTab: NavTab,
    onSelectTab: (NavTab) -> Unit,
) {
    NavigationBar(
        containerColor = SurfaceContainerHigh,
        contentColor = FitTrackTheme.colors.onSurfaceVariant,
    ) {
        val tabs = listOf(
            NavTab.Home to stringResource(Res.string.nav_home),
            NavTab.Record to stringResource(Res.string.nav_record),
            NavTab.Exercises to stringResource(Res.string.nav_exercises),
            NavTab.Profile to stringResource(Res.string.nav_profile),
        )

        tabs.forEach { (tab, label) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    // Placeholder — replace with Icon(painter = painterResource(...)) when assets land
                    NavIconPlaceholder(
                        tab = tab,
                        tint = if (selected) FitTrackTheme.colors.primary
                               else FitTrackTheme.colors.onSurfaceVariant,
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = FitTrackTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitTrackTheme.colors.primary,
                    selectedTextColor = FitTrackTheme.colors.primary,
                    unselectedIconColor = FitTrackTheme.colors.onSurfaceVariant,
                    unselectedTextColor = FitTrackTheme.colors.onSurfaceVariant,
                    indicatorColor = FitTrackTheme.colors.primary.copy(alpha = 0.12f),
                ),
            )
        }
    }
}

/**
 * Geometric placeholder shapes, one per tab.
 * Replace each Box with Icon(painter = painterResource(Res.drawable.ic_*)) when assets are ready.
 */
@Composable
private fun NavIconPlaceholder(tab: NavTab, tint: Color) {
    val size = 24.dp
    when (tab) {
        NavTab.Home -> Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Box(Modifier.size(18.dp, 14.dp).background(tint, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp, bottomStart = 3.dp, bottomEnd = 3.dp)))
        }
        NavTab.Record -> Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Box(Modifier.size(18.dp).background(tint.copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center) {
                Box(Modifier.size(8.dp).background(tint, CircleShape))
            }
        }
        NavTab.Exercises -> Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Box(Modifier.size(18.dp, 5.dp).background(tint, RoundedCornerShape(2.dp)))
        }
        NavTab.Profile -> Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Box(Modifier.size(12.dp).background(tint, CircleShape))
        }
    }
}
