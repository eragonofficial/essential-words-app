const fs = require('fs');
const path = require('path');

const wordsPath = path.join(__dirname, '../app/src/main/assets/words_data.json');
const publicWordsPath = path.join(__dirname, '../public/words.json');

const words = JSON.parse(fs.readFileSync(wordsPath, 'utf8'));
console.log(`Loaded ${words.length} words from ${wordsPath}`);

const overrides = {
  'advertise': 'reklama qilmoq, e\'lon qilmoq',
  'assign': 'tayinlamoq, topshirmoq',
  'afraid': 'qo\'rqqan, cho\'chigan',
  'agree': 'rozi bo\'lmoq, ma\'qullamoq',
  'angry': 'jahli chiqqan, g\'azablangan',
  'arrive': 'yetib kelmoq, kelmoq',
  'attack': 'hujum qilmoq',
  'bottom': 'tag, tub, pastki qism',
  'clever': 'aqlli, zehnli, ziyrak',
  'cruel': 'shafqatsiz, zolim',
  'finally': 'nihoyat, oxiri',
  'hide': 'yashirinmoq, bekinmoq',
  'hunt': 'ov qilmoq',
  'lot': 'ko\'p, talaygina',
  'middle': 'o\'rtasi, markaz',
  'moment': 'lahza, on',
  'promise': 'va\'da bermoq',
  'reply': 'javob bermoq',
  'safe': 'xavfsiz, bexatar',
  'trick': 'hiyla, nayrang',
  'well': 'yaxshi, durust'
};

// Super robust, ultra-slow translation with exponential backoff and individual processing
async function translateIndividual(item) {
  const url = 'https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=uz&dt=t&q=' + encodeURIComponent(item);
  
  for (let attempt = 0; attempt < 5; attempt++) {
    try {
      const res = await fetch(url, { headers: { 'User-Agent': 'Mozilla/5.0' } });
      if (res.status === 429) {
        console.warn(`Rate limited (429) on attempt ${attempt + 1}, waiting 60s...`);
        await new Promise(r => setTimeout(r, 60000 * (attempt + 1))); 
        continue;
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      return data[0].map(c => c[0]).join('').trim();
    } catch (e) {
      console.warn(`Translate attempt ${attempt + 1} failed: ${e.message}`);
      await new Promise(r => setTimeout(r, 5000 * (attempt + 1)));
    }
  }
  return item; // Fallback to original
}

async function run() {
  console.log('Resuming translation pipeline...');
  
  let processed = 0;
  for (let i = 0; i < words.length; i++) {
    const w = words[i];
    const needsTranslation = (
      w.uzbekTranslation.includes('tushunchasi') || 
      w.uzbekDefinition.includes('tushunchasi') || 
      w.exampleSentenceUz.includes('Ushbu matnda')
    );

    if (needsTranslation) {
      console.log(`Translating word ${i + 1}/${words.length}: ${w.word}`);
      
      // Translate in parallel components to be safe
      w.uzbekTranslation = await translateIndividual(w.word);
      w.uzbekDefinition = await translateIndividual(w.englishDefinition || w.definition);
      w.exampleSentenceUz = await translateIndividual(w.exampleSentenceEn || w.exampleSentence);
      
      processed++;
      if (processed % 10 === 0) {
        fs.writeFileSync(wordsPath, JSON.stringify(words, null, 2), 'utf8');
        fs.writeFileSync(publicWordsPath, JSON.stringify(words, null, 2), 'utf8');
        console.log(`Saved checkpoint after ${processed} translations.`);
      }
      // Extremely conservative delay
      await new Promise(r => setTimeout(r, 5000));
    }
  }
  
  fs.writeFileSync(wordsPath, JSON.stringify(words, null, 2), 'utf8');
  fs.writeFileSync(publicWordsPath, JSON.stringify(words, null, 2), 'utf8');
  console.log('Translation complete.');
}

function cleanUzbekText(text) {
  return text
    .replace(/tushunchasi — bu muhim tushuncha yoki holatni ifodalovchi so'z\.?/gi, '')
    .replace(/Ushbu matnda [^\"]+ so'zi keng qo'llanilgan\.?/gi, '')
    .trim();
}

async function run() {
  console.log('Resuming translation pipeline...');
  
  let processed = 0;
  for (let i = 0; i < words.length; i++) {
    const w = words[i];
    const needsTranslation = (
      w.uzbekTranslation.includes('tushunchasi') || 
      w.uzbekDefinition.includes('tushunchasi') || 
      w.exampleSentenceUz.includes('Ushbu matnda')
    );

    if (needsTranslation) {
      console.log(`Translating word ${i + 1}/${words.length}: ${w.word}`);
      
      const lower = w.word.toLowerCase();
      if (overrides[lower]) {
        w.uzbekTranslation = overrides[lower];
      } else {
        w.uzbekTranslation = await translateIndividual(w.word);
      }
      
      if (lower === 'advertise') {
        w.uzbekTranslation = "reklama qilmoq, e'lon qilmoq";
        w.uzbekDefinition = "Mahsulot, xizmat yoki tadbir haqida ommaga e'lon berish yoki reklama qilish.";
        w.exampleSentenceUz = "Kompaniya o'zining yangi mahsulotini televideniyeda reklama qildi.";
      } else if (lower === 'assign') {
        w.uzbekTranslation = "tayinlamoq, topshirmoq";
        w.uzbekDefinition = "Biror vazifa, ish yoki majburiyatni kimgadir yuklamoq yoki topshirmoq.";
        w.exampleSentenceUz = "O'qituvchi o'quvchilarga uy vazifasini topshirdi.";
      } else {
        w.uzbekDefinition = await translateIndividual(w.englishDefinition || w.definition);
        w.exampleSentenceUz = await translateIndividual(w.exampleSentenceEn || w.exampleSentence);
      }
      
      processed++;
      if (processed % 10 === 0) {
        fs.writeFileSync(wordsPath, JSON.stringify(words, null, 2), 'utf8');
        fs.writeFileSync(publicWordsPath, JSON.stringify(words, null, 2), 'utf8');
        console.log(`Saved checkpoint after ${processed} translations.`);
      }
      await new Promise(r => setTimeout(r, 5000));
    }
  }
  
  fs.writeFileSync(wordsPath, JSON.stringify(words, null, 2), 'utf8');
  fs.writeFileSync(publicWordsPath, JSON.stringify(words, null, 2), 'utf8');
  console.log('Translation complete.');
}

run().catch(console.error);
