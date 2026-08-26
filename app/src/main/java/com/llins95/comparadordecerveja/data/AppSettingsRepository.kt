package com.llins95.comparadordecerveja.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class PackageSizeOption(
    val name: String,
    val volumeMl: Int,
)

class AppSettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _packageSizes = MutableStateFlow(loadPackageSizes())
    val packageSizes: StateFlow<List<PackageSizeOption>> = _packageSizes.asStateFlow()

    private val _stores = MutableStateFlow(loadStores())
    val stores: StateFlow<List<String>> = _stores.asStateFlow()

    fun addPackageSize(name: String, volumeMl: Int): Boolean {
        val normalizedName = name.trim().replace(Regex("\\s+"), " ")
        if (normalizedName.isBlank() || volumeMl <= 0) return false

        val current = _packageSizes.value
        if (current.any {
                it.name.equals(normalizedName, ignoreCase = true) && it.volumeMl == volumeMl
            }
        ) {
            return false
        }

        val updated = current + PackageSizeOption(normalizedName, volumeMl)
        savePackageSizes(updated)
        _packageSizes.value = updated
        return true
    }

    fun deletePackageSize(option: PackageSizeOption) {
        val updated = _packageSizes.value.filterNot { it == option }
        savePackageSizes(updated)
        _packageSizes.value = updated
    }

    fun addStore(name: String): Boolean {
        val normalizedName = name.trim().replace(Regex("\\s+"), " ")
        if (normalizedName.isBlank()) return false

        val current = _stores.value
        if (current.any { it.equals(normalizedName, ignoreCase = true) }) return false

        val updated = current + normalizedName
        saveStores(updated)
        _stores.value = updated
        return true
    }

    fun deleteStore(name: String) {
        val updated = _stores.value.filterNot { it.equals(name, ignoreCase = true) }
        saveStores(updated)
        _stores.value = updated
    }

    private fun loadPackageSizes(): List<PackageSizeOption> {
        val saved = preferences.getString(KEY_PACKAGE_SIZES, null) ?: return DEFAULT_PACKAGE_SIZES
        return runCatching {
            val array = JSONArray(saved)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val name = item.optString("name").trim()
                    val volumeMl = item.optInt("volumeMl")
                    if (name.isNotBlank() && volumeMl > 0) {
                        add(PackageSizeOption(name, volumeMl))
                    }
                }
            }
        }.getOrElse { DEFAULT_PACKAGE_SIZES }
    }

    private fun savePackageSizes(options: List<PackageSizeOption>) {
        val array = JSONArray()
        options.forEach { option ->
            array.put(
                JSONObject()
                    .put("name", option.name)
                    .put("volumeMl", option.volumeMl)
            )
        }
        preferences.edit().putString(KEY_PACKAGE_SIZES, array.toString()).apply()
    }

    private fun loadStores(): List<String> {
        val saved = preferences.getString(KEY_STORES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(saved)
            buildList {
                for (index in 0 until array.length()) {
                    val name = array.optString(index).trim()
                    if (name.isNotBlank()) add(name)
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun saveStores(stores: List<String>) {
        val array = JSONArray()
        stores.forEach(array::put)
        preferences.edit().putString(KEY_STORES, array.toString()).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "cerva_settings"
        const val KEY_PACKAGE_SIZES = "package_sizes_v1"
        const val KEY_STORES = "stores_v1"

        val DEFAULT_PACKAGE_SIZES = listOf(
            PackageSizeOption("Lata", 269),
            PackageSizeOption("Long neck", 330),
            PackageSizeOption("Lata", 350),
            PackageSizeOption("Latão", 473),
            PackageSizeOption("Garrafa", 600),
            PackageSizeOption("Garrafa", 1_000),
        )
    }
}
