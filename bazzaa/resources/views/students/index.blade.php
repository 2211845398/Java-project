<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>قائمة الطلاب</title>
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
            max-width: 1200px;
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
        .btn {
            display: inline-block;
            padding: 12px 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            margin-bottom: 20px;
            transition: transform 0.2s;
            border: none;
            cursor: pointer;
            font-size: 16px;
        }
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .btn-danger {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }
        .btn-edit {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }
        .btn-view {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            padding: 15px;
            text-align: right;
            border-bottom: 1px solid #ddd;
        }
        th {
            background: #f8f9fa;
            font-weight: bold;
            color: #333;
        }
        tr:hover {
            background: #f8f9fa;
        }
        .actions {
            display: flex;
            gap: 10px;
        }
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .no-data {
            text-align: center;
            padding: 40px;
            color: #666;
        }
        .pagination {
            margin-top: 20px;
            display: flex;
            justify-content: center;
            gap: 10px;
        }
        .pagination a, .pagination span {
            padding: 10px 15px;
            background: #f8f9fa;
            color: #667eea;
            text-decoration: none;
            border-radius: 5px;
        }
        .pagination .active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>نظام إدارة الطلاب</h1>
            <p>قائمة جميع الطلاب المسجلين</p>
        </div>
        <div class="content">
            @if(session('success'))
                <div class="alert">
                    {{ session('success') }}
                </div>
            @endif

            <a href="{{ route('students.create') }}" class="btn">إضافة طالب جديد</a>

            @if($students->count() > 0)
                <table>
                    <thead>
                        <tr>
                            <th>الرقم</th>
                            <th>الاسم</th>
                            <th>البريد الإلكتروني</th>
                            <th>رقم الطالب</th>
                            <th>الهاتف</th>
                            <th>التخصص</th>
                            <th>الإجراءات</th>
                        </tr>
                    </thead>
                    <tbody>
                        @foreach($students as $student)
                            <tr>
                                <td>{{ $student->id }}</td>
                                <td>{{ $student->name }}</td>
                                <td>{{ $student->email }}</td>
                                <td>{{ $student->student_id }}</td>
                                <td>{{ $student->phone ?? 'غير محدد' }}</td>
                                <td>{{ $student->major ?? 'غير محدد' }}</td>
                                <td>
                                    <div class="actions">
                                        <a href="{{ route('students.show', $student) }}" class="btn btn-view">عرض</a>
                                        <a href="{{ route('students.edit', $student) }}" class="btn btn-edit">تعديل</a>
                                        <form action="{{ route('students.destroy', $student) }}" method="POST" style="display: inline;">
                                            @csrf
                                            @method('DELETE')
                                            <button type="submit" class="btn btn-danger" onclick="return confirm('هل أنت متأكد من حذف هذا الطالب؟')">حذف</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        @endforeach
                    </tbody>
                </table>

                <div class="pagination">
                    {{ $students->links() }}
                </div>
            @else
                <div class="no-data">
                    <h2>لا توجد طلاب مسجلين</h2>
                    <p>ابدأ بإضافة طالب جديد</p>
                </div>
            @endif
        </div>
    </div>
</body>
</html>

