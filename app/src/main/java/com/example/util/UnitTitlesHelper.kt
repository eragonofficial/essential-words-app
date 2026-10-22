package com.example.util

object UnitTitlesHelper {
    private val book1Titles = listOf(
        "The Lion and the Mouse", "The Laboratory", "The Report", "The Dog's Bell", "The Renowned Hunter",
        "The Friendly Ghost", "The Prince and the Farm Girl", "The Clever Monkey", "The Greedy Bear", "The Brave Guard",
        "The Forest Journey", "An Artist's Dream", "The Golden Harvest", "The Young Poet", "The Great Race",
        "The Ancient Secret", "The Mountain Stream", "The Village Market", "The Voyage Home", "The Soldier's Victory",
        "The Kind Nurse", "The Lost Treasure", "The Marathon Runner", "The Town Festival", "The Herbalist",
        "The Wisdom Tree", "The Desert Oasis", "The Big Concert", "The Student Election", "The Wise Old Master"
    )

    private val book2Titles = listOf(
        "The Dragon's Egg", "The Castle in the North", "The Royal Archer", "The Marathon of Hope", "The Legend of the Mountain",
        "The Royal Wedding", "The Island Volcano", "The Hidden Valley", "The King's Decree", "The Giant of the Forest",
        "The Star Gazer", "The Village Baker", "The High Peak Expedition", "The Solar Observatory", "The Organic Harvest",
        "The Cold Chamber", "The Modern Carriage", "The Monastery Scroll", "The Bird's Nest", "The New Settlement",
        "The Lunar Station", "The Dog Show", "The Master Inventor", "The Island Lighthouse", "The Ancient Tablet",
        "The River Crossing", "The Silk Trader", "The Olympic Challenge", "The Coral Reef", "The Glacier Quest"
    )

    private val book3Titles = listOf(
        "The Knight's Honor", "The Mysterious Cave", "The City Architecture", "The Arctic Explorer", "The Symphony of Nature",
        "The Deep Sea Vessel", "The Celestial Map", "The Royal Banquet", "The Forest Guardian", "The Ancient Library",
        "The Desert Caravan", "The Grand Bazaar", "The Mountain Sanctuary", "The River Navigation", "The Solar Eclipse",
        "The Falcon's Flight", "The Island Fortress", "The Windmill Valley", "The Master Painter", "The Herbal Remedy",
        "The Secret Tunnel", "The Crystal Cavern", "The Harvest Moon", "The Royal Academy", "The Clockmaker's Tower",
        "The Whispering Woods", "The Golden Compass", "The Emerald Valley", "The Lighthouse Keeper", "The King's Legacy"
    )

    private val book4Titles = listOf(
        "The Frontier Settlement", "The Great Canyon", "The Sky Observatory", "The Ancient Citadel", "The Mountain Glacier",
        "The Lost Colony", "The Master Carpenter", "The Solar Flare", "The Ocean Current", "The Desert Bloom",
        "The Forest Canopy", "The Steam Engine", "The Polar Expedition", "The Royal Treasury", "The Deep Cavern",
        "The Island Archipelago", "The Highland Clan", "The Star Constellation", "The River Delta", "The Ancient Monolith",
        "The Silk Road", "The Volcanic Caldera", "The Rainforest Mystery", "The Grand Exhibition", "The Iron Foundry",
        "The Arctic Tundra", "The Coral Atoll", "The Highland Fortress", "The Midnight Sun", "The Final Odyssey"
    )

    fun getTitle(book: Int, unit: Int): String {
        val list = when (book) {
            1 -> book1Titles
            2 -> book2Titles
            3 -> book3Titles
            4 -> book4Titles
            else -> book1Titles
        }
        val index = (unit - 1).coerceIn(0, list.lastIndex)
        return list[index]
    }
}
