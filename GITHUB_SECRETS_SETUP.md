# GitHub Secrets Setup - Quick Start Guide

This guide helps you configure GitHub Secrets for automated signed APK builds.

## 🚀 Quick Setup (5 minutes)

### Step 1: Get the Keystore Values

Run these commands in the project root:

```bash
# 1. Copy the base64-encoded keystore
cat keystore.base64

# 2. View the credentials
cat keystore.properties
```

You'll see output like:
```
storePassword=xSnh5UiWf7DffI3xU4Qslkak
keyPassword=qlCpjHPrLZlUtsx8Y4Ub7Ozz
keyAlias=pokus-release
storeFile=app/keystore/release.keystore
```

### Step 2: Add GitHub Secrets

1. Go to: `https://github.com/YOUR_USERNAME/pokus/settings/secrets/actions`
2. Click **"New repository secret"** for each of these:

| Secret Name | Value | Where to Get |
|-------------|-------|--------------|
| `KEYSTORE_BASE64` | *entire output* | From `cat keystore.base64` |
| `KEYSTORE_PASSWORD` | `xSnh5UiWf7DffI3xU4Qslkak` | From `keystore.properties` (storePassword) |
| `KEY_PASSWORD` | `qlCpjHPrLZlUtsx8Y4Ub7Ozz` | From `keystore.properties` (keyPassword) |
| `KEY_ALIAS` | `pokus-release` | Fixed value |

### Step 3: Verify

Push to `master` branch and check GitHub Actions builds a signed APK!

```bash
git push origin master
```

Then visit: `https://github.com/YOUR_USERNAME/pokus/actions`

---

## 📋 Adding Each Secret

### KEYSTORE_BASE64

1. Click **"New repository secret"**
2. Name: `KEYSTORE_BASE64`
3. Value: Copy **entire output** from:
   ```bash
   cat keystore.base64
   ```
4. Click **"Add secret"**

### KEYSTORE_PASSWORD

1. Click **"New repository secret"**
2. Name: `KEYSTORE_PASSWORD`
3. Value: Copy the `storePassword` value from `keystore.properties`
4. Click **"Add secret"**

### KEY_PASSWORD

1. Click **"New repository secret"**
2. Name: `KEY_PASSWORD`
3. Value: Copy the `keyPassword` value from `keystore.properties`
4. Click **"Add secret"**

### KEY_ALIAS

1. Click **"New repository secret"**
2. Name: `KEY_ALIAS`
3. Value: `pokus-release`
4. Click **"Add secret"**

---

## ✅ Verification Checklist

After setup, verify:

- [ ] All 4 secrets are added in GitHub Settings → Secrets
- [ ] Pushed code to `master` branch
- [ ] GitHub Actions workflow runs successfully
- [ ] `build-release` job completes without errors
- [ ] Signed APK artifact is available for download
- [ ] APK signature verification passes

---

## 🔐 Security Reminders

⚠️ **NEVER**:
- Commit `keystore.properties` or `*.keystore` files to git
- Share keystore passwords in issues, PRs, or messages
- Push `keystore.base64` to the repository

✅ **ALWAYS**:
- Keep `keystore.properties` and `*.keystore` excluded from git
- Backup keystore and credentials securely
- Store credentials in password manager
- Rotate credentials if exposed

---

## 🆘 Troubleshooting

### "Secret not found" error in Actions
- Verify secret names match exactly (case-sensitive)
- Check you added secrets to the correct repository

### "Invalid keystore format" error
- Re-encode keystore: `base64 -w 0 app/keystore/release.keystore > keystore.base64`
- Re-add `KEYSTORE_BASE64` secret

### "Incorrect password" error
- Double-check passwords from `keystore.properties`
- Ensure no extra spaces or newlines when copying

### Build succeeds but APK not signed
- Check workflow logs for keystore decoding steps
- Verify all 4 secrets are configured

---

## 📚 Additional Resources

- **Detailed Setup Guide**: [.github/KEYSTORE_SETUP.md](.github/KEYSTORE_SETUP.md)
- **Keystore Documentation**: [app/keystore/README.md](app/keystore/README.md)
- **CI Workflow**: [.github/workflows/ci.yml](.github/workflows/ci.yml)

---

## 🔄 For Team Members

If you need to set up locally:

1. Get credentials from maintainer or secure backup
2. Create `keystore.properties` in project root:
   ```bash
   cp keystore.properties.template keystore.properties
   nano keystore.properties  # Fill in actual credentials
   ```
3. Copy `app/keystore/release.keystore` from secure backup
4. Build: `./gradlew assembleRelease`

---

**Questions?** Contact the repository maintainers.
