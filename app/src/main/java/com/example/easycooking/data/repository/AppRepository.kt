package com.example.easycooking.data.repository

import android.content.Context
import com.example.easycooking.data.local.AppDatabase
import com.example.easycooking.data.local.PreferencesManager
import com.example.easycooking.data.local.entity.GroceryItemEntity
import com.example.easycooking.data.local.entity.GroceryListEntity
import com.example.easycooking.data.local.entity.RecipeEntity
import com.example.easycooking.model.GroceryItem
import com.example.easycooking.model.GroceryList
import com.example.easycooking.model.Ingredient
import com.example.easycooking.model.Recipe
import com.example.easycooking.model.RecipeStep
import com.example.easycooking.model.ReviewItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val database = AppDatabase.getDatabase(context)
    private val recipeDao = database.recipeDao()
    private val groceryDao = database.groceryDao()
    val preferencesManager = PreferencesManager(context)

    private val inMemoryRecipes = mutableMapOf<String, Recipe>()

    init {
        val defaultIngredients = listOf(
            Ingredient("1", "Chicken breast", "tender diced fillets", 400.0, "g", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("2", "Garlic", "finely minced", 3.0, "cloves", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("3", "Heavy cream", "rich cooking cream", 100.0, "ml", "Dairy & Eggs", allergenTags = listOf("Dairy")),
            Ingredient("4", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", allergenTags = emptyList()),
            Ingredient("5", "Baby spinach & sun-dried tomatoes", "fresh & chopped", 50.0, "g", "Specialty", allergenTags = emptyList())
        )

        val defaultSteps = listOf(
            RecipeStep(1, "Sear Chicken", "Heat olive oil in a deep skillet over medium-high heat. Brown seasoned chicken breasts 4-5 minutes per side until deeply golden brown.", 6, false, "Make sure the pan is hot before adding chicken for optimal sear."),
            RecipeStep(2, "Simmer Garlic Cream", "Saute minced garlic for 1 minute, then pour rich heavy cream & sun-dried tomatoes. Simmer gently on low heat until velvety smooth.", 5, false, "Keep heat at medium-low to prevent cream from curdling."),
            RecipeStep(3, "Combine & Garnish", "Return chicken breasts into the skillet with warm garlic cream sauce. Spoon sauce generously over chicken and garnish with freshly chopped basil and shaved parmesan.", 4, true, "Let rest for 2 minutes before serving so the chicken retains its juiciness.")
        )

        val defaultReviews = listOf(
            ReviewItem("r1", "Sarah K.", 5, "2d ago", "Super easy for weeknights, the garlic aroma is incredible! Paired it with freshly toasted sourdough."),
            ReviewItem("r2", "Alex M.", 4, "5d ago", "Delicious sauce, perfectly balanced with the chicken. Added extra sun-dried tomatoes for sweetness.")
        )

        val seeds = listOf(
            Recipe(
                id = "1",
                title = "Tuscan Garlic Chicken",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 25,
                calories = 420,
                rating = 4.8,
                reviewCount = 420,
                isBookmarked = true,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free", "Nut-Free"),
                allergens = listOf("Dairy"),
                defaultServings = 2,
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "2",
                title = "Lemon Butter Salmon",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 20,
                calories = 420,
                rating = 4.9,
                reviewCount = 310,
                isBookmarked = true,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free", "Low Carb"),
                allergens = listOf("Fish", "Dairy"),
                defaultServings = 2,
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "3",
                title = "Creamy Truffle Pasta",
                chefName = "Chef Elena Vance",
                difficulty = "Medium",
                durationMinutes = 25,
                calories = 380,
                rating = 4.8,
                reviewCount = 180,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Vegetarian"),
                allergens = listOf("Gluten", "Dairy"),
                defaultServings = 2,
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "4",
                title = "Garlic Herb Chicken",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 15,
                calories = 350,
                rating = 4.8,
                reviewCount = 95,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "5",
                title = "Avocado Pesto Pasta",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 12,
                calories = 320,
                rating = 4.7,
                reviewCount = 140,
                isBookmarked = true,
                category = "Lunch",
                dietaryBadges = listOf("Vegan", "Dairy-Free"),
                allergens = listOf("Gluten", "Tree Nuts"),
                defaultServings = 2,
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "6",
                title = "Berry Acai Bowl",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 10,
                calories = 260,
                rating = 5.0,
                reviewCount = 88,
                isBookmarked = true,
                category = "Breakfast",
                dietaryBadges = listOf("Vegan", "Gluten-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            )
        )

        seeds.forEach { inMemoryRecipes[it.id] = it }
    }

    suspend fun seedDatabaseIfEmpty() = withContext(ioDispatcher) {
        val entities = inMemoryRecipes.values.map { r ->
            RecipeEntity(
                id = r.id,
                title = r.title,
                chefName = r.chefName,
                difficulty = r.difficulty,
                durationMinutes = r.durationMinutes,
                calories = r.calories,
                rating = r.rating,
                reviewCount = r.reviewCount,
                isBookmarked = r.isBookmarked,
                category = r.category,
                dietaryBadgesRaw = r.dietaryBadges.joinToString(","),
                allergensRaw = r.allergens.joinToString(","),
                defaultServings = r.defaultServings
            )
        }
        recipeDao.insertAll(entities)

        val initialLists = listOf(
            GroceryListEntity("g1", "Tuscan Garlic Chicken", "Updated 2h ago", isCompleted = false),
            GroceryListEntity("g2", "Berry Blast Smoothie", "Updated 5h ago", isCompleted = false),
            GroceryListEntity("g3", "Avocado Pasta", "Yesterday", isCompleted = true)
        )
        val initialItems = listOf(
            GroceryItemEntity("gi1", "g1", "1", "Chicken breast", 400.0, "g", "Produce & Meat", isChecked = true),
            GroceryItemEntity("gi2", "g1", "1", "Olive oil", 1.0, "tbsp", "Pantry Essentials", isChecked = true),
            GroceryItemEntity("gi3", "g1", "1", "Fresh Garlic", 3.0, "cloves", "Produce & Meat", isChecked = false),
            GroceryItemEntity("gi4", "g1", "1", "Heavy whipping cream", 100.0, "ml", "Dairy & Eggs", isChecked = false),
            GroceryItemEntity("gi5", "g1", "1", "Baby spinach & sun-dried tomatoes", 50.0, "g", "Specialty", isChecked = false),
            GroceryItemEntity("gi6", "g2", "6", "Fresh Strawberries", 200.0, "g", "Produce & Meat", isChecked = true),
            GroceryItemEntity("gi7", "g2", "6", "Almond Milk", 250.0, "ml", "Dairy & Eggs", isChecked = true),
            GroceryItemEntity("gi8", "g2", "6", "Chia Seeds", 2.0, "tbsp", "Pantry Essentials", isChecked = false),
            GroceryItemEntity("gi9", "g2", "6", "Organic Honey", 1.0, "tbsp", "Pantry Essentials", isChecked = false),
            GroceryItemEntity("gi10", "g3", "5", "Ripe Avocados", 2.0, "buah", "Produce & Meat", isChecked = true),
            GroceryItemEntity("gi11", "g3", "5", "Spaghetti Pasta", 200.0, "g", "Pantry Essentials", isChecked = true),
            GroceryItemEntity("gi12", "g3", "5", "Fresh Basil", 10.0, "lembar", "Specialty", isChecked = true),
            GroceryItemEntity("gi13", "g3", "5", "Lemon", 1.0, "buah", "Produce & Meat", isChecked = true),
            GroceryItemEntity("gi14", "g3", "5", "Garlic cloves", 2.0, "cloves", "Produce & Meat", isChecked = true)
        )
        initialLists.forEach { groceryDao.insertList(it) }
        groceryDao.insertItems(initialItems)
    }

    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities ->
            entities.map { entity ->
                val cached = inMemoryRecipes[entity.id]
                Recipe(
                    id = entity.id,
                    title = entity.title,
                    chefName = entity.chefName,
                    difficulty = entity.difficulty,
                    durationMinutes = entity.durationMinutes,
                    calories = entity.calories,
                    rating = entity.rating,
                    reviewCount = entity.reviewCount,
                    isBookmarked = entity.isBookmarked,
                    category = entity.category,
                    dietaryBadges = if (entity.dietaryBadgesRaw.isBlank()) emptyList() else entity.dietaryBadgesRaw.split(","),
                    allergens = if (entity.allergensRaw.isBlank()) emptyList() else entity.allergensRaw.split(","),
                    defaultServings = entity.defaultServings,
                    ingredients = cached?.ingredients ?: emptyList(),
                    steps = cached?.steps ?: emptyList(),
                    reviews = cached?.reviews ?: emptyList()
                )
            }
        }.flowOn(ioDispatcher)
    }

    suspend fun toggleBookmark(recipeId: String, currentStatus: Boolean) = withContext(ioDispatcher) {
        recipeDao.updateBookmark(recipeId, !currentStatus)
    }

    fun getGroceryLists(): Flow<List<GroceryList>> {
        return combine(groceryDao.getAllLists(), groceryDao.getAllItems()) { lists, allItems ->
            lists.map { listEntity ->
                val matchingItems = allItems.filter { it.listId == listEntity.id }.map { itemEntity ->
                    GroceryItem(
                        id = itemEntity.id,
                        recipeId = itemEntity.recipeId,
                        name = itemEntity.name,
                        quantity = itemEntity.quantity,
                        unit = itemEntity.unit,
                        category = itemEntity.category,
                        isChecked = itemEntity.isChecked
                    )
                }
                val isCompleted = matchingItems.isNotEmpty() && matchingItems.all { it.isChecked }
                GroceryList(
                    id = listEntity.id,
                    title = listEntity.title,
                    updatedAt = listEntity.updatedAt,
                    items = matchingItems,
                    isCompleted = isCompleted
                )
            }
        }.flowOn(ioDispatcher)
    }

    suspend fun toggleGroceryItem(itemId: String, currentChecked: Boolean, listId: String) = withContext(ioDispatcher) {
        groceryDao.updateItemChecked(itemId, !currentChecked)
    }

    suspend fun deleteGroceryList(listId: String) = withContext(ioDispatcher) {
        groceryDao.deleteList(listId)
        groceryDao.deleteItemsForList(listId)
    }

    suspend fun addCustomGroceryItem(listId: String, name: String) = withContext(ioDispatcher) {
        val newItem = GroceryItemEntity(
            id = "custom_${System.currentTimeMillis()}",
            listId = listId,
            recipeId = "",
            name = name.trim(),
            quantity = 1.0,
            unit = "pcs",
            category = "Pantry Essentials",
            isChecked = false
        )
        groceryDao.insertItems(listOf(newItem))
    }

    suspend fun exportRecipeToGrocery(
        recipe: Recipe,
        targetServings: Int
    ) = withContext(ioDispatcher) {
        val multiplier = targetServings.toDouble() / recipe.defaultServings.toDouble()
        val listId = "list_${System.currentTimeMillis()}"

        val newEntities = mutableListOf<GroceryItemEntity>()

        for (ingredient in recipe.ingredients) {
            val scaledQty = ingredient.baseAmount * multiplier
            val unitNormalized = ingredient.unit.trim().lowercase()

            val entity = GroceryItemEntity(
                id = "item_${recipe.id}_${ingredient.id}_${System.currentTimeMillis()}",
                listId = listId,
                recipeId = recipe.id,
                name = ingredient.name,
                quantity = scaledQty,
                unit = ingredient.unit,
                category = ingredient.category,
                isChecked = false
            )
            newEntities.add(entity)
        }

        val listEntity = GroceryListEntity(
            id = listId,
            title = recipe.title,
            updatedAt = "Just now",
            isCompleted = false
        )

        groceryDao.insertList(listEntity)
        groceryDao.insertItems(newEntities)
    }

    fun addReviewInMemory(recipeId: String, review: ReviewItem) {
        val r = inMemoryRecipes[recipeId]
        if (r != null) {
            val updated = r.copy(
                reviews = listOf(review) + r.reviews,
                reviewCount = r.reviewCount + 1
            )
            inMemoryRecipes[recipeId] = updated
        }
    }
}
