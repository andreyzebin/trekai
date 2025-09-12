const fs = require('fs');
const path = require('path');
const { JSDOM } = require('jsdom');

/**
 * Загружает ES6 модуль в изолированном DOM окружении
 */
function loadES6Module(modulePath) {
    const fullPath = path.resolve(__dirname, modulePath);
    const moduleCode = fs.readFileSync(fullPath, 'utf8');

    // Создаем изолированное окружение
    const dom = new JSDOM('<!DOCTYPE html><html><body></body></html>', {
        runScripts: 'outside-only',
        resources: 'usable'
    });

    const { window } = dom;
    const { document } = window;

    // Добавляем глобальные объекты
    global.window = window;
    global.document = document;
    global.console = console;

    // Выполняем код модуля
    const script = new window.Function('exports', 'module', moduleCode);

    try {
        const mockModule = { exports: {} };
        script(mockModule.exports, mockModule);
        return mockModule.exports;
    } catch (error) {
        console.error('Error loading ES6 module:', error);
        throw error;
    }
}

module.exports = { loadES6Module };