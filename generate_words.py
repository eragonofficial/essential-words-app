import json

# Target words mapping for Book 1
b1_units = {
    1: ["afraid", "agree", "angry", "arrive", "attack", "bottom", "clever", "cruel", "finally", "hide", "hunt", "lot", "middle", "moment", "pleased", "promise", "reply", "safe", "trick", "well"],
    2: ["adventure", "approach", "carefully", "chemical", "create", "evil", "experiment", "kill", "laboratory", "laugh", "loud", "nervous", "noise", "project", "scare", "secret", "shout", "smell", "terrible", "worse"],
    3: ["alien", "among", "chart", "cloud", "comprehend", "describe", "ever", "fail", "friendly", "grade", "instead", "library", "planet", "report", "several", "solve", "suddenly", "suppose", "universe", "view"],
    4: ["appropriate", "avoid", "behave", "calm", "concern", "content", "expect", "frequently", "habit", "instruct", "issue", "none", "patient", "positive", "punish", "represent", "shake", "spread", "stroll", "village"],
    5: ["aware", "badly", "belong", "continue", "error", "experience", "field", "hurt", "judgment", "likely", "normal", "rare", "relax", "request", "reside", "result", "roll", "since", "visible", "wild"],
    6: ["advantage", "cause", "choice", "community", "dead", "distance", "escape", "face", "follow", "fright", "ghost", "individual", "pet", "reach", "return", "survive", "upset", "voice", "weather", "wise"],
    7: ["allow", "announce", "beside", "challenge", "claim", "condition", "contribute", "difference", "divide", "expert", "famous", "force", "harm", "lay", "peace", "prince", "protect", "sense", "sudden", "therefore"],
    8: ["accept", "arrange", "attend", "balance", "contrast", "encourage", "familiar", "grab", "hang", "huge", "necessary", "pattern", "propose", "purpose", "release", "require", "single", "success", "tear", "theory"],
    9: ["against", "beach", "damage", "discover", "emotion", "fix", "frank", "identify", "island", "ocean", "perhaps", "pleasant", "prevent", "rock", "save", "step", "still", "taste", "throw", "wave"],
    10: ["benefit", "certain", "chance", "effect", "essential", "far", "focus", "function", "grass", "guard", "image", "immediate", "primary", "proud", "remain", "rest", "separate", "site", "tail", "trouble"],
    11: ["anymore", "asleep", "berry", "collect", "compete", "conversation", "creature", "decision", "either", "forest", "ground", "introduce", "marry", "prepare", "sail", "serious", "spend", "strange", "truth", "wake"],
    12: ["alone", "apartment", "article", "artist", "attitude", "compare", "judge", "magazine", "material", "meal", "method", "neighbor", "professional", "profit", "quality", "shape", "space", "stair", "symbol", "thin"],
    13: ["blood", "burn", "cell", "contain", "correct", "crop", "demand", "equal", "feed", "hole", "increase", "lord", "owe", "position", "raise", "responsible", "sight", "spot", "structure", "whole"],
    14: ["coach", "control", "description", "direct", "exam", "example", "limit", "local", "magical", "mail", "novel", "outline", "poet", "print", "scene", "sheet", "silly", "store", "suffer", "technology"],
    15: ["across", "breathe", "characteristic", "consume", "excite", "extreme", "fear", "fortunate", "happen", "length", "mistake", "observe", "opportunity", "prize", "race", "realize", "respond", "risk", "wonder", "yet"],
    16: ["academy", "ancient", "board", "century", "clue", "concert", "county", "dictionary", "exist", "flat", "gentleman", "hidden", "maybe", "officer", "original", "pound", "process", "publish", "theater", "wealth"],
    17: ["appreciate", "available", "beat", "bright", "celebrate", "determine", "disappear", "else", "fair", "flow", "forward", "hill", "level", "lone", "puddle", "response", "season", "solution", "waste", "whether"],
    18: ["argue", "communicate", "crowd", "depend", "dish", "empty", "exact", "fresh", "gather", "indicate", "item", "offer", "price", "product", "property", "purchase", "recommend", "select", "tool", "treat"],
    19: ["alive", "bone", "bother", "captain", "conclusion", "doubt", "explore", "foreign", "glad", "however", "injustice", "international", "lawyer", "mention", "policy", "social", "speech", "staff", "toward", "wood"],
    20: ["achieve", "advise", "already", "basic", "bit", "consider", "destroy", "entertain", "extra", "goal", "lie", "meat", "opinion", "real", "reflect", "regard", "serve", "vegetable", "war", "worth"],
    21: ["appear", "base", "brain", "career", "clerk", "effort", "enter", "excellent", "hero", "hurry", "inform", "later", "leave", "locate", "nurse", "operation", "pain", "refuse", "though", "various"],
    22: ["actual", "amaze", "charge", "comfort", "contact", "customer", "deliver", "earn", "gate", "include", "manage", "mystery", "occur", "opposite", "plate", "receive", "reward", "set", "steal", "thief"],
    23: ["advance", "athlete", "average", "behavior", "behind", "course", "lower", "match", "member", "mental", "passenger", "personality", "poem", "pole", "remove", "safety", "shoot", "sound", "swim", "web"],
    24: ["block", "cheer", "complex", "critic", "event", "exercise", "fit", "friendship", "guide", "lack", "passage", "perform", "pressure", "probable", "public", "strike", "support", "task", "term", "unite"],
    25: ["associate", "environment", "factory", "feature", "instance", "involve", "medicine", "mix", "organize", "period", "populate", "produce", "range", "recognize", "regular", "sign", "tip", "tradition", "trash", "wide"],
    26: ["advice", "along", "attention", "attract", "climb", "drop", "final", "further", "imply", "maintain", "neither", "otherwise", "physical", "prove", "react", "ride", "situated", "society", "standard", "suggest"],
    27: ["actually", "bite", "coast", "deal", "desert", "earthquake", "effective", "examine", "false", "gift", "hunger", "imagine", "journey", "puzzle", "quite", "rather", "specific", "tour", "trip", "value"],
    28: ["band", "barely", "boring", "cancel", "driveway", "garbage", "instrument", "list", "magic", "message", "notice", "own", "predict", "professor", "rush", "schedule", "share", "stage", "storm", "within"],
    29: ["advertise", "assign", "audience", "breakfast", "competition", "cool", "gain", "importance", "knowledge", "major", "mean", "prefer", "president", "progress", "respect", "rich", "skill", "somehow", "strength", "vote"],
    30: ["above", "ahead", "amount", "belief", "center", "common", "cost", "demonstrate", "different", "evidence", "honesty", "idiom", "independent", "inside", "master", "memory", "proper", "scan", "section", "surface"]
}

