// Main entry point
import { FieldEditor } from './field-editor.js';
import { DateFieldHandler } from './date-field-handler.js';
import { ApiService } from './api-service.js';
import { WebSocketService } from './websocket-service.js';

class ItemView {
    constructor() {
        console.log('🚀 ItemView constructor called');
        this.baseUrl = window.BASE_URL;
        this.itemId = window.ITEM_ID;

        console.log(`📋 Initializing with BASE_URL: ${this.baseUrl}, ITEM_ID: ${this.itemId}`);

        this.fieldEditor = new FieldEditor(this);
        this.dateFieldHandler = new DateFieldHandler();
        this.apiService = new ApiService(this.baseUrl);
        this.webSocketService = new WebSocketService(this.baseUrl);

        this.init();
    }

    init() {
        console.log('🔧 ItemView initialization started');
        this.initEventListeners();
        this.initWebSocket();
        console.log('✅ ItemView initialization completed');
    }

    initEventListeners() {
        console.log('📝 Setting up event listeners');

            this.fieldEditor.initEditableFields();
            this.dateFieldHandler.initDateFields();
            this.initSelectFields();
            this.initGlobalEventHandlers();
            console.log('✅ All components initialized');

    }

    initGlobalEventHandlers() {
        console.log('🌐 Setting up global event handlers');

        // Обработка Enter в редактируемых полях
        document.addEventListener('keydown', (e) => {
            if (e.target.matches('.editable-input') && e.key === 'Enter') {
                console.log('⌨️ Enter key pressed in editable field');
                e.preventDefault();
                e.target.blur();
            }
        });

        // Форматирование date полей
        document.addEventListener('input', (e) => {
            if (e.target.matches('input[placeholder="dd-MM-yyyy"]')) {
                console.log('📅 Date field input detected');
                this.dateFieldHandler.formatDateInput(e.target);
            }
        });

        console.log('✅ Global event handlers setup completed');
    }

    initSelectFields() {
        const selectFields = document.querySelectorAll('select[data-item-id]');
        console.log(`🔍 Found ${selectFields.length} select fields with data-item-id`);

        // Детальное логирование каждого найденного поля
        if (selectFields.length > 0) {
            console.log('📋 List of found select fields:');
            selectFields.forEach((select, index) => {
                console.log(`   ${index + 1}. ID: ${select.id}, Name: ${select.name}, Data-item-id: ${select.dataset.itemId}`);
            });
        } else {
            console.log('⚠️ No select fields with data-item-id attribute found');
        }

        selectFields.forEach(select => {
            select.addEventListener('change', () => {
                console.log(`🔄 Select field changed: ${select.id}, New value: ${select.value}`);
                this.patchField(select);
            });

            // Логирование начального значения
            console.log(`📝 Select field initialized: ${select.id}, Initial value: ${select.value}`);
        });

        console.log(`✅ Successfully initialized ${selectFields.length} select fields`);
    }

    async patchField(component) {
        const currentValue = component.value;
        const originalValue = component.dataset.originalValue;
        const fieldName = component.id || component.dataset.fieldCode;

        console.log(`🔄 Patching field: ${fieldName}`, {
            currentValue,
            originalValue,
            isDateField: component.type === 'text' && component.hasAttribute('placeholder') &&
                        component.getAttribute('placeholder') === 'dd-MM-yyyy'
        });

        // Валидация date полей
        if (component.type === 'text' && component.hasAttribute('placeholder') &&
            component.getAttribute('placeholder') === 'dd-MM-yyyy') {

            console.log('📅 Validating date field');
            if (!this.dateFieldHandler.validateDateField(component)) {
                console.warn('❌ Date validation failed');
                return Promise.reject(new Error('Пожалуйста, введите корректную дату в формате dd-MM-yyyy'));
            }

            const formattedValue = this.dateFieldHandler.formatDateForBackend(component.value);
            console.log(`📅 Date formatted: ${component.value} -> ${formattedValue}`);
            component.value = formattedValue;
        }

        if (currentValue === originalValue) {
            console.log('⏩ No changes detected, skipping patch');
            return Promise.resolve(); // Возвращаем resolved promise
        }

        try {
            console.log(`📤 Sending PATCH request for field: ${fieldName}`);
            await this.apiService.patchField(this.itemId, fieldName, currentValue);
            component.dataset.originalValue = currentValue;
            console.log('✅ Patch successful');
            return Promise.resolve(); // Успешное завершение
        } catch (error) {
            console.error('❌ Patch failed:', error);
            return Promise.reject(error); // Пробрасываем ошибку
        }
    }

    initWebSocket() {
        if (!this.itemId) {
            console.warn('⚠️ WebSocket not initialized: itemId is null');
            return;
        }

        console.log('🔌 Initializing WebSocket connection');

        this.webSocketService.connect(this.itemId, {
            onConnected: () => {
                console.log('✅ WebSocket connected successfully');
                this.updateWsIndicator('connected', 'WebSocket connected');
            },
            onMessage: (message) => {
                console.log('📨 WebSocket message received:', message);
                console.log('🔄 Reloading page due to WebSocket update');
                location.reload();
            },
            onDisconnected: () => {
                console.log('🔌 WebSocket disconnected');
                this.updateWsIndicator('disconnected', 'WebSocket disconnected');
            },
            onError: (error) => {
                console.error('❌ WebSocket error:', error);
                this.updateWsIndicator('error', 'WebSocket error');
            }
        });
    }

    updateWsIndicator(status, title) {
        const wsIndicator = document.getElementById('ws-indicator');
        if (!wsIndicator) {
            console.warn('⚠️ WebSocket indicator element not found');
            return;
        }

        console.log(`🔵 WebSocket status changed to: ${status}`);

        wsIndicator.classList.remove('ws-connected', 'ws-disconnected', 'ws-error');

        switch (status) {
            case 'connected':
                wsIndicator.classList.add('ws-connected');
                break;
            case 'disconnected':
                wsIndicator.classList.add('ws-disconnected');
                break;
            case 'error':
                wsIndicator.classList.add('ws-error');
                break;
        }

        wsIndicator.setAttribute('title', title);
    }
}

if (document.readyState === 'loading') {
            console.log('⏳ Document still loading, waiting for DOMContentLoaded...');
            document.addEventListener('DOMContentLoaded', () => {
                console.log('📄 DOM fully loaded, initializing ItemView');
                window.itemView = new ItemView();
            });
} else {
    console.log('✅ DOM already loaded, initializing immediately');
    window.itemView = new ItemView();
}

console.log('📦 ItemView module loaded');