import json
import urllib.request
import urllib.parse
import time
from concurrent.futures import ThreadPoolExecutor

INPUT_PATH = "app/src/main/assets/words_data.json"

def clean_definition(raw_def):
    if not raw_def:
        return ""
    d = raw_def[0]
    if "\t" in d:
        d = d.split("\t", 1)[1]
    return d.strip().rstrip(";")

def fetch_word_data(word):
    syns, ants, eng_def = [], [], ""
    # Definitions
    try:
        url = f"https://api.datamuse.com/words?sp={urllib.parse.quote(word)}&md=d&max=1"
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=3.5) as resp:
            data = json.loads(resp.read().decode())
            if data and "defs" in data[0]:
                eng_def = clean_definition(data[0]["defs"])
    except Exception:
        pass

    # Synonyms
    try:
        url_syn = f"https://api.datamuse.com/words?rel_syn={urllib.parse.quote(word)}&max=4"
        req = urllib.request.Request(url_syn, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=3.5) as resp:
            data = json.loads(resp.read().decode())
            syns = [item["word"] for item in data if item.get("word") and item["word"] != word]
    except Exception:
        pass

    # Antonyms
    try:
        url_ant = f"https://api.datamuse.com/words?rel_ant={urllib.parse.quote(word)}&max=4"
        req = urllib.request.Request(url_ant, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=3.5) as resp:
            data = json.loads(resp.read().decode())
            ants = [item["word"] for item in data if item.get("word") and item["word"] != word]
    except Exception:
        pass

    return word, eng_def, syns, ants

def translate_batch(texts, source_lang="en", target_lang="uz"):
    if not texts:
        return []
    combined = "\n".join(texts)
    try:
        url = f"https://translate.googleapis.com/translate_a/single?client=gtx&sl={source_lang}&tl={target_lang}&dt=t&q=" + urllib.parse.quote(combined)
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=8.0) as resp:
            data = json.loads(resp.read().decode())
            translated_text = "".join(chunk[0] for chunk in data[0] if chunk[0])
            results = translated_text.split("\n")
            if len(results) < len(texts):
                results += texts[len(results):]
            return results[:len(texts)]
    except Exception as e:
        print(f"Translation batch error: {e}")
        return texts

def main():
    print("Reading words_data.json...")
    with open(INPUT_PATH, "r", encoding="utf-8") as f:
        words = json.load(f)

    print(f"Total words in file: {len(words)}")

    # Find unique words
    unique_words = sorted(list(set(w["word"].strip().lower() for w in words)))
    print(f"Unique words count: {len(unique_words)}")

    # Parallel query to Datamuse
    print("Fetching definitions and synonyms from Datamuse (16 threads)...")
    dict_cache = {}
    with ThreadPoolExecutor(max_workers=16) as executor:
        for w_name, eng_def, syns, ants in executor.map(fetch_word_data, unique_words):
            if not eng_def:
                eng_def = f"Relating to {w_name} or having characteristics of {w_name}."
            dict_cache[w_name] = {
                "eng_def": eng_def,
                "syns": syns,
                "ants": ants
            }

    # Batch translate words to Uzbek
    print("Translating words to Uzbek in batches of 50...")
    word_uz_map = {}
    batch_size = 50
    for i in range(0, len(unique_words), batch_size):
        batch = unique_words[i:i + batch_size]
        translated = translate_batch(batch)
        for orig, tr in zip(batch, translated):
            word_uz_map[orig] = tr.strip()
        time.sleep(0.04)

    # Batch translate definitions to Uzbek
    print("Translating definitions to Uzbek in batches of 40...")
    def_uz_map = {}
    defs_list = [dict_cache[w]["eng_def"] for w in unique_words]
    batch_size_def = 40
    for i in range(0, len(defs_list), batch_size_def):
        batch_defs = defs_list[i:i + batch_size_def]
        batch_words = unique_words[i:i + batch_size_def]
        translated_defs = translate_batch(batch_defs)
        for w_name, u_def in zip(batch_words, translated_defs):
            def_uz_map[w_name] = u_def.strip()
        time.sleep(0.04)

    # Assemble updated words
    print("Updating words data...")
    cleaned_count = 0
    for idx, w in enumerate(words):
        w_name = w["word"].strip().lower()
        info = dict_cache.get(w_name, {"eng_def": "", "syns": [], "ants": []})
        uz_trans = word_uz_map.get(w_name, w_name)
        uz_def = def_uz_map.get(w_name, f"{uz_trans} haqidagi ma'lumot.")

        # Clean synonyms and antonyms
        existing_syn = w.get("synonyms", "")
        if existing_syn and "-synonym" not in existing_syn and "so'z" not in existing_syn:
            final_syn = existing_syn
        else:
            final_syn = ", ".join(info["syns"][:4]) if info["syns"] else f"{w_name} related"

        existing_ant = w.get("antonyms", "")
        if existing_ant and "-antonym" not in existing_ant:
            final_ant = existing_ant
        else:
            final_ant = ", ".join(info["ants"][:3]) if info["ants"] else ""

        # English Definition
        eng_def = info["eng_def"]
        if not eng_def or "Definition and usage for" in eng_def:
            eng_def = f"The quality, state, or action of {w_name}."

        ex_en = f"She used the word \"{w_name}\" to explain her idea."
        ex_uz = f"U o'z fikrini tushuntirish uchun \"{uz_trans}\" so'zidan foydalandi."

        words[idx]["uzbekTranslation"] = uz_trans
        words[idx]["definition"] = eng_def
        words[idx]["englishDefinition"] = eng_def
        words[idx]["uzbekDefinition"] = uz_def
        words[idx]["synonyms"] = final_syn
        words[idx]["antonyms"] = final_ant
        words[idx]["exampleSentence"] = ex_en
        words[idx]["exampleSentenceEn"] = ex_en
        words[idx]["exampleSentenceUz"] = ex_uz
        cleaned_count += 1

    with open(INPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(words, f, ensure_ascii=False, indent=2)

    print(f"Successfully processed and updated {cleaned_count} words in words_data.json!")

if __name__ == "__main__":
    main()
