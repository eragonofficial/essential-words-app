package com.example

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.data.dictionary.GeographicEntityInfo
import com.example.data.dictionary.GlobalDictionaryEntry
import com.example.data.dictionary.GlobalDictionaryRepository
import com.example.data.dictionary.SearchDirection
import com.example.data.dictionary.SearchResultTier
import com.example.ui.components.GlobalDictionaryEntryCard
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GlobalDictionaryRepositoryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tier 1 geographic lookup resolves countries capitals and cities bidirectionally`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GlobalDictionaryRepository(context)

        // ENG -> UZB: Country ("Uzbekistan", "Germany")
        val uzbResult = repo.search("Uzbekistan", SearchDirection.ENG_TO_UZB)
        assertTrue(uzbResult.entries.isNotEmpty())
        val uzbEntry = uzbResult.entries.first()
        assertEquals(SearchResultTier.TIER_1_GEOGRAPHIC, uzbEntry.tier)
        assertEquals(SearchDirection.ENG_TO_UZB, uzbEntry.direction)
        assertNotNull(uzbEntry.geoInfo)
        assertEquals("🇺🇿", uzbEntry.geoInfo?.flagEmoji)
        assertEquals("Uzbekistan", uzbEntry.geoInfo?.nameEn)
        assertEquals("Oʻzbekiston", uzbEntry.geoInfo?.nameUz)
        assertEquals("Tashkent", uzbEntry.geoInfo?.capitalEn)
        assertEquals("Central Asia", uzbEntry.geoInfo?.regionEn)

        val germanyResult = repo.search("Germany", SearchDirection.ENG_TO_UZB)
        assertEquals(SearchResultTier.TIER_1_GEOGRAPHIC, germanyResult.entries.first().tier)
        assertEquals("🇩🇪", germanyResult.entries.first().geoInfo?.flagEmoji)
        assertEquals("Germaniya", germanyResult.entries.first().geoInfo?.nameUz)
        assertEquals("Berlin", germanyResult.entries.first().geoInfo?.capitalEn)

        // ENG -> UZB: Capital City ("Paris")
        val parisResult = repo.search("Paris", SearchDirection.ENG_TO_UZB)
        val parisGeo = parisResult.entries.first().geoInfo
        assertEquals(SearchResultTier.TIER_1_GEOGRAPHIC, parisResult.entries.first().tier)
        assertNotNull(parisGeo)
        assertEquals("Capital City", parisGeo?.entityType)
        assertEquals("🇫🇷", parisGeo?.flagEmoji)
        assertEquals("Parij", parisGeo?.nameUz)
        assertEquals("France", parisGeo?.countryEn)

        // UZB -> ENG: Apostrophe variants for O'zbekiston + Cities
        listOf("Oʻzbekiston", "O'zbekiston", "Ozbekiston").forEach { uzVariant ->
            val reverseUzb = repo.search(uzVariant, SearchDirection.UZB_TO_ENG)
            assertTrue("Expected match for $uzVariant", reverseUzb.entries.isNotEmpty())
            val first = reverseUzb.entries.first()
            assertEquals(SearchResultTier.TIER_1_GEOGRAPHIC, first.tier)
            assertEquals(SearchDirection.UZB_TO_ENG, first.direction)
            assertEquals("Uzbekistan", first.headword)
            assertEquals("🇺🇿", first.geoInfo?.flagEmoji)
        }

        val toshkent = repo.search("Toshkent", SearchDirection.UZB_TO_ENG).entries.first()
        assertEquals(SearchResultTier.TIER_1_GEOGRAPHIC, toshkent.tier)
        assertEquals("Tashkent", toshkent.headword)
        assertEquals("Uzbekistan", toshkent.geoInfo?.countryEn)

        val samarqand = repo.search("Samarqand", SearchDirection.UZB_TO_ENG).entries.first()
        assertEquals(SearchResultTier.TIER_1_GEOGRAPHIC, samarqand.tier)
        assertEquals("Samarkand", samarqand.headword)
        assertEquals("Major City", samarqand.geoInfo?.entityType)

        val parij = repo.search("Parij", SearchDirection.UZB_TO_ENG).entries.first()
        assertEquals(SearchResultTier.TIER_1_GEOGRAPHIC, parij.tier)
        assertEquals("Paris", parij.headword)
        assertEquals("France", parij.geoInfo?.countryEn)
    }

    @Test
    fun `eng to uzb lookup resolves non-textbook vocabulary`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GlobalDictionaryRepository(context)

        val result = repo.search("serendipity", SearchDirection.ENG_TO_UZB)

        assertTrue("Expected at least one global entry", result.entries.isNotEmpty())
        val entry = result.entries.first()
        assertEquals(SearchResultTier.TIER_2_DICTIONARY_THESAURUS, entry.tier)
        assertEquals(SearchDirection.ENG_TO_UZB, entry.direction)
        assertEquals("serendipity", entry.headword.lowercase())
        assertTrue("Expected Uzbek meaning", entry.primaryUzbekTranslation.isNotBlank())
        assertTrue("Expected English definitions", entry.meanings.isNotEmpty())
        assertTrue("Expected synonyms", entry.synonyms.isNotEmpty())
        assertTrue("Expected antonyms", entry.antonyms.isNotEmpty())
    }

    @Test
    fun `uzb to eng lookup resolves English equivalents with metadata`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GlobalDictionaryRepository(context)

        val result = repo.search("muvaffaqiyat", SearchDirection.UZB_TO_ENG)

        assertTrue("Expected English equivalents for Uzbek query", result.entries.isNotEmpty())
        val headwords = result.entries.map { it.headword.lowercase() }
        assertTrue(
            "Expected 'success' or 'achievement' in $headwords",
            headwords.any { it == "success" || it == "achievement" }
        )
        val first = result.entries.first()
        assertEquals(SearchResultTier.TIER_2_DICTIONARY_THESAURUS, first.tier)
        assertEquals(SearchDirection.UZB_TO_ENG, first.direction)
        assertEquals("muvaffaqiyat", first.matchedFromUzbek)
        assertTrue("Expected English definitions", first.meanings.isNotEmpty())
        assertTrue("Expected synonyms", first.synonyms.isNotEmpty())
    }

    @Test
    fun `global recent search history is independent of Room`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GlobalDictionaryRepository(context)
        repo.clearRecentSearches()

        val afterAdd = repo.recordRecentSearch("algorithm", SearchDirection.ENG_TO_UZB)
        assertEquals(listOf("algorithm"), afterAdd)
        assertEquals(SearchDirection.ENG_TO_UZB, repo.getRecentSearchDirection("algorithm"))

        val afterSecond = repo.recordRecentSearch("kashfiyot", SearchDirection.UZB_TO_ENG)
        assertEquals(listOf("kashfiyot", "algorithm"), afterSecond)
        assertEquals(SearchDirection.UZB_TO_ENG, repo.getRecentSearchDirection("kashfiyot"))

        // Recording more than 5 items keeps only the last 5 successful lookups in MRU order
        repo.recordRecentSearch("serendipity", SearchDirection.ENG_TO_UZB)
        repo.recordRecentSearch("kitob", SearchDirection.UZB_TO_ENG)
        repo.recordRecentSearch("ephemeral", SearchDirection.ENG_TO_UZB)
        val afterSixth = repo.recordRecentSearch("muvaffaqiyat", SearchDirection.UZB_TO_ENG)
        assertEquals(
            listOf("muvaffaqiyat", "ephemeral", "kitob", "serendipity", "kashfiyot"),
            afterSixth
        )
        assertEquals(5, repo.getRecentSearches().size)

        val afterDelete = repo.deleteRecentSearch("kashfiyot")
        assertEquals(listOf("muvaffaqiyat", "ephemeral", "kitob", "serendipity"), afterDelete)

        val afterClear = repo.clearRecentSearches()
        assertFalse(afterClear.isNotEmpty())
    }

    @Test
    fun `uzb to eng dictionary card inverts heading to Uzbek term`() {
        val uzbToEngLexical = GlobalDictionaryEntry(
            headword = "success",
            matchedFromUzbek = "muvaffaqiyat",
            phonetic = "/səkˈses/",
            primaryUzbekTranslation = "muvaffaqiyat, omad",
            direction = SearchDirection.UZB_TO_ENG,
            tier = SearchResultTier.TIER_2_DICTIONARY_THESAURUS
        )

        composeTestRule.setContent {
            MaterialTheme {
                GlobalDictionaryEntryCard(
                    entry = uzbToEngLexical,
                    onPronounceClick = {},
                    onRelatedWordClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("global_entry_heading_success").assertTextEquals("muvaffaqiyat")
        composeTestRule.onNodeWithText("🇬🇧 English Equivalent").assertIsDisplayed()
        composeTestRule.onNodeWithText("success").assertIsDisplayed()
    }

    @Test
    fun `uzb to eng geographic card renders flag details and inverted heading`() {
        val geoUzbToEng = GlobalDictionaryEntry(
            headword = "Tashkent",
            matchedFromUzbek = "Toshkent",
            primaryUzbekTranslation = "Toshkent",
            direction = SearchDirection.UZB_TO_ENG,
            tier = SearchResultTier.TIER_1_GEOGRAPHIC,
            geoInfo = GeographicEntityInfo(
                entityType = "Capital City",
                flagEmoji = "🇺🇿",
                nameEn = "Tashkent",
                nameUz = "Toshkent",
                countryEn = "Uzbekistan",
                countryUz = "Oʻzbekiston",
                capitalEn = "Tashkent",
                capitalUz = "Toshkent",
                regionEn = "Central Asia",
                regionUz = "Markaziy Osiyo"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                GlobalDictionaryEntryCard(
                    entry = geoUzbToEng,
                    onPronounceClick = {},
                    onRelatedWordClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("global_entry_heading_Tashkent").assertTextEquals("Toshkent")
        composeTestRule.onNodeWithText("🇬🇧 Official English Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tashkent").assertIsDisplayed()
        composeTestRule.onNodeWithText("🇺🇿").assertIsDisplayed()
        composeTestRule.onNodeWithTag("geo_details_panel_Tashkent").assertIsDisplayed()
    }
}
