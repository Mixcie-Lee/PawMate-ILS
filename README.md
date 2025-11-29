# PawMate - Pet Adoption App

PawMate is a modern Android application built with Jetpack Compose that connects pet adopters with adoption centers. The app features a Tinder-like swiping interface for browsing pets, real-time chat functionality, educational resources, and a gem-based reward system.

## 📱 Key Features

### 1. **Pet Swiping Interface**
- Tinder-style card swiping to browse available pets
- Filter pets by type (All, Dogs, Cats)
- Detailed pet information modal (hold to view)
- Swipe right to like, left to pass
- Gem-based system: 5 gems per swipe (10 gems on signup)

### 2. **User Authentication**
- Email/Password authentication
- Google Sign-In integration
- Role-based access (Adopter/Shelter)
- Email verification support
- Onboarding flow for first-time users
- Welcome popup for returning users

### 3. **Real-Time Chat System**
- Direct messaging between adopters and shelters
- Real-time message synchronization
- Channel-based conversation management
- Unread message count tracking

### 4. **Educational Resources**
- Educational articles on pet care topics
- Embedded YouTube video tutorials
- Step-by-step guides for:
  - Pet care basics
  - Training tips
  - Health and wellness
  - Nutrition guidelines
  - Grooming techniques
  - Behavioral management
  - Adoption preparation
- Tutorial images shown once on first visit

### 5. **Adoption Center Dashboard** (Shelter Role)
- Add new pets to the platform
- View all listed pets
- Manage adoption applications
- Track pet listings and status

### 6. **Profile & Settings**
- User profile management
- Account settings
- Gem counter display
- Dark mode support
- Theme customization

### 7. **Gem System**
- Starting balance: 10 gems for new users
- Swipe cost: 5 gems per swipe
- In-app gem purchases (4 packages available)
- Gem count synced with Firestore
- Persistent storage via SharedPreferences

## 📋 Requirements

### System Requirements
- **Operating System**: Windows 10/11, macOS 10.15+, or Linux (Ubuntu 18.04+)
- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: At least 10GB free space for Android Studio, SDK, and project files
- **Internet Connection**: Required for Firebase services and dependency downloads

