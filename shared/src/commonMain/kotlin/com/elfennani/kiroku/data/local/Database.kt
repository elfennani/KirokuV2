package com.elfennani.kiroku.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.elfennani.kiroku.data.local.dao.MediaDao
import com.elfennani.kiroku.data.local.dao.SessionDao
import com.elfennani.kiroku.data.local.dao.UserDao
import com.elfennani.kiroku.data.local.entity.LocalMediaEntity
import com.elfennani.kiroku.data.local.entity.OngoingMediaEntity
import com.elfennani.kiroku.data.local.entity.SessionEntity
import com.elfennani.kiroku.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


@Database(
    entities = [
        SessionEntity::class,
        UserEntity::class,
        LocalMediaEntity::class,
        OngoingMediaEntity::class
    ],
    version = 1,
    autoMigrations = [
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun getSessionDao(): SessionDao
    abstract fun getUserDao(): UserDao
    abstract fun getMediaDao(): MediaDao

}

// The Room compiler generates the `actual` implementations.

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
