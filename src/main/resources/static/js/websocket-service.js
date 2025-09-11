export class WebSocketService {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
        this.stompClient = null;
        this.socket = null;
        console.log('🔌 WebSocketService created with baseUrl:', baseUrl);
    }

    connect(itemId, callbacks = {}) {
        console.log('🔗 Connecting WebSocket for item:', itemId);

        try {
            const wsUrl = `${this.baseUrl}ws`;
            console.log('🌐 Creating SockJS connection to:', wsUrl);

            this.socket = new SockJS(wsUrl);
            this.stompClient = Stomp.over(this.socket);

            // Enable debug logging
            this.stompClient.debug = (message) => {
                console.log('🔍 STOMP Debug:', message);
            };

            console.log('🔄 Attempting STOMP connection...');

            this.stompClient.connect({}, () => {
                console.log('✅ STOMP connection established');
                callbacks.onConnected?.();

                const subscriptionTopic = `/topic/item/${itemId}`;
                console.log('📡 Subscribing to topic:', subscriptionTopic);

                this.stompClient.subscribe(subscriptionTopic, (message) => {
                    console.log('📨 Received message on topic:', subscriptionTopic, message);
                    callbacks.onMessage?.(message);
                });

                console.log('✅ WebSocket subscription active');
            });

            this.socket.onclose = (event) => {
                console.log('🔌 WebSocket closed:', event);
                callbacks.onDisconnected?.(event);
            };

            this.socket.onerror = (error) => {
                console.error('❌ WebSocket error:', error);
                callbacks.onError?.(error);
            };

            console.log('✅ WebSocket connection initiated');

        } catch (error) {
            console.error("❌ WebSocket initialization error:", error);
            callbacks.onError?.(error);
        }
    }

    disconnect() {
        console.log('🔌 Disconnecting WebSocket');
        if (this.stompClient) {
            this.stompClient.disconnect();
            console.log('✅ STOMP client disconnected');
        }
        if (this.socket) {
            this.socket.close();
            console.log('✅ WebSocket closed');
        }
    }

    getConnectionStatus() {
        return this.stompClient?.connected ? 'connected' : 'disconnected';
    }
}