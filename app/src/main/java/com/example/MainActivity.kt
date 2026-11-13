package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.viewmodel.GlobalSearchViewModel
import com.example.viewmodel.VocabularyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private val viewModel: VocabularyViewModel by viewModels()
    private val globalSearchViewModel: GlobalSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.PaymentManager.initialize(this)

        setContent {
            com.example.ui.theme.MyApplicationTheme {
                com.example.ui.components.CosmicGlassBackdrop {
                    var showSplash by remember { mutableStateOf(true) }

                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350))
                        },
                        label = "splashTransition"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(onSplashFinished = { showSplash = false })
                        } else {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val navController = rememberNavController()
                                val currentUser = Firebase.auth.currentUser
                                val startDestination = if (currentUser != null) Screen.Home.route else Screen.Auth.route

                                NavHost(navController = navController, startDestination = startDestination) {
                                composable(Screen.Auth.route) {
                            AuthScreen(
                                viewModel = viewModel,
                                onSignedIn = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Auth.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                vocabularyViewModel = viewModel,
                                globalSearchViewModel = globalSearchViewModel,
                                onOpenEssentialBooksClick = {
                                    navController.navigate(Screen.BookList.route)
                                },
                                onBookClick = { bookNum ->
                                    navController.navigate(Screen.UnitList.createRoute(bookNum))
                                },
                                onBookSearchClick = {
                                    navController.navigate(Screen.BookSearch.route)
                                },
                                onSynonymsClick = {
                                    navController.navigate(Screen.SynonymsAntonyms.route)
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onDashboardClick = {
                                    navController.navigate("dashboard")
                                },
                                onSpacedRepetitionClick = {
                                    navController.navigate(Screen.SpacedRepetitionReview.route)
                                },
                                onWordClick = { wordId ->
                                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                                },
                                onSavedWordsClick = {
                                    navController.navigate("saved")
                                }
                            )
                        }

                        composable(Screen.BookList.route) {
                            BookListScreen(
                                viewModel = viewModel,
                                onBookClick = { bookNum ->
                                    navController.navigate(Screen.UnitList.createRoute(bookNum))
                                },
                                onUnitClick = { bookNum, unitNum ->
                                    navController.navigate(Screen.WordList.createRoute(bookNum, unitNum))
                                },
                                onWordClick = { wordId ->
                                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onSearchClick = {
                                    navController.navigate(Screen.Search.route)
                                },
                                onSynonymsClick = {
                                    navController.navigate(Screen.SynonymsAntonyms.route)
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onDashboardClick = {
                                    navController.navigate("dashboard")
                                },
                                onSpacedRepetitionClick = {
                                    navController.navigate(Screen.SpacedRepetitionReview.route)
                                },
                                onBookSearchClick = {
                                    navController.navigate(Screen.BookSearch.route)
                                }
                            )
                        }

                        composable(
                            route = Screen.UnitList.route,
                            arguments = listOf(navArgument("bookNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val bookNum = backStackEntry.arguments?.getInt("bookNumber") ?: 1
                            UnitListScreen(
                                bookNumber = bookNum,
                                viewModel = viewModel,
                                onUnitClick = { bNum, uNum ->
                                    navController.navigate(Screen.WordList.createRoute(bNum, uNum))
                                },
                                onQuizClick = { bNum, uNum ->
                                    navController.navigate(Screen.Quiz.createRoute(bNum, uNum))
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = Screen.WordList.route,
                            arguments = listOf(
                                navArgument("bookNumber") { type = NavType.IntType },
                                navArgument("unitNumber") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val bookNum = backStackEntry.arguments?.getInt("bookNumber") ?: 1
                            val unitNum = backStackEntry.arguments?.getInt("unitNumber") ?: 1
                            WordListScreen(
                                bookNumber = bookNum,
                                unitNumber = unitNum,
                                viewModel = viewModel,
                                onQuizClick = { bNum, uNum ->
                                    navController.navigate(Screen.Quiz.createRoute(bNum, uNum))
                                },
                                onPracticeClick = { bNum, uNum ->
                                    navController.navigate(Screen.VocabularyPractice.createRoute(bNum, uNum))
                                },
                                onWordClick = { wordId ->
                                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = Screen.Quiz.route,
                            arguments = listOf(
                                navArgument("bookNumber") { type = NavType.IntType },
                                navArgument("unitNumber") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val bookNum = backStackEntry.arguments?.getInt("bookNumber") ?: 1
                            val unitNum = backStackEntry.arguments?.getInt("unitNumber") ?: 1
                            QuizScreen(
                                bookNumber = bookNum,
                                unitNumber = unitNum,
                                viewModel = viewModel,
                                onQuizPassed = {
                                    navController.popBackStack()
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.Search.route) {
                            SearchScreen(
                                viewModel = globalSearchViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.BookSearch.route) {
                            BookSearchScreen(
                                viewModel = viewModel,
                                onWordClick = { wordId ->
                                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onUnitClick = { bookNum, unitNum ->
                                    navController.navigate(Screen.WordList.createRoute(bookNum, unitNum))
                                }
                            )
                        }

                        composable(Screen.SynonymsAntonyms.route) {
                            SynonymsAntonymsScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = viewModel,
                                onSignOut = {
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.SpacedRepetitionReview.route) {
                            SpacedRepetitionReviewScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Review.route) {
                            ReviewScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        
                        composable("saved") {
                            SavedWordsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
                        }
                        
                        composable("settings") {
                            SettingsScreen(onBackClick = { navController.popBackStack() })
                        }

                        composable(
                            route = Screen.VocabularyPractice.route,
                            arguments = listOf(
                                navArgument("bookNumber") { type = NavType.IntType },
                                navArgument("unitNumber") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val bookNum = backStackEntry.arguments?.getInt("bookNumber") ?: 1
                            val unitNum = backStackEntry.arguments?.getInt("unitNumber") ?: 1
                            VocabularyPracticeScreen(
                                bookNumber = bookNum,
                                unitNumber = unitNum,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.WordDetail.route,
                            arguments = listOf(
                                navArgument("wordId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val wordId = backStackEntry.arguments?.getLong("wordId") ?: 0L
                            WordDetailScreen(
                                wordId = wordId,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

