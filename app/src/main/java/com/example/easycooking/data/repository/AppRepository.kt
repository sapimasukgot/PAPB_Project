package com.example.easycooking.data.repository

import android.content.Context
import com.example.easycooking.data.local.AppDatabase
import com.example.easycooking.data.local.PreferencesManager
import com.example.easycooking.data.local.entity.GroceryItemEntity
import com.example.easycooking.data.local.entity.GroceryListEntity
import com.example.easycooking.data.local.entity.RecipeEntity
import com.example.easycooking.data.local.entity.UserEntity
import com.example.easycooking.model.GroceryItem
import com.example.easycooking.model.GroceryList
import com.example.easycooking.model.Ingredient
import com.example.easycooking.model.Recipe
import com.example.easycooking.model.RecipeStep
import com.example.easycooking.model.ReviewItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val userDao = database.userDao()
    val preferencesManager = PreferencesManager(context)

    private val inMemoryRecipes = mutableMapOf<String, Recipe>()
    private val _recipeReviewsFlow = MutableStateFlow<Map<String, List<ReviewItem>>>(emptyMap())

    init {
        val ingredients1 = listOf(
            Ingredient("1_1", "Chicken breast", "tender diced fillets", 400.0, "g", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("1_2", "Garlic", "finely minced", 3.0, "cloves", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("1_3", "Heavy cream", "rich cooking cream", 100.0, "ml", "Dairy & Eggs", allergenTags = listOf("Dairy")),
            Ingredient("1_4", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", allergenTags = emptyList()),
            Ingredient("1_5", "Baby spinach & sun-dried tomatoes", "fresh & chopped", 50.0, "g", "Specialty", allergenTags = emptyList())
        )
        val steps1 = listOf(
            RecipeStep(1, "Sear Chicken", "Heat olive oil in a deep skillet over medium-high heat. Brown seasoned chicken breasts 4-5 minutes per side until deeply golden brown.", 6, false, "Make sure the pan is hot before adding chicken for optimal sear."),
            RecipeStep(2, "Simmer Garlic Cream", "Saute minced garlic for 1 minute, then pour rich heavy cream & sun-dried tomatoes. Simmer gently on low heat until velvety smooth.", 5, false, "Keep heat at medium-low to prevent cream from curdling."),
            RecipeStep(3, "Combine & Garnish", "Return chicken breasts into the skillet with warm garlic cream sauce. Spoon sauce generously over chicken and garnish with freshly chopped basil and shaved parmesan.", 4, true, "Let rest for 2 minutes before serving so the chicken retains its juiciness.")
        )
        val reviews1 = listOf(
            ReviewItem("r1_1", "Sarah K.", 5, "2d ago", "Super easy for weeknights, the garlic aroma is incredible! Paired it with freshly toasted sourdough."),
            ReviewItem("r1_2", "Alex M.", 4, "5d ago", "Delicious sauce, perfectly balanced with the chicken. Added extra sun-dried tomatoes for sweetness.")
        )

        val ingredients2 = listOf(
            Ingredient("2_1", "Salmon fillets", "skin-on fresh fillets", 350.0, "g", "Produce & Meat", allergenTags = listOf("Fish")),
            Ingredient("2_2", "Unsalted butter", "melted creamy butter", 2.0, "tbsp", "Dairy & Eggs", allergenTags = listOf("Dairy")),
            Ingredient("2_3", "Fresh lemon juice", "freshly squeezed", 2.0, "tbsp", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("2_4", "Garlic", "finely minced", 2.0, "cloves", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("2_5", "Fresh dill & parsley", "finely chopped", 15.0, "g", "Specialty", allergenTags = emptyList()),
            Ingredient("2_6", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", allergenTags = emptyList())
        )
        val steps2 = listOf(
            RecipeStep(1, "Season & Sear Salmon", "Pat salmon dry with paper towels, season with sea salt and black pepper. Heat olive oil in a skillet and sear skin-side down for 4 minutes until crispy.", 5, false, "Press salmon down gently with a spatula to prevent curling."),
            RecipeStep(2, "Make Lemon Butter Glaze", "Flip salmon fillets. Add butter, minced garlic, and fresh lemon juice into the skillet, spooning the foaming butter continuously over salmon.", 4, false, "Baste continuously to keep the salmon moist and tender."),
            RecipeStep(3, "Rest & Garnish", "Remove from heat when salmon flakes easily with a fork. Scatter fresh dill and parsley, serve with lemon wedges.", 2, true, "Do not overcook; internal salmon should remain tender pink.")
        )
        val reviews2 = listOf(
            ReviewItem("r2_1", "David L.", 5, "1d ago", "The salmon came out perfectly flaky and the lemon butter sauce was incredible! Restaurant quality at home."),
            ReviewItem("r2_2", "Maya R.", 5, "3d ago", "So fast to make and crispy skin on point. Paired with roasted asparagus.")
        )

        val ingredients3 = listOf(
            Ingredient("3_1", "Fettuccine pasta", "durum wheat pasta", 250.0, "g", "Pantry Essentials", allergenTags = listOf("Gluten")),
            Ingredient("3_2", "Cremini mushrooms", "freshly sliced", 200.0, "g", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("3_3", "White truffle oil", "aromatic finishing oil", 1.0, "tbsp", "Specialty", allergenTags = emptyList()),
            Ingredient("3_4", "Heavy cream", "cooking cream", 120.0, "ml", "Dairy & Eggs", allergenTags = listOf("Dairy")),
            Ingredient("3_5", "Parmesan cheese", "freshly grated", 40.0, "g", "Dairy & Eggs", allergenTags = listOf("Dairy")),
            Ingredient("3_6", "Garlic", "minced cloves", 2.0, "cloves", "Produce & Meat", allergenTags = emptyList())
        )
        val steps3 = listOf(
            RecipeStep(1, "Boil Pasta", "Bring a large pot of salted water to a rolling boil. Cook fettuccine al dente for 8-9 minutes, reserving 1/2 cup pasta water before draining.", 9, false, "Reserve pasta water to emulsify the truffle sauce later."),
            RecipeStep(2, "Saute Mushrooms", "Heat butter and olive oil in a pan, saute sliced mushrooms until caramelized and golden, then add garlic for 1 minute.", 5, false, "Do not crowd the mushrooms so they brown nicely instead of steaming."),
            RecipeStep(3, "Toss with Truffle Cream", "Stir heavy cream and grated parmesan into pan until smooth. Toss fettuccine and drizzle white truffle oil gently before serving.", 4, true, "Add truffle oil off heat to preserve its delicate earthy aroma.")
        )
        val reviews3 = listOf(
            ReviewItem("r3_1", "Kevin T.", 5, "2d ago", "Heavenly truffle aroma! Truly luxurious vegetarian pasta, tasted like fine dining."),
            ReviewItem("r3_2", "Clara S.", 4, "4d ago", "Rich and creamy. Make sure to use high quality truffle oil for the best fragrance.")
        )

        val ingredients4 = listOf(
            Ingredient("4_1", "Chicken breast tenderloins", "skinless and boneless", 450.0, "g", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("4_2", "Fresh rosemary & thyme", "finely minced", 10.0, "g", "Specialty", allergenTags = emptyList()),
            Ingredient("4_3", "Garlic", "finely minced cloves", 3.0, "cloves", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("4_4", "Extra virgin olive oil", "cold pressed", 2.0, "tbsp", "Pantry Essentials", allergenTags = emptyList()),
            Ingredient("4_5", "Lemon zest & juice", "freshly grated", 1.0, "tsp", "Produce & Meat", allergenTags = emptyList())
        )
        val steps4 = listOf(
            RecipeStep(1, "Herb Rub", "Coat chicken tenderloins with olive oil, minced garlic, chopped fresh rosemary, thyme, salt, and black pepper.", 3, false, "Massage herbs into chicken for even seasoning."),
            RecipeStep(2, "Pan Sear", "Cook seasoned tenderloins in a hot skillet for 5 minutes each side until golden brown and cooked through.", 6, false, "Avoid moving chicken during the first 3 minutes to get nice sear marks."),
            RecipeStep(3, "Lemon Finish", "Drizzle fresh lemon juice and zest over chicken, turn off heat, and rest 2 minutes before serving.", 2, true, "Resting keeps all savory juices sealed inside.")
        )
        val reviews4 = listOf(
            ReviewItem("r4_1", "Rachel W.", 5, "3d ago", "Quick 15-minute dinner that tastes phenomenal! Very juicy and lean protein."),
            ReviewItem("r4_2", "Brian K.", 5, "1w ago", "Great for weekly meal prep. The herb blend smells wonderful.")
        )

        val ingredients5 = listOf(
            Ingredient("5_1", "Spaghetti", "whole grain pasta", 200.0, "g", "Pantry Essentials", allergenTags = listOf("Gluten")),
            Ingredient("5_2", "Ripe avocados", "creamy halved & peeled", 2.0, "pcs", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("5_3", "Fresh basil leaves", "fragrant sweet basil", 30.0, "g", "Specialty", allergenTags = emptyList()),
            Ingredient("5_4", "Walnuts or pine nuts", "toasted nuts", 30.0, "g", "Pantry Essentials", allergenTags = listOf("Tree Nuts")),
            Ingredient("5_5", "Lemon juice", "freshly squeezed", 2.0, "tbsp", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("5_6", "Extra virgin olive oil", "unfiltered", 2.0, "tbsp", "Pantry Essentials", allergenTags = emptyList()),
            Ingredient("5_7", "Garlic", "peeled clove", 1.0, "cloves", "Produce & Meat", allergenTags = emptyList())
        )
        val steps5 = listOf(
            RecipeStep(1, "Boil Spaghetti", "Cook spaghetti in boiling salted water according to package instructions until al dente. Drain and set aside.", 8, false, "Do not rinse with hot water; keep pasta warm."),
            RecipeStep(2, "Blend Avocado Pesto", "Place ripe avocados, basil, garlic, toasted nuts, lemon juice, salt, and olive oil in a blender. Pulse until silky smooth and vibrant green.", 3, false, "Lemon juice keeps the avocado bright green and prevents browning."),
            RecipeStep(3, "Toss Together", "Combine warm pasta with smooth avocado pesto in a large bowl. Toss evenly and garnish with fresh basil leaves and cracked pepper.", 2, true, "Serve immediately while creamy and fresh.")
        )
        val reviews5 = listOf(
            ReviewItem("r5_1", "Emily H.", 5, "2d ago", "Incredible creamy texture without any dairy! Super vibrant and satisfying vegan lunch."),
            ReviewItem("r5_2", "Marcus J.", 5, "5d ago", "The avocado pesto is so velvety and the lemon balances it out perfectly. Will make this again!")
        )

        val ingredients6 = listOf(
            Ingredient("6_1", "Frozen acai puree", "unsweetened acai packet", 100.0, "g", "Specialty", allergenTags = emptyList()),
            Ingredient("6_2", "Fresh strawberries & blueberries", "fresh mixed berries", 150.0, "g", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("6_3", "Frozen banana", "sliced ripe banana", 1.0, "pcs", "Produce & Meat", allergenTags = emptyList()),
            Ingredient("6_4", "Almond milk", "unsweetened plant milk", 100.0, "ml", "Specialty", allergenTags = emptyList()),
            Ingredient("6_5", "Chia seeds", "raw black chia", 1.0, "tbsp", "Pantry Essentials", allergenTags = emptyList()),
            Ingredient("6_6", "Gluten-free granola", "crunchy rolled oats & nuts", 30.0, "g", "Pantry Essentials", allergenTags = emptyList())
        )
        val steps6 = listOf(
            RecipeStep(1, "Blend Smoothie Base", "Add frozen acai, frozen banana slices, half the berries, and almond milk into a high-powered blender. Blend until thick and spoonable.", 4, false, "Use tamper tool to keep the consistency thick like soft-serve ice cream."),
            RecipeStep(2, "Spoon into Bowl", "Pour the thick chilled acai smoothie into a serving bowl and smooth the top with the back of a spoon.", 1, false, "Chill bowl in freezer 5 minutes beforehand to keep smoothie frozen longer."),
            RecipeStep(3, "Decorate Toppings", "Neatly arrange sliced fresh strawberries, blueberries, crunchy granola, and chia seeds on top.", 2, true, "Enjoy immediately with a chilled spoon.")
        )
        val reviews6 = listOf(
            ReviewItem("r6_1", "Jessica P.", 5, "1d ago", "Refreshing and energizing breakfast! The texture is just like an authentic acai bar."),
            ReviewItem("r6_2", "Daniel C.", 5, "4d ago", "Healthy and ready in minutes. Loved the crunch of the granola with the berries.")
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
                ingredients = ingredients1,
                steps = steps1,
                reviews = reviews1
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
                ingredients = ingredients2,
                steps = steps2,
                reviews = reviews2
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
                ingredients = ingredients3,
                steps = steps3,
                reviews = reviews3
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
                ingredients = ingredients4,
                steps = steps4,
                reviews = reviews4
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
                ingredients = ingredients5,
                steps = steps5,
                reviews = reviews5
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
                ingredients = ingredients6,
                steps = steps6,
                reviews = reviews6
            )
        )

        seeds.forEach { inMemoryRecipes[it.id] = it }
        _recipeReviewsFlow.value = seeds.associate { it.id to it.reviews }
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
        return combine(recipeDao.getAllRecipes(), _recipeReviewsFlow) { entities, reviewsMap ->
            entities.map { entity ->
                val cached = inMemoryRecipes[entity.id]
                val reviews = reviewsMap[entity.id] ?: cached?.reviews ?: emptyList()
                val dynamicRating = if (reviews.isNotEmpty()) {
                    val avg = reviews.map { it.rating }.average()
                    Math.round(avg * 10.0) / 10.0
                } else entity.rating
                val dynamicCount = reviews.size.coerceAtLeast(entity.reviewCount)
                Recipe(
                    id = entity.id,
                    title = entity.title,
                    chefName = entity.chefName,
                    difficulty = entity.difficulty,
                    durationMinutes = entity.durationMinutes,
                    calories = entity.calories,
                    rating = dynamicRating,
                    reviewCount = dynamicCount,
                    isBookmarked = entity.isBookmarked,
                    category = entity.category,
                    dietaryBadges = if (entity.dietaryBadgesRaw.isBlank()) emptyList() else entity.dietaryBadgesRaw.split(","),
                    allergens = if (entity.allergensRaw.isBlank()) emptyList() else entity.allergensRaw.split(","),
                    defaultServings = entity.defaultServings,
                    ingredients = cached?.ingredients ?: emptyList(),
                    steps = cached?.steps ?: emptyList(),
                    reviews = reviews
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
        val currentMap = _recipeReviewsFlow.value.toMutableMap()
        val currentList = currentMap[recipeId] ?: (inMemoryRecipes[recipeId]?.reviews ?: emptyList())
        val updatedList = listOf(review) + currentList
        currentMap[recipeId] = updatedList
        _recipeReviewsFlow.value = currentMap

        val r = inMemoryRecipes[recipeId]
        if (r != null) {
            inMemoryRecipes[recipeId] = r.copy(
                reviews = updatedList,
                reviewCount = r.reviewCount + 1
            )
        }
    }

    suspend fun registerUser(fullName: String, email: String, password: String): Result<Unit> = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = fullName.trim()
        if (cleanName.isBlank() || cleanEmail.isBlank() || password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Semua field wajib diisi"))
        }
        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(IllegalStateException("Email sudah terdaftar"))
        }
        val user = UserEntity(
            email = cleanEmail,
            fullName = cleanName,
            password = password
        )
        userDao.insertUser(user)
        preferencesManager.setLoggedInUser(cleanEmail, cleanName)
        Result.success(Unit)
    }

    suspend fun loginUser(email: String, password: String): Result<Unit> = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email dan password tidak boleh kosong"))
        }
        val user = userDao.getUserByEmail(cleanEmail)
        if (user == null || user.password != password) {
            return@withContext Result.failure(IllegalArgumentException("Email atau kata sandi salah"))
        }
        preferencesManager.setLoggedInUser(user.email, user.fullName)
        Result.success(Unit)
    }

    suspend fun logoutUser() = withContext(ioDispatcher) {
        preferencesManager.clearLoggedInUser()
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(ioDispatcher) {
        val email = preferencesManager.loggedInEmailFlow.first()
        if (email.isNotBlank()) {
            userDao.deleteUser(email)
        }
        preferencesManager.clearLoggedInUser()
        Result.success(Unit)
    }
}
