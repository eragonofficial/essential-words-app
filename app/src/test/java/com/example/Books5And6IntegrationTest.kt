package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.VocabularyRepository
import com.example.util.UnitTitlesHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Books5And6IntegrationTest {

    @Test
    fun `words_data json contains complete 3600 words across 6 books and 180 units`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val jsonText = context.assets.open("words_data.json").bufferedReader().use { it.readText() }
        val array = JSONArray(jsonText)

        assertEquals(3600, array.length())

        val countsByBookUnit = mutableMapOf<Pair<Int, Int>, Int>()
        val seenIds = HashSet<Long>(3600)

        val validPos = setOf("n.", "v.", "adj.", "adv.", "prep.", "conj.", "pron.", "interj.")

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.getLong("id")
            val book = obj.getInt("bookNumber")
            val unit = obj.getInt("unitNumber")
            val word = obj.getString("word")
            val phonetic = obj.getString("phonetic")
            val partOfSpeech = obj.getString("partOfSpeech")
            val defn = obj.getString("definition")
            val exEn = obj.getString("exampleSentence")
            val uzTr = obj.getString("uzbekTranslation")
            val uzDef = obj.getString("uzbekDefinition")
            val uzEx = obj.getString("exampleSentenceUz")

            assertTrue("Duplicate id $id", seenIds.add(id))
            assertTrue("Book out of range: $book", book in 1..6)
            assertTrue("Unit out of range: $unit", unit in 1..30)
            assertTrue("Blank word at id=$id", word.isNotBlank())
            assertTrue("Blank phonetic at id=$id", phonetic.startsWith("[") && phonetic.endsWith("]"))
            assertTrue("Invalid partOfSpeech '$partOfSpeech' at id=$id ($word)", partOfSpeech in validPos)
            assertTrue("Blank definition at id=$id", defn.isNotBlank())
            assertTrue("Blank exampleSentence at id=$id", exEn.isNotBlank())
            assertTrue("Blank uzbekTranslation at id=$id", uzTr.isNotBlank())
            assertTrue("Blank uzbekDefinition at id=$id", uzDef.isNotBlank())
            assertTrue("Blank exampleSentenceUz at id=$id", uzEx.isNotBlank())

            if (book in 5..6) {
                assertTrue("Placeholder in $word", !uzDef.lowercase().contains("tushunchasi"))
                assertTrue("Ellipsis placeholder in $word", !uzEx.contains("..."))
            }

            val key = book to unit
            countsByBookUnit[key] = (countsByBookUnit[key] ?: 0) + 1
        }

        for (book in 1..6) {
            for (unit in 1..30) {
                assertEquals(
                    "Book $book Unit $unit should have 20 words",
                    20,
                    countsByBookUnit[book to unit]
                )
            }
        }
    }

    @Test
    fun `unit titles helper provides distinct titles for Books 5 and 6`() {
        val book1First = UnitTitlesHelper.getTitle(1, 1)
        val book5First = UnitTitlesHelper.getTitle(5, 1)
        val book6First = UnitTitlesHelper.getTitle(6, 1)

        assertEquals("The Little Mice", book5First)
        assertEquals("The North Star", book6First)
        assertNotEquals(book1First, book5First)
        assertNotEquals(book1First, book6First)

        val b5Titles = (1..30).map { UnitTitlesHelper.getTitle(5, it) }.toSet()
        val b6Titles = (1..30).map { UnitTitlesHelper.getTitle(6, it) }.toSet()
        assertEquals(30, b5Titles.size)
        assertEquals(30, b6Titles.size)
        assertEquals("The Demon's Bridge", UnitTitlesHelper.getTitle(5, 30))
        assertEquals("The Mad Hatter", UnitTitlesHelper.getTitle(6, 30))
    }

    @Test
    fun `repository seeds all 6 books and 180 units cleanly and preserves existing progress`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = VocabularyRepository(context)

        repo.initializeDatabaseIfNeeded()

        val allWords = db.wordDao().getAllWordsList()
        val allUnits = db.unitProgressDao().getAllProgressList()

        assertEquals(3600, allWords.size)
        assertEquals(180, allUnits.size)
        assertEquals(600, db.wordDao().getWordsByBookList(5).size)
        assertEquals(600, db.wordDao().getWordsByBookList(6).size)
        assertEquals(30, allUnits.count { it.bookNumber == 5 })
        assertEquals(30, allUnits.count { it.bookNumber == 6 })

        // Verify every seeded Room entity has non-blank valid partOfSpeech
        val validPos = setOf("n.", "v.", "adj.", "adv.", "prep.", "conj.", "pron.", "interj.")
        assertTrue("All 3600 Room words should have valid partOfSpeech", allWords.all { it.partOfSpeech in validPos })
        assertEquals(0, db.wordDao().getWordsMissingPartOfSpeechCount())

        // Verify incremental re-seed preserves Book 1 learned state AND backfills blank partOfSpeech
        val firstBookWord = db.wordDao().getWordsByBookList(1).first()
        repo.updateWordLearned(firstBookWord.id, true)
        db.wordDao().updatePartOfSpeech(firstBookWord.id, "")
        assertEquals(1, db.wordDao().getWordsMissingPartOfSpeechCount())
        repo.initializeDatabaseIfNeeded()
        val reloaded = db.wordDao().getWordById(firstBookWord.id)
        assertTrue("Existing learned state should be preserved", reloaded?.isLearned == true)
        assertEquals("Blank partOfSpeech should be backfilled", firstBookWord.partOfSpeech, reloaded?.partOfSpeech)

        // Verify search finds Book 5 and Book 6 words with partOfSpeech
        val b5Match = repo.searchWords("allot").first()
        assertTrue(b5Match.any { it.bookNumber == 5 && it.unitNumber == 1 && it.word == "allot" && it.partOfSpeech == "v." })

        val b6Match = repo.searchWords("alleviate").first()
        assertTrue(b6Match.any { it.bookNumber == 6 && it.unitNumber == 1 && it.word == "alleviate" && it.partOfSpeech == "v." })
    }
}
