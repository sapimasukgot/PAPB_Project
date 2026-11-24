package com.example.easycooking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.easycooking.data.local.entity.GroceryItemEntity
import com.example.easycooking.data.local.entity.GroceryListEntity
import com.example.easycooking.data.local.entity.RecipeEntity
import com.example.easycooking.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isBookmarked = 1")
    fun getBookmarkedRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Query("UPDATE recipes SET isBookmarked = :isBookmarked WHERE id = :recipeId")
    suspend fun updateBookmark(recipeId: String, isBookmarked: Boolean): Int
}

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_lists ORDER BY id DESC")
    fun getAllLists(): Flow<List<GroceryListEntity>>

    @Query("SELECT * FROM grocery_items")
    fun getAllItems(): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE listId = :listId")
    fun getItemsForList(listId: String): Flow<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: GroceryListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GroceryItemEntity>)

    @Query("UPDATE grocery_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateItemChecked(itemId: String, isChecked: Boolean): Int

    @Query("UPDATE grocery_lists SET isCompleted = :isCompleted WHERE id = :listId")
    suspend fun updateListCompleted(listId: String, isCompleted: Boolean): Int

    @Query("DELETE FROM grocery_lists WHERE id = :listId")
    suspend fun deleteList(listId: String): Int

    @Query("DELETE FROM grocery_items WHERE listId = :listId")
    suspend fun deleteItemsForList(listId: String): Int
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE LOWER(email) = LOWER(:email)")
    suspend fun deleteUser(email: String): Int
}
