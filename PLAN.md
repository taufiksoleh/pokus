# Pokus - Android App Blocker Implementation Plan

## Overview
**Pokus** is a productivity-focused Android application built with Jetpack Compose that blocks access to selected applications (like social media apps) to help users stay focused. When a user tries to open a blocked app, a fullscreen overlay appears with a "You need to focus" message.

## Core Features
1. **App Blocking** - Block selected applications from being accessed
2. **Focus Mode** - Enable/disable blocking with a single tap
3. **Blocked App Selection** - Choose which apps to block from installed apps list
4. **Overlay Popup** - Show "You need to focus" screen when blocked app is opened
5. **Persistent Notification** - Show active notification when focus mode is enabled
6. **Schedule Support** - Set automatic focus schedules (optional future feature)

---

## Technical Architecture

### Required Android Permissions
```xml
<!-- Detect foreground app -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />

<!-- Show overlay on top of other apps -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- Run foreground service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

<!-- Query installed packages -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
    tools:ignore="QueryAllPackagesPermission" />

<!-- Post notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Receive boot completed to restart service -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### App Detection Method
Use **UsageStatsManager** to poll the current foreground app. This approach:
- Requires `PACKAGE_USAGE_STATS` permission (granted via Settings)
- Polls every 500ms-1000ms to detect app switches
- Works without Accessibility Service (less intrusive)

### Project Structure
```
app/src/main/java/com/tscorp/pokus/
├── MainActivity.kt                    # Main entry point
├── PokusApplication.kt               # Application class
│
├── data/
│   ├── local/
│   │   ├── PokusDatabase.kt          # Room database
│   │   ├── dao/
│   │   │   └── BlockedAppDao.kt      # DAO for blocked apps
│   │   └── entity/
│   │       └── BlockedApp.kt         # Entity for blocked app
│   ├── repository/
│   │   └── AppRepository.kt          # Repository for app data
│   └── preferences/
│       └── PreferencesManager.kt     # DataStore preferences
│
├── domain/
│   ├── model/
│   │   ├── InstalledApp.kt           # Model for installed app info
│   │   └── FocusState.kt             # Focus mode state
│   └── usecase/
│       ├── GetInstalledAppsUseCase.kt
│       ├── BlockAppUseCase.kt
│       └── CheckBlockedAppUseCase.kt
│
├── service/
│   ├── AppMonitorService.kt          # Foreground service to monitor apps
│   └── BootReceiver.kt               # Restart service on boot
│
├── ui/
│   ├── navigation/
│   │   └── PokusNavigation.kt        # Navigation setup
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt         # Main dashboard
│   │   │   └── HomeViewModel.kt
│   │   ├── applist/
│   │   │   ├── AppListScreen.kt      # Select apps to block
│   │   │   └── AppListViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt     # App settings
│   │   │   └── SettingsViewModel.kt
│   │   └── permissions/
│   │       └── PermissionsScreen.kt  # Permission request flow
│   ├── components/
│   │   ├── AppItem.kt                # App list item component
│   │   ├── FocusToggle.kt            # Focus mode toggle
│   │   └── BlockedAppCard.kt         # Blocked app display
│   └── overlay/
│       ├── BlockOverlayActivity.kt   # Fullscreen blocking overlay
│       └── BlockOverlayScreen.kt     # Compose UI for overlay
│
├── util/
│   ├── AppUtils.kt                   # App info utilities
│   ├── PermissionUtils.kt            # Permission helpers
│   └── NotificationHelper.kt         # Notification creation
│
└── di/
    └── AppModule.kt                  # Dependency injection (if using Hilt)
```

---

## Implementation Phases

### Phase 1: Project Setup & Dependencies
**Goal:** Set up the project foundation with all required dependencies

**Tasks:**
1. Update `build.gradle.kts` with dependencies:
   - Room database
   - Navigation Compose
   - ViewModel Compose
   - DataStore Preferences
   - Hilt (optional, for DI)
   - Coil (for app icons)

2. Update `AndroidManifest.xml` with:
   - Required permissions
   - Service declaration
   - Boot receiver
   - Overlay activity

3. Create `PokusApplication.kt` class

**Files to create/modify:**
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/tscorp/pokus/PokusApplication.kt`

---

### Phase 2: Data Layer
**Goal:** Implement data persistence for blocked apps

**Tasks:**
1. Create Room database with BlockedApp entity
2. Create DAO for CRUD operations
3. Create PreferencesManager for app settings (focus mode state)
4. Create AppRepository to manage data

