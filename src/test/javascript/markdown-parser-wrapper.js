const fs = require('fs');
const path = require('path');

// Читаем исходный код ES6 модуля
const modulePath = path.resolve(__dirname, '../../main/resources/static/js/markdown-parser.js');
const moduleCode = fs.readFileSync(modulePath, 'utf8');

// Создаем изолированное окружение с помощью vm модуля
const { VM } = require('vm2');

const vm = new VM({
    sandbox: {
        console: console,
        document: {
            createElement: (tag) => {
                return {
                    textContent: '',
                    innerHTML: '',
                    set textContent(value) { this._textContent = value; },
                    get innerHTML() {
                        return this._textContent
                            .replace(/&/g, '&amp;')
                            .replace(/</g, '&lt;')
                            .replace(/>/g, '&gt;')
                            .replace(/"/g, '&quot;')
                            .replace(/'/g, '&#x27;');
                    }
                };
            }
        }
    }
});

// Выполняем код модуля
vm.run(moduleCode);

// Получаем класс из sandbox
const MarkdownParser = vm._context.MarkdownParser;

module.exports = { MarkdownParser };