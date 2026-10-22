const fs = require('fs');
const path = require('path');

let wordsCache = null;

function getWords() {
    if (!wordsCache) {
        try {
            // Vercel Serverless Function uchun ishlaydigan barcha ehtimoliy yo'llar
            const possiblePaths = [
                path.join(__dirname, '../public/words.json'),
                path.join(process.cwd(), 'public', 'words.json'),
                path.join(__dirname, 'public', 'words.json')
            ];

            let filePath = null;
            for (const p of possiblePaths) {
                if (fs.existsSync(p)) {
                    filePath = p;
                    break;
                }
            }

            if (filePath) {
                const raw = fs.readFileSync(filePath, 'utf8');
                wordsCache = JSON.parse(raw);
            } else {
                console.error('words.json fayli hech qaysi yo\'ldan topilmadi');
                wordsCache = [];
            }
        } catch (e) {
            console.error('words.json ni yuklashda xato:', e);
            wordsCache = [];
        }
    }
    return wordsCache;
}

module.exports = (req, res) => {
    // CORS sozlamalari
    res.setHeader('Access-Control-Allow-Credentials', true);
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version');

    if (req.method === 'OPTIONS') {
        res.status(200).end();
        return;
    }

    try {
        const urlObj = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
        const query = urlObj.searchParams.get('q') || '';
        const book = urlObj.searchParams.get('book');
        const unit = urlObj.searchParams.get('unit');
        const id = urlObj.searchParams.get('id');

        let words = getWords();

        if (id) {
            const word = words.find(w => String(w.id) === String(id));
            if (word) {
                return res.status(200).json({ success: true, data: word });
            } else {
                return res.status(404).json({ success: false, error: 'Soz topilmadi' });
            }
        }

        if (book) {
            const bookNum = parseInt(book, 10);
            words = words.filter(w => w.bookNumber === bookNum);
        }

        if (unit) {
            const unitNum = parseInt(unit, 10);
            words = words.filter(w => w.unitNumber === unitNum);
        }

        if (query) {
            const q = query.toLowerCase().trim();
            words = words.filter(w => 
                (w.word && w.word.toLowerCase().includes(q)) ||
                (w.definition && w.definition.toLowerCase().includes(q)) || 
                (w.uzbekTranslation && w.uzbekTranslation.toLowerCase().includes(q)) || 
                (w.synonyms && w.synonyms.toLowerCase().includes(q))
            );
        }

        const limit = parseInt(urlObj.searchParams.get('limit') || '100', 10);
        const page = parseInt(urlObj.searchParams.get('page') || '1', 10);
        const startIndex = (page - 1) * limit;
        const paginatedWords = words.slice(startIndex, startIndex + limit);

        return res.status(200).json({
            success: true,
            total: words.length,
            page: page,
            limit: limit,
            data: paginatedWords
        });
    } catch (error) {
        return res.status(500).json({ success: false, error: error.message });
    }
};
