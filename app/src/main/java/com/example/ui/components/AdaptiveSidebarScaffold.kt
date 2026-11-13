package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassDrawerTint
import com.example.ui.theme.GlassScrimDark
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier1Fill
import com.example.ui.theme.GlassTier1Highlight
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import kotlinx.coroutines.launch

const val SIDEBAR_EXPANDED_BREAKPOINT_DP = 760f

enum class SidebarLayoutMode {
    COMPACT_AUTO_COLLAPSED,
    EXPANDED_PUSH
}

enum class SidebarDestination {
    HOME,
    ESSENTIAL_BOOKS,
    BOOK_SEARCH,
    SPACED_REPETITION,
    SYNONYMS,
    DASHBOARD,
    PROFILE
}

fun computeSidebarMode(availableWidthDp: Float): SidebarLayoutMode {
    return if (availableWidthDp >= SIDEBAR_EXPANDED_BREAKPOINT_DP) {
        SidebarLayoutMode.EXPANDED_PUSH
    } else {
        SidebarLayoutMode.COMPACT_AUTO_COLLAPSED
    }
}

fun defaultSidebarExpanded(availableWidthDp: Float): Boolean {
    return computeSidebarMode(availableWidthDp) == SidebarLayoutMode.EXPANDED_PUSH
}

@Composable
fun AdaptiveSidebarScaffold(
    activeDestination: SidebarDestination,
    dueCount: Int,
    learnedCount: Int,
    onNavigateHome: () -> Unit,
    onNavigateEssentialBooks: () -> Unit,
    onNavigateBookSearch: () -> Unit,
    onNavigateSpacedRepetition: () -> Unit,
    onNavigateSynonyms: () -> Unit,
    onNavigateDashboard: () -> Unit,
    onNavigateProfile: () -> Unit,
    onQuickBookSelect: ((Int) -> Unit)? = null,
    content: @Composable (
        onToggleSidebar: () -> Unit,
        isSidebarOpen: Boolean,
        isWideScreen: Boolean
    ) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidthDp = maxWidth.value
        val layoutMode = computeSidebarMode(availableWidthDp)
        val isWideScreen = layoutMode == SidebarLayoutMode.EXPANDED_PUSH

        var isExpandedSidebarOpen by rememberSaveable {
            mutableStateOf(defaultSidebarExpanded(availableWidthDp))
        }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        LaunchedEffect(isWideScreen) {
            isExpandedSidebarOpen = isWideScreen
            if (!isWideScreen && drawerState.isOpen) {
                drawerState.close()
            }
        }

        val closeCompactDrawer: () -> Unit = {
            if (!isWideScreen && drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
        }

        val toggleSidebar: () -> Unit = {
            if (isWideScreen) {
                isExpandedSidebarOpen = !isExpandedSidebarOpen
            } else {
                scope.launch {
                    if (drawerState.isOpen) drawerState.close() else drawerState.open()
                }
            }
        }

        val collapseSidebar: () -> Unit = {
            if (isWideScreen) {
                isExpandedSidebarOpen = false
            } else {
                scope.launch { drawerState.close() }
            }
        }

        val sidebarInnerContent: @Composable () -> Unit = {
            SidebarPanelContent(
                activeDestination = activeDestination,
                dueCount = dueCount,
                learnedCount = learnedCount,
                onCollapseClick = collapseSidebar,
                onSelectDestination = { dest ->
                    closeCompactDrawer()
                    when (dest) {
                        SidebarDestination.HOME -> onNavigateHome()
                        SidebarDestination.ESSENTIAL_BOOKS -> onNavigateEssentialBooks()
                        SidebarDestination.BOOK_SEARCH -> onNavigateBookSearch()
                        SidebarDestination.SPACED_REPETITION -> onNavigateSpacedRepetition()
                        SidebarDestination.SYNONYMS -> onNavigateSynonyms()
                        SidebarDestination.DASHBOARD -> onNavigateDashboard()
                        SidebarDestination.PROFILE -> onNavigateProfile()
                    }
                },
                onQuickBookSelect = onQuickBookSelect?.let { callback ->
                    { bookNum ->
                        closeCompactDrawer()
                        callback(bookNum)
                    }
                }
            )
        }

        if (isWideScreen) {
            // Flexible Row layout: Tier 1 frosted glass sidebar pushes main content cleanly
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isExpandedSidebarOpen,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .width(272.dp)
                                .fillMaxHeight()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            GlassTier1Fill.copy(alpha = 0.09f),
                                            GlassTier1Fill
                                        )
                                    )
                                )
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .testTag("responsive_sidebar_panel")
                        ) {
                            sidebarInnerContent()
                        }
                        VerticalDivider(
                            thickness = 1.dp,
                            color = GlassTier1Highlight.copy(alpha = 0.28f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    content(toggleSidebar, isExpandedSidebarOpen, true)
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = true,
                scrimColor = GlassScrimDark,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .width(292.dp)
                            .border(
                                width = 1.dp,
                                color = GlassTier1Border,
                                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                            )
                            .testTag("responsive_sidebar_panel"),
                        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                        drawerContainerColor = GlassDrawerTint,
                        drawerContentColor = TextPrimaryWhite,
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            GlassCyanFill.copy(alpha = 0.14f),
                                            Color.Transparent,
                                            GlassVioletFill.copy(alpha = 0.12f)
                                        )
                                    )
                                )
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            sidebarInnerContent()
                        }
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content(toggleSidebar, drawerState.isOpen, false)
                }
            }
        }
    }
}

