# Keystore Configuration

This directory contains the keystores used to sign the Android application.

## Files

- **debug.keystore**: Used for debug builds. Safe to commit to version control.
- **release.keystore**: Used for release builds. **NEVER commit to version control!**

## Setup for Release Builds

1. The release keystore has been generated with placeholder credentials
2. Copy `keystore.properties.template` to `keystore.properties` in the project root
3. Update the credentials in `keystore.properties` with secure values
4. **Important**: Change the keystore passwords using:

```bash
# Change keystore password
keytool -storepasswd -keystore app/keystore/release.keystore

# Change key password
keytool -keypasswd -alias pokus-release -keystore app/keystore/release.keystore
```

## Current Placeholder Credentials (CHANGE THESE!)

- Store Password: `changeme123`
- Key Password: `changeme123`
- Key Alias: `pokus-release`

## Building Release APK

Once configured, build the release APK with:

```bash
./gradlew assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Security Notes

- The `keystore.properties` file is excluded from version control via `.gitignore`
- Never share your release keystore or its passwords
- Keep backups of your release keystore in a secure location
- Losing your release keystore means you cannot update your app on Play Store
