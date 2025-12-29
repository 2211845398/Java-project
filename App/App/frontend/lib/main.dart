import 'package:flutter/material.dart';
import 'screens/login_screen.dart';

/// =====================================================
/// Main Entry Point
/// =====================================================
/// This is the main entry point of the Flutter application.
/// It initializes the app and loads the LoginScreen first.
/// =====================================================

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Chat Application',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      // Start with LoginScreen
      home: const LoginScreen(),
    );
  }
}

