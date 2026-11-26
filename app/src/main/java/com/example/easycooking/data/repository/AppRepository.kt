package com.example.easycooking.data.repository

import android.content.Context
import com.example.easycooking.data.local.AppDatabase
import com.example.easycooking.data.local.PasswordHasher
import com.example.easycooking.data.local.PreferencesManager
import com.example.easycooking.data.local.entity.GroceryItemEntity
import com.example.easycooking.data.local.entity.GroceryListEntity
import com.example.easycooking.data.local.entity.RecipeEntity
import com.example.easycooking.data.local.entity.UserEntity
import com.example.easycooking.model.ExportGroceryResult
import com.example.easycooking.model.ExportGroceryStatus
import com.example.easycooking.model.GroceryCategories
import com.example.easycooking.model.GroceryItem
import com.example.easycooking.model.GroceryList
import com.example.easycooking.model.Ingredient
import com.example.easycooking.model.Recipe
import com.example.easycooking.model.RecipeStep
import com.example.easycooking.model.ReviewItem
import com.example.easycooking.model.ScalingMode
import com.example.easycooking.model.normalizeUnitName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.roundToInt

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
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    init {
        val ingredients1 = listOf(
            Ingredient("1_1", "Chicken breast", "tender diced fillets", 400.0, "g", GroceryCategories.POULTRY, allergenTags = emptyList()),
            Ingredient("1_2", "Garlic", "finely minced", 3.0, "cloves", GroceryCategories.HERBS_AROMATICS, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("1_3", "Heavy cream", "rich cooking cream", 100.0, "ml", GroceryCategories.DAIRY_EGGS, allergenTags = listOf("Dairy"), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("1_4", "Olive oil", "extra virgin", 2.0, "tbsp", GroceryCategories.OILS_SAUCES, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("1_5", "Baby spinach & sun-dried tomatoes", "fresh & chopped", 50.0, "g", GroceryCategories.FRESH_VEGETABLES, allergenTags = emptyList()),
            Ingredient("1_6", "Parmesan cheese", "shaved garnish", 25.0, "g", GroceryCategories.CHEESE, allergenTags = listOf("Dairy")),
            Ingredient("1_7", "Fresh basil", "chopped garnish", 10.0, "g", GroceryCategories.HERBS_AROMATICS, allergenTags = emptyList(), scalingMode = ScalingMode.FIXED)
        )
        val steps1 = listOf(
            RecipeStep(1, "Sear Chicken", "Heat olive oil in a deep skillet over medium-high heat. Brown seasoned chicken breasts 4-5 minutes per side until deeply golden brown.", 9, false, "Make sure the pan is hot before adding chicken for optimal sear."),
            RecipeStep(2, "Simmer Garlic Cream", "Saute minced garlic for 1 minute, then pour rich heavy cream, baby spinach & sun-dried tomatoes. Simmer gently on low heat until velvety smooth.", 9, false, "Keep heat at medium-low to prevent cream from curdling."),
            RecipeStep(3, "Combine & Garnish", "Return chicken breasts into the skillet with warm garlic cream sauce. Spoon sauce generously over chicken and garnish with freshly chopped basil and shaved parmesan.", 7, true, "Let rest for 2 minutes before serving so the chicken retains its juiciness.")
        )
        val reviews1 = listOf(
            ReviewItem("r1_1", "Sarah K.", 5, "2d ago", "Super easy for weeknights, the garlic aroma is incredible! Paired it with freshly toasted sourdough."),
            ReviewItem("r1_2", "Alex M.", 4, "5d ago", "Delicious sauce, perfectly balanced with the chicken. Added extra sun-dried tomatoes for sweetness.")
        )

        val ingredients2 = listOf(
            Ingredient("2_1", "Salmon fillets", "skin-on fresh fillets", 350.0, "g", GroceryCategories.SEAFOOD_SHELLFISH, allergenTags = listOf("Fish")),
            Ingredient("2_2", "Unsalted butter", "melted creamy butter", 2.0, "tbsp", GroceryCategories.DAIRY_EGGS, allergenTags = listOf("Dairy"), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("2_3", "Fresh lemon juice", "freshly squeezed", 2.0, "tbsp", GroceryCategories.FRESH_FRUITS, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("2_4", "Garlic", "finely minced", 2.0, "cloves", GroceryCategories.HERBS_AROMATICS, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("2_5", "Fresh dill & parsley", "finely chopped", 15.0, "g", GroceryCategories.HERBS_AROMATICS, allergenTags = emptyList(), scalingMode = ScalingMode.FIXED),
            Ingredient("2_6", "Olive oil", "extra virgin", 1.0, "tbsp", GroceryCategories.OILS_SAUCES, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE)
        )
        val steps2 = listOf(
            RecipeStep(1, "Season & Sear Salmon", "Pat salmon dry with paper towels, season with sea salt and black pepper. Heat olive oil in a skillet and sear skin-side down for 4 minutes until crispy.", 8, false, "Press salmon down gently with a spatula to prevent curling."),
            RecipeStep(2, "Make Lemon Butter Glaze", "Flip salmon fillets. Add butter, minced garlic, and fresh lemon juice into the skillet, spooning the foaming butter continuously over salmon.", 8, false, "Baste continuously to keep the salmon moist and tender."),
            RecipeStep(3, "Rest & Garnish", "Remove from heat when salmon flakes easily with a fork. Scatter fresh dill and parsley, serve with lemon wedges.", 4, true, "Do not overcook; internal salmon should remain tender pink.")
        )
        val reviews2 = listOf(
            ReviewItem("r2_1", "David L.", 5, "1d ago", "The salmon came out perfectly flaky and the lemon butter sauce was incredible! Restaurant quality at home."),
            ReviewItem("r2_2", "Maya R.", 5, "3d ago", "So fast to make and crispy skin on point. Paired with roasted asparagus.")
        )

        val ingredients3 = listOf(
            Ingredient("3_1", "Fettuccine pasta", "durum wheat pasta", 250.0, "g", GroceryCategories.PASTA_GRAINS, allergenTags = listOf("Gluten")),
            Ingredient("3_2", "Cremini mushrooms", "freshly sliced", 200.0, "g", GroceryCategories.FRESH_VEGETABLES, allergenTags = emptyList()),
            Ingredient("3_3", "White truffle oil", "aromatic finishing oil", 1.0, "tbsp", GroceryCategories.OILS_SAUCES, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("3_4", "Heavy cream", "cooking cream", 120.0, "ml", GroceryCategories.DAIRY_EGGS, allergenTags = listOf("Dairy"), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("3_5", "Parmesan cheese", "freshly grated", 40.0, "g", GroceryCategories.CHEESE, allergenTags = listOf("Dairy")),
            Ingredient("3_6", "Garlic", "minced cloves", 2.0, "cloves", GroceryCategories.HERBS_AROMATICS, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("3_7", "Unsalted butter", "rich cooking butter", 1.0, "tbsp", GroceryCategories.DAIRY_EGGS, allergenTags = listOf("Dairy"), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("3_8", "Olive oil", "extra virgin", 1.0, "tbsp", GroceryCategories.OILS_SAUCES, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE)
        )
        val steps3 = listOf(
            RecipeStep(1, "Boil Pasta", "Bring a large pot of salted water to a rolling boil. Cook fettuccine al dente for 8-9 minutes, reserving 1/2 cup pasta water before draining.", 10, false, "Reserve pasta water to emulsify the truffle sauce later."),
            RecipeStep(2, "Saute Mushrooms", "Heat butter and olive oil in a pan, saute sliced mushrooms until caramelized and golden, then add garlic for 1 minute.", 9, false, "Do not crowd the mushrooms so they brown nicely instead of steaming."),
            RecipeStep(3, "Toss with Truffle Cream", "Stir heavy cream and grated parmesan into pan until smooth. Toss fettuccine and drizzle white truffle oil gently before serving.", 6, true, "Add truffle oil off heat to preserve its delicate earthy aroma.")
        )
        val reviews3 = listOf(
            ReviewItem("r3_1", "Kevin T.", 5, "2d ago", "Heavenly truffle aroma! Truly luxurious vegetarian pasta, tasted like fine dining."),
            ReviewItem("r3_2", "Clara S.", 4, "4d ago", "Rich and creamy. Make sure to use high quality truffle oil for the best fragrance.")
        )

        val ingredients4 = listOf(
            Ingredient("4_1", "Spaghetti", "whole grain pasta", 200.0, "g", GroceryCategories.PASTA_GRAINS, allergenTags = listOf("Gluten")),
            Ingredient("4_2", "Ripe avocados", "creamy halved & peeled", 2.0, "pcs", GroceryCategories.FRESH_VEGETABLES, allergenTags = emptyList()),
            Ingredient("4_3", "Fresh basil leaves", "fragrant sweet basil", 30.0, "g", GroceryCategories.HERBS_AROMATICS, allergenTags = emptyList()),
            Ingredient("4_4", "Walnuts or pine nuts", "toasted nuts", 30.0, "g", GroceryCategories.NUTS_SEEDS_BAKING, allergenTags = listOf("Tree Nuts")),
            Ingredient("4_5", "Lemon juice", "freshly squeezed", 2.0, "tbsp", GroceryCategories.FRESH_FRUITS, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("4_6", "Extra virgin olive oil", "unfiltered", 2.0, "tbsp", GroceryCategories.OILS_SAUCES, allergenTags = emptyList(), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("4_7", "Garlic", "peeled clove", 1.0, "cloves", GroceryCategories.HERBS_AROMATICS, allergenTags = emptyList(), scalingMode = ScalingMode.FIXED)
        )
        val steps4 = listOf(
            RecipeStep(1, "Boil Spaghetti", "Cook spaghetti in boiling salted water according to package instructions until al dente. Drain and set aside.", 7, false, "Do not rinse with hot water; keep pasta warm."),
            RecipeStep(2, "Blend Avocado Pesto", "Place ripe avocados, basil, garlic, toasted nuts, lemon juice, salt, and olive oil in a blender. Pulse until silky smooth and vibrant green.", 3, false, "Lemon juice keeps the avocado bright green and prevents browning."),
            RecipeStep(3, "Toss Together", "Combine warm pasta with smooth avocado pesto in a large bowl. Toss evenly and garnish with fresh basil leaves and cracked pepper.", 2, true, "Serve immediately while creamy and fresh.")
        )
        val reviews4 = listOf(
            ReviewItem("r4_1", "Emily H.", 5, "2d ago", "Incredible creamy texture without any dairy! Super vibrant and satisfying vegan lunch."),
            ReviewItem("r4_2", "Marcus J.", 5, "5d ago", "The avocado pesto is so velvety and the lemon balances it out perfectly. Will make this again!")
        )

        val ingredients5 = listOf(
            Ingredient("5_1", "Frozen acai puree", "unsweetened acai packet", 100.0, "g", GroceryCategories.FROZEN_FOODS, allergenTags = emptyList()),
            Ingredient("5_2", "Fresh strawberries & blueberries", "fresh mixed berries", 150.0, "g", GroceryCategories.FRESH_FRUITS, allergenTags = emptyList()),
            Ingredient("5_3", "Frozen banana", "sliced ripe banana", 1.0, "pcs", GroceryCategories.FROZEN_FOODS, allergenTags = emptyList()),
            Ingredient("5_4", "Almond milk", "unsweetened plant milk", 100.0, "ml", GroceryCategories.DAIRY_EGGS, allergenTags = listOf("Tree Nuts"), scalingMode = ScalingMode.COOKING_BASE),
            Ingredient("5_5", "Chia seeds", "raw black chia", 1.0, "tbsp", GroceryCategories.NUTS_SEEDS_BAKING, allergenTags = emptyList(), scalingMode = ScalingMode.FIXED),
            Ingredient("5_6", "Gluten-free granola", "crunchy rolled oats & nuts", 30.0, "g", GroceryCategories.BREAD_BAKERY, allergenTags = listOf("Tree Nuts"))
        )
        val steps5 = listOf(
            RecipeStep(1, "Blend Smoothie Base", "Add frozen acai, frozen banana slices, half the berries, and almond milk into a high-powered blender. Blend until thick and spoonable.", 5, false, "Use tamper tool to keep the consistency thick like soft-serve ice cream."),
            RecipeStep(2, "Spoon into Bowl", "Pour the thick chilled acai smoothie into a serving bowl and smooth the top with the back of a spoon.", 2, false, "Chill bowl in freezer 5 minutes beforehand to keep smoothie frozen longer."),
            RecipeStep(3, "Decorate Toppings", "Neatly arrange sliced fresh strawberries, blueberries, crunchy granola, and chia seeds on top.", 3, true, "Enjoy immediately as a cool sweet dessert bowl.")
        )
        val reviews5 = listOf(
            ReviewItem("r5_1", "Jessica P.", 5, "1d ago", "Refreshing and naturally sweet! The texture is just like frozen gelato."),
            ReviewItem("r5_2", "Daniel C.", 5, "4d ago", "Healthy dessert ready in minutes. Loved the crunch of the granola with berries.")
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
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free", "Nut-Free", "High Protein"),
                allergens = listOf("Dairy"),
                defaultServings = 2,
                ingredients = ingredients1,
                steps = steps1,
                reviews = reviews1,
                imageUri = "res:recipe_1"
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
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free", "Low Carb", "Pescatarian", "High Protein"),
                allergens = listOf("Fish", "Dairy"),
                defaultServings = 2,
                ingredients = ingredients2,
                steps = steps2,
                reviews = reviews2,
                imageUri = "res:recipe_2"
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
                reviews = reviews3,
                imageUri = "res:recipe_3"
            ),
            Recipe(
                id = "4",
                title = "Avocado Pesto Pasta",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 12,
                calories = 320,
                rating = 4.7,
                reviewCount = 140,
                isBookmarked = false,
                category = "Lunch",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Dairy-Free"),
                allergens = listOf("Gluten", "Tree Nuts"),
                defaultServings = 2,
                ingredients = ingredients4,
                steps = steps4,
                reviews = reviews4,
                imageUri = "res:recipe_4"
            ),
            Recipe(
                id = "5",
                title = "Berry Acai Bowl",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 10,
                calories = 260,
                rating = 5.0,
                reviewCount = 88,
                isBookmarked = false,
                category = "Dessert",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free"),
                allergens = listOf("Tree Nuts"),
                defaultServings = 2,
                ingredients = ingredients5,
                steps = steps5,
                reviews = reviews5,
                imageUri = "res:recipe_5"
            )
        )

        (seeds + createAdditionalSeeds()).forEach { recipe ->
            inMemoryRecipes[recipe.id] = recipe.copy(
                ingredients = recipe.ingredients.map { ingredient ->
                    ingredient.copy(category = GroceryCategories.refineCategory(ingredient.name, ingredient.category))
                }
            )
        }
    }

    private fun ing(
        id: String,
        name: String,
        subtitle: String,
        amount: Double,
        unit: String,
        category: String,
        allergenTags: List<String> = emptyList(),
        scalingMode: ScalingMode = ScalingMode.PROPORTIONAL
    ): Ingredient = Ingredient(
        id = id,
        name = name,
        subtitle = subtitle,
        baseAmount = amount,
        unit = unit,
        category = category,
        isChecked = false,
        allergenTags = allergenTags,
        scalingMode = scalingMode
    )

    private fun createAdditionalSeeds(): List<Recipe> {
        return listOf(
            Recipe(
                id = "6",
                title = "Mediterranean Quinoa Salad",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 15,
                calories = 280,
                rating = 4.8,
                reviewCount = 112,
                isBookmarked = false,
                category = "Lunch",
                dietaryBadges = listOf("Vegetarian", "Gluten-Free", "Nut-Free"),
                allergens = listOf("Dairy"),
                defaultServings = 2,
                imageUri = "res:recipe_6",
                ingredients = listOf(
                    ing("6_1", "Cooked quinoa", "fluffy tri-color quinoa", 200.0, "g", GroceryCategories.PASTA_GRAINS),
                    ing("6_2", "English cucumber", "diced crisp cucumber", 1.0, "pcs", GroceryCategories.FRESH_VEGETABLES),
                    ing("6_3", "Cherry tomatoes", "halved sweet tomatoes", 120.0, "g", GroceryCategories.FRESH_VEGETABLES),
                    ing("6_4", "Kalamata olives", "pitted and sliced", 40.0, "g", GroceryCategories.FRESH_VEGETABLES),
                    ing("6_5", "Feta cheese", "crumbled Greek feta", 40.0, "g", GroceryCategories.CHEESE, listOf("Dairy")),
                    ing("6_6", "Extra virgin olive oil", "cold pressed dressing", 2.0, "tbsp", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE),
                    ing("6_7", "Fresh lemon juice", "freshly squeezed", 2.0, "tbsp", GroceryCategories.FRESH_FRUITS, scalingMode = ScalingMode.COOKING_BASE)
                ),
                steps = listOf(
                    RecipeStep(1, "Cool Quinoa", "Spread warm cooked quinoa on a plate to cool to room temperature.", 5, false, "Cool quinoa prevents fresh diced vegetables from wilting."),
                    RecipeStep(2, "Chop Produce", "Dice English cucumber and halve fresh cherry tomatoes evenly.", 6, false, "Uniform sizes give each forkful a balanced crunch."),
                    RecipeStep(3, "Whisk & Toss", "Whisk olive oil, lemon juice, salt, and pepper in a bowl. Toss quinoa, vegetables, olives, and feta together.", 4, true, "Serve chilled or at room temperature.")
                ),
                reviews = listOf(
                    ReviewItem("r6_1", "Liam N.", 5, "1d ago", "So fresh and vibrant! Perfect healthy lunch for work."),
                    ReviewItem("r6_2", "Sophia B.", 5, "3d ago", "The lemon dressing pulls all the Mediterranean flavors together nicely.")
                )
            ),
            Recipe(
                id = "7",
                title = "Classic Margherita Pizza",
                chefName = "Chef Marco",
                difficulty = "Medium",
                durationMinutes = 25,
                calories = 450,
                rating = 4.9,
                reviewCount = 260,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Vegetarian", "Nut-Free"),
                allergens = listOf("Gluten", "Dairy"),
                defaultServings = 2,
                imageUri = "res:recipe_7",
                ingredients = listOf(
                    ing("7_1", "Pizza dough", "fermented artisanal dough", 300.0, "g", GroceryCategories.BREAD_BAKERY, listOf("Gluten")),
                    ing("7_2", "San Marzano sauce", "crushed plum tomatoes", 100.0, "ml", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE),
                    ing("7_3", "Fresh mozzarella", "sliced buffalo mozzarella", 150.0, "g", GroceryCategories.CHEESE, listOf("Dairy")),
                    ing("7_4", "Fresh basil leaves", "sweet Italian basil", 10.0, "leaves", GroceryCategories.HERBS_AROMATICS, scalingMode = ScalingMode.FIXED),
                    ing("7_5", "Olive oil", "extra virgin", 1.0, "tbsp", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE)
                ),
                steps = listOf(
                    RecipeStep(1, "Stretch Dough", "Gently stretch pizza dough into a 12-inch round on parchment paper.", 7, false, "Do not use a rolling pin; preserve airy dough bubbles."),
                    RecipeStep(2, "Sauce & Top", "Spread crushed tomato sauce in concentric circles and scatter fresh mozzarella slices.", 4, false, "Avoid overloading with sauce to keep the crust crispy."),
                    RecipeStep(3, "Bake High Heat", "Bake at 250C on a preheated pizza stone for 10-12 minutes until crust is puffed and blistered. Top with fresh basil.", 14, true, "Add fresh basil immediately after baking so it releases fragrant oils.")
                ),
                reviews = listOf(
                    ReviewItem("r7_1", "Antonio G.", 5, "2d ago", "Authentic Neapolitan taste! The crust puffed up beautifully."),
                    ReviewItem("r7_2", "Emily V.", 5, "5d ago", "Simple ingredients done right. Best homemade pizza recipe.")
                )
            ),
            Recipe(
                id = "8",
                title = "Thai Coconut Tofu Curry",
                chefName = "Chef Somchai",
                difficulty = "Easy",
                durationMinutes = 25,
                calories = 340,
                rating = 4.8,
                reviewCount = 175,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free", "Nut-Free"),
                allergens = listOf("Soy"),
                defaultServings = 2,
                imageUri = "res:recipe_8",
                ingredients = listOf(
                    ing("8_1", "Firm tofu", "pressed and cubed", 300.0, "g", GroceryCategories.TOFU_LEGUMES, listOf("Soy")),
                    ing("8_2", "Coconut milk", "full fat creamy milk", 250.0, "ml", GroceryCategories.DAIRY_EGGS, scalingMode = ScalingMode.COOKING_BASE),
                    ing("8_3", "Red curry paste", "authentic Thai paste", 2.0, "tbsp", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE),
                    ing("8_4", "Bell peppers", "sliced red and yellow", 100.0, "g", GroceryCategories.FRESH_VEGETABLES),
                    ing("8_5", "Bamboo shoots", "tender sliced shoots", 50.0, "g", GroceryCategories.FRESH_VEGETABLES),
                    ing("8_6", "Coconut oil", "pan frying oil", 1.0, "tbsp", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE)
                ),
                steps = listOf(
                    RecipeStep(1, "Crisp Tofu", "Pan-fry tofu cubes in coconut oil until golden and crisp on all sides.", 9, false, "Pressing water out of tofu ensures a delightfully crispy crust."),
                    RecipeStep(2, "Simmer Curry Paste", "Fry red curry paste in pan until aromatic, then whisk in rich coconut milk.", 6, false, "Frying the paste releases its fragrant chili and herb oils."),
                    RecipeStep(3, "Combine Vegetables", "Add bell peppers and bamboo shoots, simmer gently for 5 minutes, and return crispy tofu.", 10, true, "Serve warm with fragrant jasmine rice.")
                ),
                reviews = listOf(
                    ReviewItem("r8_1", "Maya K.", 5, "2d ago", "The curry sauce is restaurant-worthy! Wonderfully balanced spicy and sweet."),
                    ReviewItem("r8_2", "Arthur L.", 5, "6d ago", "Crispy tofu soaked up all the red curry flavor beautifully.")
                )
            ),
            Recipe(
                id = "9",
                title = "Grilled Ribeye with Herb Butter",
                chefName = "Chef Gordon",
                difficulty = "Medium",
                durationMinutes = 20,
                calories = 580,
                rating = 5.0,
                reviewCount = 310,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Keto", "Low Carb", "Gluten-Free", "High Protein", "Nut-Free"),
                allergens = listOf("Dairy"),
                defaultServings = 2,
                imageUri = "res:recipe_9",
                ingredients = listOf(
                    ing("9_1", "Ribeye steak", "prime marbling cut", 450.0, "g", GroceryCategories.BEEF_PORK),
                    ing("9_2", "Butter", "softened grass-fed butter", 3.0, "tbsp", GroceryCategories.DAIRY_EGGS, listOf("Dairy"), scalingMode = ScalingMode.COOKING_BASE),
                    ing("9_3", "Fresh rosemary & thyme", "finely minced", 10.0, "g", GroceryCategories.HERBS_AROMATICS, scalingMode = ScalingMode.FIXED),
                    ing("9_4", "Garlic", "crushed cloves", 3.0, "cloves", GroceryCategories.HERBS_AROMATICS, scalingMode = ScalingMode.COOKING_BASE),
                    ing("9_5", "Olive oil", "high smoke point searing oil", 1.0, "tbsp", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE)
                ),
                steps = listOf(
                    RecipeStep(1, "Temper & Season", "Let ribeye rest at room temp for 20 minutes, then season heavily with coarse sea salt and cracked pepper.", 6, false, "Cold meat straight from fridge lowers skillet temperature instantly."),
                    RecipeStep(2, "Cast Iron Sear", "Sear ribeye in smoking hot cast iron skillet for 3 minutes per side until deeply caramelized.", 8, false, "Do not touch meat during sear to form a crusty exterior."),
                    RecipeStep(3, "Butter Baste & Rest", "Add butter, garlic, and fresh herbs into pan. Tilt pan and spoon foaming butter over steak for 1 minute, then rest 5 minutes.", 6, true, "Resting allows muscle fibers to relax and retain rich juices.")
                ),
                reviews = listOf(
                    ReviewItem("r9_1", "Ethan H.", 5, "1d ago", "Perfection! The herb butter melts over the juicy steak creating an insane crust."),
                    ReviewItem("r9_2", "Victor S.", 5, "4d ago", "Ultimate keto comfort meal. Restaurant quality steak at home.")
                )
            ),
            Recipe(
                id = "10",
                title = "Bacon Wrapped Asparagus",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 15,
                calories = 220,
                rating = 4.8,
                reviewCount = 120,
                isBookmarked = false,
                category = "Snack",
                dietaryBadges = listOf("Keto", "Low Carb", "Gluten-Free", "Dairy-Free", "Nut-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                imageUri = "res:recipe_10",
                ingredients = listOf(
                    ing("10_1", "Fresh asparagus", "woody ends trimmed", 200.0, "g", GroceryCategories.FRESH_VEGETABLES),
                    ing("10_2", "Smoked bacon", "thin cut bacon slices", 6.0, "pcs", GroceryCategories.BEEF_PORK),
                    ing("10_3", "Black pepper", "freshly cracked", 1.0, "tsp", GroceryCategories.SPICES_SEASONINGS, scalingMode = ScalingMode.COOKING_BASE),
                    ing("10_4", "Olive oil spray", "light roasting mist", 1.0, "tsp", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.FIXED)
                ),
                steps = listOf(
                    RecipeStep(1, "Trim Asparagus", "Snap off tough woody bottom ends of asparagus spears.", 3, false, "Asparagus naturally snaps right where tender stalk begins."),
                    RecipeStep(2, "Wrap Bacon", "Tightly wrap 3-4 asparagus spears together with a strip of bacon spiraling up the stalks.", 4, false, "Overlap bacon edges slightly so it stays wrapped during cooking."),
                    RecipeStep(3, "Roast or Air Fry", "Roast at 200C for 8-10 minutes until bacon is crispy and asparagus is tender-crisp.", 8, true, "Turn halfway through for 360-degree bacon crispiness.")
                ),
                reviews = listOf(
                    ReviewItem("r10_1", "Nathan G.", 5, "2d ago", "Fantastic savory appetizer! Bacon was super crispy and asparagus tender."),
                    ReviewItem("r10_2", "Amber Y.", 4, "5d ago", "So easy to make in the air fryer. Everyone devoured them in minutes.")
                )
            ),
            Recipe(
                id = "11",
                title = "Baja Crispy Fish Tacos",
                chefName = "Chef Carlos",
                difficulty = "Medium",
                durationMinutes = 25,
                calories = 410,
                rating = 4.8,
                reviewCount = 220,
                isBookmarked = false,
                category = "Lunch",
                dietaryBadges = listOf("Pescatarian", "High Protein", "Nut-Free"),
                allergens = listOf("Fish", "Gluten", "Dairy"),
                defaultServings = 2,
                imageUri = "res:recipe_11",
                ingredients = listOf(
                    ing("11_1", "Cod fish fillets", "fresh wild cod cut into strips", 300.0, "g", GroceryCategories.SEAFOOD_SHELLFISH, listOf("Fish")),
                    ing("11_2", "Tortillas", "soft flour tortillas", 4.0, "pcs", GroceryCategories.BREAD_BAKERY, listOf("Gluten")),
                    ing("11_3", "Cabbage slaw", "shredded red & white cabbage", 100.0, "g", GroceryCategories.FRESH_VEGETABLES),
                    ing("11_4", "Chipotle crema", "spicy smoky cream", 3.0, "tbsp", GroceryCategories.OILS_SAUCES, listOf("Dairy"), scalingMode = ScalingMode.COOKING_BASE),
                    ing("11_5", "Olive oil", "pan frying oil", 200.0, "ml", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE),
                    ing("11_6", "Fresh lime", "juicy wedges", 1.0, "pcs", GroceryCategories.FRESH_FRUITS, scalingMode = ScalingMode.FIXED)
                ),
                steps = listOf(
                    RecipeStep(1, "Fry Fish Strips", "Heat frying oil in a deep skillet and sear dusted cod strips for 3 minutes per side until crispy.", 12, false, "Cod stays flaky and juicy when fried quickly over medium-high heat."),
                    RecipeStep(2, "Warm Tortillas", "Warm soft tortillas on a dry skillet for 30 seconds per side until pliable.", 5, false, "Warming tortillas prevents them from tearing under taco fillings."),
                    RecipeStep(3, "Assemble Tacos", "Fill tortillas with crispy cod, crunchy cabbage slaw, chipotle crema, and fresh squeeze of lime.", 8, true, "Zesty, crunchy, and packed with fresh Mexican coastal flavor.")
                ),
                reviews = listOf(
                    ReviewItem("r11_1", "Carlos M.", 5, "1d ago", "Tastes just like the authentic street tacos in Ensenada! Amazing."),
                    ReviewItem("r11_2", "Serena K.", 5, "4d ago", "The chipotle crema pairs insanely well with the crunchy cabbage and tender fish.")
                )
            ),
            Recipe(
                id = "12",
                title = "Spinach & Feta Omelette",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 12,
                calories = 310,
                rating = 4.8,
                reviewCount = 195,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegetarian", "Keto", "Low Carb", "Gluten-Free", "High Protein", "Nut-Free"),
                allergens = listOf("Eggs", "Dairy"),
                defaultServings = 1,
                imageUri = "res:recipe_12",
                ingredients = listOf(
                    ing("12_1", "Fresh eggs", "large free-range eggs", 3.0, "pcs", GroceryCategories.DAIRY_EGGS, listOf("Eggs")),
                    ing("12_2", "Feta cheese", "crumbled Greek feta", 40.0, "g", GroceryCategories.CHEESE, listOf("Dairy")),
                    ing("12_3", "Baby spinach", "washed fresh spinach", 60.0, "g", GroceryCategories.FRESH_VEGETABLES),
                    ing("12_4", "Olive oil", "extra virgin", 1.0, "tbsp", GroceryCategories.OILS_SAUCES, scalingMode = ScalingMode.COOKING_BASE),
                    ing("12_5", "Black pepper", "freshly ground", 1.0, "tsp", GroceryCategories.SPICES_SEASONINGS, scalingMode = ScalingMode.FIXED)
                ),
                steps = listOf(
                    RecipeStep(1, "Wilt Spinach", "Heat olive oil in non-stick pan, add baby spinach and cook for 1 minute until wilted.", 3, false, "Wilting spinach beforehand releases liquid so omelette stays fluffy."),
                    RecipeStep(2, "Cook Eggs", "Whisk eggs with pepper, pour over spinach in pan, and swirl on medium-low heat until edges set.", 6, false, "Low and slow heat yields creamy velvety curds without browning."),
                    RecipeStep(3, "Fold with Feta", "Scatter crumbled feta cheese over half of omelette, fold over gently, and slide onto plate.", 3, true, "Serve warm with rich melted feta inside.")
                ),
                reviews = listOf(
                    ReviewItem("r12_1", "Vanessa R.", 5, "2d ago", "The salty feta with wilted spinach is a classic breakfast match! Fluffy and rich."),
                    ReviewItem("r12_2", "Gregory P.", 5, "4d ago", "Takes 10 minutes and keeps me fueled throughout entire morning.")
                )
            ),
            Recipe(
                id = "13",
                title = "Fluffy Buttermilk Pancakes",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 15,
                calories = 350,
                rating = 4.9,
                reviewCount = 210,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegetarian", "Nut-Free"),
                allergens = listOf("Gluten", "Dairy", "Eggs"),
                defaultServings = 2,
                imageUri = "res:recipe_13",
                ingredients = listOf(
                    ing("13_1", "All-purpose flour", "unbleached flour", 200.0, "g", GroceryCategories.NUTS_SEEDS_BAKING, listOf("Gluten")),
                    ing("13_2", "Fresh buttermilk", "cultured buttermilk", 250.0, "ml", GroceryCategories.DAIRY_EGGS, listOf("Dairy")),
                    ing("13_3", "Large egg", "fresh free-range egg", 1.0, "pcs", GroceryCategories.DAIRY_EGGS, listOf("Eggs")),
                    ing("13_4", "Melted butter", "unsalted butter", 2.0, "tbsp", GroceryCategories.DAIRY_EGGS, listOf("Dairy"), scalingMode = ScalingMode.COOKING_BASE),
                    ing("13_5", "Pure maple syrup", "grade A dark maple", 2.0, "tbsp", GroceryCategories.NUTS_SEEDS_BAKING, scalingMode = ScalingMode.COOKING_BASE)
                ),
                steps = listOf(
                    RecipeStep(1, "Whisk Batter", "Whisk dry ingredients, then fold in buttermilk, egg, and melted butter gently.", 5, false, "Do not overmix; small lumps make the pancakes extra fluffy."),
                    RecipeStep(2, "Griddle Cooking", "Ladle 1/4 cup batter onto a buttered non-stick skillet over medium heat.", 7, false, "Flip when small bubbles pop on surface and edges look set."),
                    RecipeStep(3, "Stack & Drizzle", "Cook 2 minutes on second side until golden brown, stack high and top with maple syrup.", 3, true, "Serve steaming hot with extra whipped butter.")
                ),
                reviews = listOf(
                    ReviewItem("r13_1", "Lucas M.", 5, "1d ago", "The fluffiest pancakes I've ever made at home! Kids loved them."),
                    ReviewItem("r13_2", "Chloe R.", 5, "3d ago", "Pure weekend brunch perfection.")
                )
            )
        )
    }

    suspend fun seedDatabaseIfEmpty() = withContext(ioDispatcher) {
        val currentSeedVersion = 6
        val prevSeedVersion = preferencesManager.getSeedVersion()
        val existingCount = recipeDao.getRecipeCount()

        if (existingCount > 0) {
            if (prevSeedVersion < currentSeedVersion) {
                if (prevSeedVersion < 6) {
                    recipeDao.deleteAllRecipes()
                    groceryDao.deleteLegacySampleItems()
                    groceryDao.deleteLegacySampleLists()
                    inMemoryRecipes.entries.removeAll {
                        it.key.startsWith("user_rec_") || it.value.ownerEmail.isNotBlank()
                    }
                    recipeDao.insertAll(inMemoryRecipes.values.map { r -> toRecipeEntity(r) })
                } else {
                    val updatedSeeds = inMemoryRecipes.values.map { seed ->
                        val existing = recipeDao.getRecipeById(seed.id)
                        existing?.copy(
                            durationMinutes = seed.durationMinutes,
                            dietaryBadgesRaw = seed.dietaryBadges.joinToString(","),
                            allergensRaw = seed.allergens.joinToString(","),
                            ingredientsJson = ingredientsToJson(seed.ingredients),
                            stepsJson = stepsToJson(seed.steps),
                            imageUri = seed.imageUri
                        ) ?: toRecipeEntity(seed)
                    }
                    if (updatedSeeds.isNotEmpty()) {
                        recipeDao.insertAll(updatedSeeds)
                    }
                }
                preferencesManager.setSeedVersion(currentSeedVersion)
            }
            preferencesManager.setDatabaseSeeded(true)
            return@withContext
        }

        val entities = inMemoryRecipes.values.map { r -> toRecipeEntity(r) }
        recipeDao.insertAll(entities)
        preferencesManager.setDatabaseSeeded(true)
        preferencesManager.setSeedVersion(currentSeedVersion)
    }

    fun getAllRecipes(): Flow<List<Recipe>> {
        return combine(
            recipeDao.getAllRecipes(),
            preferencesManager.isLoggedInFlow,
            preferencesManager.loggedInEmailFlow
        ) { entities, isLoggedIn, loggedEmail ->
            entities
                .filter { entity ->
                    entity.ownerEmail.isBlank() ||
                        (isLoggedIn && entity.ownerEmail.equals(loggedEmail, ignoreCase = true))
                }
                .map { entity ->
                    val cached = inMemoryRecipes[entity.id]
                    val ingredients = if (entity.ingredientsJson.isNotBlank()) {
                        jsonToIngredients(entity.ingredientsJson)
                    } else {
                        cached?.ingredients ?: emptyList()
                    }
                    val steps = if (entity.stepsJson.isNotBlank()) {
                        jsonToSteps(entity.stepsJson)
                    } else {
                        cached?.steps ?: emptyList()
                    }
                    val reviews = if (entity.reviewsJson.isNotBlank()) {
                        jsonToReviews(entity.reviewsJson)
                    } else {
                        cached?.reviews ?: emptyList()
                    }
                    Recipe(
                        id = entity.id,
                        title = entity.title,
                        chefName = entity.chefName,
                        difficulty = entity.difficulty,
                        durationMinutes = entity.durationMinutes,
                        calories = entity.calories,
                        rating = entity.rating,
                        reviewCount = entity.reviewCount,
                        isBookmarked = if (isLoggedIn) entity.isBookmarked else false,
                        category = entity.category,
                        dietaryBadges = if (entity.dietaryBadgesRaw.isBlank()) emptyList() else entity.dietaryBadgesRaw.split(","),
                        allergens = if (entity.allergensRaw.isBlank()) emptyList() else entity.allergensRaw.split(","),
                        defaultServings = entity.defaultServings,
                        ingredients = ingredients,
                        steps = steps,
                        reviews = reviews,
                        ownerEmail = entity.ownerEmail,
                        isDraft = entity.isDraft,
                        imageUri = entity.imageUri
                    )
                }
        }.flowOn(ioDispatcher)
    }

    suspend fun toggleBookmark(recipeId: String, currentStatus: Boolean) = withContext(ioDispatcher) {
        recipeDao.updateBookmark(recipeId, !currentStatus)
    }

    fun getGroceryLists(): Flow<List<GroceryList>> {
        return combine(
            groceryDao.getAllLists(),
            groceryDao.getAllItems(),
            preferencesManager.isLoggedInFlow,
            preferencesManager.loggedInEmailFlow
        ) { lists, allItems, isLoggedIn, loggedEmail ->
            if (!isLoggedIn || loggedEmail.isBlank()) {
                return@combine emptyList()
            }
            lists.filter { it.ownerEmail.equals(loggedEmail, ignoreCase = true) }
                .map { listEntity ->
                    val matchingItems = allItems.filter { it.listId == listEntity.id }.map { itemEntity ->
                        GroceryItem(
                            id = itemEntity.id,
                            recipeId = itemEntity.recipeId,
                            name = itemEntity.name,
                            quantity = itemEntity.quantity,
                            unit = normalizeUnitName(itemEntity.unit),
                            category = GroceryCategories.refineCategory(itemEntity.name, itemEntity.category),
                            isChecked = itemEntity.isChecked
                        )
                    }
                    val isCompleted = matchingItems.isNotEmpty() && matchingItems.all { it.isChecked }
                    GroceryList(
                        id = listEntity.id,
                        title = listEntity.title,
                        updatedAt = listEntity.updatedAt,
                        items = matchingItems,
                        isCompleted = isCompleted,
                        ownerEmail = listEntity.ownerEmail
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

    suspend fun createNewGroceryList(title: String): String = withContext(ioDispatcher) {
        val ownerEmail = preferencesManager.loggedInEmailFlow.first()
        val listId = "list_${UUID.randomUUID()}"
        groceryDao.insertList(
            GroceryListEntity(
                id = listId,
                title = title.trim().ifBlank { "Custom Shopping List" },
                updatedAt = "Just now",
                isCompleted = false,
                ownerEmail = ownerEmail
            )
        )
        listId
    }

    suspend fun addCustomGroceryItem(
        listId: String,
        name: String,
        category: String = GroceryCategories.DEFAULT_SECTION
    ) = withContext(ioDispatcher) {
        val trimmed = name.trim()
        val chosenCategory = category.ifBlank { GroceryCategories.DEFAULT_SECTION }
        if (trimmed.isBlank()) return@withContext
        val existingItems = groceryDao.getItemsForListOnce(listId)
        val duplicate = existingItems.find {
            it.name.equals(trimmed, ignoreCase = true) &&
                it.unit.equals("pcs", ignoreCase = true) &&
                it.category.equals(chosenCategory, ignoreCase = true)
        }
        if (duplicate != null) {
            groceryDao.updateItemQuantity(duplicate.id, duplicate.quantity + 1.0)
        } else {
            val newItem = GroceryItemEntity(
                id = "custom_${UUID.randomUUID()}",
                listId = listId,
                recipeId = "",
                name = trimmed,
                quantity = 1.0,
                unit = "pcs",
                category = chosenCategory,
                isChecked = false
            )
            groceryDao.insertItems(listOf(newItem))
        }
    }

    suspend fun exportRecipeToGrocery(
        recipe: Recipe,
        targetServings: Int
    ): ExportGroceryResult = withContext(ioDispatcher) {
        if (recipe.ingredients.isEmpty()) {
            return@withContext ExportGroceryResult(
                status = ExportGroceryStatus.FAILED,
                recipeTitle = recipe.title,
                message = "No ingredients found in \"${recipe.title}\" to export."
            )
        }

        try {
            val ownerEmail = preferencesManager.loggedInEmailFlow.first()
            val multiplier = targetServings.toDouble() / recipe.defaultServings.coerceAtLeast(1).toDouble()

            val existingLists = groceryDao.getAllListsOnce()
            val ownedLists = existingLists.filter { list ->
                list.ownerEmail.isBlank() || list.ownerEmail.equals(ownerEmail, ignoreCase = true)
            }

            var existingActiveList: GroceryListEntity? = null
            var existingActiveItems: List<GroceryItemEntity> = emptyList()
            for (list in ownedLists) {
                val items = groceryDao.getItemsForListOnce(list.id)
                val isListCompleted = list.isCompleted || (items.isNotEmpty() && items.all { it.isChecked })
                val matchesRecipe = list.title.equals(recipe.title, ignoreCase = true) ||
                    items.any { it.recipeId == recipe.id }
                if (!isListCompleted && matchesRecipe) {
                    existingActiveList = list
                    existingActiveItems = items
                    break
                }
            }

            if (existingActiveList != null) {
                val remainingCount = existingActiveItems.count { !it.isChecked }
                val activeDetail = if (existingActiveItems.isNotEmpty()) {
                    " ($remainingCount/${existingActiveItems.size} item(s) remaining)"
                } else {
                    ""
                }
                return@withContext ExportGroceryResult(
                    status = ExportGroceryStatus.ALREADY_ACTIVE,
                    listId = existingActiveList.id,
                    recipeTitle = recipe.title,
                    message = "\"${recipe.title}\" already has an active grocery list$activeDetail."
                )
            }

            val targetListId = "list_${UUID.randomUUID()}"
            val itemsToInsert = recipe.ingredients.map { ingredient ->
                GroceryItemEntity(
                    id = "item_${recipe.id}_${ingredient.id}_${UUID.randomUUID()}",
                    listId = targetListId,
                    recipeId = recipe.id,
                    name = ingredient.name,
                    quantity = ingredient.scaledAmount(multiplier),
                    unit = ingredient.unit,
                    category = GroceryCategories.refineCategory(ingredient.name, ingredient.category),
                    isChecked = false
                )
            }

            groceryDao.insertList(
                GroceryListEntity(
                    id = targetListId,
                    title = recipe.title,
                    updatedAt = "Just now",
                    isCompleted = false,
                    ownerEmail = ownerEmail
                )
            )
            groceryDao.insertItems(itemsToInsert)

            ExportGroceryResult(
                status = ExportGroceryStatus.SUCCESS,
                listId = targetListId,
                recipeTitle = recipe.title,
                addedCount = itemsToInsert.size,
                updatedCount = 0,
                message = "Created grocery list \"${recipe.title}\" with ${itemsToInsert.size} ingredient(s)."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ExportGroceryResult(
                status = ExportGroceryStatus.FAILED,
                recipeTitle = recipe.title,
                message = e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Could not export ingredients to your grocery list. Please try again."
            )
        }
    }

    suspend fun addReview(recipeId: String, review: ReviewItem) = withContext(ioDispatcher) {
        val entity = recipeDao.getRecipeById(recipeId) ?: return@withContext
        val currentReviews = if (entity.reviewsJson.isNotBlank()) {
            jsonToReviews(entity.reviewsJson)
        } else {
            inMemoryRecipes[recipeId]?.reviews ?: emptyList()
        }
        val updatedReviews = listOf(review) + currentReviews
        val newRating = ((updatedReviews.map { it.rating }.average()) * 10.0).roundToInt() / 10.0
        val newCount = entity.reviewCount + 1

        inMemoryRecipes[recipeId]?.let { cached ->
            inMemoryRecipes[recipeId] = cached.copy(
                reviews = updatedReviews,
                rating = newRating,
                reviewCount = newCount
            )
        }
        recipeDao.updateRecipeReviews(
            recipeId = recipeId,
            reviewsJson = reviewsToJson(updatedReviews),
            reviewCount = newCount,
            rating = newRating
        )
    }

    suspend fun publishUserRecipe(recipe: Recipe) = withContext(ioDispatcher) {
        val ownerEmail = preferencesManager.loggedInEmailFlow.first()
        val ownedRecipe = recipe.copy(ownerEmail = ownerEmail)
        inMemoryRecipes[ownedRecipe.id] = ownedRecipe
        recipeDao.insertRecipe(toRecipeEntity(ownedRecipe))
    }

    suspend fun deleteUserRecipe(recipeId: String) = withContext(ioDispatcher) {
        inMemoryRecipes.remove(recipeId)
        recipeDao.deleteRecipe(recipeId)
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        rememberMe: Boolean = false
    ): Result<Unit> = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = fullName.trim()
        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Full name is required."))
        }
        if (!emailRegex.matches(cleanEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(IllegalStateException("Email is already registered."))
        }
        val hashed = PasswordHasher.hashPassword(password)
        val user = UserEntity(
            email = cleanEmail,
            fullName = cleanName,
            password = hashed
        )
        userDao.insertUser(user)
        preferencesManager.setLoggedInUser(cleanEmail, cleanName, rememberMe)
        Result.success(Unit)
    }

    suspend fun loginUser(
        email: String,
        password: String,
        rememberMe: Boolean = false
    ): Result<Unit> = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        if (!emailRegex.matches(cleanEmail) || password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Enter a valid email and a password of at least 6 characters."))
        }
        val user = userDao.getUserByEmail(cleanEmail)
        if (user == null || !PasswordHasher.verifyPassword(password, user.password)) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect email or password."))
        }
        preferencesManager.setLoggedInUser(user.email, user.fullName, rememberMe)
        Result.success(Unit)
    }

    suspend fun checkEmailExists(email: String): Result<Unit> = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        if (!emailRegex.matches(cleanEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("No account found for this email."))
        Result.success(Unit)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> = withContext(ioDispatcher) {
        val cleanEmail = email.trim().lowercase()
        if (newPassword.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("Account not found."))
        val hashed = PasswordHasher.hashPassword(newPassword)
        userDao.updatePassword(user.email, hashed)
        Result.success(Unit)
    }

    suspend fun logoutUser() = withContext(ioDispatcher) {
        preferencesManager.clearLoggedInUser()
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(ioDispatcher) {
        val email = preferencesManager.loggedInEmailFlow.first()
        if (email.isNotBlank()) {
            userDao.deleteUser(email)
            recipeDao.deleteRecipesByOwner(email)
            groceryDao.deleteItemsForOwnerLists(email)
            groceryDao.deleteListsByOwner(email)
            inMemoryRecipes.entries.removeAll { it.value.ownerEmail.equals(email, ignoreCase = true) }
        }
        preferencesManager.clearLoggedInUser(clearRememberedEmail = true)
        Result.success(Unit)
    }

    private fun toRecipeEntity(r: Recipe): RecipeEntity {
        return RecipeEntity(
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
            defaultServings = r.defaultServings,
            ownerEmail = r.ownerEmail,
            isDraft = r.isDraft,
            ingredientsJson = ingredientsToJson(r.ingredients),
            stepsJson = stepsToJson(r.steps),
            reviewsJson = reviewsToJson(r.reviews),
            imageUri = r.imageUri
        )
    }

    private fun ingredientsToJson(list: List<Ingredient>): String {
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("subtitle", item.subtitle)
            obj.put("baseAmount", item.baseAmount)
            obj.put("unit", item.unit)
            obj.put("category", item.category)
            obj.put("scalingMode", item.scalingMode.id)
            obj.put("allergenTags", JSONArray(item.allergenTags))
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToIngredients(raw: String): List<Ingredient> {
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                val tagsArr = obj.optJSONArray("allergenTags")
                val tags = if (tagsArr != null) {
                    (0 until tagsArr.length()).map { tagsArr.getString(it) }
                } else emptyList()
                val itemName = obj.optString("name")
                val rawCategory = obj.optString("category", GroceryCategories.DEFAULT_SECTION)
                Ingredient(
                    id = obj.optString("id"),
                    name = itemName,
                    subtitle = obj.optString("subtitle"),
                    baseAmount = obj.optDouble("baseAmount", 1.0),
                    unit = normalizeUnitName(obj.optString("unit")),
                    category = GroceryCategories.refineCategory(itemName, rawCategory),
                    isChecked = false,
                    allergenTags = tags,
                    scalingMode = ScalingMode.fromId(obj.optString("scalingMode"))
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun stepsToJson(list: List<RecipeStep>): String {
        val arr = JSONArray()
        for (step in list) {
            val obj = JSONObject()
            obj.put("stepNumber", step.stepNumber)
            obj.put("title", step.title)
            obj.put("description", step.description)
            obj.put("durationMinutes", step.durationMinutes)
            obj.put("isFinalStep", step.isFinalStep)
            obj.put("tip", step.tip)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToSteps(raw: String): List<RecipeStep> {
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                RecipeStep(
                    stepNumber = obj.optInt("stepNumber", idx + 1),
                    title = obj.optString("title"),
                    description = obj.optString("description"),
                    durationMinutes = obj.optInt("durationMinutes", 5),
                    isFinalStep = obj.optBoolean("isFinalStep", false),
                    tip = obj.optString("tip")
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun reviewsToJson(list: List<ReviewItem>): String {
        val arr = JSONArray()
        for (rev in list) {
            val obj = JSONObject()
            obj.put("id", rev.id)
            obj.put("userName", rev.userName)
            obj.put("rating", rev.rating)
            obj.put("timeAgo", rev.timeAgo)
            obj.put("comment", rev.comment)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToReviews(raw: String): List<ReviewItem> {
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                ReviewItem(
                    id = obj.optString("id"),
                    userName = obj.optString("userName"),
                    rating = obj.optInt("rating", 5),
                    timeAgo = obj.optString("timeAgo"),
                    comment = obj.optString("comment")
                )
            }
        }.getOrDefault(emptyList())
    }
}
