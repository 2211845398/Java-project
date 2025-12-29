# اختبار WebSocket Server

## معلومات الاتصال

- **WebSocket URL:** `ws://localhost:8080`
- **Protocol:** WebSocket (ws://)
- **Port:** 8080

## كيفية الاختبار

### الطريقة 1: استخدام متصفح الويب (JavaScript Console)

افتح متصفح الويب (Chrome/Firefox) وافتح Developer Console (F12)، ثم نفذ:

```javascript
// الاتصال بالـ WebSocket Server
const ws = new WebSocket('ws://localhost:8080');

// عند الاتصال
ws.onopen = function() {
    console.log('Connected to WebSocket server');
    
    // إرسال رسالة تسجيل دخول
    ws.send(JSON.stringify({
        type: "LOGIN",
        username: "john",
        password: "password123"
    }));
};

// عند استقبال رسالة
ws.onmessage = function(event) {
    console.log('Received:', JSON.parse(event.data));
};

// عند إغلاق الاتصال
ws.onclose = function() {
    console.log('Connection closed');
};

// عند حدوث خطأ
ws.onerror = function(error) {
    console.error('WebSocket error:', error);
};
```

### الطريقة 2: استخدام أداة Postman

1. افتح Postman
2. اختر "New" → "WebSocket Request"
3. أدخل URL: `ws://localhost:8080`
4. اضغط "Connect"
5. أرسل رسالة JSON:
```json
{"type":"LOGIN","username":"john","password":"password123"}
```

### الطريقة 3: استخدام Flutter WebSocket Client

في Flutter، استخدم مكتبة `web_socket_channel`:

```dart
import 'package:web_socket_channel/web_socket_channel.dart';

final channel = WebSocketChannel.connect(
  Uri.parse('ws://localhost:8080'),
);

// إرسال رسالة
channel.sink.add(jsonEncode({
  'type': 'LOGIN',
  'username': 'john',
  'password': 'password123',
}));

// استقبال رسالة
channel.stream.listen((message) {
  print('Received: $message');
});
```

## أمثلة الرسائل

### 1. تسجيل الدخول (LOGIN)
```json
{
  "type": "LOGIN",
  "username": "john",
  "password": "password123"
}
```

### 2. البحث عن مستخدم (SEARCH_USER)
```json
{
  "type": "SEARCH_USER",
  "username": "j"
}
```

### 3. إنشاء محادثة (CREATE_CONVERSATION)
```json
{
  "type": "CREATE_CONVERSATION",
  "targetUsername": "jane"
}
```

### 4. إرسال رسالة (SEND_MESSAGE)
```json
{
  "type": "SEND_MESSAGE",
  "conversationId": 1,
  "content": "Hello!"
}
```

## ملاحظات مهمة

1. **لا حاجة لـ newline (`\n`)**: WebSocket يتعامل مع الرسائل تلقائياً
2. **JSON فقط**: جميع الرسائل يجب أن تكون JSON صحيح
3. **الاتصال المستمر**: WebSocket يحافظ على الاتصال مفتوحاً
4. **Bidirectional**: يمكن إرسال واستقبال الرسائل في أي وقت

## الفرق بين TCP Socket و WebSocket

| TCP Socket | WebSocket |
|------------|-----------|
| يحتاج `\n` في نهاية الرسالة | لا يحتاج |
| Connection-oriented | Connection-oriented مع handshake |
| Raw TCP | Protocol built on HTTP |
| `ServerSocket` / `Socket` | `WebSocketServer` |