@Composable
fun SidebarToggleIconButton(
    isSidebarOpen: Boolean,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggleClick,
        shape = RoundedCornerShape(12.dp),
        color = GlassTier3Fill,
        contentColor = TextPrimaryWhite,
        border = BorderStroke(1.dp, GlassTier3Border),
        modifier = modifier
            .padding(horizontal = 4.dp)
            .size(44.dp)
            .testTag("sidebar_toggle_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isSidebarOpen) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu,
                contentDescription = if (isSidebarOpen) "Collapse navigation sidebar" else "Open navigation sidebar",
                tint = TextPrimaryWhite
            )
        }
    }
}

@Composable
private fun SidebarPanelContent(
    activeDestination: SidebarDestination,
    dueCount: Int,
    learnedCount: Int,
    onCollapseClick: () -> Unit,
    onSelectDestination: (SidebarDestination) -> Unit,
    onQuickBookSelect: ((Int) -> Unit)?
) {
    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = GlassCyanFill,
        unselectedContainerColor = Color.Transparent,
        selectedIconColor = GlassCyanAccent,
        unselectedIconColor = TextBodyTranslucent,
        selectedTextColor = TextPrimaryWhite,
        unselectedTextColor = TextBodyTranslucent,
        selectedBadgeColor = GlassCyanAccent,
        unselectedBadgeColor = TextMetaTranslucent
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Sidebar Header with Collapse Arrow Glass Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "4000 Essential Words",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimaryWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$learnedCount / 3,600 mastered",
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassCyanAccent
                )
            }
            GlassPill(
                shape = RoundedCornerShape(12.dp),
                tintColor = GlassTier3Fill,
                borderColor = GlassTier3Border,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("sidebar_collapse_button"),
                onClick = onCollapseClick
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Hide sidebar",
                    tint = TextPrimaryWhite
                )
            }
        }

        GlassHairlineDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = GlassTier1Border
        )

        @Composable
        fun GlassNavItem(
            label: String,
            selected: Boolean,
            icon: androidx.compose.ui.graphics.vector.ImageVector,
            tag: String,
            badgeText: String? = null,
            onClick: () -> Unit
        ) {
            val borderMod = if (selected) {
                Modifier.border(1.dp, GlassCyanBorder, RoundedCornerShape(14.dp))
            } else {
                Modifier
            }
            NavigationDrawerItem(
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                badge = badgeText?.let { text ->
                    {
                        GlassPill(
                            tintColor = if (selected) GlassCyanFill else GlassTier3Fill,
                            borderColor = if (selected) GlassCyanBorder else GlassTier3Border,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                selected = selected,
                onClick = onClick,
                icon = { Icon(icon, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                colors = itemColors,
                modifier = borderMod.testTag(tag)
            )
        }

        GlassNavItem(
            label = "Home & Global Search",
            selected = activeDestination == SidebarDestination.HOME,
            icon = Icons.Default.Home,
            tag = "sidebar_item_home",
            onClick = { onSelectDestination(SidebarDestination.HOME) }
        )

        GlassNavItem(
            label = "Essential English Words All",
            selected = activeDestination == SidebarDestination.ESSENTIAL_BOOKS,
            icon = Icons.AutoMirrored.Filled.MenuBook,
            tag = "sidebar_item_essential_books",
            badgeText = "1–6",
            onClick = { onSelectDestination(SidebarDestination.ESSENTIAL_BOOKS) }
        )

        GlassNavItem(
            label = "Saved & Book Search",
            selected = activeDestination == SidebarDestination.BOOK_SEARCH,
            icon = Icons.Default.Bookmarks,
            tag = "sidebar_item_book_search",
            onClick = { onSelectDestination(SidebarDestination.BOOK_SEARCH) }
        )

        GlassHairlineDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = GlassTier1Border
        )

        GlassNavItem(
            label = "Spaced Repetition Review",
            selected = activeDestination == SidebarDestination.SPACED_REPETITION,
            icon = Icons.Default.Repeat,
            tag = "sidebar_item_spaced_repetition",
            badgeText = if (dueCount > 0) "$dueCount due" else null,
            onClick = { onSelectDestination(SidebarDestination.SPACED_REPETITION) }
        )

        GlassNavItem(
            label = "Progress Dashboard",
            selected = activeDestination == SidebarDestination.DASHBOARD,
            icon = Icons.Default.Leaderboard,
            tag = "sidebar_item_dashboard",
            onClick = { onSelectDestination(SidebarDestination.DASHBOARD) }
        )

        GlassNavItem(
            label = "Profile & Stats",
            selected = activeDestination == SidebarDestination.PROFILE,
            icon = Icons.Default.Person,
            tag = "sidebar_item_profile",
            onClick = { onSelectDestination(SidebarDestination.PROFILE) }
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
