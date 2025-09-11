export class DateFieldHandler {
    initDateFields() {
        document.querySelectorAll('input[placeholder="dd-MM-yyyy"]').forEach(input => {
            input._lastValueLength = 0;
        });
    }

    isValidDate(dateString) {
        const datePattern = /^(\d{2})-(\d{2})(-(\d{4}))?$/;
        const match = dateString.match(datePattern);

        if (!match) return false;

        let day = parseInt(match[1], 10);
        let month = parseInt(match[2], 10);
        let year = match[4] ? parseInt(match[4], 10) : new Date().getFullYear();

        if (month < 1 || month > 12) return false;

        const daysInMonth = new Date(year, month, 0).getDate();
        return day >= 1 && day <= daysInMonth;
    }

    formatDateForBackend(dateString) {
        const datePattern = /^(\d{2})-(\d{2})(-(\d{4}))?$/;
        const match = dateString.match(datePattern);

        if (!match) return dateString;

        let day = match[1];
        let month = match[2];
        let year = match[4] ? match[4] : new Date().getFullYear();

        return `${day}-${month}-${year}`;
    }

    formatDateInput(input) {
        const cursorPosition = input.selectionStart;
        const originalValue = input.value;
        const isDeletion = originalValue.length < input._lastValueLength;

        input._lastValueLength = originalValue.length;

        const allDigits = originalValue.replace(/\D/g, '');

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

        input.value = formattedValue;

        let newCursorPosition;

        if (isDeletion) {
            newCursorPosition = Math.min(cursorPosition, formattedValue.length);
        } else {
            const digitsBeforeCursor = originalValue.substring(0, cursorPosition).replace(/\D/g, '').length;

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

        if (value === '') {
            input.classList.remove('is-invalid');
            return true;
        }

        if (this.isValidDate(value)) {
            input.classList.remove('is-invalid');
            return true;
        } else {
            input.classList.add('is-invalid');
            return false;
        }
    }
}