export class MarkdownParser {
    static parseTelegramMarkdown(input) {
        if (!input) return '';

        let text = input;

        // 1. Экранированные маркеры — заменяем на безопасный текст сразу
        text = text
            .replace(/\\\*/g, '&#42;')
            .replace(/\\_/g, '&#95;')
            .replace(/\\\//g, '&#47;');

        // 2. Вырезаем code blocks
        const codeBlocks = [];
        text = text.replace(/```([\s\S]+?)```/g, (_, code) => {
            const token = `:::CODEBLOCK${codeBlocks.length}:::`;
            codeBlocks.push(`<pre><code>${this.escapeHtml(code)}</code></pre>`);
            return token;
        });

        // 3. Вырезаем inline code
        const inlineCodes = [];
        text = text.replace(/`([^`]+?)`/g, (_, code) => {
            const token = `:::INLINECODE${inlineCodes.length}:::`;
            inlineCodes.push(`<code>${this.escapeHtml(code)}</code>`);
            return token;
        });

        // 4. Вырезаем ссылки (не экранируем '&' заранее)
        const links = [];
        text = text.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g, (_, label, url) => {
            const token = `:::LINK${links.length}:::`;
            // экранируем только для HTML-атрибута
            const safeUrl = url.replace(/&/g, '&amp;').replace(/"/g, '&quot;');
            links.push(`<a href="${safeUrl}" target="_blank" rel="noopener noreferrer">${this.escapeHtml(label)}</a>`);
            return token;
        });

        // 5. Экранируем HTML остального текста
        text = this.escapeHtml(text);

        // 6. Bold — ищем минимальные совпадения и не захватываем пустые
        text = text.replace(/(\*\*|__)(?=\S)(.+?)(?<=\S)\1/g, '<strong>$2</strong>');

        // 7. Italic — только непустые, и не ломаем соседний HTML
        text = text.replace(/(?:^|(?<!\w))(\/|_)(?=\S)(.+?)(?<=\S)\1(?!\w)/g, '<em>$2</em>');

        // 8. Переводы строк
        text = text.replace(/\r?\n/g, '<br>');

        // 9. Возвращаем токены
        codeBlocks.forEach((html, i) => text = text.replace(`:::CODEBLOCK${i}:::`, html));
        inlineCodes.forEach((html, i) => text = text.replace(`:::INLINECODE${i}:::`, html));
        links.forEach((html, i) => text = text.replace(`:::LINK${i}:::`, html));

        return text;
    }

    static escapeHtml(str) {
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#x27;');
    }

    static sanitizeHtml(html) {
        // используем ту же функцию, что и в парсере
        return this.escapeHtml(html);
    }

    static formatComment(comment) {
        if (!comment) return '';
        return this.parseTelegramMarkdown(comment);
    }
}



// CommonJS совместимость
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { MarkdownParser };
}
