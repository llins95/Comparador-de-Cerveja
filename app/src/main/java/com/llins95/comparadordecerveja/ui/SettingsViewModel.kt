package com.llins95.comparadordecerveja.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.llins95.comparadordecerveja.data.AppSettingsRepository
import com.llins95.comparadordecerveja.data.PackageSizeOption

class SettingsViewModel(private val repository: AppSettingsRepository) : ViewModel() {
    val packageSizes = repository.packageSizes
    val stores = repository.stores

    fun addPackageSize(name: String, volumeMl: Int): Boolean =
        repository.addPackageSize(name, volumeMl)

    fun deletePackageSize(option: PackageSizeOption) = repository.deletePackageSize(option)

    fun addStore(name: String): Boolean = repository.addStore(name)

    fun deleteStore(name: String) = repository.deleteStore(name)

    companion object {
        fun factory(repository: AppSettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(repository) as T
                }
            }
    }
}