# Target words mapping for Book 2
b2_units = {
    1: ["anxious", "awful", "consist", "desire", "eager", "household", "intent", "landscape", "lift", "load", "lung", "motion", "pace", "polite", "possess", "rapidly", "remark", "seek", "shine", "spill"],
    2: ["bring", "castle", "command", "counsel", "ensure", "explosion", "jewelry", "land", "meteor", "monster", "northern", "remote", "southern", "statue", "steam", "submit", "temple", "upper", "weed", "wing"],
    3: ["arrow", "battle", "bow", "brave", "chief", "disadvantage", "enemy", "entrance", "hardly", "intend", "laughter", "log", "military", "obey", "secure", "steady", "trust", "twist", "unless", "weapon"],
    4: ["chest", "confidence", "consequence", "disaster", "disturb", "estimate", "honor", "impress", "marathon", "narrow", "pale", "rough", "satisfy", "scream", "sensitive", "shade", "supplement", "terror", "threat", "victim"],
    5: ["ancestor", "angle", "boot", "border", "congratulate", "frame", "heaven", "incredible", "legend", "praise", "proceed", "pure", "relative", "senior", "silent", "sink", "superior", "surround", "thick", "wrap"],
    6: ["abroad", "anger", "bride", "brief", "chase", "disappoint", "dive", "exchange", "favor", "fee", "forever", "guy", "lovely", "mood", "palace", "permit", "protest", "sculpture", "tribe", "youth"],
    7: ["basis", "biology", "cage", "colleague", "colony", "debate", "depart", "depress", "factual", "fascinate", "mission", "nevertheless", "occupation", "overseas", "persuade", "route", "ruins", "scholar", "significant", "volcano"],
    8: ["broad", "bush", "capable", "cheat", "concentrate", "conclude", "confident", "considerable", "convey", "definite", "delight", "destination", "dictate", "edge", "path", "resort", "shadow", "succeed", "suspect", "valley"],
    9: ["admire", "aid", "attempt", "authority", "capital", "cooperate", "defend", "destruction", "disorder", "division", "enable", "frustrate", "govern", "plenty", "relieve", "reputation", "royal", "slave", "struggle", "stupid"],
    10: ["citizen", "council", "declare", "enormous", "extraordinary", "fog", "funeral", "giant", "impression", "income", "mad", "ought", "resist", "reveal", "rid", "sword", "tale", "trap", "trial", "violent"],
    11: ["admission", "astronomy", "blame", "chemistry", "despite", "dinosaur", "exhibit", "fame", "forecast", "genius", "gentle", "geography", "interfere", "lightly", "principal", "row", "shelf", "spite", "super", "wet"],
    12: ["abuse", "afford", "bake", "bean", "candle", "convert", "debt", "decrease", "fault", "fund", "generous", "ingredient", "insist", "mess", "metal", "monitor", "oppose", "passive", "quantity", "sue"],
    13: ["anxiety", "army", "billion", "carve", "consult", "emergency", "fortune", "guarantee", "hike", "initial", "intense", "lend", "peak", "potential", "pride", "proof", "quit", "spin", "tiny", "tutor"],
    14: ["apparent", "blind", "calculate", "chat", "commit", "compose", "dormitory", "exhaust", "greenhouse", "ignore", "obvious", "physics", "portion", "remind", "secretary", "severe", "talent", "thesis", "uniform", "vision"],
    15: ["absorb", "boss", "committee", "contract", "crew", "devote", "dig", "dine", "donate", "double", "elevate", "flavor", "foundation", "generation", "handle", "layer", "mud", "smooth", "soil", "unique"],
    16: ["chamber", "deny", "document", "emphasize", "fever", "flu", "freeze", "gesture", "interrupt", "last", "likeness", "moreover", "perspective", "rational", "recover", "rely", "shock", "shy", "stare", "thus"],
    17: ["aim", "attach", "bet", "carriage", "classic", "commute", "confirm", "criticize", "differ", "expense", "formal", "height", "invent", "junior", "labor", "mechanic", "prime", "shift", "signal", "sincere"],
    18: ["ability", "agriculture", "cartoon", "ceiling", "convince", "curious", "delay", "diary", "element", "faith", "grain", "greet", "investigate", "joy", "label", "monk", "odd", "pause", "priest", "profession"],
    19: ["adopt", "beg", "beyond", "costume", "exclaim", "extend", "fool", "forbid", "illustrate", "indeed", "interpret", "kindly", "motive", "nest", "origin", "reception", "reject", "silence", "stream", "tone"],
    20: ["accomplish", "approve", "approximate", "barrier", "detect", "duty", "elementary", "failure", "gradual", "immigrant", "insert", "instant", "poverty", "pretend", "rank", "recognition", "refrigerate", "rent", "retire", "statistic"],
    21: ["astronaut", "awake", "courage", "float", "grant", "gravity", "jewel", "miner", "mineral", "participate", "permission", "pour", "presence", "raw", "satellite", "scale", "skip", "stretch", "telescope", "underground"],
    22: ["alarm", "apart", "arrest", "award", "breed", "bucket", "contest", "convict", "garage", "journalist", "pup", "qualify", "repair", "resume", "rob", "slip", "somewhat", "stable", "tissue", "yard"],
    23: ["alike", "annoy", "architecture", "artificial", "chain", "distinct", "distinguish", "dust", "excitement", "heal", "inherit", "manner", "mount", "roof", "shortage", "solid", "stock", "substance", "tomb", "wound"],
    24: ["bath", "bend", "chew", "disabled", "fantastic", "fiction", "flag", "inspect", "journal", "liquid", "marvel", "nutrient", "overcome", "recall", "regret", "soul", "sufficient", "surgery", "tough", "tube"],
    25: ["admit", "bin", "bowl", "cabin", "cash", "criminal", "dozen", "elder", "facial", "fence", "inspire", "mere", "neat", "occasion", "penalty", "rude", "settle", "vehicle", "wallet", "yell"],
    26: ["accuse", "adjust", "amuse", "coral", "cotton", "crash", "deck", "engage", "firm", "fuel", "grand", "hurricane", "loss", "plain", "reef", "shut", "strict", "surf", "task", "zone"],
    27: ["apology", "bold", "capture", "cardinal", "duke", "expose", "guilty", "hire", "innocent", "jail", "minister", "ordinary", "permanent", "preserve", "pronounce", "resemble", "symptom", "tobacco", "twin", "witch"],
    28: ["accompany", "bare", "branch", "breath", "bridge", "cast", "dare", "electronic", "inn", "net", "philosophy", "pot", "seed", "sharp", "sort", "subtract", "tight", "virtual", "weigh", "whisper"],
    29: ["abstract", "annual", "clay", "cloth", "curtain", "deserve", "feather", "fertile", "flood", "furniture", "grave", "ideal", "intelligence", "nowadays", "obtain", "religious", "romantic", "shell", "shore", "wheel"],
    30: ["appeal", "assume", "borrow", "client", "downtown", "dull", "embarrass", "fare", "former", "formula", "found", "invest", "loan", "practical", "quarter", "salary", "scholarship", "temporary", "treasure", "urge"]
}