### IDE Requirements
- **Android Studio** (Required)
  - Version: Hedgehog (2023.1.1) or later
  - Download from [developer.android.com/studio](https://developer.android.com/studio)
  - Recommended: Latest stable version (Flamingo, Giraffe, or Hedgehog)
  - Android Studio includes:
    - Android SDK Manager
    - Android Emulator
    - Gradle build system
    - Kotlin plugin
    - Integrated Development Environment (IDE) for Android development

  - **Android Studio System Requirements**:
    - **Operating System**:
      - Windows: Windows 10/11 (64-bit)
      - macOS: macOS 10.15 (Catalina) or later
      - Linux: Any 64-bit Linux distribution that supports Gnome, KDE, or Unity DE
    - **RAM**: 
      - Minimum: 8GB
      - Recommended: 16GB or more
      - Note: Android Studio uses significant memory, especially when running emulators
    - **Disk Space**:
      - Minimum: 8GB free space
      - Recommended: 16GB+ free space
      - Additional space needed for:
        - Android SDK and system images
        - Emulator AVDs (2-4GB each)
        - Build artifacts and caches
    - **Screen Resolution**:
      - Minimum: 1280 x 800
      - Recommended: 1920 x 1080 or higher
    - **Graphics**:
      - Hardware acceleration recommended for emulator performance
      - OpenGL 2.0 compatible graphics card
    - **Network**: Internet connection required for:
      - Initial setup and SDK downloads
      - Plugin updates
      - Firebase services

  - **Note**: Ensure Android Studio is fully updated before opening the project
  
  - **Alternative IDEs**: While Android Studio is recommended, you can also use:
    - IntelliJ IDEA (with Android plugin) - Not recommended for Android development
    - Visual Studio Code (with Android extensions) - Limited support, not recommended

### Development Tools

- **JDK (Java Development Kit)**: Version 11 or later
  - Android Studio includes JDK 11 by default
  - Can also use OpenJDK 11, 17, or 21
  - Verify installation: `java -version`

- **Gradle**: Version 8.0+ (included with Android Studio)
  - Wrapper included in project (`gradlew` / `gradlew.bat`)
  - No manual installation required

- **Kotlin**: Version 1.9.0+ (managed by Gradle)
  - Automatically downloaded via build configuration

### Android SDK Requirements
- **Minimum SDK Version**: 31 (Android 12)
- **Target SDK Version**: 35
- **Compile SDK Version**: 35
- **Build Tools Version**: 35.0.0

**Required SDK components** (install via SDK Manager):
- Android SDK Platform 31, 33, 35
- Android SDK Build-Tools 35.0.0
- Android SDK Platform-Tools
- Android SDK Command-line Tools
- Google Play services
- Android Emulator (for testing)

### Firebase Requirements
- **Firebase Account**: Google account required
- **Firebase Project**: Create at [Firebase Console](https://console.firebase.google.com/)
- **Firebase Services** (must be enabled):
  - Authentication (Email/Password + Google Sign-In)
  - Cloud Firestore Database
  - Realtime Database
- **google-services.json**: Download from Firebase Console and place in `app/` directory

### Testing Requirements
- **Physical Device**: Android 12+ (API 31+) with USB debugging enabled
  
  **OR**
  
- **Android Emulator**: API 31+ (Android 12+) with Google Play Services
  - Recommended: Pixel 5 or newer device profile
  - Minimum: 2GB RAM allocated to emulator

### Additional Tools (Optional but Recommended)
- **Git**: For version control
  - Windows: [Git for Windows](https://git-scm.com/download/win)
  - macOS: `brew install git` or Xcode Command Line Tools
  - Linux: `sudo apt-get install git`

- **ADB (Android Debug Bridge)**: Included with Android SDK Platform-Tools
  - Used for device debugging and APK installation

## 🚀 Installation

### Prerequisites
- All requirements listed above must be met
- Firebase project created and configured

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd pawmate-ils
   ```

2. **Firebase Configuration**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password and Google Sign-In)
   - Enable Cloud Firestore Database
   - Enable Realtime Database
   - Download `google-services.json` and place it in `app/` directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```
   On Windows, use:
   ```bash
   gradlew.bat build
   ```

4. **Run the app**
   - Connect an Android device or start an emulator (API 31+)
   - Click "Run" in Android Studio or execute:
   ```bash
   ./gradlew installDebug
   ```
   On Windows, use:
   ```bash
   gradlew.bat installDebug
   ```

## 🔐 Permissions Required

The app requires the following permissions (declared in `AndroidManifest.xml`):

- **INTERNET** - Required for Firebase services and network requests
- **ACCESS_NETWORK_STATE** - To check network connectivity
- **READ_EXTERNAL_STORAGE** - For accessing pet images (Android 12 and below)
- **READ_MEDIA_IMAGES** - For accessing pet images (Android 13+)

### Manifest Configuration
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## 🗄️ Database Structure

### Firebase Firestore Collections

#### 1. **users** Collection
```kotlin
{
  id: String (document ID = user UID)
  name: String
  email: String
  role: String ("adopter" | "shelter")
  gems: Int (default: 10)
  likedPetsCount: Int
  photoUri: String?
  MobileNumber: String?
  Address: String?
  Age: String?
  createdAt: Long (timestamp)
  lastActive: Long? (timestamp)
  isNewUser: Boolean
  isOnline: Boolean
  shelterName: String? (for shelters)
  adopterName: String? (for adopters)
}
```

#### 2. **pets** Collection
```kotlin
{
  name: String
  breed: String
  age: String
  description: String
  type: String ("dog" | "cat")
  imageRes: Int (local resource ID)
  additionalImages: List<Int>
  shelterId: String
  shelterName: String?
  validationStatus: Boolean
}
```

#### 3. **likedPets** Collection (Subcollection under users)
```kotlin
{
  petId: String
  petName: String
  petBreed: String
  petAge: String
  petType: String
  petImageRes: Int
  likedAt: Long (timestamp)
}
```

### Firebase Realtime Database

#### **channels** Node
```kotlin
// Structure: /channels/{channelId}
{
  adopterId: String
  adopterName: String
  shelterId: String
  shelterName: String
  petName: String
  lastMessage: String
  timestamp: Long
  unreadCount: Int
  createdAt: Long
}
```

#### **messages** Node
```kotlin
// Structure: /messages/{channelId}/{messageId}
{
  senderId: String
  senderName: String
  senderImage: String?
  receiverId: String
  messageText: String
  imageUrl: String?
  createdAt: Long
}
```

### Local Storage (SharedPreferences)

- **Onboarding Status**: `onboarding_completed` (Boolean)
- **Gem Count**: `gem_prefs` → `gem_count` (Int)
- **Theme Preference**: Managed by `ThemeManager`
- **User Settings**: Managed by `SettingsManager`

## 🐛 Recent Bug Fixes and Improvements

### Authentication & User Management
- ✅ **Fixed gem count persistence on login**: Gems now properly sync from Firestore to local storage when users log in
- ✅ **Fixed onboarding flow**: Onboarding screen now appears only once for first-time users
- ✅ **Fixed welcome popup**: Welcome popup correctly shows for authenticated returning users
- ✅ **Fixed user role fetching**: User role now fetched directly from Firestore instead of relying on cached state

### UI/UX Improvements
- ✅ **Redesigned pet detail modal**: Improved "hold screen" with larger icons, better typography, and custom info cards
- ✅ **Fixed navigation bar highlighting**: Bottom navigation bar now accurately reflects the current active screen
- ✅ **Dark mode consistency**: Implemented consistent dark mode theming across all screens
- ✅ **Fixed tutorial dialog styling**: Added dark mode support to tutorial AlertDialogs
- ✅ **Replaced app logo**: Updated to `blackpawmateicon3.png` across all screens
- ✅ **Replaced gem icon**: Changed from emoji to `diamond.png` image asset

### Educational Screen
- ✅ **Added tutorial images**: Implemented `educationaltuto1.png` and `eductionaltuto2.png` that appear only once
- ✅ **Fixed video playback**: Integrated YouTube player library with fallback to external app/browser
- ✅ **Error handling**: Added robust error handling for YouTube embedding restrictions (Error Code 15)

### Gem System
- ✅ **Restored full gem system**: Re-implemented gem counter, purchase dialog, and consumption logic
- ✅ **Fixed gem synchronization**: Gems now properly sync between Firestore and local storage
- ✅ **Starting balance**: New users receive 10 gems on signup
- ✅ **Swipe cost**: Set to 5 gems per swipe

### Crash Fixes
- ✅ **Fixed AdopterLikeScreen crashes**: Implemented `SafePetImage` composable to handle invalid image resources
- ✅ **Fixed navigation crashes**: Resolved "Navigation destination not found" errors
- ✅ **Fixed WebView crashes**: Enhanced WebView configuration and added proper error handling
- ✅ **Fixed property delegate errors**: Corrected state management in MainActivity

### Code Quality
- ✅ **Fixed Material3 API usage**: Updated `Icons.Default.*` to `Icons.Filled.*` for consistency
- ✅ **Fixed button colors**: Changed `TextButtonDefaults` to `ButtonDefaults.textButtonColors()`
- ✅ **Removed unused imports**: Cleaned up unused import directives
- ✅ **Fixed duplicate definitions**: Removed duplicate `isTablet` definitions

## 🛠️ Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Firebase
  - Authentication
  - Cloud Firestore
  - Realtime Database
- **Image Loading**: Coil
- **Video Player**: Android YouTube Player Library
- **State Management**: StateFlow, LiveData
- **Navigation**: Jetpack Navigation Compose
- **Dependency Injection**: Manual (ViewModelFactory pattern)

## 📦 Key Dependencies

- Jetpack Compose BOM: 2025.06.01
- Material3: Latest
- Firebase BOM: 34.2.0
- Coil: 2.4.0
- Android YouTube Player: com.pierfrancescosoffritti.androidyoutubeplayer
- Navigation Compose: Latest
- Lifecycle ViewModel: 2.9.1

## 📝 Notes

- Minimum SDK: 31 (Android 12)
- Target SDK: 35
- Compile SDK: 35
- The app uses hardware acceleration for better performance
- Cleartext traffic is enabled for development (disable in production)

## 👥 User Roles

### Adopter
- Browse and swipe through pets
- Like pets and view liked pets list
- Chat with shelters
- Access educational resources
- Manage profile and settings

### Shelter/Adoption Center
- Add new pets to the platform
- View and manage pet listings
- View adoption applications
- Chat with potential adopters
- Manage shelter profile

## 🔄 Future Improvements

- Push notifications for new matches and messages
- Advanced pet filtering (age, breed, size)

---

**Version**: 1.0  
**Last Updated**: 2025

## Ga's System Things:

- **Device Name**: DESKTOP-ELU4QBM
- **Operating System**: Windows 11 (Build 26100)
- **System Type**: 64-bit operating system, x64-based processor
- **Processor**: Intel(R) Core(TM) i5-10300H CPU @ 2.50GHz (2.50 GHz)
- **Installed RAM**: 32.0 GB (31.8 GB usable)
- **Device ID**: DD4BE020-F4A8-40FE-8F1E-1A369F9DDFFA
- **Product ID**: 00329-10458-00000-AA949
- **Pen and Touch**: No pen or touch input available
- **Workspace Path**: D:\Coding Backup

