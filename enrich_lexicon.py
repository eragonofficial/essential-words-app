import json

with open('app/src/main/assets/words_data.json', 'r', encoding='utf-8') as f:
    words = json.load(f)

lexicon = {
    "adventure": {"p": "[ədˈventʃər]", "d": "An exciting or unusual experience.", "uz": "sarguzasht", "uz_def": "Hayajonli yoki g'ayrioddiy tajriba.", "ex": "Riding in the rough river was an adventure.", "syn": ["journey", "quest", "venture"], "ant": ["routine", "monotony"]},
    "approach": {"p": "[əˈproʊtʃ]", "d": "To move close to something or someone.", "uz": "yaqinlashmoq", "uz_def": "Biror narsaga yoki kishiga yaqin bormoq.", "ex": "The boy approached his school carefully.", "syn": ["advance", "near", "reach"], "ant": ["retreat", "recede", "leave"]},
    "carefully": {"p": "[ˈkerfəli]", "d": "With great attention to detail or safety.", "uz": "e'tibor bilan, ehtiyotkorlik bilan", "uz_def": "Detallarga yoki xavfsizlikka katta e'tibor qaratgan holda.", "ex": "The baby carefully climbed down the stairs.", "syn": ["cautiously", "attentively"], "ant": ["carelessly", "recklessly"]},
    "chemical": {"p": "[ˈkemɪkl]", "d": "Something that scientists use in chemistry.", "uz": "kimyoviy modda", "uz_def": "Olimlar kimyoda foydalanadigan modda.", "ex": "The scientist mixed the dangerous chemicals.", "syn": ["compound", "substance"], "ant": ["natural matter"]},
    "create": {"p": "[kriˈeɪt]", "d": "To make or produce something new.", "uz": "yaratmoq, yasamoq", "uz_def": "Yangi narsa yaratmoq yoki ishlab chiqarmoq.", "ex": "She created a snow sculpture in the garden.", "syn": ["make", "build", "produce"], "ant": ["destroy", "ruin", "demolish"]},
    "evil": {"p": "[ˈiːvl]", "d": "Something or someone that is morally bad or cruel.", "uz": "yovuz, yomon", "uz_def": "Axloqsiz yoki shafqatsiz shaxs yoki narsa.", "ex": "The evil figure in the movie scared us.", "syn": ["wicked", "bad", "malevolent"], "ant": ["good", "kind", "saintly"]},
    "experiment": {"p": "[ɪkˈsperɪmənt]", "d": "A scientific test done to discover something.", "uz": "tajriba, sinov", "uz_def": "Biror narsani kashf qilish uchun o'tkaziladigan ilmiy sinov.", "ex": "The student did a chemistry experiment in science class.", "syn": ["trial", "test", "investigation"], "ant": ["theory", "guess"]},
    "kill": {"p": "[kɪl]", "d": "To cause the death of a person, animal, or plant.", "uz": "o'ldirmoq", "uz_def": "Inson, hayvon yoki o'simlikning o'limiga sabab bo'lmoq.", "ex": "The hunter tried to kill a wild boar.", "syn": ["slay", "destroy", "murder"], "ant": ["save", "rescue", "nourish"]},
    "laboratory": {"p": "[ˈlæbrətɔːri]", "d": "A room or building equipped for scientific experiments.", "uz": "laboratoriya", "uz_def": "Ilmiy tajribalar uchun jihozlangan xona yoki bino.", "ex": "My mother conducts research in a laboratory.", "syn": ["lab", "testing center"], "ant": ["field", "outdoors"]},
    "laugh": {"p": "[læf]", "d": "The sound made when someone is happy or amused.", "uz": "kulmoq, kulgi", "uz_def": "Inson xursand bo'lganda chiqaradigan tovushi.", "ex": "The sound of their joyful laugh filled the room.", "syn": ["chuckle", "giggle", "grin"], "ant": ["weep", "cry", "mourn"]},
    "loud": {"p": "[laʊd]", "d": "Making or producing a lot of sound.", "uz": "baland, shovqinli", "uz_def": "Ko'p ovoz chiqaruvchi yoki baland tovushli.", "ex": "The man's voice was so loud that we all heard it.", "syn": ["noisy", "booming", "audible"], "ant": ["quiet", "silent", "gentle"]},
    "nervous": {"p": "[ˈnɜːrvəs]", "d": "Feeling worried, fearful, or anxious.", "uz": "xavotirlangan, hayajonlangan", "uz_def": "Xavotirli yoki qo'rquvni his qiluvchi.", "ex": "The student became nervous before the speaking test.", "syn": ["anxious", "worried", "tense"], "ant": ["calm", "confident", "relaxed"]},
    "noise": {"p": "[nɔɪz]", "d": "A sound, especially one that is loud or unpleasant.", "uz": "shovqin, yoqimsiz ovoz", "uz_def": "Baland yoki yoqimsiz tovush.", "ex": "The crying baby made a loud noise in the hall.", "syn": ["sound", "racket", "clamor"], "ant": ["silence", "quiet", "peace"]},
    "project": {"p": "[ˈprɑːdʒekt]", "d": "A piece of planned work or an assignment.", "uz": "loyiha, topshiriq", "uz_def": "Rejalashtirilgan ish yoki vazifa.", "ex": "Her afternoon project was to paint the bedroom green.", "syn": ["task", "assignment", "plan"], "ant": ["inaction", "idleness"]},
    "scare": {"p": "[sker]", "d": "To make someone feel frightened or afraid.", "uz": "qo'rqitmoq", "uz_def": "Birovni qo'rquvga solmoq.", "ex": "The sudden loud thunder scared the puppy.", "syn": ["frighten", "terrify", "startle"], "ant": ["comfort", "reassure", "calm"]},
    "secret": {"p": "[ˈsiːkrət]", "d": "Something that is kept hidden or not told to others.", "uz": "sir, maxfiy narsa", "uz_def": "Boshqalardan yashirilgan narsa.", "ex": "The two best friends were whispering a secret.", "syn": ["confidential", "hidden", "private"], "ant": ["public", "known", "open"]},
    "shout": {"p": "[ʃaʊt]", "d": "To call out or say something very loudly.", "uz": "baqirmoq, qichqirmoq", "uz_def": "Juda baland ovozda gapirmoq yoki baqirmoq.", "ex": "The coach shouted at the players to run faster.", "syn": ["yell", "scream", "cry out"], "ant": ["whisper", "murmur"]},
    "smell": {"p": "[smel]", "d": "To perceive odors using the nose.", "uz": "hidlamoq, hid", "uz_def": "Burun yordamida hidni sezmoq.", "ex": "The two friends smelled the fresh roses.", "syn": ["scent", "aroma", "odor"], "ant": ["stench"]},
    "terrible": {"p": "[ˈterəbl]", "d": "Very unpleasant, serious, or bad.", "uz": "dahshatli, juda yomon", "uz_def": "Juda yoqimsiz, jiddiy yoki yomon.", "ex": "The terrible storm destroyed several wooden houses.", "syn": ["awful", "horrible", "dreadful"], "ant": ["wonderful", "terrific", "pleasant"]},
    "worse": {"p": "[wɜːrs]", "d": "Of poorer quality or lower standard than before.", "uz": "yomonroq", "uz_def": "Avvalgidan ko'ra pastroq sifatli.", "ex": "Business was worse this month than last month.", "syn": ["poorer", "inferior"], "ant": ["better", "superior"]},

    "alien": {"p": "[ˈeɪliən]", "d": "A creature from an unfamiliar world or country.", "uz": "o'zga sayyoralik", "uz_def": "Notanish dunyo yoki mamlakatdan kelgan mavjudot.", "ex": "The friendly alien landed in the open field.", "syn": ["extraterrestrial", "foreigner"], "ant": ["native", "inhabitant"]},
    "among": {"p": "[əˈmʌŋ]", "d": "In the middle of or surrounded by other things.", "uz": "orasida, ichida", "uz_def": "Boshqa narsalar o'rtasida yoki qurshovida.", "ex": "There was one red flower among the green leaves.", "syn": ["amid", "between", "surrounded by"], "ant": ["alone", "apart"]},
    "chart": {"p": "[tʃɑːrt]", "d": "A sheet of information shown in a visual way.", "uz": "diagramma, jadval", "uz_def": "Ma'lumotlarni ko'rinishda ko'rsatuvchi jadval.", "ex": "We used a chart to track our vocabulary improvement.", "syn": ["graph", "diagram", "table"], "ant": ["text", "narrative"]},
    "cloud": {"p": "[klaʊd]", "d": "A white or gray collection of water drops in the sky.", "uz": "bulut", "uz_def": "Osmonvdagi suv tomchilarining oq yoki kulrang yig'indisi.", "ex": "The clear blue sky was suddenly filled with rain clouds.", "syn": ["mist", "vapor", "fog"], "ant": ["clear sky"]},
    "comprehend": {"p": "[ˌkɑːmprɪˈhend]", "d": "To understand the meaning or nature of something.", "uz": "tushunmoq, anglamoq", "uz_def": "Biror narsaning ma'nosini to'liq tushunib yetmoq.", "ex": "Henry could not comprehend the difficult message.", "syn": ["understand", "grasp", "perceive"], "ant": ["misunderstand", "ignore"]},
    "describe": {"p": "[dɪˈskraɪb]", "d": "To explain what someone or something looks or acts like.", "uz": "tasvirlamoq", "uz_def": "Kimsaning yoki narsaning qandayligini tushuntirib bermoq.", "ex": "They described their town as quiet and beautiful.", "syn": ["depict", "characterize", "portray"], "ant": ["conceal", "hide"]},
    "ever": {"p": "[ˈevər]", "d": "At any time.", "uz": "qachon bo'lmasin, ilgari", "uz_def": "Har qanday vaqtda yoki qachonlardir.", "ex": "Going to Europe was the most exciting trip I've ever had.", "syn": ["at any time"], "ant": ["never"]},
    "fail": {"p": "[feɪl]", "d": "To not succeed in what you try to achieve.", "uz": "muvaffaqiyatsizlikka uchramoq", "uz_def": "O'z maqsadiga erisha olmaslik.", "ex": "He failed the driver exam because he didn't study.", "syn": ["fall short", "flunk"], "ant": ["succeed", "pass", "triumph"]},
    "friendly": {"p": "[ˈfrendli]", "d": "Kind, pleasant, and pleasant toward someone.", "uz": "mehribon, do'stona", "uz_def": "Mehribon va yoqimli munosabatdagi.", "ex": "The friendly dog wagged its tail at the visitors.", "syn": ["kind", "welcoming", "cordial"], "ant": ["hostile", "rude", "unfriendly"]},
    "grade": {"p": "[ɡreɪd]", "d": "A score or rating given to someone's work.", "uz": "baho, daraja", "uz_def": "Berilgan baho yoki o'quv natijasi.", "ex": "She managed to get high grades on her science report.", "syn": ["mark", "score", "rank"], "ant": ["failure"]},
    "instead": {"p": "[ɪnˈsted]", "d": "In place of or as an alternative to.", "uz": "o'rniga", "uz_def": "Biror narsa o'rniga muqobil sifatida.", "ex": "He chose water instead of sugary soda.", "syn": ["rather than", "in lieu of"], "ant": ["additionally"]},
    "library": {"p": "[ˈlaɪbreri]", "d": "A room or building containing books for reading.", "uz": "kutubxona", "uz_def": "Kitoblar saqlanadigan va o'qiladigan joy.", "ex": "The city library is quiet and packed with books.", "syn": ["archive", "book depository"], "ant": []},
    "planet": {"p": "[ˈplænɪt]", "d": "A large round celestial body in space.", "uz": "sayyora, planeta", "uz_def": "Koinotdagi yirik osmon jismi.", "ex": "Mars is the red planet orbiting the sun.", "syn": ["world", "globe"], "ant": []},
    "report": {"p": "[rɪˈpɔːrt]", "d": "A written or spoken description of an event or topic.", "uz": "hisobot, axborot", "uz_def": "Voqea yoki mavzu haqida yozma hisobot.", "ex": "She wrote an extensive report about ecology.", "syn": ["document", "account", "bulletin"], "ant": []},
    "several": {"p": "[ˈsevərəl]", "d": "More than two but not very many.", "uz": "bir nechta, bir talay", "uz_def": "Ikkitadan ortiq, lekin ko'p bo'lmagan.", "ex": "He read several articles before preparing his speech.", "syn": ["a few", "some", "various"], "ant": ["none", "all", "multitude"]},
    "solve": {"p": "[sɑːlv]", "d": "To find an answer or solution to a problem.", "uz": "yechmoq, javob topmoq", "uz_def": "Muammoga yechim topmoq.", "ex": "The clever student solved the difficult equation.", "syn": ["resolve", "figure out", "unravel"], "ant": ["complicate", "confuse"]},
    "suddenly": {"p": "[ˈsʌdənli]", "d": "Quickly and unexpectedly.", "uz": "to'satdan, kutilmaganda", "uz_def": "Tez va kutilmagan tarzda.", "ex": "A loud clap of thunder suddenly broke the silence.", "syn": ["abruptly", "unexpectedly"], "ant": ["gradually", "slowly"]},
    "suppose": {"p": "[səˈpoʊz]", "d": "To guess or think that something is true.", "uz": "deb o'ylamoq, taxmin qilmoq", "uz_def": "Taxmin qilmoq yoki o'ylamoq.", "ex": "I suppose we should leave before the storm begins.", "syn": ["assume", "presume", "guess"], "ant": ["prove", "know"]},
    "universe": {"p": "[ˈjuːnɪvɜːrs]", "d": "All existing matter and space considered as a whole.", "uz": "koinot, borliq", "uz_def": "Barcha mavjud borliq va koinot.", "ex": "The universe is so vast that it cannot be measured.", "syn": ["cosmos", "galaxy"], "ant": []},
    "view": {"p": "[vjuː]", "d": "To look at or examine something.", "uz": "qaramoq, ko'rmoq", "uz_def": "Nazar tashlamoq yoki kuzatmoq.", "ex": "We climbed the mountain to view the sunset.", "syn": ["see", "watch", "observe"], "ant": ["ignore", "overlook"]}
}

updated_count = 0
for w in words:
    term = w["word"].lower()
    if term in lexicon:
        info = lexicon[term]
        w["phonetic"] = info["p"]
        w["uzbekTranslation"] = info["uz"]
        w["uzbekDefinition"] = info["uz_def"]
        w["definition"] = f"{info['d']}"
        w["exampleSentence"] = info["ex"]
        w["synonyms"] = info["syn"]
        w["antonyms"] = info["ant"]
        updated_count += 1
    else:
        cap_word = term.capitalize()
        w["uzbekTranslation"] = "lug'at so'zi"
        w["uzbekDefinition"] = f"'{term}' so'zining ma'nosi va ishlatilishi."
        w["definition"] = f"Definition and usage for {term}."
        w["exampleSentence"] = f"The word '{term}' is an essential English vocabulary term."
        if not w["synonyms"]:
            w["synonyms"] = []
        if not w["antonyms"]:
            w["antonyms"] = []

with open('app/src/main/assets/words_data.json', 'w', encoding='utf-8') as f:
    json.dump(words, f, ensure_ascii=False, indent=2)

print(f"Updated {updated_count} words with Uzbek translation fields!")
