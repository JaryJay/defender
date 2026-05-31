# defender
Accessibility-based Android app and site blocker.

## Features
- Always-blocked and blocked lists for apps and sites
- One daily pause window for the blocked list
- Wildcard site matching (for example, `*.instagram.com`)
- PIN-gated edits with a 30-second authorization window
- Haptic feedback on block (default on)
- Configurable daily summary notification

## Usage
1. Install the app and open **Defender**.
2. Enable the accessibility service from the in-app settings shortcut.
3. Add app package names and site patterns to the lists.
4. Configure the daily summary time and toggle as needed.

## Build
```bash
./gradlew assembleDebug
```
