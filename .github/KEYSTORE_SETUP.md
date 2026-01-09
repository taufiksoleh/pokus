# Keystore Setup for GitHub Actions

This guide explains how to set up the keystore for automated signed APK builds in GitHub Actions.

## Overview

The project uses a release keystore to sign production APKs. The keystore and credentials are stored securely as GitHub Secrets and decoded during the CI/CD pipeline.

## Prerequisites

- Repository admin access to manage GitHub Secrets
- The `keystore.base64` file (contains the base64-encoded keystore)
- The keystore credentials from `keystore.properties`

## Step 1: Get the Base64-Encoded Keystore

The keystore has been encoded to base64 and saved in `keystore.base64` in the project root. You can view it with:

```bash
cat keystore.base64
```

**Important**: This file is excluded from git via `.gitignore`. Keep it secure!

## Step 2: Configure GitHub Secrets

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add the following secrets:

### Required Secrets

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file | Copy entire contents of `keystore.base64` |
| `KEYSTORE_PASSWORD` | Keystore password | Get from `keystore.properties` (`storePassword` value) |
| `KEY_PASSWORD` | Key password | Get from `keystore.properties` (`keyPassword` value) |
| `KEY_ALIAS` | Key alias | Use: `pokus-release` |

### Getting the Values

#### KEYSTORE_BASE64
```bash
# Copy this entire output
cat keystore.base64
```

#### KEYSTORE_PASSWORD, KEY_PASSWORD, KEY_ALIAS
```bash
# View credentials
cat keystore.properties
```

Example output:
```
storePassword=xSnh5UiWf7DffI3xU4Qslkak
keyPassword=qlCpjHPrLZlUtsx8Y4Ub7Ozz
keyAlias=pokus-release
storeFile=app/keystore/release.keystore
```

## Step 3: Verify Setup

After adding the secrets:

1. Push changes to the `master` branch
2. GitHub Actions will trigger the `build-release` job
3. Check the workflow run for:
   - ✓ Keystore decoded successfully
   - ✓ keystore.properties created
   - Signed APK uploaded as artifact

## How It Works

The CI/CD pipeline (`ci.yml`) performs these steps for release builds:

1. **Decode Keystore**: Converts base64 secret back to binary keystore file
2. **Create Properties**: Generates `keystore.properties` from secrets
3. **Build Release APK**: Gradle uses the keystore to sign the APK
4. **Verify Signature**: Validates the APK is properly signed
5. **Upload Artifact**: Makes signed APK available for download

## Security Notes

⚠️ **Critical Security Information**:

- **NEVER** commit `keystore.properties` or `*.keystore` files to git
- **NEVER** share the keystore passwords publicly
- **BACKUP** the keystore file securely (losing it means you can't update your app)
- **ROTATE** credentials if they're ever exposed
- Only repository admins should have access to GitHub Secrets

## Backup Instructions

⚠️ **Create a backup NOW**:

```bash
# Create secure backup directory
mkdir -p ~/secure-backups/pokus-keystore

# Copy keystore and credentials
cp app/keystore/release.keystore ~/secure-backups/pokus-keystore/
cp keystore.properties ~/secure-backups/pokus-keystore/
cp keystore.base64 ~/secure-backups/pokus-keystore/

# Create backup archive with password
tar czf ~/pokus-keystore-backup-$(date +%Y%m%d).tar.gz \
  -C ~/secure-backups pokus-keystore

# Store this archive in a secure location (password manager, encrypted drive, etc.)
```

## Troubleshooting

### Build fails with "keystore not found"
- Verify `KEYSTORE_BASE64` secret is set correctly
- Check base64 decoding in workflow logs

### Build fails with "invalid keystore format"
- Keystore may be corrupted during base64 encoding/decoding
- Re-encode: `base64 -w 0 app/keystore/release.keystore > keystore.base64`

### Build fails with "incorrect password"
- Verify `KEYSTORE_PASSWORD` and `KEY_PASSWORD` secrets match `keystore.properties`
- Check for extra spaces or newlines in secret values

### APK signature verification fails
- Keystore may not be properly signed
- Try rebuilding: `./gradlew clean assembleRelease`

## Local Development

For local release builds, ensure `keystore.properties` exists in project root:

```bash
# Verify file exists
cat keystore.properties

# Build locally
./gradlew assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Rotating Credentials

If you need to generate a new keystore:

1. Generate new keystore:
```bash
keytool -genkeypair -v \
  -keystore app/keystore/release-new.keystore \
  -alias pokus-release-v2 \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

2. Update `keystore.properties` with new credentials
3. Re-encode and update GitHub secrets
4. **Important**: Keep the old keystore for existing app updates!

## Contact

For questions about keystore setup, contact the repository maintainers.
