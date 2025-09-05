# Did-It 📝

A beautiful, intuitive task management app that helps you track what you've accomplished and when you did it. Built with modern Android development practices and optimized for high-refresh-rate displays.

## ✨ Features

### 🎯 Core Functionality
- **Task Management**: Create, edit, and organize your daily tasks
- **Time Tracking**: Log when you complete tasks with timestamps
- **Smart Persistence**: Tasks are automatically saved and persist between app sessions
- **Undo Functionality**: Easily undo completed tasks if needed

### 🎨 User Experience
- **Modern Material Design**: Clean, intuitive interface following Material Design principles
- **Smooth Animations**: Fluid transitions and animations optimized for 120Hz displays
- **Dark/Light Themes**: Automatic theme switching based on system preferences
- **Haptic Feedback**: Tactile feedback for better user interaction

### 📝 Advanced Features
- **Rich Notes**: Add detailed notes to your tasks with scrollable text input
- **Smart Reminders**: Set custom reminders for important tasks
- **Multi-Select Mode**: Select and manage multiple tasks at once
- **Collapsible Interface**: Expandable task details with smooth animations

### 🔧 Technical Features
- **High Performance**: Optimized for devices with up to 120Hz refresh rates
- **Edge-to-Edge Display**: Modern full-screen experience
- **Widget Support**: Home screen widgets for quick task logging
- **Notification System**: Smart reminder notifications

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26 (Android 8.0) or higher
- Kotlin 2.0.21 or later

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/did-it.git
   cd did-it
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Build and Run**
   - Connect your Android device or start an emulator
   - Click the "Run" button or use `Ctrl+R` (Windows/Linux) or `Cmd+R` (Mac)

### Building from Command Line

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## 📱 Screenshots

*Add your app screenshots here*

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Android Views with Material Design Components
- **Architecture**: MVVM pattern with Repository pattern
- **Data Persistence**: SharedPreferences with Gson serialization
- **Build System**: Gradle with Kotlin DSL

### Project Structure
```
app/src/main/java/com/harshal/didit/
├── MainActivity.kt              # Main activity with task management
├── TaskAdapter.kt               # RecyclerView adapter for tasks
├── Task.kt                      # Task data model
├── TaskRepository.kt            # Data persistence layer
├── LauncherActivity.kt          # App launcher with theme selection
├── SetReminderDialogFragment.kt # Reminder dialog
├── NotificationHelper.kt        # Notification management
├── ReminderScheduler.kt         # Alarm scheduling
├── ReminderReceiver.kt          # Broadcast receiver for reminders
└── SimpleWidgetLogReceiver.kt   # Widget interaction handling
```

## 🎯 Usage

### Creating Tasks
1. Tap the "+" floating action button
2. Enter your task name
3. Optionally add notes and set reminders
4. Tap "Save" to create the task

### Logging Time
1. Tap the "Log Time" button on any task
2. The task will be marked as completed with a timestamp
3. Use the "Undo" button to revert if needed

### Managing Tasks
- **Expand**: Tap on a task to view notes and options
- **Edit Notes**: Tap the notes field to add or edit task details
- **Set Reminders**: Use the reminder button to schedule notifications
- **Multi-Select**: Long press to enter selection mode

### Themes
- The app automatically adapts to your system theme
- First-time users can choose their preferred theme
- Supports Light, Dark, and System themes

## 🔧 Configuration

### Build Configuration
The app is configured for:
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 36

### Performance Optimizations
- Optimized for high-refresh-rate displays (up to 120Hz)
- Efficient RecyclerView with view recycling
- Smooth animations with proper interpolators
- Hardware acceleration enabled

## 📋 Permissions

The app requires the following permissions:
- `POST_NOTIFICATIONS`: For reminder notifications
- `VIBRATE`: For haptic feedback

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Test on multiple device configurations
- Ensure accessibility compliance

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Material Design Components for Android
- Android Jetpack libraries
- The Android development community

## 📞 Support

If you encounter any issues or have questions:
- Open an issue on GitHub
- Check the existing issues for solutions
- Review the documentation

## 🔄 Version History

### v1.0.1-alpha
- Fixed keyboard input issues in notes
- Improved task persistence
- Enhanced UI animations
- Added comprehensive error handling

### v1.0.0-alpha
- Initial release
- Core task management functionality
- Theme selection
- Widget support
- Reminder system

---

**Made with ❤️ for productivity enthusiasts**
