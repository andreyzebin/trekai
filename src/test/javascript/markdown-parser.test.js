/**
 * @jest-environment jsdom
 */

const { MarkdownParser } = require('../../main/resources/static/js/markdown-parser');

describe('MarkdownParser', () => {
    let originalConsoleLog;

    beforeEach(() => {
        // Сохраняем и мокаем console.log
        originalConsoleLog = console.log;
        console.log = jest.fn();
    });

    afterEach(() => {
        // Восстанавливаем console.log
        console.log = originalConsoleLog;
    });

    describe('parseTelegramMarkdown', () => {
        test('should return empty string for null or undefined input', () => {
            expect(MarkdownParser.parseTelegramMarkdown(null)).toBe('');
            expect(MarkdownParser.parseTelegramMarkdown(undefined)).toBe('');
            expect(MarkdownParser.parseTelegramMarkdown('')).toBe('');
        });

        test('should parse bold text with **', () => {
            const result = MarkdownParser.parseTelegramMarkdown('**bold text**');
            expect(result).toBe('<strong>bold text</strong>');
        });

        test('should parse bold text with __', () => {
            const result = MarkdownParser.parseTelegramMarkdown('__bold text__');
            expect(result).toBe('<strong>bold text</strong>');
        });

        test('should parse italic text with /', () => {
            const result = MarkdownParser.parseTelegramMarkdown('/italic text/');
            expect(result).toBe('<em>italic text</em>');
        });

        test('should parse italic text with _', () => {
            const result = MarkdownParser.parseTelegramMarkdown('_italic text_');
            expect(result).toBe('<em>italic text</em>');
        });

        test('should not parse escaped italic markers', () => {
            const result = MarkdownParser.parseTelegramMarkdown('\\/escaped/ and \\_escaped_');
            expect(result).toBe('/escaped/ and _escaped_');
        });

        test('should parse links', () => {
            const result = MarkdownParser.parseTelegramMarkdown('[Google](https://google.com)');
            expect(result).toBe('<a href="https://google.com" target="_blank" rel="noopener noreferrer">Google</a>');
        });

        test('should handle code blocks', () => {
            const result = MarkdownParser.parseTelegramMarkdown('```const x = 10;```');
            expect(result).toBe('<pre><code>const x = 10;</code></pre>');
        });

        test('should handle inline code', () => {
            const result = MarkdownParser.parseTelegramMarkdown('`const x = 10;`');
            expect(result).toBe('<code>const x = 10;</code>');
        });

        test('should convert newlines to br tags', () => {
            const result = MarkdownParser.parseTelegramMarkdown('Line 1\nLine 2');
            expect(result).toBe('Line 1<br>Line 2');
        });

        test('should escape HTML tags in text', () => {
            const result = MarkdownParser.parseTelegramMarkdown('Text with <script>alert("xss")</script>');
            expect(result).toContain('&lt;script&gt;');
            expect(result).toContain('&lt;/script&gt;');
        });

        // Новые тесты для множественных элементов
        test('should handle multiple bold elements in a row', () => {
            const result = MarkdownParser.parseTelegramMarkdown('**bold1** **bold2**');
            expect(result).toBe('<strong>bold1</strong> <strong>bold2</strong>');
        });

        test('should handle multiple italic elements in a row', () => {
            const result = MarkdownParser.parseTelegramMarkdown('/italic1/ /italic2/');
            expect(result).toBe('<em>italic1</em> <em>italic2</em>');
        });

        test('should handle multiple underline elements in a row', () => {
            const result = MarkdownParser.parseTelegramMarkdown('__underline1__ __underline2__');
            expect(result).toBe('<strong>underline1</strong> <strong>underline2</strong>');
        });

        test('should handle mixed multiple formatting elements', () => {
            const result = MarkdownParser.parseTelegramMarkdown('**bold** /italic/ __underline__');
            expect(result).toBe('<strong>bold</strong> <em>italic</em> <strong>underline</strong>');
        });

        test('should handle multiple links in a row', () => {
            const result = MarkdownParser.parseTelegramMarkdown('[Google](https://google.com) [GitHub](https://github.com)');
            expect(result).toBe('<a href="https://google.com" target="_blank" rel="noopener noreferrer">Google</a> <a href="https://github.com" target="_blank" rel="noopener noreferrer">GitHub</a>');
        });

        test('should handle multiple code blocks in a row', () => {
            const result = MarkdownParser.parseTelegramMarkdown('```code1``` ```code2```');
            expect(result).toBe('<pre><code>code1</code></pre> <pre><code>code2</code></pre>');
        });

        test('should handle multiple inline codes in a row', () => {
            const result = MarkdownParser.parseTelegramMarkdown('`code1` `code2`');
            expect(result).toBe('<code>code1</code> <code>code2</code>');
        });

        test('should handle complex mixed content with multiple elements', () => {
            const input = '**Bold1** and **Bold2** with /italic1/ and /italic2/\n```code block1``` and ```code block2```\n`inline1` and `inline2`';
            const result = MarkdownParser.parseTelegramMarkdown(input);

            expect(result).toContain('<strong>Bold1</strong>');
            expect(result).toContain('<strong>Bold2</strong>');
            expect(result).toContain('<em>italic1</em>');
            expect(result).toContain('<em>italic2</em>');
            expect(result).toContain('<pre><code>code block1</code></pre>');
            expect(result).toContain('<pre><code>code block2</code></pre>');
            expect(result).toContain('<code>inline1</code>');
            expect(result).toContain('<code>inline2</code>');
            expect(result).toContain('<br>');
        });

        test('should handle adjacent formatting without spaces', () => {
            const result = MarkdownParser.parseTelegramMarkdown('**bold**/italic/');
            expect(result).toBe('<strong>bold</strong><em>italic</em>');
        });

        test('should handle formatting with special characters', () => {
            const result = MarkdownParser.parseTelegramMarkdown('**bold with "quotes"** /italic with \'apostrophes\'/');
            expect(result).toContain('<strong>bold with &quot;quotes&quot;</strong>');
            expect(result).toContain('<em>italic with &#x27;apostrophes&#x27;</em>');
        });

        test('should handle empty formatting markers', () => {
            const result = MarkdownParser.parseTelegramMarkdown('**** ____ // __');
            expect(result).toBe('**** ____ // __'); // Должны остаться как есть
        });

        test('should handle nested formatting in code blocks', () => {
            const result = MarkdownParser.parseTelegramMarkdown('```**bold inside code** /italic inside code/```');
            expect(result).toBe('<pre><code>**bold inside code** /italic inside code/</code></pre>');
        });

        test('should handle URLs with special characters in links', () => {
            const result = MarkdownParser.parseTelegramMarkdown('[Test](https://example.com/path?query=value&other=thing)');
            expect(result).toContain('href="https://example.com/path?query=value&amp;other=thing"');
        });
    });

    describe('sanitizeHtml', () => {
        test('should escape HTML tags', () => {
            const result = MarkdownParser.sanitizeHtml('<script>alert("xss")</script>');
            expect(result).toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
        });

        test('should handle multiple HTML tags', () => {
            const result = MarkdownParser.sanitizeHtml('<div><span>test</span></div>');
            expect(result).toBe('&lt;div&gt;&lt;span&gt;test&lt;/span&gt;&lt;/div&gt;');
        });
    });

    describe('formatComment', () => {
        test('should return empty string for empty input', () => {
            expect(MarkdownParser.formatComment('')).toBe('');
            expect(MarkdownParser.formatComment(null)).toBe('');
            expect(MarkdownParser.formatComment(undefined)).toBe('');
        });

        test('should delegate to parseTelegramMarkdown', () => {
            const result = MarkdownParser.formatComment('**test**');
            expect(result).toBe('<strong>test</strong>');
        });

        test('should handle complex comments with multiple elements', () => {
            const input = '**Important**: Please check /this/ and [link](url)';
            const result = MarkdownParser.formatComment(input);

            expect(result).toContain('<strong>Important</strong>');
            expect(result).toContain('<em>this</em>');
            expect(result).toContain('<a href="url" target="_blank" rel="noopener noreferrer">link</a>');
        });
    });

    describe('edge cases', () => {
        test('should handle very long text', () => {
            const longText = 'A'.repeat(1000) + '**bold**' + 'B'.repeat(1000);
            const result = MarkdownParser.parseTelegramMarkdown(longText);
            expect(result).toContain('<strong>bold</strong>');
        });

        test('should handle text with only formatting', () => {
            const result = MarkdownParser.parseTelegramMarkdown('**bold**');
            expect(result).toBe('<strong>bold</strong>');
        });

        test('should handle text with only code', () => {
            const result = MarkdownParser.parseTelegramMarkdown('```code```');
            expect(result).toBe('<pre><code>code</code></pre>');
        });

        test('should handle mixed line endings', () => {
            const result = MarkdownParser.parseTelegramMarkdown('Line 1\r\nLine 2\nLine 3');
            expect(result).toBe('Line 1<br>Line 2<br>Line 3');
        });
    });
});