# Target words mapping for Book 3
b3_units = {
    1: ["arise", "benefactor", "blacksmith", "charitable", "chimney", "compensate", "encounter", "exceed", "forge", "humble", "iron", "ladder", "modest", "occupy", "penny", "preach", "prosper", "province", "satisfaction", "sustain"],
    2: ["acquire", "awkward", "caretaker", "deceive", "discourage", "fake", "hatred", "hut", "inferior", "lodge", "neglect", "newcomer", "offense", "overlook", "repay", "ridiculous", "satisfactory", "shepherd", "venture", "wheat"],
    3: ["alley", "ax", "bunch", "chore", "decent", "disgrace", "elbow", "grateful", "irritate", "kid", "loose", "offend", "overnight", "persist", "pine", "scar", "sensation", "sled", "tease", "valentine"],
    4: ["bloom", "compact", "curl", "decay", "dessert", "dip", "distant", "eclipse", "fairy", "grace", "leisure", "mankind", "passion", "pillow", "pulse", "refresh", "sneeze", "spice", "whistle", "wool"],
    5: ["acquaint", "cemetery", "curse", "disguise", "fancy", "flashlight", "hood", "inhabitant", "nourish", "pirate", "publication", "riddle", "rot", "scare", "shortly", "skeleton", "spoil", "starve", "thrill", "wicked"],
    6: ["alert", "broadcast", "bulletin", "bump", "chop", "closet", "console", "district", "drawer", "endure", "execute", "grasp", "rear", "senator", "skull", "stir", "tap", "tremendous", "underneath", "worm"],
    7: ["abandon", "ambitious", "bark", "bay", "brilliant", "chin", "complaint", "deaf", "enthusiastic", "expedition", "horizon", "loyal", "mayor", "mutual", "overweight", "refuge", "restore", "rub", "senses", "veterinarian"],
    8: ["anniversary", "arithmetic", "ashamed", "burst", "carpenter", "coal", "couch", "drip", "elegant", "fabric", "highlands", "ivory", "mill", "needle", "polish", "sew", "shed", "thread", "trim", "upwards"],
    9: ["ail", "ally", "boast", "bounce", "bully", "carbohydrate", "crawl", "defeat", "dial", "dominant", "mercy", "nod", "opponent", "quarrel", "rival", "sore", "sting", "strain", "torture", "wrestle"],
    10: ["absence", "aloud", "bald", "blanket", "creep", "divorce", "imitate", "infant", "kidnap", "nap", "nowhere", "pat", "relief", "reproduce", "rhyme", "suck", "urgent", "vanish", "wagon", "wrinkle"],
    11: ["abnormal", "bamboo", "blossom", "compass", "dialect", "dishonest", "dwarf", "ecosystem", "fatal", "impatient", "leaf", "manuscript", "marsh", "patience", "perfume", "pond", "proverb", "pursuit", "recite", "wilderness"],
    12: ["anticipate", "barrel", "beam", "casual", "caution", "contrary", "deliberate", "dissolve", "explode", "fasten", "germ", "kit", "puff", "rag", "scatter", "scent", "steel", "swift", "toss", "triumph"],
    13: ["aboard", "bitter", "bullet", "devil", "drift", "enforce", "fountain", "harbor", "inhabit", "march", "millionaire", "port", "sheriff", "startle", "sweat", "trigger", "unify", "vessel", "voyage", "worship"],
    14: ["apprentice", "assure", "bandage", "bleed", "bond", "chef", "crown", "departure", "diligent", "emperor", "fiber", "horrible", "impolite", "kneel", "luxury", "massive", "panic", "priority", "robe", "scold"],
    15: ["affair", "assembly", "bless", "cereal", "cheerful", "diameter", "exploit", "famine", "harvest", "merry", "nut", "pardon", "pharaoh", "ripe", "roast", "routine", "scheme", "slim", "stove", "theft"],
    16: ["adolescent", "aptitude", "compliment", "hinder", "journalism", "jury", "justice", "liberty", "literary", "pharmacy", "pill", "presume", "privacy", "punishment", "sensible", "slice", "sorrow", "straw", "swell", "tidy"],
    17: ["affection", "agency", "ash", "confine", "dismiss", "erupt", "fate", "lava", "miserable", "navigate", "originate", "remainder", "retrieve", "shallow", "slope", "span", "superstition", "sympathy", "vibrate", "wander"],
    18: ["armor", "blaze", "boom", "cliff", "flame", "independence", "invasion", "knight", "lightning", "rebel", "retreat", "revolution", "spear", "steep", "summit", "thunder", "troops", "warrior", "withdraw", "yield"],
    19: ["bench", "confront", "daisy", "dispute", "horror", "incident", "mist", "object", "orphan", "plot", "pregnant", "rage", "revenge", "shame", "sigh", "sneak", "spare", "stem", "supper", "tender"],
    20: ["beneath", "cub", "dawn", "dissatisfied", "ease", "evident", "hail", "howl", "leap", "magnificent", "necessity", "outcome", "pile", "profound", "seize", "squeeze", "supreme", "terrific", "trait", "vital"],
    21: ["accustomed", "affirm", "astonished", "bang", "clan", "dim", "emphasis", "fable", "feast", "glow", "hollow", "instinct", "joint", "leak", "physician", "sacrifice", "stiff", "stroke", "tragic", "tune"],
    22: ["accommodate", "circus", "coincide", "commission", "dose", "dye", "extent", "gender", "headline", "informal", "inquire", "messenger", "peer", "portrait", "pose", "ranch", "steer", "stripe", "tame", "tempt"],
    23: ["Aborigine", "ban", "cautious", "confess", "cottage", "daytime", "desperate", "fade", "fierce", "gamble", "lawn", "mow", "outlaw", "prospect", "purse", "rod", "seldom", "shave", "terrified", "wizard"],
    24: ["baggage", "bulb", "bundle", "cattle", "flee", "graze", "greed", "herd", "initiate", "lane", "luggage", "nerve", "optimist", "parade", "pave", "phantom", "portable", "poster", "scratch", "symphony", "widow"],
    25: ["circulate", "consequent", "derive", "drown", "dynasty", "fraction", "frost", "illusion", "invade", "lieutenant", "marine", "merit", "navy", "polar", "ray", "resign", "suicide", "tremble", "underlying", "via"],
    26: ["alter", "aside", "autumn", "blend", "collapse", "crush", "curve", "disgusting", "drain", "embrace", "envy", "fireworks", "flour", "fuse", "ginger", "jealous", "paste", "receipt", "wipe", "wire"],
    27: ["acknowledge", "ambassador", "blonde", "conquer", "drag", "exaggerate", "heritage", "insult", "meanwhile", "necklace", "noble", "precious", "prejudice", "rumor", "sin", "spectacle", "stack", "suspicious", "tin", "vase"],
    28: ["accompany", "bare", "branch", "breath", "bridge", "cast", "dare", "electronic", "inn", "net", "philosophy", "pot", "seed", "sharp", "sort", "subtract", "tight", "virtual", "weigh", "whisper"],
    29: ["abstract", "annual", "clay", "cloth", "curtain", "deserve", "feather", "fertile", "flood", "furniture", "grave", "ideal", "intelligence", "nowadays", "obtain", "religious", "romantic", "shell", "shore", "wheel"],
    30: ["appliance", "basin", "broom", "caterpillar", "cupboard", "delicate", "emerge", "handicap", "hook", "hop", "laundry", "pursue", "reluctant", "sleeve", "spine", "stain", "strip", "swear", "swing", "utilize"]
}

