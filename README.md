# Sentinel One X — Android starter

A starter app with biometric app access and a permission-based heuristic review of installed apps.

## Build
1. Open this folder in Android Studio (recent version).
2. Allow Gradle sync and install required SDK (API 35).
3. Build > Build APK(s).
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Current limitations
- This is a prototype, not a certified antivirus and cannot claim to outperform commercial products.
- It checks requested permissions and some install-source signals only. It does not scan files, inspect app code, detect malware signatures, or monitor continuously.
- `QUERY_ALL_PACKAGES` is highly restricted for Google Play publication. Review Play policy and use a narrower package visibility strategy if needed.
- Biometric lock only gates this app's UI. It does not prevent Android or other apps from uninstalling/disabling it.
- No cloud service, signature database, background monitoring, VPN/network filtering, or remediation is implemented yet.

## Privacy
The starter performs its checks locally and does not upload scan data.
