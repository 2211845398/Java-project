<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Student extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'email',
        'student_id',
        'phone',
        'address',
        'birth_date',
        'major',
    ];

    protected $casts = [
        'birth_date' => 'date',
    ];
}
