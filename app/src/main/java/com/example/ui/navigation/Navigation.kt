package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object BookList : Screen("book_list")
    object UnitList : Screen("unit_list/{bookNumber}") {
        fun createRoute(bookNumber: Int) = "unit_list/$bookNumber"
    }
    object WordList : Screen("word_list/{bookNumber}/{unitNumber}") {
        fun createRoute(bookNumber: Int, unitNumber: Int) = "word_list/$bookNumber/$unitNumber"
    }
    object Quiz : Screen("quiz/{bookNumber}/{unitNumber}") {
        fun createRoute(bookNumber: Int, unitNumber: Int) = "quiz/$bookNumber/$unitNumber"
    }
    object Search : Screen("search")
    object SynonymsAntonyms : Screen("synonyms_antonyms")
    object Profile : Screen("profile")
    object Dashboard : Screen("dashboard")
    object Saved : Screen("saved")
    object Settings : Screen("settings")
    object VocabularyPractice : Screen("practice/{bookNumber}/{unitNumber}") {
        fun createRoute(bookNumber: Int, unitNumber: Int) = "practice/$bookNumber/$unitNumber"
    }
    object WordDetail : Screen("word_detail/{wordId}") {
        fun createRoute(wordId: Long) = "word_detail/$wordId"
    }
}
