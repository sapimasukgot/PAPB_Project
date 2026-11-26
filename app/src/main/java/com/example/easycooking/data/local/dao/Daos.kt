package com.example.easycooking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.easycooking.data.local.entity.GroceryItemEntity
import com.example.easycooking.data.local.entity.GroceryListEntity
import com.example.easycooking.data.local.entity.RecipeEntity
import com.example.easycooking.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE isBookmarked = 1")
    fun getBookmarkedRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("UPDATE recipes SET isBookmarked = :isBookmarked WHERE id = :recipeId")
    suspend fun updateBookmark(recipeId: String, isBookmarked: Boolean): Int

    @Query("UPDATE recipes SET reviewsJson = :reviewsJson, reviewCount = :reviewCount, rating = :rating WHERE id = :recipeId")
    suspend fun updateRecipeReviews(recipeId: String, reviewsJson: String, reviewCount: Int, rating: Double): Int

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: String): Int

    @Query("DELETE FROM recipes WHERE LOWER(ownerEmail) = LOWER(:ownerEmail)")
    suspend fun deleteRecipesByOwner(ownerEmail: String): Int

    @Query("DELETE FROM recipes WHERE id LIKE 'user_rec_%' OR ownerEmail != ''")
    suspend fun deleteManualRecipes(): Int

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes(): Int
}

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_lists ORDER BY id DESC")
    fun getAllLists(): Flow<List<GroceryListEntity>>

    @Query("SELECT * FROM grocery_lists")
    suspend fun getAllListsOnce(): List<GroceryListEntity>

    @Query("SELECT * FROM grocery_items")
    fun getAllItems(): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE listId = :listId")
    fun getItemsForList(listId: String): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE listId = :listId")
    suspend fun getItemsForListOnce(listId: String): List<GroceryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: GroceryListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GroceryItemEntity>)

    @Query("UPDATE grocery_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateItemChecked(itemId: String, isChecked: Boolean): Int

    @Query("UPDATE grocery_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateItemQuantity(itemId: String, quantity: Double): Int

    @Query("UPDATE grocery_lists SET isCompleted = :isCompleted WHERE id = :listId")
    suspend fun updateListCompleted(listId: String, isCompleted: Boolean): Int

    @Query("DELETE FROM grocery_lists WHERE id = :listId")
    suspend fun deleteList(listId: String): Int

    @Query("DELETE FROM grocery_items WHERE listId = :listId")
    suspend fun deleteItemsForList(listId: String): Int

    @Query("DELETE FROM grocery_items WHERE listId IN (SELECT id FROM grocery_lists WHERE LOWER(ownerEmail) = LOWER(:ownerEmail))")
    suspend fun deleteItemsForOwnerLists(ownerEmail: String): Int

    @Query("DELETE FROM grocery_lists WHERE LOWER(ownerEmail) = LOWER(:ownerEmail)")
    suspend fun deleteListsByOwner(ownerEmail: String): Int

    @Query("DELETE FROM grocery_items WHERE listId IN ('g1', 'g2', 'g3')")
    suspend fun deleteLegacySampleItems(): Int

    @Query("DELETE FROM grocery_lists WHERE id IN ('g1', 'g2', 'g3')")
    suspend fun deleteLegacySampleLists(): Int
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET password = :newPassword WHERE LOWER(email) = LOWER(:email)")
    suspend fun updatePassword(email: String, newPassword: String): Int

    @Query("DELETE FROM users WHERE LOWER(email) = LOWER(:email)")
    suspend fun deleteUser(email: String): Int
}