**Files to create:**
- `data/local/entity/BlockedApp.kt`
- `data/local/dao/BlockedAppDao.kt`
- `data/local/PokusDatabase.kt`
- `data/preferences/PreferencesManager.kt`
- `data/repository/AppRepository.kt`

**BlockedApp Entity:**
```kotlin
@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)
```

---

### Phase 3: Domain Layer
**Goal:** Create business logic and models

**Tasks:**
1. Create InstalledApp model
2. Create use cases for app operations
3. Create utility functions for getting installed apps

**Files to create:**
- `domain/model/InstalledApp.kt`
- `domain/model/FocusState.kt`
- `domain/usecase/GetInstalledAppsUseCase.kt`
- `util/AppUtils.kt`

**InstalledApp Model:**
```kotlin
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val isBlocked: Boolean = false
)
```

---

### Phase 4: App Monitoring Service
**Goal:** Create background service that monitors foreground app

**Tasks:**
1. Create foreground service with notification
2. Implement UsageStatsManager polling
3. Detect when blocked app comes to foreground
4. Launch blocking overlay when blocked app detected
5. Create boot receiver to restart service

**Files to create:**
- `service/AppMonitorService.kt`
- `service/BootReceiver.kt`
- `util/NotificationHelper.kt`
- `util/PermissionUtils.kt`

**Service Flow:**
```
Service Started
    ↓
Create Notification Channel
    ↓
Start Foreground with Notification
    ↓
Start Polling Loop (every 500ms)
    ↓
Get Current Foreground App (UsageStatsManager)
    ↓
Check if App is in Blocked List
    ↓
If Blocked → Launch BlockOverlayActivity
    ↓
Continue Polling
```

---

### Phase 5: Blocking Overlay
**Goal:** Create fullscreen overlay that appears over blocked apps

**Tasks:**
1. Create BlockOverlayActivity with special window flags
2. Create Compose UI for the overlay
3. Handle back button to return to home
4. Add motivational message and icon

**Files to create:**
- `ui/overlay/BlockOverlayActivity.kt`
- `ui/overlay/BlockOverlayScreen.kt`

**Overlay Features:**
- Fullscreen display over blocked app
- "You need to focus" message
- App icon that was blocked
- "Go Back" button to return to home
- Optional: Countdown timer to unlock temporarily

---

### Phase 6: Main UI Screens
**Goal:** Create the main app interface with Jetpack Compose

**Tasks:**
1. Create navigation setup
2. Create Home screen with focus toggle
3. Create App List screen to select blocked apps
4. Create Settings screen
5. Create Permissions screen for onboarding

**Files to create:**
- `ui/navigation/PokusNavigation.kt`
- `ui/screens/home/HomeScreen.kt`
- `ui/screens/home/HomeViewModel.kt`
- `ui/screens/applist/AppListScreen.kt`
- `ui/screens/applist/AppListViewModel.kt`
- `ui/screens/settings/SettingsScreen.kt`
- `ui/screens/permissions/PermissionsScreen.kt`
- `ui/components/AppItem.kt`
- `ui/components/FocusToggle.kt`
- `ui/components/BlockedAppCard.kt`

**Home Screen Features:**
- Large toggle for Focus Mode ON/OFF
- Status indicator (Active/Inactive)
- Quick stats (apps blocked today, focus time)
- List of currently blocked apps
- Button to add/manage blocked apps

**App List Screen Features:**
- List all installed apps (non-system)
- Search/filter functionality
- Checkbox to select apps to block
- App icon, name, and package name display
- Save button

---

### Phase 7: Permission Flow
**Goal:** Guide users through required permission grants

**Tasks:**
1. Check required permissions on app start
2. Create permission request UI
3. Handle permission results
4. Navigate to Settings when needed

**Required Permissions Flow:**
1. **Usage Access** → Settings > Apps > Special access > Usage access
2. **Overlay Permission** → Settings > Apps > Special access > Display over other apps
3. **Notification Permission** (Android 13+) → Runtime permission

**Permission Screen UI:**
- Step-by-step permission guide
- Current permission status indicators
- Buttons to open relevant Settings pages
- "All permissions granted" confirmation

---

### Phase 8: Integration & Testing
**Goal:** Connect all components and test the complete flow

**Tasks:**
1. Integrate all layers in MainActivity
2. Test permission flow
3. Test app blocking functionality
4. Test overlay display
5. Test service persistence
6. Handle edge cases