# Target words mapping for Book 4
b4_units = {
    1: ["aroma", "beverage", "cluster", "combine", "condensed", "contemporary", "cultivate", "divine", "humid", "odor", "palate", "paradise", "plantation", "rapid", "rate", "soothing", "subtle", "texture", "toxic", "vary"],
    2: ["accident", "admiral", "arc", "character", "conscience", "fiery", "flesh", "grapefruit", "hay", "horrified", "kerosene", "loop", "paddle", "raft", "sour", "stake", "steward", "string", "thorn", "wreck"],
    3: ["admonish", "audible", "awesome", "beware", "brag", "conscious", "disagree", "echo", "eventual", "hint", "idiot", "immense", "indirect", "option", "pastime", "perfect", "pinpoint", "switch", "thorough", "torment"],
    4: ["beak", "damp", "disapprove", "except", "flight", "fond", "immoral", "ivy", "moan", "oblivious", "perish", "pit", "rim", "roost", "slippery", "soar", "trivial", "typical", "utterly", "weep"],
    5: ["awhile", "cyberspace", "edit", "essay", "evaluate", "faint", "global", "gymnasium", "highlight", "ignorant", "index", "lecture", "moral", "operate", "private", "recent", "resolution", "semester", "typewritten", "weird"],
    6: ["absolute", "alas", "attentive", "cape", "envision", "evenly", "folk", "melt", "patch", "pleasure", "pop", "pudding", "rail", "recipe", "role", "shrink", "soak", "spark", "spirit", "suit"],
    7: ["account", "architect", "conceal", "crime", "deed", "gratitude", "habitat", "intervene", "landmark", "legal", "memorable", "oblige", "offense", "proclaim", "rally", "resolve", "resource", "sentence", "volunteer", "witness"],
    8: ["access", "conduct", "constant", "crack", "device", "enclose", "grip", "halt", "impending", "influence", "law", "mode", "perspire", "replace", "snap", "sly", "tend", "valid", "version", "whatsoever"],
    9: ["alongside", "appetite", "assist", "breeze", "defy", "display", "efficient", "feeble", "forgive", "lively", "majestic", "nor", "outraged", "pessimistic", "rumor", "slap", "smash", "subject", "wage", "whereas"],
    10: ["animate", "classify", "concede", "concept", "construct", "decade", "diagram", "ferry", "handy", "isolate", "longing", "numerous", "particle", "plea", "refrain", "review", "sophisticated", "surrender", "upright", "worthwhile"],
    11: ["alliance", "applause", "armed", "authoritative", "ceremony", "culture", "defense", "detail", "diverse", "enchant", "equip", "exception", "genre", "impact", "lure", "obstacle", "shelter", "sort", "supply", "vain"],
    12: ["alternative", "avenue", "belly", "bid", "blow", "conflict", "continent", "current", "disrespect", "enthusiasm", "harsh", "lean", "meantime", "mischief", "muscle", "rescue", "succession", "terrain", "timid", "violence"],
    13: ["affect", "autograph", "bead", "brew", "charm", "destiny", "horn", "irritable", "lag", "maximize", "nightmare", "nutritious", "protein", "signature", "stuff", "subconscious", "van", "warn", "workout", "zoom"],
    14: ["brick", "crumble", "dough", "express", "fist", "flexible", "flush", "injure", "lump", "mixture", "reconcile", "ruin", "shatter", "shutter", "sift", "slight", "sparkle", "sprinkle", "stale", "utter"],
    15: ["although", "apply", "await", "beloved", "bury", "climate", "complain", "confuse", "due", "entire", "establish", "furnace", "leash", "mature", "measure", "midst", "misery", "prior", "research", "variety"],
    16: ["altogether", "bind", "bruise", "custom", "disobedient", "foresee", "glimpse", "hoop", "misfortune", "negative", "per", "plead", "rip", "sake", "scrape", "source", "stern", "stitch", "thump", "vehement"],
    17: ["civilization", "convenient", "den", "dew", "drastic", "exit", "flock", "fold", "lid", "loom", "mighty", "mushroom", "native", "poison", "reed", "shield", "stormy", "sway", "urban", "wade"],
    18: ["accent", "barber", "basement", "blank", "blink", "choir", "comic", "complicate", "decline", "errand", "glove", "hermit", "justly", "leather", "ponder", "reserve", "script", "search", "slam", "staircase"],
    19: ["afflicted", "aisle", "atmosphere", "author", "breakdown", "cargo", "chapter", "connect", "etc.", "flip", "idle", "notify", "pea", "raisin", "retain", "state", "tray", "unfortunate", "vivid", "vomit"],
    20: ["betray", "blast", "bracelet", "cease", "choke", "civil", "comment", "cross", "dent", "distrust", "fort", "found", "lining", "mass", "pray", "rife", "sole", "sweep", "treachery", "tuck"],
    21: ["background", "bait", "chronicle", "copper", "disease", "folklore", "infect", "itch", "literature", "millennium", "myth", "promote", "relate", "religion", "sum", "teller", "trustworthy", "update", "vein", "venom"],
    22: ["charity", "commerce", "condemn", "cozy", "deplete", "economy", "empire", "goods", "heed", "hitchhike", "mock", "neutral", "persecute", "pity", "reduce", "scribe", "temper", "throne", "unity", "victor"],
    23: ["accurate", "analyze", "asteroid", "controversy", "evolve", "factor", "genetic", "genome", "identical", "intellectual", "majority", "mammal", "multiply", "offspring", "pesticide", "regulate", "reinforce", "stricken", "vast", "vegetarian"],
    24: ["cherish", "compassion", "consent", "core", "cunning", "dizzy", "equilibrium", "foster", "grind", "growl", "moderation", "predator", "sane", "saucer", "snatch", "stagger", "stumble", "tense", "tumble", "withhold"],
    25: ["aircraft", "celebrity", "concrete", "decisive", "esteemed", "ethical", "extinct", "hardy", "institute", "jealousy", "migrate", "nurture", "overhead", "principle", "rural", "secluded", "species", "swamp", "traverse", "zoology"],
    26: ["assumption", "barley", "beast", "colonel", "contagious", "corpse", "crisis", "cure", "deformed", "discriminate", "embassy", "extinguish", "flint", "harass", "integrate", "miniature", "nutrition", "promptly", "technician", "tropics"],
    27: ["beneficial", "birthplace", "capacity", "comparative", "comprehensive", "conserve", "crucial", "cumulative", "deposit", "distribute", "equator", "exotic", "federal", "formation", "frequency", "objective", "oxygen", "rainforest", "strategy", "wooded"],
    28: ["avail", "expand", "define", "dread", "fundamental", "horrifying", "incredulous", "linger", "organism", "paraphrase", "plague", "presently", "random", "riot", "scribble", "shrine", "solitude", "stark", "summon", "worsen"],
    29: ["automobile", "candidate", "confidential", "corporate", "enhance", "era", "guideline", "incorporate", "interact", "interval", "mobile", "modify", "parallel", "phenomenon", "pollute", "ridicule", "solar", "territory", "tournament", "transportation"],
    30: ["bill", "boundary", "chaos", "consistent", "cyclone", "doomed", "heir", "martial", "organic", "poultry", "scramble", "sergeant", "sheer", "stance", "telegraph", "textile", "tornado", "typhoon", "wail", "wardrobe"]
}

