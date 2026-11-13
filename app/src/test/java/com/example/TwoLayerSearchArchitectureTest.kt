package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.VocabularyRepository
import com.example.ui.components.mergeThesaurusTerms
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TwoLayerSearchArchitectureTest {

    @Test
    fun `local book search is bidirectional across English and Uzbek and strictly scoped to 3600 textbook words`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = VocabularyRepository(context)
        repo.initializeDatabaseIfNeeded()

        // English textbook lookup
        val allotMatches = repo.searchWords("allot").first()
        assertTrue(allotMatches.any { it.word == "allot" && it.bookNumber == 5 && it.unitNumber == 1 })

        // Uzbek textbook lookup
        val uzbAllotMatches = repo.searchWords("ajratmoq").first()
        assertTrue(uzbAllotMatches.any { it.word == "allot" })

        // Apostrophe-insensitive Uzbek lookup ("qo'rqqan" -> "afraid")
        val afraidMatches = repo.searchWords("qo'rqqan").first()
        assertTrue("Expected 'afraid' for Uzbek query qo'rqqan", afraidMatches.any { it.word == "afraid" })

        // Non-textbook words must NOT appear in Local Book Search
        val nonBookEnglish = repo.searchWords("serendipity").first()
        assertTrue("Non-textbook word 'serendipity' must not be returned by Local Book Search", nonBookEnglish.isEmpty())
    }

    @Test
    fun `local book search language modes scope queries and handle Uzbek apostrophe variations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = VocabularyRepository(context)
        repo.initializeDatabaseIfNeeded()

        // ALL mode matches both English and Uzbek
        val allEn = repo.searchWords("allot", VocabularyRepository.LocalBookSearchLanguage.ALL).first()
        val allUz = repo.searchWords("ajratmoq", VocabularyRepository.LocalBookSearchLanguage.ALL).first()
        assertTrue(allEn.any { it.word == "allot" })
        assertTrue(allUz.any { it.word == "allot" })

        // ENGLISH mode matches English word/definition only, NOT Uzbek-only query
        val enOnlyMatch = repo.searchWords("allot", VocabularyRepository.LocalBookSearchLanguage.ENGLISH).first()
        val enOnlyNoUz = repo.searchWords("ajratmoq", VocabularyRepository.LocalBookSearchLanguage.ENGLISH).first()
        assertTrue(enOnlyMatch.any { it.word == "allot" })
        assertTrue(enOnlyNoUz.isEmpty())

        // UZBEK mode matches Uzbek translations/meanings with apostrophe variations, NOT English-only headword
        val uzVariant1 = repo.searchWords("qo'rqqan", VocabularyRepository.LocalBookSearchLanguage.UZBEK).first()
        val uzVariant2 = repo.searchWords("qo‘rqqan", VocabularyRepository.LocalBookSearchLanguage.UZBEK).first()
        val uzVariant3 = repo.searchWords("qoʻrqqan", VocabularyRepository.LocalBookSearchLanguage.UZBEK).first()
        val uzVariantNoApos = repo.searchWords("qorqqan", VocabularyRepository.LocalBookSearchLanguage.UZBEK).first()
        val uzOnlyNoEn = repo.searchWords("allot", VocabularyRepository.LocalBookSearchLanguage.UZBEK).first()

        assertTrue(uzVariant1.any { it.word == "afraid" })
        assertTrue(uzVariant2.any { it.word == "afraid" })
        assertTrue(uzVariant3.any { it.word == "afraid" })
        assertTrue(uzVariantNoApos.any { it.word == "afraid" })
        assertTrue(uzOnlyNoEn.isEmpty())
    }

    @Test
    fun `mergeThesaurusTerms combines local and dynamic thesaurus without duplicates or headword`() {
        val merged = mergeThesaurusTerms(
            localCsv = "scared, fearful, Afraid",
            dynamicList = listOf("FEARFUL", "terrified", "anxious", "afraid"),
            headword = "afraid"
        )

        assertEquals(listOf("scared", "fearful", "terrified", "anxious"), merged)
        assertFalse(merged.any { it.equals("afraid", ignoreCase = true) })
    }
}
