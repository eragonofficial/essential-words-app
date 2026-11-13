package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.ui.components.AdaptiveSidebarScaffold
import com.example.ui.components.SidebarDestination
import com.example.ui.components.SidebarLayoutMode
import com.example.ui.components.SidebarToggleIconButton
import com.example.ui.components.computeSidebarMode
import com.example.ui.components.defaultSidebarExpanded
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ResponsiveSidebarLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `sidebar breakpoint helper auto-collapses on compact and split screens`() {
        assertEquals(SidebarLayoutMode.COMPACT_AUTO_COLLAPSED, computeSidebarMode(360f))
        assertEquals(SidebarLayoutMode.COMPACT_AUTO_COLLAPSED, computeSidebarMode(540f))
        assertEquals(SidebarLayoutMode.EXPANDED_PUSH, computeSidebarMode(760f))
        assertEquals(SidebarLayoutMode.EXPANDED_PUSH, computeSidebarMode(1024f))

        assertFalse(defaultSidebarExpanded(360f))
        assertFalse(defaultSidebarExpanded(600f))
        assertTrue(defaultSidebarExpanded(840f))
    }

    @Test
    @Config(qualifiers = "w1024dp-h800dp")
    fun `wide screen sidebar pushes main content without overlap and toggles smoothly`() {
        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AdaptiveSidebarScaffold(
                        activeDestination = SidebarDestination.HOME,
                        dueCount = 2,
                        learnedCount = 120,
                        onNavigateHome = {},
                        onNavigateEssentialBooks = {},
                        onNavigateBookSearch = {},
                        onNavigateSpacedRepetition = {},
                        onNavigateSynonyms = {},
                        onNavigateDashboard = {},
                        onNavigateProfile = {}
                    ) { onToggleSidebar, isSidebarOpen, _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("main_content_area")
                        ) {
                            SidebarToggleIconButton(
                                isSidebarOpen = isSidebarOpen,
                                onToggleClick = onToggleSidebar
                            )
                            Text("Main Content Grid")
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("responsive_sidebar_panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("main_content_area").assertIsDisplayed()

        val sidebarBounds = composeTestRule.onNodeWithTag("responsive_sidebar_panel").getBoundsInRoot()
        val mainBoundsBefore = composeTestRule.onNodeWithTag("main_content_area").getBoundsInRoot()
        assertTrue(
            "Main content (${mainBoundsBefore.left}) must start at/after sidebar right (${sidebarBounds.right})",
            mainBoundsBefore.left.value >= sidebarBounds.right.value - 2f
        )

        // Collapse via sidebar collapse arrow button
        composeTestRule.onNodeWithTag("sidebar_collapse_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("responsive_sidebar_panel").assertDoesNotExist()

        val mainBoundsAfter = composeTestRule.onNodeWithTag("main_content_area").getBoundsInRoot()
        assertTrue(
            "Main content should expand cleanly to left edge when collapsed (${mainBoundsAfter.left})",
            mainBoundsAfter.left.value <= 2f
        )

        // Re-open via navbar toggle button
        composeTestRule.onNodeWithTag("sidebar_toggle_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("responsive_sidebar_panel").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w412dp-h800dp")
    fun `compact screen auto-collapses sidebar by default and opens on toggle`() {
        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AdaptiveSidebarScaffold(
                        activeDestination = SidebarDestination.ESSENTIAL_BOOKS,
                        dueCount = 0,
                        learnedCount = 50,
                        onNavigateHome = {},
                        onNavigateEssentialBooks = {},
                        onNavigateBookSearch = {},
                        onNavigateSpacedRepetition = {},
                        onNavigateSynonyms = {},
                        onNavigateDashboard = {},
                        onNavigateProfile = {}
                    ) { onToggleSidebar, isSidebarOpen, _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("main_content_area")
                        ) {
                            SidebarToggleIconButton(
                                isSidebarOpen = isSidebarOpen,
                                onToggleClick = onToggleSidebar
                            )
                            Text("Book Words")
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        val mainBounds = composeTestRule.onNodeWithTag("main_content_area").getBoundsInRoot()
        assertTrue(
            "Main content must be completely unobstructed at x=0 on compact screen (${mainBounds.left})",
            mainBounds.left.value <= 2f
        )

        // Open drawer using visible hamburger toggle button
        composeTestRule.onNodeWithTag("sidebar_toggle_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sidebar_collapse_button").assertIsDisplayed()
    }
}
