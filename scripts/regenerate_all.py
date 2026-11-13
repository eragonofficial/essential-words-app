import json
import re

def clean_all_data():
    with open('app/src/main/assets/words_data.json', 'r', encoding='utf-8') as f:
        words = json.load(f)

    for w in words:
        # 1. Clean Definitions & Uzbek (Remove parenthetical clutter)
        for field in ['definition', 'uzbekDefinition', 'uzbekTranslation']:
            if isinstance(w.get(field), str):
                w[field] = re.sub(r'\(.*?\)', '', w[field]).strip()

        # 2. Fix specific entries
        lower_word = w['word'].lower()
        if lower_word == 'badly':
            w['definition'] = 'In a poor or improper manner'
            w['uzbekTranslation'] = 'yomon, qoniqarsiz tarzda'
            w['uzbekDefinition'] = 'Yomon yoki qoniqarsiz tarzda.'
            w['exampleSentenceEn'] = 'He played badly in the match.'
            w['exampleSentenceUz'] = 'U o\'yinda yomon o\'ynadi.'

        # 3. Keyword Check (Very basic: is word or word+d/s/es in sentence)
        # Regex for whole word
        pattern = r'\b' + re.escape(lower_word) + r'(?:d|es|s|ing|ed)?\b'
        if not re.search(pattern, w['exampleSentenceEn'].lower()):
            # If not found, replace with a simple template
            w['exampleSentenceEn'] = f"He did it {w['word']}."
            w['exampleSentenceUz'] = f"U buni {w['uzbekTranslation'].split(',')[0]} qildi."
            
        # 4. Verb Normalization (Infinitive form for definitions - simple heuristic)
        if lower_word not in ['badly', 'well', 'angry']: # Exception list
            if 'moq' not in w['uzbekTranslation'] and w['definition'].startswith(('To ', 'To')):
                # Try simple conjugation addition if not already a verb
                if not w['uzbekTranslation'].endswith('moq'):
                    w['uzbekTranslation'] += 'moq'

    with open('app/src/main/assets/words_data.json', 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)
    with open('public/words.json', 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)
    print(f'Sanitized {len(words)} words.')

if __name__ == '__main__':
    clean_all_data()
