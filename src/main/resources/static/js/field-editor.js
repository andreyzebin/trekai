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

        display.classList.remove('hidden');
        input.classList.add('hidden');

        if (input.type === 'text' && input.hasAttribute('placeholder') &&
            input.getAttribute('placeholder') === 'dd-MM-yyyy') {

            if (save && newValue !== originalValue) {
                console.log('📅 Date field changed, triggering patch');
                this.itemView.patchField(input);
            }
        } else {
            if (save && newValue !== originalValue) {
                console.log('📝 Field changed, triggering patch');
                this.itemView.patchField(input);
            }
        }

        console.log('✅ Editor deactivated');
    }
}