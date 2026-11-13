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

    private val book5Titles = listOf(
        "The Little Mice", "The Helpful Abbey", "The Bachelor's Lesson", "The Corrupt Administrator", "A Famous Accident",
        "The Island", "Small World", "Becoming a Healer", "The Weaving Machine", "Life on the Farm",
        "Beethoven's Gift", "Brothers", "The Old Hound", "Day Without Sight", "The Big Ship",
        "The History of Parachutes", "\"I Didn't Do It!\"", "The Soldier's Decision", "Jane's Pride", "Microchips",
        "The Twins", "The New Bioco", "How Comet Got His Tail", "The Resourceful Landlord", "The Man and the Monkey",
        "Cosmo's Flight", "The First Organ Transplant", "The Lottery", "Jen's New Job", "The Demon's Bridge"
    )

    private val book6Titles = listOf(
        "The North Star", "The Fossil Hunters", "Dressed to Excess", "The Butler's Bad Day", "A Bet",
        "Amazing Komodo Dragons", "Greek Magical Papyri", "Watch Out!", "Dangerous Bites", "The Avalanche",
        "The Lydian King", "The Butler", "The End of Smallpox", "The Coward's Lesson", "Epidemic in Zimbabwe",
        "The Brute and the Billionaire", "The Tenacious Inventor", "The Nurse's Lesson", "Seizures Then and Now", "The Greedy Bee",
        "The Mayor of Sherman", "The Editor's Choice", "The Ice House", "Preparing for the Future", "Hundred Plays",
        "The Kidnapping", "The Earl of Shining", "The Lord and the Farmers", "The Shortcut", "The Mad Hatter"
    )

    fun getTitle(book: Int, unit: Int): String {
        val list = when (book) {
            1 -> book1Titles
            2 -> book2Titles
            3 -> book3Titles
            4 -> book4Titles
            5 -> book5Titles
            6 -> book6Titles
            else -> book1Titles
        }
        val index = (unit - 1).coerceIn(0, list.lastIndex)
        return list[index]
    }
}
