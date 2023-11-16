import 'package:flutter/services.dart';

class FlutterGpsManager {
  static const _methodChannel = MethodChannel('navideck/flutter_gps_manager');

  static Future<bool> turnOnGps() async {
    return (await _methodChannel.invokeMethod<bool>('turnOnGps')) ?? false;
  }

  static Future<bool> isOn() async {
    return (await _methodChannel.invokeMethod<bool>('isOn')) ?? false;
  }
}
