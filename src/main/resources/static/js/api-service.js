export class ApiService {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
        console.log('🌐 ApiService created with baseUrl:', baseUrl);
    }

    async patchField(itemId, fieldCode, value) {
        console.log('📤 Preparing PATCH request:', {
            itemId,
            fieldCode,
            value,
            url: `${this.baseUrl}web/item/${itemId}`
        });

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        if (!csrfToken || !csrfHeader) {
            console.error('❌ CSRF token not found');
            throw new Error('CSRF protection error');
        }

        const payload = {
            customFields: {
                [fieldCode]: value
            }
        };

        console.log('📦 Request payload:', payload);

        try {
            const response = await fetch(`${this.baseUrl}web/item/${itemId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload)
            });

            console.log('📨 Response received:', {
                status: response.status,
                statusText: response.statusText
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('❌ Server error response:', errorText);
                throw new Error(errorText || `Server error: ${response.status}`);
            }

            console.log('✅ PATCH request successful');
            return response;

        } catch (error) {
            console.error('❌ Network error:', error);
            throw new Error(error.message || 'Network error');
        }
    }
}