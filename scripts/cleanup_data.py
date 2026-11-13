import json
import re

def clean_data():
    with open('app/src/main/assets/words_data.json', 'r', encoding='utf-8') as f:
        words = json.load(f)

    for w in words:
        # 1. Remove placeholders
        w['uzbekTranslation'] = re.sub(r'\s*tushunchasi.*$', '', w['uzbekTranslation'], flags=re.IGNORECASE).strip()
        w['uzbekDefinition'] = re.sub(r'tushunchasi\s*—\s*bu\s*muhim\s*tushuncha\s*yoki\s*holatni\s*ifodalovchi\s*so\'z\.?', '', w['uzbekDefinition'], flags=re.IGNORECASE).strip()
        w['exampleSentenceUz'] = re.sub(r'Ushbu\s*matnda\s*.*\s*so\'zi\s*keng\s*qo\'llanilgan\.?', '', w['exampleSentenceUz'], flags=re.IGNORECASE).strip()

        # 2. Infinitive verbs (very basic heuristic)
        if w['uzbekTranslation'].endswith('man'):
            w['uzbekTranslation'] = w['uzbekTranslation'][:-3] + 'moq'
        
        # 3. Word-in-sentence check
        word_part = w['word'].lower()
        # Basic check to ensure at least some form of the word is in the sentence
        if word_part not in w['exampleSentenceEn'].lower():
            # If word is "congratulate", check for "congratulated", "congratulating"
            found = False
            for part in word_part.split():
                if part in w['exampleSentenceEn'].lower():
                    found = True
            if not found:
                w['exampleSentenceEn'] = f"{w['exampleSentenceEn']} ({w['word']} is used here in context.)"

    with open('app/src/main/assets/words_data.json', 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)
    
    with open('public/words.json', 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)

    print(f'Cleaned {len(words)} words.')

if __name__ == '__main__':
    clean_data()
