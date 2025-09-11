export class DateFieldHandler {
    initDateFields() {
        const dateFields = document.querySelectorAll('input[placeholder="dd-MM-yyyy"]');
        console.log(`📅 Found ${dateFields.length} date fields`);

        dateFields.forEach(input => {
            input._lastValueLength = 0;
            console.log('⚙️ Date field initialized:', input.id);
        });
    }

    isValidDate(dateString) {
        console.log('🔍 Validating date:', dateString);
        const datePattern = /^(\d{2})-(\d{2})(-(\d{4}))?$/;
        const match = dateString.match(datePattern);

        if (!match) {
            console.log('❌ Date format invalid');
            return false;
        }

        let day = parseInt(match[1], 10);
        let month = parseInt(match[2], 10);
        let year = match[4] ? parseInt(match[4], 10) : new Date().getFullYear();

        if (month < 1 || month > 12) {
            console.log('❌ Month invalid:', month);
            return false;
        }

        const daysInMonth = new Date(year, month, 0).getDate();
        const isValid = day >= 1 && day <= daysInMonth;

        console.log('📅 Date validation result:', {
            day, month, year, daysInMonth, isValid
        });

        return isValid;
    }

    // Форматирование для отображения (с подстановкой года если нужно)
    formatDateForDisplay(dateString) {
        console.log('🔄 Formatting date for display:', dateString);
        const datePattern = /^(\d{2})-(\d{2})(-(\d{4}))?$/;
        const match = dateString.match(datePattern);

        if (!match) {
            console.log('⚠️ No formatting needed for display - not a date pattern');
            return dateString;
        }

        let day = match[1];
        let month = match[2];
        let year = match[4] ? match[4] : new Date().getFullYear();

        const formatted = `${day}-${month}-${year}`;
        console.log('✅ Date formatted for display:', formatted);
        return formatted;
    }

    // Форматирование для сервера (всегда с годом)
    formatDateForBackend(dateString) {
        console.log('🔄 Formatting date for backend:', dateString);
        const datePattern = /^(\d{2})-(\d{2})(-(\d{4}))?$/;
        const match = dateString.match(datePattern);

        if (!match) {
            console.log('⚠️ No formatting needed for backend - not a date pattern');
            return dateString;
        }

        let day = match[1];
        let month = match[2];
        let year = match[4] ? match[4] : new Date().getFullYear();

        const formatted = `${day}-${month}-${year}`;
        console.log('✅ Date formatted for backend:', formatted);
        return formatted;
    }

    // Получение полного значения даты (с годом) из input
    getFullDateValue(input) {
        const value = input.value.trim();
        console.log('📅 Getting full date value from input:', value);

        if (!value) {
            console.log('📅 Empty date value');
            return value;
        }

        return this.formatDateForBackend(value);
    }

    formatDateInput(input) {
        console.log('⌨️ Date input detected');
        const cursorPosition = input.selectionStart;
        const originalValue = input.value;
        const isDeletion = originalValue.length < input._lastValueLength;

        console.log('📊 Input state:', {
            cursorPosition,
            originalValue,
            isDeletion,
            lastLength: input._lastValueLength
        });

        input._lastValueLength = originalValue.length;

        const allDigits = originalValue.replace(/\D/g, '');
        console.log('🔢 Extracted digits:', allDigits);

        let formattedValue = '';
        if (allDigits.length > 0) {
            formattedValue = allDigits.substring(0, 2);
        }
        if (allDigits.length > 2) {
            formattedValue += '-' + allDigits.substring(2, 4);
        }
        if (allDigits.length > 4) {
            formattedValue += '-' + allDigits.substring(4, 8);
        }

        console.log('🔄 Formatted value:', formattedValue);
        input.value = formattedValue;

        let newCursorPosition;

        if (isDeletion) {
            newCursorPosition = Math.min(cursorPosition, formattedValue.length);
            console.log('🔙 Deletion - cursor position:', newCursorPosition);
        } else {
            const digitsBeforeCursor = originalValue.substring(0, cursorPosition).replace(/\D/g, '').length;
            console.log('🔢 Digits before cursor:', digitsBeforeCursor);

            if (digitsBeforeCursor <= 2) {
                newCursorPosition = digitsBeforeCursor;
            } else if (digitsBeforeCursor <= 4) {
                newCursorPosition = 3 + (digitsBeforeCursor - 2);
            } else {
                newCursorPosition = 6 + (digitsBeforeCursor - 4);
            }
        }

        newCursorPosition = Math.min(newCursorPosition, formattedValue.length);
        input.setSelectionRange(newCursorPosition, newCursorPosition);

        this.validateDateField(input);
    }

    validateDateField(input) {
        const value = input.value.trim();
        console.log('🔍 Validating date field value:', value);

        if (value === '') {
            input.classList.remove('is-invalid');
            console.log('✅ Empty date - validation passed');
            return true;
        }

        // Используем форматированное значение для валидации
        const fullDateValue = this.getFullDateValue(input);
        const isValid = this.isValidDate(fullDateValue);

        if (isValid) {
            input.classList.remove('is-invalid');
            console.log('✅ Date validation passed');
            return true;
        } else {
            input.classList.add('is-invalid');
            console.log('❌ Date validation failed');
            return false;
        }
    }
}