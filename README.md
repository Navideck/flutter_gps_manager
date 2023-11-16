# Flutter Gps Manager

Flutter plugin to handle Gps state

## Getting Started

Add in your pubspec.yaml 

```yaml
flutter_gps_manager:
  git:
    url: git@github.com:Navideck/flutter_gps_manager.git
```

To check if Gps is On

```dart
FlutterGpsManager.isOn()
```

To turn on Gps programmatically

```dart
FlutterGpsManager.turnOnGps()
```

This method will show a dialog to turn on GPS, and returns boolean based on user's response
