package com.navideck.flutter_gps_manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.Priority
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry


/** FlutterGpsManagerPlugin */
class FlutterGpsManagerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {
    private lateinit var channel: MethodChannel
    private val locationRequestCode = 852915
    private val pendingResults = mutableListOf<Result>()
    private var activity: Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel =
            MethodChannel(flutterPluginBinding.binaryMessenger, "navideck/flutter_gps_manager")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "turnOnGps" -> turnOnGps(result)
            "isOn" -> {
                val mLocationManager =
                    activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isOn: Boolean = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                result.success(isOn)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    private fun turnOnGps(result: Result) {
        val activity = activity ?: return
        val task = LocationServices
            .getSettingsClient(activity)
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(
                        LocationRequest
                            .Builder(1000L)
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .build()
                    ).build()
            )
        task.addOnSuccessListener(activity) {
            result.success(true)
        }
        task.addOnFailureListener(activity) { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(activity, locationRequestCode)
                    pendingResults.add(result)
                } catch (sendEx: SendIntentException) {
                    result.error("Error", "Error in starting resolution activity", null)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == locationRequestCode) {
            pendingResults.removeAll {
                it.success(resultCode == Activity.RESULT_OK)
                true
            }
        }
        return false
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
}