**Test Scenarios:**
- [ ] App starts and shows permission screen if needed
- [ ] User can grant all permissions
- [ ] User can select apps to block
- [ ] Focus mode can be toggled on/off
- [ ] Blocked app triggers overlay when opened
- [ ] Overlay displays correctly over blocked app
- [ ] User can return home from overlay
- [ ] Service survives app close
- [ ] Service restarts on device boot
- [ ] Notification shows when focus mode active

---

## UI/UX Design Guidelines

### Color Scheme
- **Primary:** Deep Purple (#6750A4) - Focus and productivity
- **Secondary:** Teal (#03DAC6) - Active states
- **Background:** Dark (#1C1B1F) - Reduce eye strain
- **Surface:** Elevated dark (#2B2930)
- **Error:** Red (#CF6679) - Blocked state

### Typography
- Use Material 3 typography
- Large, clear text for focus message
- Readable list items

### Overlay Design
```
┌─────────────────────────────────┐
│                                 │
│                                 │
│         🎯                      │
│                                 │
│    You need to focus            │
│                                 │
│    [Instagram] is blocked       │
│                                 │
│    Stay focused on what         │
│    matters most                 │
│                                 │
│    ┌─────────────────────┐      │
│    │    Go Back Home     │      │
│    └─────────────────────┘      │
│                                 │
│    Focus time: 2h 30m           │
│                                 │
└─────────────────────────────────┘
```

### Home Screen Design
```
┌─────────────────────────────────┐
│  Pokus                     ⚙️   │
├─────────────────────────────────┤
│                                 │
│    ┌───────────────────────┐    │
│    │                       │    │
│    │     FOCUS MODE        │    │
│    │        🎯             │    │
│    │       [ON]            │    │
│    │                       │    │
│    └───────────────────────┘    │
│                                 │
│    Today's Stats                │
│    ├─ Apps blocked: 12          │
│    └─ Focus time: 2h 30m        │
│                                 │
│    Blocked Apps (5)        [+]  │
│    ┌─────────────────────────┐  │
│    │ 📸 Instagram            │  │
│    │ 📘 Facebook             │  │
│    │ 🐦 Twitter              │  │
│    │ 📱 TikTok               │  │
│    │ 📺 YouTube              │  │
│    └─────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
```

---

## Key Implementation Details

### 1. UsageStatsManager Implementation
```kotlin
fun getCurrentForegroundApp(context: Context): String? {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
        as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val beginTime = endTime - 1000 * 60 // Last minute

    val usageStatsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        beginTime,
        endTime
    )

    return usageStatsList
        ?.maxByOrNull { it.lastTimeUsed }
        ?.packageName
}
```

### 2. Overlay Window Flags
```kotlin
// For BlockOverlayActivity
window.setFlags(
    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
)
window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
```

### 3. Foreground Service Type
```xml
<service
    android:name=".service.AppMonitorService"
    android:foregroundServiceType="specialUse"
    android:exported="false">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="app_blocker" />
</service>
```

---

## Dependencies to Add

```kotlin
// build.gradle.kts (app)
dependencies {
    // Existing dependencies...

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coil for images
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

// Add KSP plugin
plugins {
    // ...existing plugins
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}
```

---

## Future Enhancements (Optional)
1. **Schedule Mode** - Set automatic focus schedules (work hours, etc.)
2. **App Categories** - Pre-defined categories (Social Media, Games, etc.)
3. **Statistics Dashboard** - Detailed usage analytics
4. **Strict Mode** - Prevent disabling focus mode for set duration
5. **Whitelist Mode** - Only allow specific apps during focus
6. **Widget** - Home screen widget for quick toggle
7. **Break Timer** - Allow short breaks with countdown
8. **Cloud Sync** - Sync settings across devices

---

## Implementation Order Summary

1. **Phase 1:** Project setup, dependencies, manifest
2. **Phase 2:** Data layer (Room, DataStore, Repository)
3. **Phase 3:** Domain layer (Models, Utils)
4. **Phase 4:** Background service (Monitoring, Notifications)
5. **Phase 5:** Blocking overlay (Activity, Compose UI)
6. **Phase 6:** Main UI screens (Home, App List, Settings)
7. **Phase 7:** Permission flow
8. **Phase 8:** Integration and testing

---

## Notes for Implementation

- Always test on physical device (emulator may not accurately simulate UsageStats)
- Handle Android version differences (especially for notifications and services)
- Consider battery optimization - don't poll too frequently
- Add proper error handling for permission denied scenarios
- Use StateFlow/SharedFlow for reactive UI updates
- Follow Material 3 design guidelines
- Test with various blocked apps to ensure overlay works correctly
