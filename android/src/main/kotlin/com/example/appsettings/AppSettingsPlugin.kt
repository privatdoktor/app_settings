package com.example.appsettings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


class AppSettingsPlugin() : MethodCallHandler, FlutterPlugin, ActivityAware {
    /// Private variable to hold instance of Registrar for creating Intents.
    private var binding: FlutterPluginBinding? = null
    private var activityBinding: ActivityPluginBinding? = null

    /// Private method to open device settings window
    private fun openSettings(url: String, asAnotherTask: Boolean = false) {
        val activity = activityBinding?.activity
        if (activity == null) {
            return
        }
        try {
            val intent = Intent(url)
            if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Default to APP Settings if setting activity fails to load/be available on device
            openAppSettings(asAnotherTask)
        }
    }

    private fun openNotification(asAnotherTask: Boolean) {
        val activity = activityBinding?.activity
        if (activity == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= 21) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
            if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        } else {
            openSettings(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, asAnotherTask)
        }
    }

    private fun openAppSettings(asAnotherTask: Boolean = false) {
        val activity = activityBinding?.activity
        if (activity == null) {
            return
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }


    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        this.binding = binding
        val channel = MethodChannel(binding.binaryMessenger, "app_settings")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        this.binding = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activityBinding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activityBinding = binding
    }

    override fun onDetachedFromActivity() {
        activityBinding = null
    }

    /// Handler method to manage method channel calls.
    private fun handle(call: MethodCall) {
        val asAnotherTask = call.argument("asAnotherTask") ?: false

        when (call.method) {
            "wifi" -> {
                openSettings(Settings.ACTION_WIFI_SETTINGS, asAnotherTask)
            }
            "wireless" -> {
                openSettings(Settings.ACTION_WIRELESS_SETTINGS, asAnotherTask)
            }
            "location" -> {
                openSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS, asAnotherTask)
            }
            "security" -> {
                openSettings(Settings.ACTION_SECURITY_SETTINGS, asAnotherTask)
            }
            "bluetooth" -> {
                openSettings(Settings.ACTION_BLUETOOTH_SETTINGS, asAnotherTask)
            }
            "data_roaming" -> {
                openSettings(Settings.ACTION_DATA_ROAMING_SETTINGS, asAnotherTask)
            }
            "date" -> {
                openSettings(Settings.ACTION_DATE_SETTINGS, asAnotherTask)
            }
            "display" -> {
                openSettings(Settings.ACTION_DISPLAY_SETTINGS, asAnotherTask)
            }
            "notification" -> {
                openNotification(asAnotherTask)
            }
            "nfc" -> {
                openSettings(Settings.ACTION_NFC_SETTINGS, asAnotherTask)
            }
            "sound" -> {
                openSettings(Settings.ACTION_SOUND_SETTINGS, asAnotherTask)
            }
            "internal_storage" -> {
                openSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, asAnotherTask)
            }
            "battery_optimization" -> {
                openSettings(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, asAnotherTask)
            }
            "vpn" ->
              if (Build.VERSION.SDK_INT >= 24) {
                  openSettings(Settings.ACTION_VPN_SETTINGS, asAnotherTask)
              } else {
                  openSettings("android.net.vpn.SETTINGS", asAnotherTask)
              }
            "app_settings" -> {
                openAppSettings(asAnotherTask)
            }
            "device_settings" -> {
                openSettings(Settings.ACTION_SETTINGS, asAnotherTask)
            }
            "accessibility" -> {
                openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS, asAnotherTask)
            }
            else -> {
                throw NotImplementedError()
            }
        }
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        try {
            handle(call = call)
            result.success(null)
        } catch (e: NotImplementedError) {
            result.notImplemented()
        } catch (e: Throwable) {
            result.error("",e.localizedMessage, null)
        }
    }
}
