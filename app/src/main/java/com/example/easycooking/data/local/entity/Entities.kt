package com.example.easycooking.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val chefName: String,
    val difficulty: String,
    val durationMinutes: Int,
    val calories: Int,
    val rating: Double,
    val reviewCount: Int,
    val isBookmarked: Boolean,
    val category: String,
    val dietaryBadgesRaw: String,
    val allergensRaw: String,
    val defaultServings: Int = 2,
    val ownerEmail: String = "",
    val isDraft: Boolean = false,
    val ingredientsJson: String = "",
    val stepsJson: String = "",
    val reviewsJson: String = "",
    val imageUri: String = ""
)

@Entity(tableName = "grocery_lists")
data class GroceryListEntity(
    @PrimaryKey val id: String,
    val title: String,
    val updatedAt: String,
    val isCompleted: Boolean = false,
    val ownerEmail: String = ""
)

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val recipeId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val isChecked: Boolean = false
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)
