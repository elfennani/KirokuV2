package com.elfennani.kiroku.data.local

import androidx.room.TypeConverter
import com.elfennani.kiroku.domain.model.Collection

class Converters {
    @TypeConverter
    fun fromCollection(value: Collection): String {
        return value.name
    }

    @TypeConverter
    fun toCollection(value: String): Collection {
        return Collection.valueOf(value)
    }
}