# A dictionary of well-known definitions, phonetics, uzbek definitions, examples, synonyms, antonyms
word_lexicon = {
    "afraid": {"phonetic": "[əˈfreɪd]", "def": "When someone is afraid, they feel fear.", "uz": "qo'rqqan, cho'chigan", "ex": "The woman was afraid of what she saw.", "syn": ["scared", "fearful", "frightened"], "ant": ["brave", "confident", "fearless"]},
    "agree": {"phonetic": "[əˈɡriː]", "def": "To agree is to say yes or to think the same way.", "uz": "fikriga qo'shilmoq, rozi bo'lmoq", "ex": "I agree with your suggestion completely.", "syn": ["concur", "consent", "accept"], "ant": ["disagree", "differ", "oppose"]},
    "angry": {"phonetic": "[ˈæŋɡri]", "def": "When someone is angry, they may want to speak loudly or fight.", "uz": "jahli chiqqan, badjahl", "ex": "She didn't do her homework, so her father is angry.", "syn": ["mad", "furious", "irritated"], "ant": ["calm", "pleased", "peaceful"]},
    "arrive": {"phonetic": "[əˈraɪv]", "def": "To arrive is to get to or reach some place.", "uz": "yetib kelmoq, kelmoq", "ex": "The bus always arrives at the corner at 4:00.", "syn": ["reach", "appear", "come"], "ant": ["depart", "leave", "exit"]},
    "attack": {"phonetic": "[əˈtæk]", "def": "To attack is to try to fight or to hurt.", "uz": "hujum qilmoq", "ex": "The soldiers attacked the enemy fortress.", "syn": ["assault", "strike", "charge"], "ant": ["defend", "protect", "shield"]},
    "bottom": {"phonetic": "[ˈbɑːtəm]", "def": "The bottom is the lowest part of something.", "uz": "tag, pastki qism", "ex": "The bottom of my shoe has a hole in it.", "syn": ["base", "foot", "foundation"], "ant": ["top", "peak", "summit"]},
    "clever": {"phonetic": "[ˈklevər]", "def": "When someone is clever, they can solve a hard puzzle or problem.", "uz": "aqlli, ziyrak", "ex": "The clever boy thought of a great idea.", "syn": ["smart", "intelligent", "ingenious"], "ant": ["stupid", "foolish", "silly"]},
    "cruel": {"phonetic": "[ˈkruːəl]", "def": "When someone is cruel, they do bad things to hurt others.", "uz": "shafqatsiz, berahm", "ex": "The cruel man yelled at his sister.", "syn": ["mean", "brutal", "harsh"], "ant": ["kind", "gentle", "compassionate"]},
    "finally": {"phonetic": "[ˈfaɪnəli]", "def": "If something happens finally, it happens after a long time.", "uz": "oxiri, nihoyat", "ex": "He finally crossed the finish line after five hours.", "syn": ["at last", "eventually", "ultimately"], "ant": ["initially", "firstly"]},
    "hide": {"phonetic": "[haɪd]", "def": "To hide is to try not to let others see you.", "uz": "yashirinmoq, bekinmoq", "ex": "The children hide while you count to 100.", "syn": ["conceal", "cover", "screen"], "ant": ["reveal", "expose", "show"]},
    "hunt": {"phonetic": "[hʌnt]", "def": "To hunt is to look for animals to catch or kill.", "uz": "ov qilmoq", "ex": "Long ago, people hunted with bows and arrows.", "syn": ["chase", "pursue", "track"], "ant": ["flee", "escape"]},
    "lot": {"phonetic": "[lɑːt]", "def": "A lot means a large number or amount of things.", "uz": "juda ko'p", "ex": "There are a lot of apples in the basket.", "syn": ["plenty", "abundance", "many"], "ant": ["few", "little", "scarcity"]},
    "middle": {"phonetic": "[ˈmɪdl]", "def": "The middle of something is the center or halfway point.", "uz": "o'rta, markaz", "ex": "The Canadian flag has a maple leaf in the middle.", "syn": ["center", "core", "midpoint"], "ant": ["edge", "border", "rim"]},
    "moment": {"phonetic": "[ˈmoʊmənt]", "def": "A moment is a second or a very short time.", "uz": "lahza, on, sekund", "ex": "I was only a few moments late for the meeting.", "syn": ["instant", "second", "flash"], "ant": ["eternity", "era"]},
    "pleased": {"phonetic": "[pliːzd]", "def": "When someone is pleased, they are happy.", "uz": "xursand, mamnun", "ex": "She was pleased with the phone call she received.", "syn": ["satisfied", "glad", "delighted"], "ant": ["dissatisfied", "upset", "unhappy"]},
    "promise": {"phonetic": "[ˈprɑːmɪs]", "def": "To promise is to say you will do something for sure.", "uz": "va'da bermoq", "ex": "He promised to return my key by tomorrow.", "syn": ["swear", "pledge", "vow"], "ant": ["break", "betray"]},
    "reply": {"phonetic": "[rɪˈplaɪ]", "def": "To reply is to give an answer or say back to someone.", "uz": "javob bermoq", "ex": "She asked him the time, and he replied at three.", "syn": ["answer", "respond", "retort"], "ant": ["ask", "inquire", "question"]},
    "safe": {"phonetic": "[seɪf]", "def": "When a person is safe, they are not in danger.", "uz": "xavfsiz, bexatar", "ex": "Put on your seat belt in the car to be safe.", "syn": ["secure", "protected", "shielded"], "ant": ["dangerous", "risky", "unsafe"]},
    "trick": {"phonetic": "[trɪk]", "def": "A trick is something you do to fool another person.", "uz": "xiyla, nayrang", "ex": "His card trick really surprised everyone.", "syn": ["deception", "prank", "illusion"], "ant": ["honesty", "truth", "reality"]},
    "well": {"phonetic": "[wel]", "def": "You use well to say that something was done in a good way.", "uz": "yaxshi, soz", "ex": "The couple can dance quite well.", "syn": ["skillfully", "expertly", "nicely"], "ant": ["badly", "poorly"]}
}

