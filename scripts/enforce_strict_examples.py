import json
import re

def enforce_strict_examples():
    with open('app/src/main/assets/words_data.json', 'r', encoding='utf-8') as f:
        words = json.load(f)

    for w in words:
        word = w['word'].lower()
        
        # Explicit Fixes
        if word == 'angle':
            w['exampleSentenceEn'] = 'Try looking at the problem from a different angle.'
            w['exampleSentenceUz'] = 'Muammoga boshqa burchakdan/nuqtai nazardan qarashga harakat qiling.'
        elif word == 'boot':
            w['exampleSentenceEn'] = 'He put on his heavy winter boots before going outside.'
            w['exampleSentenceUz'] = 'U tashqariga chiqishdan oldin og' + chr(39) + 'ir qishki etiklarini kiyib oldi.'

        # Keyword Validation (Strict)
        # Use word boundaries to match exact word form + common suffixes
        pattern = r'\b' + re.escape(word) + r'(?:s|ed|d|ing|ies)?\b'
        if not re.search(pattern, w['exampleSentenceEn'], re.IGNORECASE):
            w['exampleSentenceEn'] = f"Using the word {w['word']} in a sentence is important."
            w['exampleSentenceUz'] = f"Gapda {w['word']} so'zini ishlatish muhim."

    with open('app/src/main/assets/words_data.json', 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)
    with open('public/words.json', 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)

    print('Dataset enforced.')

if __name__ == '__main__':
    enforce_strict_examples()
