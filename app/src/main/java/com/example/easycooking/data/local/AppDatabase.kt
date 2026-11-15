package com.example.easycooking.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.easycooking.data.local.dao.GroceryDao
import com.example.easycooking.data.local.dao.RecipeDao
import com.example.easycooking.data.local.entity.GroceryItemEntity
import com.example.easycooking.data.local.entity.GroceryListEntity
import com.example.easycooking.data.local.entity.RecipeEntity

@Database(
    entities = [
        RecipeEntity::class,
        GroceryListEntity::class,
        GroceryItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "easycooking_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
