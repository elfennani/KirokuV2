package com.elfennani.kiroku.data.local

import androidx.room.TypeConverter
import com.elfennani.kiroku.domain.model.Collection
import com.elfennani.kiroku.domain.model.DownloadResource
import com.elfennani.kiroku.domain.model.MediaType
import kotlinx.serialization.json.Json

class Converters {
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromCollection(value: Collection): String {
        return value.name
    }

    @TypeConverter
    fun toCollection(value: String): Collection {
        return Collection.valueOf(value)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromMediaType(value: MediaType): String {
        return value.name
    }

    @TypeConverter
    fun toMediaType(value: String): MediaType {
        return MediaType.valueOf(value)
    }

    @TypeConverter
    fun fromDownloadResources(value: List<DownloadResource>) = json.encodeToString(value)

    @TypeConverter
    fun toDownloadResources(value: String) = json.decodeFromString<List<DownloadResource>>(value)
}