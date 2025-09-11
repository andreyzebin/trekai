export class MarkdownParser {
    static parseTelegramMarkdown(text) {
        if (!text) return '';

        console.log('📝 Parsing Telegram Markdown:', text);

        // Экранируем HTML теги для безопасности
        let html = this.sanitizeHtml(text);

        // Временные маркеры для защиты блоков кода
        const codeBlocks = [];
        html = html.replace(/```([\s\S]*?)```/g, (match, code) => {
            codeBlocks.push(code);
            return `:::CODEBLOCK${codeBlocks.length - 1}:::`;
        });

        const inlineCodes = [];
        html = html.replace(/`([^`]+)`/g, (match, code) => {
            inlineCodes.push(code);
            return `:::INLINECODE${inlineCodes.length - 1}:::`;
        });

        // Жирный текст
        html = html.replace(/(\*\*)(?=\S)([^\r]*?\S)\1/g, '<strong>$2</strong>');
        html = html.replace(/(__)(?=\S)([^\r]*?\S)\1/g, '<strong>$2</strong>');

        // Курсив
        html = html.replace(/(?<!\\)\/([^\/\r]+)\/(?!\\)/g, '<em>$1</em>');
        html = html.replace(/(?<!\\)\_([^\_\r]+)\_(?!\\)/g, '<em>$1</em>');

        // Ссылки
        html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

        // Восстанавливаем блоки кода
        html = html.replace(/:::CODEBLOCK(\d+):::/g, (match, index) => {
            return `<pre><code>${codeBlocks[parseInt(index)]}</code></pre>`;
        });

        html = html.replace(/:::INLINECODE(\d+):::/g, (match, index) => {
            return `<code>${inlineCodes[parseInt(index)]}</code>`;
        });

        // Переносы строк
        html = html.replace(/\n/g, '<br>');

        console.log('✅ Parsed to HTML:', html);
        return html;
    }

    static sanitizeHtml(html) {
        const div = document.createElement('div');
        div.textContent = html;
        return div.innerHTML;
    }

    static formatComment(comment) {
        if (!comment) return '';
        return this.parseTelegramMarkdown(comment);
    }
}