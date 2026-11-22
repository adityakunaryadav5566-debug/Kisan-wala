package com.example.platform

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.example.data.model.BleDeviceCandidate
import com.example.data.model.BleSensorReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.random.Random

class BleSensorManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "BleSensorManager"
        private val ENV_SENSING_SERVICE: UUID = UUID.fromString("0000181a-0000-1000-8000-00805f9b34fb")
        private val TEMP_CHARACTERISTIC: UUID = UUID.fromString("00002a6e-0000-1000-8000-00805f9b34fb")
        private val HUMIDITY_CHARACTERISTIC: UUID = UUID.fromString("00002a6f-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _connectionState = MutableStateFlow("डिस्कनेक्टेड (Disconnected)")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDeviceCandidate>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDeviceCandidate>> = _discoveredDevices.asStateFlow()

    private val _currentReading = MutableStateFlow<BleSensorReading?>(null)
    val currentReading: StateFlow<BleSensorReading?> = _currentReading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private var activeGatt: BluetoothGatt? = null
    private var scanTimeoutJob: Job? = null
    private var simulationJob: Job? = null
    private val deviceMap = LinkedHashMap<String, BluetoothDevice>()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val address = device.address ?: return
            deviceMap[address] = device
            val name = try {
                device.name ?: result.scanRecord?.deviceName ?: "मिट्टी सेंसर ($address)"
            } catch (_: SecurityException) {
                "BLE Sensor ($address)"
            }
            val current = _discoveredDevices.value.toMutableList()
            val idx = current.indexOfFirst { it.address == address }
            val candidate = BleDeviceCandidate(name = name, address = address, rssi = result.rssi)
            if (idx >= 0) current[idx] = candidate else current.add(candidate)
            _discoveredDevices.value = current.sortedByDescending { it.rssi }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
            _statusMessage.value = "BLE स्कैन विफल हुआ (Error $errorCode)"
        }
    }

    fun isBluetoothAvailableAndEnabled(): Boolean {
        val adapter = bluetoothAdapter ?: return false
        return try {
            adapter.isEnabled
        } catch (_: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        stopSimulation()
        val adapter = bluetoothAdapter
        if (adapter == null) {
            _statusMessage.value = "इस डिवाइस में Bluetooth LE हार्डवेयर उपलब्ध नहीं है।"
            return
        }
        if (!adapter.isEnabled) {
            _statusMessage.value = "कृपया फोन की सेटिंग से Bluetooth चालू करें।"
            return
        }
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            _statusMessage.value = "BLE स्कैनर उपलब्ध नहीं है।"
            return
        }

        try {
            _discoveredDevices.value = emptyList()
            deviceMap.clear()
            _statusMessage.value = "पास के मिट्टी नमी व तापमान BLE सेंसर खोजे जा रहे हैं..."
            _isScanning.value = true
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            scanner.startScan(null, settings, scanCallback)

            scanTimeoutJob?.cancel()
            scanTimeoutJob = scope.launch {
                delay(12_000L)
                stopScan()
                if (_discoveredDevices.value.isEmpty()) {
                    _statusMessage.value = "कोई संगत BLE मिट्टी सेंसर नहीं मिला। सेंसर को पास लाएं या नीचे 'सिम्युलेशन डेमो मोड' आज़माएं।"
                }
            }
        } catch (se: SecurityException) {
            _isScanning.value = false
            _statusMessage.value = "Bluetooth स्कैन अनुमति नहीं मिली।"
            Log.w(TAG, "Missing BLE scan permission", se)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanTimeoutJob?.cancel()
        if (!_isScanning.value) return
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: Exception) {
        }
        _isScanning.value = false
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        stopScan()
        stopSimulation()
        val device = deviceMap[address] ?: run {
            _statusMessage.value = "चयनित डिवाइस अब उपलब्ध नहीं है।"
            return
        }

        try {
            activeGatt?.close()
            _connectionState.value = "कनेक्ट हो रहा है..."
            activeGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        val deviceLabel = try {
                            device.name ?: address
                        } catch (_: SecurityException) {
                            address
                        }
                        _connectionState.value = "कनेक्टेड: $deviceLabel"
                        _statusMessage.value = "सेंसर सेवाएं खोजी जा रही हैं..."
                        try {
                            gatt.discoverServices()
                        } catch (_: SecurityException) {
                        }
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        _connectionState.value = "डिस्कनेक्टेड"
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        _statusMessage.value = "सेंसर सेवा पढ़ने में त्रुटि (Status $status)"
                        return
                    }
                    val envService = gatt.getService(ENV_SENSING_SERVICE)
                    if (envService == null) {
                        _statusMessage.value = "कनेक्टेड डिवाइस में Environmental Sensing (0x181A) सेवा नहीं मिली।"
                        return
                    }
                    val humidityChar = envService.getCharacteristic(HUMIDITY_CHARACTERISTIC)
                    val tempChar = envService.getCharacteristic(TEMP_CHARACTERISTIC)
                    try {
                        if (humidityChar != null) {
                            gatt.readCharacteristic(humidityChar)
                        } else if (tempChar != null) {
                            gatt.readCharacteristic(tempChar)
                        }
                    } catch (_: SecurityException) {
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (status != BluetoothGatt.GATT_SUCCESS) return
                    val bytes = characteristic.value ?: return
                    parseEnvironmentalCharacteristic(gatt, characteristic.uuid, bytes)
                }
            })
        } catch (se: SecurityException) {
            _connectionState.value = "अनुमति त्रुटि"
            _statusMessage.value = "Bluetooth कनेक्ट अनुमति आवश्यक है।"
        }
    }

    @SuppressLint("MissingPermission")
    private fun parseEnvironmentalCharacteristic(
        gatt: BluetoothGatt,
        uuid: UUID,
        bytes: ByteArray
    ) {
        if (bytes.size < 2) return
        val raw = ByteBuffer.wrap(bytes.copyOf(2)).order(ByteOrder.LITTLE_ENDIAN).short
        val prev = _currentReading.value
        val deviceName = try {
            gatt.device?.name ?: "BLE Sensor"
        } catch (_: SecurityException) {
            "BLE Sensor"
        }
        val address = gatt.device?.address ?: "Unknown"

        when (uuid) {
            HUMIDITY_CHARACTERISTIC -> {
                val moisture = (raw.toInt() and 0xFFFF) / 100f
                _currentReading.value = BleSensorReading(
                    deviceName = deviceName,
                    deviceAddress = address,
                    moisturePercent = moisture,
                    temperatureCelsius = prev?.temperatureCelsius,
                    isDemoSimulation = false
                )
                // Also read temperature if present
                gatt.getService(ENV_SENSING_SERVICE)?.getCharacteristic(TEMP_CHARACTERISTIC)?.let { tempChar ->
                    try {
                        gatt.readCharacteristic(tempChar)
                    } catch (_: SecurityException) {
                    }
                }
            }
            TEMP_CHARACTERISTIC -> {
                val tempC = raw.toFloat() / 100f
                _currentReading.value = BleSensorReading(
                    deviceName = deviceName,
                    deviceAddress = address,
                    moisturePercent = prev?.moisturePercent,
                    temperatureCelsius = tempC,
                    isDemoSimulation = false
                )
            }
        }
    }

    fun toggleDemoSimulation(enable: Boolean) {
        if (enable) {
            disconnectHardware()
            _isSimulationMode.value = true
            _connectionState.value = "सिम्युलेशन डेमो मोड सक्रिय"
            _statusMessage.value = "चेतावनी: यह हार्डवेयर सेंसर के बिना डेमो रीडिंग दिखा रहा है।"
            simulationJob?.cancel()
            simulationJob = scope.launch(Dispatchers.Default) {
                var step = 0
                while (isActive) {
                    // Cycles smoothly through healthy, low moisture (<30%), and high temp (>36C) states for demo
                    val moisture = when (step % 4) {
                        0 -> 44.5f
                        1 -> 24.0f // Triggers Low Moisture alert
                        2 -> 27.5f
                        else -> 51.2f
                    } + Random.nextFloat() * 1.5f
                    val temp = when (step % 4) {
                        0 -> 29.4f
                        1 -> 33.8f
                        2 -> 38.2f // Triggers High Temp alert
                        else -> 30.1f
                    } + Random.nextFloat() * 0.6f

                    _currentReading.value = BleSensorReading(
                        deviceName = "Kisan Soil Probe (Demo Sim)",
                        deviceAddress = "DEMO:00:11:22:33",
                        moisturePercent = moisture,
                        temperatureCelsius = temp,
                        isDemoSimulation = true
                    )
                    step++
                    delay(3500L)
                }
            }
        } else {
            stopSimulation()
            _currentReading.value = null
            _connectionState.value = "डिस्कनेक्टेड (Disconnected)"
            _statusMessage.value = null
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        _isSimulationMode.value = false
    }

    @SuppressLint("MissingPermission")
    fun disconnectHardware() {
        stopScan()
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (_: Exception) {
        }
        activeGatt = null
        if (!_isSimulationMode.value) {
            _connectionState.value = "डिस्कनेक्टेड (Disconnected)"
        }
    }

    fun release() {
        stopSimulation()
        disconnectHardware()
    }
}
