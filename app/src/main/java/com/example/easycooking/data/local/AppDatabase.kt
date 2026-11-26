package com.example.easycooking.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.easycooking.data.local.dao.GroceryDao
import com.example.easycooking.data.local.dao.RecipeDao
import com.example.easycooking.data.local.dao.UserDao
import com.example.easycooking.data.local.entity.GroceryItemEntity
import com.example.easycooking.data.local.entity.GroceryListEntity
import com.example.easycooking.data.local.entity.RecipeEntity
import com.example.easycooking.data.local.entity.UserEntity

@Database(
    entities = [
        RecipeEntity::class,
        GroceryListEntity::class,
        GroceryItemEntity::class,
        UserEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun groceryDao(): GroceryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN ownerEmail TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE recipes ADD COLUMN isDraft INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipes ADD COLUMN ingredientsJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE recipes ADD COLUMN stepsJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE recipes ADD COLUMN reviewsJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE grocery_lists ADD COLUMN ownerEmail TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN imageUri TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "easycooking_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
