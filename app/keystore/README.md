# Keystore Configuration

This directory contains the keystores used to sign the Android application.

## Files

- **debug.keystore**: Used for debug builds. Safe to commit to version control.
- **release.keystore**: Used for release builds. **NEVER commit to version control!**

## ✅ Keystore Status

The release keystore has been **generated with secure credentials**:
- **Algorithm**: RSA 2048-bit
- **Validity**: 10,000 days (~27 years)
- **Alias**: `pokus-release`
- **Distinguished Name**: CN=Pokus App, OU=Mobile Development, O=TSCorp, L=Jakarta, ST=Jakarta, C=ID

## Local Development Setup

The keystore credentials are stored in `keystore.properties` (excluded from git):

```bash
# Verify keystore.properties exists
cat ../../../keystore.properties

# Build release APK locally
./gradlew assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions CI/CD Setup

The project is configured for automated signed APK builds via GitHub Actions.

### Quick Setup

1. **Get the base64-encoded keystore**:
   ```bash
   cat keystore.base64
   ```

2. **Get credentials from keystore.properties**:
   ```bash
   cat keystore.properties
   ```

3. **Add GitHub Secrets** (Settings → Secrets → Actions):
   - `KEYSTORE_BASE64`: Entire contents of `keystore.base64`
   - `KEYSTORE_PASSWORD`: Value from `keystore.properties`
   - `KEY_PASSWORD`: Value from `keystore.properties`
   - `KEY_ALIAS`: `pokus-release`

📖 **Detailed instructions**: See [`.github/KEYSTORE_SETUP.md`](../../.github/KEYSTORE_SETUP.md)

## Building Release APK

### Locally
```bash
./gradlew assembleRelease
```

### Via GitHub Actions
Push to `master` branch and the CI pipeline will automatically build and upload a signed APK.

## Security Notes

⚠️ **Critical**:
- `keystore.properties` and `*.keystore` files are excluded from version control
- **NEVER** commit these files to git
- **NEVER** share keystore passwords publicly
- **BACKUP** the keystore securely - losing it means you cannot update your app on Play Store

## Backup Instructions

```bash
# Create encrypted backup
mkdir -p ~/secure-backups
cp release.keystore ~/secure-backups/pokus-release-$(date +%Y%m%d).keystore
cp ../../../keystore.properties ~/secure-backups/

# Store backup in secure location (encrypted drive, password manager, etc.)
```

## Verifying APK Signature

```bash
# Verify the signed APK
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# View signature details
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### "Keystore not found" error
Ensure `keystore.properties` exists in project root with correct `storeFile` path.

### "Incorrect password" error
Verify credentials in `keystore.properties` match the keystore.

### CI build fails
Check GitHub Secrets are configured correctly. See [KEYSTORE_SETUP.md](../../.github/KEYSTORE_SETUP.md).
