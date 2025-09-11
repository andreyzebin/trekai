export class FieldEditor {
    constructor(itemView) {
        this.itemView = itemView;
        console.log('📝 FieldEditor created');
    }

    initEditableFields() {
        const editableContainers = document.querySelectorAll('.editable-container');
        console.log(`🔍 Found ${editableContainers.length} editable containers`);

        editableContainers.forEach((container, index) => {
            const display = container.querySelector('.editable-display');
            const input = container.querySelector('.editable-input');

            if (display && input) {
                console.log(`⚙️ Setting up editable field ${index + 1}:`, {
                    id: input.id,
                    type: input.type
                });

                display.addEventListener('focus', () => {
                    console.log('👆 Field focus:', input.id);
                    this.activateEditor(container);
                });

                display.addEventListener('click', () => {
                    console.log('🖱️ Field click:', input.id);
                    this.activateEditor(container);
                });

                input.addEventListener('blur', () => {
                    console.log('👋 Field blur:', input.id);
                    this.deactivateEditor(container);
                });

                // Обработка Escape для отмены изменений
                input.addEventListener('keydown', (e) => {
                    if (e.key === 'Escape') {
                        console.log('⎋ Escape pressed, canceling edit');
                        this.deactivateEditor(container, false); // Не сохранять
                        e.preventDefault();
                    }
                });
            }
        });

        console.log('✅ Editable fields initialization completed');
    }

    activateEditor(container) {
        console.log('🎬 Activating editor');
        const display = container.querySelector('.editable-display');
        const input = container.querySelector('.editable-input');

        if (!display || !input) {
            console.warn('⚠️ Editor elements not found in container');
            return;
        }

        // Сохраняем оригинальное значение для возможной отмены
        input.dataset.tempOriginalValue = input.dataset.originalValue;

        display.classList.add('hidden');
        input.value = input.dataset.originalValue || '';
        input.classList.remove('hidden');

        console.log('🔍 Editor activated for field:', input.id);

        setTimeout(() => {
            input.focus();
            console.log('🎯 Focus set to input field');

            if (input.type === 'text' && input.hasAttribute('placeholder') &&
                input.getAttribute('placeholder') === 'dd-MM-yyyy') {
                input.setSelectionRange(0, 0);
                console.log('📅 Date field - cursor set to start');
            } else if (input.type === 'text' || input.tagName === 'TEXTAREA') {
                const length = input.value.length;
                input.setSelectionRange(length, length);
                console.log('📝 Text field - cursor set to end');
            }
        }, 10);
    }

    deactivateEditor(container, save = true) {
        console.log('⏹️ Deactivating editor, save:', save);
        const display = container.querySelector('.editable-display');
        const input = container.querySelector('.editable-input');
        const editText = container.querySelector('.edit-text');

        if (!display || !input || !editText) {
            console.warn('⚠️ Editor elements not found for deactivation');
            return;
        }

        const newValue = input.value.trim();
        const originalValue = (input.dataset.originalValue || '').trim();

        console.log('📊 Field values:', { newValue, originalValue, changed: newValue !== originalValue });

        // Сначала обновляем отображение, потом синхронизируем с сервером
        if (save && newValue !== originalValue) {
            console.log('🔄 Updating display immediately');

            // Для date полей используем специальную обработку
            if (input.type === 'text' && input.hasAttribute('placeholder') &&
                input.getAttribute('placeholder') === 'dd-MM-yyyy') {

                if (this.itemView.dateFieldHandler.validateDateField(input)) {
                    // Получаем полное значение с годом для отображения
                    const formattedValue = this.itemView.dateFieldHandler.getFullDateValue(input);
                    editText.textContent = formattedValue;
                    // Также обновляем значение в input для последующей отправки
                    input.value = formattedValue;
                    console.log('📅 Date formatted for display and backend:', formattedValue);
                } else {
                    // Если валидация не прошла, восстанавливаем оригинальное значение
                    editText.textContent = originalValue;
                    input.value = originalValue;
                    console.log('❌ Date validation failed, restoring original value');
                }
            } else {
                // Для обычных полей просто обновляем текст
                editText.textContent = newValue;
                console.log('✅ Text updated in display');
            }
        } else if (!save) {
            // Отмена изменений - восстанавливаем оригинальное значение
            editText.textContent = input.dataset.tempOriginalValue || originalValue;
            input.value = input.dataset.tempOriginalValue || originalValue;
            console.log('❌ Edit canceled, restoring original value');
        }

        // Всегда скрываем input и показываем display
        display.classList.remove('hidden');
        input.classList.add('hidden');

        // Запускаем синхронизацию с сервером только если нужно сохранить и есть изменения
        if (save && newValue !== originalValue) {
            if (input.type === 'text' && input.hasAttribute('placeholder') &&
                input.getAttribute('placeholder') === 'dd-MM-yyyy') {

                if (this.itemView.dateFieldHandler.validateDateField(input)) {
                    console.log('📅 Date field changed, triggering patch');
                    // Убедимся, что значение в input уже отформатировано с годом
                    const finalValue = this.itemView.dateFieldHandler.getFullDateValue(input);
                    input.value = finalValue; // На всякий случай обновляем еще раз

                    this.itemView.patchField(input).then(() => {
                        // После успешного сохранения обновляем originalValue
                        input.dataset.originalValue = input.value;
                        console.log('✅ Server sync completed');
                    }).catch(error => {
                        console.error('❌ Server sync failed:', error);
                        // В случае ошибки восстанавливаем оригинальное значение
                        editText.textContent = originalValue;
                        input.value = originalValue;
                        input.dataset.originalValue = originalValue;
                    });
                }
            } else {
                console.log('📝 Field changed, triggering patch');
                this.itemView.patchField(input).then(() => {
                    input.dataset.originalValue = newValue;
                    console.log('✅ Server sync completed');
                }).catch(error => {
                    console.error('❌ Server sync failed:', error);
                    editText.textContent = originalValue;
                    input.value = originalValue;
                    input.dataset.originalValue = originalValue;
                });
            }
        }

        console.log('✅ Editor deactivated');
    }
}