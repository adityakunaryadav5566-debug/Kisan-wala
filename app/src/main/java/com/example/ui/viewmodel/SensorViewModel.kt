package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BleDeviceCandidate
import com.example.data.model.BleSensorReading
import com.example.platform.BleSensorManager
import kotlinx.coroutines.flow.StateFlow

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleSensorManager(application, viewModelScope)

    val isScanning: StateFlow<Boolean> = bleManager.isScanning
    val connectionState: StateFlow<String> = bleManager.connectionState
    val discoveredDevices: StateFlow<List<BleDeviceCandidate>> = bleManager.discoveredDevices
    val currentReading: StateFlow<BleSensorReading?> = bleManager.currentReading
    val statusMessage: StateFlow<String?> = bleManager.statusMessage
    val isSimulationMode: StateFlow<Boolean> = bleManager.isSimulationMode

    fun startScan() = bleManager.startScan()
    fun stopScan() = bleManager.stopScan()
    fun connectToDevice(address: String) = bleManager.connectToDevice(address)
    fun disconnect() = bleManager.disconnectHardware()
    fun toggleSimulation(enable: Boolean) = bleManager.toggleDemoSimulation(enable)

    override fun onCleared() {
        super.onCleared()
        bleManager.release()
    }
}