all_books_units = [
    (1, b1_units),
    (2, b2_units),
    (3, b3_units),
    (4, b4_units)
]

words_list = []
word_id = 1

for book_num, units in all_books_units:
    for unit_num in range(1, 31):
        target_words = units.get(unit_num, [])
        for w in target_words:
            w_lower = w.lower()
            if w_lower in word_lexicon:
                lex = word_lexicon[w_lower]
                phonetic = lex["phonetic"]
                definition = f"{lex['def']} ({lex['uz']})"
                example = lex["ex"]
                synonyms = lex["syn"]
                antonyms = lex["ant"]
            else:
                phonetic = f"[/{w_lower}/]"
                definition = f"Definition and usage for {w_lower}."
                example = f"The word '{w_lower}' is an essential English vocabulary term."
                synonyms = []
                antonyms = []

            words_list.append({
                "id": word_id,
                "bookNumber": book_num,
                "unitNumber": unit_num,
                "word": w_lower,
                "phonetic": phonetic,
                "definition": definition,
                "exampleSentence": example,
                "synonyms": synonyms,
                "antonyms": antonyms,
                "isLearned": False
            })
            word_id += 1

with open('app/src/main/assets/words_data.json', 'w', encoding='utf-8') as f:
    json.dump(words_list, f, ensure_ascii=False, indent=2)

print(f"Generated {len(words_list)} words across 4 books and 120 units!")
