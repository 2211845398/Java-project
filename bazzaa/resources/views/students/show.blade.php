<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تفاصيل الطالب</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            min-height: 100vh;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        .content {
            padding: 30px;
        }
        .info-card {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 15px 0;
            border-bottom: 1px solid #ddd;
        }
        .info-row:last-child {
            border-bottom: none;
        }
        .info-label {
            font-weight: bold;
            color: #667eea;
            font-size: 18px;
        }
        .info-value {
            color: #333;
            font-size: 18px;
        }
        .btn {
            display: inline-block;
            padding: 12px 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            margin: 10px 5px;
            transition: transform 0.2s;
            border: none;
            cursor: pointer;
            font-size: 16px;
        }
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .btn-edit {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }
        .btn-back {
            background: #6c757d;
        }
        .actions {
            text-align: center;
            margin-top: 30px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>تفاصيل الطالب</h1>
        </div>
        <div class="content">
            <div class="info-card">
                <div class="info-row">
                    <span class="info-label">الاسم:</span>
                    <span class="info-value">{{ $student->name }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">البريد الإلكتروني:</span>
                    <span class="info-value">{{ $student->email }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">رقم الطالب:</span>
                    <span class="info-value">{{ $student->student_id }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">رقم الهاتف:</span>
                    <span class="info-value">{{ $student->phone ?? 'غير محدد' }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">العنوان:</span>
                    <span class="info-value">{{ $student->address ?? 'غير محدد' }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">تاريخ الميلاد:</span>
                    <span class="info-value">{{ $student->birth_date ? $student->birth_date->format('Y-m-d') : 'غير محدد' }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">التخصص:</span>
                    <span class="info-value">{{ $student->major ?? 'غير محدد' }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">تاريخ التسجيل:</span>
                    <span class="info-value">{{ $student->created_at->format('Y-m-d H:i') }}</span>
                </div>
            </div>

            <div class="actions">
                <a href="{{ route('students.edit', $student) }}" class="btn btn-edit">تعديل</a>
                <a href="{{ route('students.index') }}" class="btn btn-back">العودة للقائمة</a>
            </div>
        </div>
    </div>
</body>
</html>

