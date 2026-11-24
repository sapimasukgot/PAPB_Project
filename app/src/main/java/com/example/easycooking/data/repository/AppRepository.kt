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
        val additional = createAdditionalSeeds()
        additional.forEach { inMemoryRecipes[it.id] = it }
        val allSeeds = seeds + additional
        _recipeReviewsFlow.value = allSeeds.associate { it.id to it.reviews }
    }

    private fun ing(
        id: String,
        name: String,
        subtitle: String,
        amount: Double,
        unit: String,
        category: String,
        allergenTags: List<String> = emptyList()
    ): Ingredient = Ingredient(
        id = id,
        name = name,
        subtitle = subtitle,
        baseAmount = amount,
        unit = unit,
        category = category,
        isChecked = false,
        allergenTags = allergenTags
    )

    private fun createAdditionalSeeds(): List<Recipe> {
        return listOf(
            Recipe(
                id = "7",
                title = "Mediterranean Quinoa Salad",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 15,
                calories = 280,
                rating = 4.8,
                reviewCount = 112,
                isBookmarked = false,
                category = "Lunch",
                dietaryBadges = listOf("Vegetarian", "Vegan", "Gluten-Free", "Nut-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("7_1", "Cooked quinoa", "fluffy tri-color quinoa", 200.0, "g", "Pantry Essentials", emptyList()),
                    ing("7_2", "English cucumber", "diced crisp cucumber", 1.0, "pcs", "Produce & Meat", emptyList()),
                    ing("7_3", "Cherry tomatoes", "halved sweet tomatoes", 120.0, "g", "Produce & Meat", emptyList()),
                    ing("7_4", "Kalamata olives", "pitted and sliced", 40.0, "g", "Specialty", emptyList()),
                    ing("7_5", "Extra virgin olive oil", "cold pressed", 2.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("7_6", "Fresh lemon juice", "freshly squeezed", 2.0, "tbsp", "Produce & Meat", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Cool Quinoa", "Spread warm cooked quinoa on a plate to cool to room temperature.", 3, false, "Cool quinoa prevents fresh diced vegetables from wilting."),
                    RecipeStep(2, "Chop Produce", "Dice English cucumber and halve fresh cherry tomatoes evenly.", 5, false, "Uniform sizes give each forkful a balanced crunch."),
                    RecipeStep(3, "Whisk & Toss", "Whisk olive oil, lemon juice, salt, and pepper in a bowl. Toss quinoa, vegetables, and olives together.", 2, true, "Serve chilled or at room temperature.")
                ),
                reviews = listOf(
                    ReviewItem("r7_1", "Liam N.", 5, "1d ago", "So fresh and vibrant! Perfect healthy lunch for work."),
                    ReviewItem("r7_2", "Sophia B.", 5, "3d ago", "The lemon dressing pulls all the Mediterranean flavors together nicely.")
                )
            ),
            Recipe(
                id = "8",
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
                ingredients = listOf(
                    ing("8_1", "Pizza dough", "fermented artisanal dough", 300.0, "g", "Pantry Essentials", listOf("Gluten")),
                    ing("8_2", "San Marzano sauce", "crushed plum tomatoes", 100.0, "ml", "Pantry Essentials", emptyList()),
                    ing("8_3", "Fresh mozzarella", "sliced buffalo mozzarella", 150.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("8_4", "Fresh basil leaves", "sweet Italian basil", 10.0, "lembar", "Specialty", emptyList()),
                    ing("8_5", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Stretch Dough", "Gently stretch pizza dough into a 12-inch round on parchment paper.", 5, false, "Do not use a rolling pin; preserve airy dough bubbles."),
                    RecipeStep(2, "Sauce & Top", "Spread crushed tomato sauce in concentric circles and scatter fresh mozzarella slices.", 3, false, "Avoid overloading with sauce to keep the crust crispy."),
                    RecipeStep(3, "Bake High Heat", "Bake at 250C on a preheated pizza stone for 10-12 minutes until crust is puffed and blistered. Top with fresh basil.", 12, true, "Add fresh basil immediately after baking so it releases fragrant oils.")
                ),
                reviews = listOf(
                    ReviewItem("r8_1", "Antonio G.", 5, "2d ago", "Authentic Neapolitan taste! The crust puffed up beautifully."),
                    ReviewItem("r8_2", "Emily V.", 5, "5d ago", "Simple ingredients done right. Best homemade pizza recipe.")
                )
            ),
            Recipe(
                id = "9",
                title = "Wild Mushroom Risotto",
                chefName = "Chef Elena Vance",
                difficulty = "Medium",
                durationMinutes = 35,
                calories = 390,
                rating = 4.7,
                reviewCount = 145,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Vegetarian", "Gluten-Free"),
                allergens = listOf("Dairy"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("9_1", "Arborio rice", "short-grain Italian rice", 180.0, "g", "Pantry Essentials", emptyList()),
                    ing("9_2", "Wild mushrooms", "cremini and shiitake sliced", 200.0, "g", "Produce & Meat", emptyList()),
                    ing("9_3", "Vegetable broth", "warm savory broth", 600.0, "ml", "Pantry Essentials", emptyList()),
                    ing("9_4", "Grated parmesan", "aged cheese", 40.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("9_5", "Butter", "cold unsalted cubes", 2.0, "tbsp", "Dairy & Eggs", listOf("Dairy")),
                    ing("9_6", "Garlic", "finely minced", 2.0, "cloves", "Produce & Meat", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Saute Mushrooms", "Cook sliced mushrooms in butter until deep golden, then set half aside for garnish.", 6, false, "High heat gives mushrooms maximum savoriness."),
                    RecipeStep(2, "Toast Rice", "Add arborio rice and minced garlic to pan, stirring 2 minutes until rice edges become translucent.", 3, false, "Toasting creates a protective shell so rice grains stay firm."),
                    RecipeStep(3, "Simmer with Broth", "Ladle hot vegetable broth one cup at a time, stirring continuously until creamy and tender. Finish with cold butter and parmesan.", 20, true, "Vigorous stirring releases starches for an ultra-creamy risotto.")
                ),
                reviews = listOf(
                    ReviewItem("r9_1", "Chloe M.", 5, "3d ago", "Creamy, decadent, and deeply savory! Took some arm workout to stir but totally worth it."),
                    ReviewItem("r9_2", "Julian P.", 4, "1w ago", "Great technique. The mushrooms add so much depth.")
                )
            ),
            Recipe(
                id = "10",
                title = "Thai Coconut Tofu Curry",
                chefName = "Chef Somchai",
                difficulty = "Easy",
                durationMinutes = 25,
                calories = 340,
                rating = 4.8,
                reviewCount = 175,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free"),
                allergens = listOf("Soy"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("10_1", "Firm tofu", "pressed and cubed", 300.0, "g", "Produce & Meat", listOf("Soy")),
                    ing("10_2", "Coconut milk", "full fat creamy milk", 250.0, "ml", "Pantry Essentials", emptyList()),
                    ing("10_3", "Red curry paste", "authentic Thai paste", 2.0, "tbsp", "Specialty", emptyList()),
                    ing("10_4", "Bell peppers", "sliced red and yellow", 100.0, "g", "Produce & Meat", emptyList()),
                    ing("10_5", "Bamboo shoots", "tender sliced shoots", 50.0, "g", "Specialty", emptyList()),
                    ing("10_6", "Coconut oil", "virgin oil", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Crisp Tofu", "Pan-fry tofu cubes in coconut oil until golden and crisp on all sides.", 6, false, "Pressing water out of tofu ensures a delightfully crispy crust."),
                    RecipeStep(2, "Simmer Curry Paste", "Fry red curry paste in pan until aromatic, then whisk in rich coconut milk.", 4, false, "Frying the paste releases its fragrant chili and herb oils."),
                    RecipeStep(3, "Combine Vegetables", "Add bell peppers and bamboo shoots, simmer gently for 5 minutes, and return crispy tofu.", 6, true, "Serve warm with fragrant jasmine rice.")
                ),
                reviews = listOf(
                    ReviewItem("r10_1", "Maya K.", 5, "2d ago", "The curry sauce is restaurant-worthy! Wonderfully balanced spicy and sweet."),
                    ReviewItem("r10_2", "Arthur L.", 5, "6d ago", "Crispy tofu soaked up all the red curry flavor beautifully.")
                )
            ),
            Recipe(
                id = "11",
                title = "Crispy Chickpea Buddha Bowl",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 20,
                calories = 360,
                rating = 4.9,
                reviewCount = 130,
                isBookmarked = false,
                category = "Lunch",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "High Protein", "Dairy-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("11_1", "Cooked chickpeas", "drained and dried", 250.0, "g", "Pantry Essentials", emptyList()),
                    ing("11_2", "Fresh kale leaves", "stemmed and chopped", 100.0, "g", "Produce & Meat", emptyList()),
                    ing("11_3", "Sweet potato", "cubed small", 150.0, "g", "Produce & Meat", emptyList()),
                    ing("11_4", "Tahini paste", "pure sesame paste", 2.0, "tbsp", "Specialty", emptyList()),
                    ing("11_5", "Lemon juice", "freshly squeezed", 1.0, "tbsp", "Produce & Meat", emptyList()),
                    ing("11_6", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Roast Chickpeas", "Toss chickpeas and sweet potato cubes with olive oil, paprika, and salt. Roast at 200C for 15 minutes.", 15, false, "Ensure chickpeas are completely dry so they crisp up."),
                    RecipeStep(2, "Massage Kale", "Massage kale leaves with a drop of olive oil and salt for 1 minute until tender and dark green.", 2, false, "Massaging breaks down tough fibers for easy digestion."),
                    RecipeStep(3, "Assemble Bowl", "Arrange roasted chickpeas, sweet potatoes, and kale in a bowl. Drizzle lemon tahini dressing generously.", 2, true, "High protein and nutrient packed clean fuel.")
                ),
                reviews = listOf(
                    ReviewItem("r11_1", "Olivia W.", 5, "1d ago", "My go-to meal prep lunch! Filling, colorful, and super healthy."),
                    ReviewItem("r11_2", "Hannah T.", 5, "4d ago", "The roasted chickpeas add such an addictive crunch.")
                )
            ),
            Recipe(
                id = "12",
                title = "Vanilla Chia Seed Pudding",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 10,
                calories = 210,
                rating = 4.7,
                reviewCount = 95,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("12_1", "Black chia seeds", "organic chia", 4.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("12_2", "Oat milk", "unsweetened oat milk", 250.0, "ml", "Dairy & Eggs", emptyList()),
                    ing("12_3", "Pure maple syrup", "grade A maple", 1.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("12_4", "Vanilla extract", "pure bourbon vanilla", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("12_5", "Fresh raspberries", "sweet ripe berries", 50.0, "g", "Produce & Meat", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Whisk Chia & Milk", "Whisk chia seeds, oat milk, maple syrup, and vanilla in a bowl until thoroughly combined.", 2, false, "Whisk vigorously to prevent chia seeds from clumping at the bottom."),
                    RecipeStep(2, "Rest & Re-stir", "Let sit for 5 minutes, then give it another gentle stir to distribute seeds evenly.", 5, false, "A second stir prevents seed settling as gelatinous coating forms."),
                    RecipeStep(3, "Chill & Top", "Cover and refrigerate until thickened. Top with vibrant raspberries before eating.", 3, true, "Rich in omega-3 and dietary fiber.")
                ),
                reviews = listOf(
                    ReviewItem("r12_1", "Grace F.", 5, "2d ago", "Smooth, creamy texture and so convenient to grab in the morning."),
                    ReviewItem("r12_2", "Lucas D.", 4, "5d ago", "Great light breakfast. Added a spoonful of almond butter on top.")
                )
            ),
            Recipe(
                id = "13",
                title = "Grilled Ribeye with Herb Butter",
                chefName = "Chef Gordon",
                difficulty = "Medium",
                durationMinutes = 20,
                calories = 580,
                rating = 5.0,
                reviewCount = 310,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Keto", "Low Carb", "Gluten-Free", "High Protein"),
                allergens = listOf("Dairy"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("13_1", "Ribeye steak", "prime marbling cut", 450.0, "g", "Produce & Meat", emptyList()),
                    ing("13_2", "Butter", "softened grass-fed butter", 3.0, "tbsp", "Dairy & Eggs", listOf("Dairy")),
                    ing("13_3", "Fresh rosemary & thyme", "finely minced", 10.0, "g", "Specialty", emptyList()),
                    ing("13_4", "Garlic", "crushed cloves", 3.0, "cloves", "Produce & Meat", emptyList()),
                    ing("13_5", "Olive oil", "high smoke point", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Temper & Season", "Let ribeye rest at room temp for 20 minutes, then season heavily with coarse sea salt and cracked pepper.", 5, false, "Cold meat straight from fridge lowers skillet temperature instantly."),
                    RecipeStep(2, "Cast Iron Sear", "Sear ribeye in smoking hot cast iron skillet for 3 minutes per side until deeply caramelized.", 6, false, "Do not touch meat during sear to form a crusty exterior."),
                    RecipeStep(3, "Butter Baste & Rest", "Add butter, garlic, and fresh herbs into pan. Tilt pan and spoon foaming butter over steak for 1 minute, then rest 5 minutes.", 5, true, "Resting allows muscle fibers to relax and retain rich juices.")
                ),
                reviews = listOf(
                    ReviewItem("r13_1", "Ethan H.", 5, "1d ago", "Perfection! The herb butter melts over the juicy steak creating an insane crust."),
                    ReviewItem("r13_2", "Victor S.", 5, "4d ago", "Ultimate keto comfort meal. Restaurant quality steak at home.")
                )
            ),
            Recipe(
                id = "14",
                title = "Bacon Wrapped Asparagus",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 15,
                calories = 220,
                rating = 4.8,
                reviewCount = 120,
                isBookmarked = false,
                category = "Snack",
                dietaryBadges = listOf("Keto", "Low Carb", "Gluten-Free", "Dairy-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("14_1", "Fresh asparagus", "woody ends trimmed", 200.0, "g", "Produce & Meat", emptyList()),
                    ing("14_2", "Smoked bacon", "thin cut bacon slices", 6.0, "pcs", "Produce & Meat", emptyList()),
                    ing("14_3", "Black pepper", "freshly cracked", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("14_4", "Olive oil spray", "extra virgin", 1.0, "tsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Trim Asparagus", "Snap off tough woody bottom ends of asparagus spears.", 3, false, "Asparagus naturally snaps right where tender stalk begins."),
                    RecipeStep(2, "Wrap Bacon", "Tightly wrap 3-4 asparagus spears together with a strip of bacon spiraling up the stalks.", 4, false, "Overlap bacon edges slightly so it stays wrapped during cooking."),
                    RecipeStep(3, "Roast or Air Fry", "Roast at 200C for 8-10 minutes until bacon is crispy and asparagus is tender-crisp.", 8, true, "Turn halfway through for 360-degree bacon crispiness.")
                ),
                reviews = listOf(
                    ReviewItem("r14_1", "Nathan G.", 5, "2d ago", "Fantastic savory appetizer! Bacon was super crispy and asparagus tender."),
                    ReviewItem("r14_2", "Amber Y.", 4, "5d ago", "So easy to make in the air fryer. Everyone devoured them in minutes.")
                )
            ),
            Recipe(
                id = "15",
                title = "Garlic Butter Shrimp Skillet",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 12,
                calories = 290,
                rating = 4.9,
                reviewCount = 240,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Keto", "Low Carb", "Pescatarian", "Gluten-Free", "High Protein"),
                allergens = listOf("Shellfish", "Dairy"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("15_1", "Jumbo shrimp", "peeled and deveined", 350.0, "g", "Produce & Meat", listOf("Shellfish")),
                    ing("15_2", "Butter", "unsalted butter", 3.0, "tbsp", "Dairy & Eggs", listOf("Dairy")),
                    ing("15_3", "Garlic", "finely minced", 4.0, "cloves", "Produce & Meat", emptyList()),
                    ing("15_4", "Fresh lemon juice", "freshly squeezed", 2.0, "tbsp", "Produce & Meat", emptyList()),
                    ing("15_5", "Parsley", "chopped fresh parsley", 10.0, "g", "Specialty", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Season Shrimp", "Pat peeled shrimp dry with paper towel and season with salt and pepper.", 2, false, "Dry shrimp sears quickly without turning rubbery."),
                    RecipeStep(2, "Quick Sear", "Melt butter in a wide skillet, add shrimp and cook for 2 minutes per side until pink and opaque.", 4, false, "Shrimp cooks extremely fast; pull them off before they curl into tight rings."),
                    RecipeStep(3, "Lemon Garlic Finish", "Stir minced garlic, lemon juice, and chopped parsley into the bubbling butter pan sauce for 1 minute.", 2, true, "Serve immediately spooning butter sauce over shrimp.")
                ),
                reviews = listOf(
                    ReviewItem("r15_1", "Dylan C.", 5, "1d ago", "Literally ready in 10 minutes and taste like heaven! Garlic lemon combo is killer."),
                    ReviewItem("r15_2", "Jessica M.", 5, "3d ago", "Juicy, succulent shrimp bathed in glorious garlic butter. Will make weekly.")
                )
            ),
            Recipe(
                id = "16",
                title = "Pan-Seared Chilean Sea Bass",
                chefName = "Chef Maria",
                difficulty = "Medium",
                durationMinutes = 20,
                calories = 320,
                rating = 4.9,
                reviewCount = 165,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Pescatarian", "Gluten-Free", "Dairy-Free", "Low Carb"),
                allergens = listOf("Fish"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("16_1", "Chilean sea bass", "thick center-cut fillets", 350.0, "g", "Produce & Meat", listOf("Fish")),
                    ing("16_2", "Olive oil", "extra virgin", 2.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("16_3", "Garlic", "smashed cloves", 2.0, "cloves", "Produce & Meat", emptyList()),
                    ing("16_4", "Fresh thyme", "fresh sprigs", 4.0, "pcs", "Specialty", emptyList()),
                    ing("16_5", "Lemon wedges", "fresh organic lemon", 1.0, "pcs", "Produce & Meat", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Season Fillets", "Pat sea bass completely dry, brush with olive oil, and season generously with sea salt.", 4, false, "Sea bass is a buttery fish; high heat locks in natural oils."),
                    RecipeStep(2, "Golden Pan Sear", "Sear fish in a hot stainless pan 4-5 minutes per side until a golden crust develops.", 8, false, "Wait until fish releases naturally before attempting to flip."),
                    RecipeStep(3, "Herb Infusion", "Add garlic and thyme sprigs to warm pan oil, spooning fragrant oil over fish before plating.", 3, true, "Silky, buttery fish that melts on your tongue.")
                ),
                reviews = listOf(
                    ReviewItem("r16_1", "Robert E.", 5, "2d ago", "Melt-in-your-mouth tenderness! Better than seafood restaurants."),
                    ReviewItem("r16_2", "Laura B.", 5, "6d ago", "Cooking sea bass can be intimidating but this guide made it flawless.")
                )
            ),
            Recipe(
                id = "17",
                title = "Baja Crispy Fish Tacos",
                chefName = "Chef Carlos",
                difficulty = "Easy",
                durationMinutes = 25,
                calories = 410,
                rating = 4.8,
                reviewCount = 220,
                isBookmarked = false,
                category = "Lunch",
                dietaryBadges = listOf("Pescatarian", "High Protein"),
                allergens = listOf("Fish", "Gluten"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("17_1", "Cod fish fillets", "fresh wild cod cut into strips", 300.0, "g", "Produce & Meat", listOf("Fish")),
                    ing("17_2", "Tortillas", "warm soft corn tortillas", 4.0, "pcs", "Pantry Essentials", listOf("Gluten")),
                    ing("17_3", "Cabbage slaw", "shredded red & white cabbage", 100.0, "g", "Produce & Meat", emptyList()),
                    ing("17_4", "Chipotle crema", "spicy smoky cream", 3.0, "tbsp", "Specialty", emptyList()),
                    ing("17_5", "Fresh lime", "juicy wedges", 1.0, "pcs", "Produce & Meat", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Cook Fish Strips", "Dust cod strips with taco seasoning and pan-sear in hot oil for 3 minutes per side until crispy.", 6, false, "Cod stays flaky and juicy when cooked quickly over high heat."),
                    RecipeStep(2, "Warm Tortillas", "Warm soft tortillas on a dry skillet for 30 seconds per side until pliable.", 2, false, "Warming tortillas prevents them from tearing under taco fillings."),
                    RecipeStep(3, "Assemble Tacos", "Fill tortillas with crispy cod, crunchy cabbage slaw, chipotle crema, and fresh squeeze of lime.", 3, true, "Zesty, crunchy, and packed with fresh Mexican coastal flavor.")
                ),
                reviews = listOf(
                    ReviewItem("r17_1", "Carlos M.", 5, "1d ago", "Tastes just like the authentic street tacos in Ensenada! Amazing."),
                    ReviewItem("r17_2", "Serena K.", 5, "4d ago", "The chipotle crema pairs insanely well with the crunchy cabbage and tender fish.")
                )
            ),
            Recipe(
                id = "18",
                title = "Seared Scallops with Lemon Herb",
                chefName = "Chef Elena Vance",
                difficulty = "Medium",
                durationMinutes = 15,
                calories = 260,
                rating = 4.9,
                reviewCount = 190,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Pescatarian", "Gluten-Free", "Low Carb", "Dairy-Free"),
                allergens = listOf("Shellfish"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("18_1", "Sea scallops", "dry sea scallops", 300.0, "g", "Produce & Meat", listOf("Shellfish")),
                    ing("18_2", "Extra virgin olive oil", "cold pressed", 2.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("18_3", "Garlic", "finely minced", 2.0, "cloves", "Produce & Meat", emptyList()),
                    ing("18_4", "Lemon juice", "freshly squeezed", 1.0, "tbsp", "Produce & Meat", emptyList()),
                    ing("18_5", "Fresh parsley", "finely chopped", 10.0, "g", "Specialty", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Dry Scallops", "Thoroughly pat scallops dry with multiple paper towels and season with sea salt.", 3, false, "Any surface moisture creates steam and prevents that deep golden sear."),
                    RecipeStep(2, "Hot Pan Sear", "Heat oil until shimmering. Place scallops without touching and sear undisturbed for 2 minutes.", 4, false, "Do not move scallops until a golden crust is formed on bottom."),
                    RecipeStep(3, "Glaze with Pan Juices", "Flip for 1 minute, swirl in minced garlic, lemon juice, and parsley, then transfer to warm plate.", 2, true, "Scallops should remain translucent and buttery in the middle.")
                ),
                reviews = listOf(
                    ReviewItem("r18_1", "Patrick W.", 5, "3d ago", "Followed the drying tip and got the most gorgeous golden crust ever!"),
                    ReviewItem("r18_2", "Monica D.", 5, "1w ago", "Light, elegant, and ready in 15 minutes flat.")
                )
            ),
            Recipe(
                id = "19",
                title = "Teriyaki Beef Bowl",
                chefName = "Chef Kenji",
                difficulty = "Easy",
                durationMinutes = 20,
                calories = 460,
                rating = 4.8,
                reviewCount = 280,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("High Protein", "Dairy-Free"),
                allergens = listOf("Soy"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("19_1", "Beef sirloin", "thinly sliced against grain", 350.0, "g", "Produce & Meat", emptyList()),
                    ing("19_2", "Teriyaki sauce", "authentic soy & mirin glaze", 60.0, "ml", "Pantry Essentials", listOf("Soy")),
                    ing("19_3", "Steamed rice", "warm jasmine rice", 250.0, "g", "Pantry Essentials", emptyList()),
                    ing("19_4", "Broccoli florets", "steamed tender-crisp", 150.0, "g", "Produce & Meat", emptyList()),
                    ing("19_5", "Sesame seeds", "toasted white seeds", 1.0, "tsp", "Specialty", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "High-Heat Beef Sear", "Stir-fry beef slices in a smoking hot wok for 3 minutes until browned on all edges.", 4, false, "Cooking in small batches prevents beef from steaming in juices."),
                    RecipeStep(2, "Glaze with Teriyaki", "Pour teriyaki sauce over hot beef, tossing rapidly until sauce reduces into a glossy lacquer.", 3, false, "Simmer until sauce bubbles thick and clings to beef slices."),
                    RecipeStep(3, "Assemble Bowl", "Spoon warm jasmine rice into bowls, top with glossy teriyaki beef, steamed broccoli, and sesame seeds.", 3, true, "Savory, sweet, and satisfying high-protein bowl.")
                ),
                reviews = listOf(
                    ReviewItem("r19_1", "Jason B.", 5, "2d ago", "The homemade teriyaki glaze is ten times better than takeout!"),
                    ReviewItem("r19_2", "Kelly N.", 5, "5d ago", "Super fast weeknight meal. Meat stayed tender and sauce is phenomenal.")
                )
            ),
            Recipe(
                id = "20",
                title = "Mango Dragonfruit Smoothie Bowl",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 8,
                calories = 190,
                rating = 4.9,
                reviewCount = 105,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("20_1", "Frozen mango", "sweet ripe mango chunks", 150.0, "g", "Produce & Meat", emptyList()),
                    ing("20_2", "Red dragonfruit", "frozen puree packet", 100.0, "g", "Specialty", emptyList()),
                    ing("20_3", "Coconut water", "pure chilled coconut water", 100.0, "ml", "Dairy & Eggs", emptyList()),
                    ing("20_4", "Kiwi fruit", "peeled and sliced", 1.0, "pcs", "Produce & Meat", emptyList()),
                    ing("20_5", "Coconut flakes", "toasted unsweetened", 15.0, "g", "Specialty", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Blend Fruit", "Blend frozen mango, dragonfruit puree, and cold coconut water on high speed until ultra-thick.", 3, false, "Minimal liquid keeps smoothie bowl thick and spoonable."),
                    RecipeStep(2, "Fill Bowl", "Pour vibrant magenta smoothie into cold bowls, smoothing surface.", 1, false, "Use frozen fruits only for maximum thickness without melting."),
                    RecipeStep(3, "Garnish Toppings", "Decorate top with sliced kiwi wheels and crunchy toasted coconut flakes.", 2, true, "Electrolyte-rich tropical antioxidant breakfast.")
                ),
                reviews = listOf(
                    ReviewItem("r20_1", "Zoe R.", 5, "1d ago", "The color is so vibrant and stunning! Tastes like pure tropical sunshine."),
                    ReviewItem("r20_2", "Benjamin K.", 5, "4d ago", "So refreshing after morning runs. The coconut flakes give great texture.")
                )
            ),
            Recipe(
                id = "21",
                title = "Slow-Cooked Beef Stew",
                chefName = "Chef Gordon",
                difficulty = "Hard",
                durationMinutes = 90,
                calories = 510,
                rating = 4.9,
                reviewCount = 340,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free", "Dairy-Free", "High Protein"),
                allergens = emptyList(),
                defaultServings = 4,
                ingredients = listOf(
                    ing("21_1", "Beef chuck", "trimmed and cut in large cubes", 600.0, "g", "Produce & Meat", emptyList()),
                    ing("21_2", "Carrots", "chunky cut carrots", 200.0, "g", "Produce & Meat", emptyList()),
                    ing("21_3", "Baby potatoes", "halved golden potatoes", 250.0, "g", "Produce & Meat", emptyList()),
                    ing("21_4", "Beef bone broth", "rich homemade bone broth", 500.0, "ml", "Pantry Essentials", emptyList()),
                    ing("21_5", "Tomato paste", "concentrated paste", 2.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("21_6", "Rosemary & thyme", "fresh whole sprigs", 15.0, "g", "Specialty", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Brown Beef", "Sear seasoned beef cubes in a heavy Dutch oven until deeply caramelized on all sides.", 8, false, "Browning creates fond at bottom of pot that flavors entire stew."),
                    RecipeStep(2, "Deglaze & Simmer", "Stir tomato paste into pot, deglaze bottom with beef broth, and bring to gentle simmer.", 5, false, "Scrape every bit of brown crust off bottom of pot with wooden spoon."),
                    RecipeStep(3, "Slow Braise", "Add carrots, potatoes, and herb bundle. Cover pot and simmer low heat for 75 minutes until fork tender.", 75, true, "Low and slow breaks down connective tissues making beef melt like butter.")
                ),
                reviews = listOf(
                    ReviewItem("r21_1", "Thomas C.", 5, "3d ago", "Incredible depth of flavor! The beef literally fell apart with a fork."),
                    ReviewItem("r21_2", "Angela L.", 5, "1w ago", "Ultimate rainy day comfort meal. Broth was rich, thick, and hearty.")
                )
            ),
            Recipe(
                id = "22",
                title = "Turkey & Sweet Potato Skillet",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 25,
                calories = 440,
                rating = 4.8,
                reviewCount = 155,
                isBookmarked = false,
                category = "Lunch",
                dietaryBadges = listOf("High Protein", "Gluten-Free", "Dairy-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("22_1", "Ground turkey", "93% lean ground turkey", 400.0, "g", "Produce & Meat", emptyList()),
                    ing("22_2", "Sweet potato", "diced 1/2 inch cubes", 250.0, "g", "Produce & Meat", emptyList()),
                    ing("22_3", "Bell peppers", "diced green and red", 100.0, "g", "Produce & Meat", emptyList()),
                    ing("22_4", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("22_5", "Smoked paprika", "Spanish smoked paprika", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Steam-Saute Potatoes", "Cook sweet potato cubes in covered skillet with olive oil and splash of water for 8 minutes.", 8, false, "Covering pan traps steam to cook potatoes tender without burning."),
                    RecipeStep(2, "Brown Turkey", "Push potatoes aside, add ground turkey and smoked paprika, breaking up meat until browned.", 6, false, "Smoked paprika adds a savory, bacon-like aroma to lean turkey."),
                    RecipeStep(3, "Toss Together", "Add diced bell peppers and toss everything in pan for 3 minutes until peppers are tender.", 3, true, "Ideal balanced lunch with complex carbs and clean protein.")
                ),
                reviews = listOf(
                    ReviewItem("r22_1", "Aaron S.", 5, "2d ago", "Healthy meal prep dream! Balanced, flavorful, and keeps great in fridge."),
                    ReviewItem("r22_2", "Danielle W.", 4, "4d ago", "The sweet potatoes paired with smoked paprika are phenomenal.")
                )
            ),
            Recipe(
                id = "23",
                title = "Protein Greek Yogurt Parfait",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 5,
                calories = 280,
                rating = 4.9,
                reviewCount = 180,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegetarian", "High Protein", "Gluten-Free"),
                allergens = listOf("Dairy"),
                defaultServings = 1,
                ingredients = listOf(
                    ing("23_1", "Greek yogurt", "plain whole milk Greek yogurt", 200.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("23_2", "Fresh berries", "strawberries & blueberries", 80.0, "g", "Produce & Meat", emptyList()),
                    ing("23_3", "Raw honey", "pure wildflower honey", 1.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("23_4", "Gluten-free granola", "oats and seeds", 30.0, "g", "Pantry Essentials", emptyList()),
                    ing("23_5", "Chia seeds", "black chia", 1.0, "tsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Layer Yogurt", "Spoon half the chilled Greek yogurt into a glass jar or bowl.", 1, false, "Greek yogurt packs 20g of natural protein to keep you satiated."),
                    RecipeStep(2, "Add Fruit & Granola", "Layer fresh berries, crunchy granola, and sprinkle chia seeds in center.", 2, false, "Layering keeps granola crisp until the very last bite."),
                    RecipeStep(3, "Honey Drizzle", "Top with remaining yogurt, berries, and finish with a golden drizzle of raw honey.", 1, true, "5-minute breakfast power bowl.")
                ),
                reviews = listOf(
                    ReviewItem("r23_1", "Tiffany L.", 5, "1d ago", "Quick, filling, and packed with protein. Keeps me full until lunch!"),
                    ReviewItem("r23_2", "Mark O.", 5, "3d ago", "Creamy yogurt with crunchy granola and sweet berries is the best combo.")
                )
            ),
            Recipe(
                id = "24",
                title = "Crispy Roasted Chickpeas",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 30,
                calories = 180,
                rating = 4.7,
                reviewCount = 88,
                isBookmarked = false,
                category = "Snack",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free", "High Protein"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("24_1", "Cooked chickpeas", "rinsed and dried", 300.0, "g", "Pantry Essentials", emptyList()),
                    ing("24_2", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("24_3", "Smoked paprika", "Spanish paprika", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("24_4", "Ground cumin", "warm fragrant cumin", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("24_5", "Sea salt", "fine sea salt", 1.0, "tsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Dry Thoroughly", "Roll chickpeas gently between paper towels to remove skins and every drop of moisture.", 5, false, "Removing loose skins helps them become extraordinarily crunchy."),
                    RecipeStep(2, "Season & Coat", "Toss chickpeas with olive oil, paprika, cumin, and sea salt until evenly coated.", 2, false, "Do not oversaturate with oil or they will soften rather than crisp."),
                    RecipeStep(3, "Bake Until Crunchy", "Roast at 200C on a large sheet pan for 22 minutes shaking pan halfway.", 22, true, "Leave in cooling oven for 10 minutes to finish drying out.")
                ),
                reviews = listOf(
                    ReviewItem("r24_1", "Eric M.", 5, "2d ago", "Insanely crunchy! Better and healthier alternative to potato chips."),
                    ReviewItem("r24_2", "Natalie H.", 4, "5d ago", "Spiced just right. Great high-fiber, high-protein midday snack.")
                )
            ),
            Recipe(
                id = "25",
                title = "Baked Zucchini Parmesan Crisps",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 20,
                calories = 150,
                rating = 4.8,
                reviewCount = 110,
                isBookmarked = false,
                category = "Snack",
                dietaryBadges = listOf("Vegetarian", "Keto", "Low Carb", "Gluten-Free"),
                allergens = listOf("Dairy"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("25_1", "Zucchini", "sliced into 1/4 inch rounds", 250.0, "g", "Produce & Meat", emptyList()),
                    ing("25_2", "Parmesan cheese", "freshly grated", 50.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("25_3", "Italian herbs", "dried oregano and basil", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("25_4", "Garlic powder", "pure garlic powder", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("25_5", "Olive oil spray", "fine mist", 1.0, "tsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Slice Zucchini", "Slice fresh zucchini into uniform 1/4 inch rounds and pat surface dry.", 4, false, "Uniform slices bake at identical rates for uniform crunch."),
                    RecipeStep(2, "Coat with Parmesan", "Dip zucchini rounds in mist of oil and press firmly into grated parmesan seasoned with herbs.", 4, false, "Press parmesan firmly so it forms a melted golden crust on each round."),
                    RecipeStep(3, "Bake Golden", "Bake at 220C on parchment for 12 minutes until cheese is golden brown and bubbly.", 12, true, "Let cool 3 minutes on sheet to allow cheese crust to crisp up.")
                ),
                reviews = listOf(
                    ReviewItem("r25_1", "Sandra K.", 5, "1d ago", "Guilt-free keto chips! The parmesan crust gets super crispy."),
                    ReviewItem("r25_2", "Justin T.", 5, "3d ago", "Simple, low carb, and delicious. My kids ate the whole tray.")
                )
            ),
            Recipe(
                id = "26",
                title = "Spinach & Feta Omelette",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 12,
                calories = 310,
                rating = 4.8,
                reviewCount = 195,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegetarian", "Keto", "Low Carb", "Gluten-Free", "High Protein"),
                allergens = listOf("Eggs", "Dairy"),
                defaultServings = 1,
                ingredients = listOf(
                    ing("26_1", "Fresh eggs", "large free-range eggs", 3.0, "pcs", "Dairy & Eggs", listOf("Eggs")),
                    ing("26_2", "Feta cheese", "crumbled Greek feta", 40.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("26_3", "Baby spinach", "washed fresh spinach", 60.0, "g", "Produce & Meat", emptyList()),
                    ing("26_4", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("26_5", "Black pepper", "freshly ground", 1.0, "tsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Wilt Spinach", "Heat olive oil in non-stick pan, add baby spinach and cook for 1 minute until wilted.", 2, false, "Wilting spinach beforehand releases liquid so omelette stays fluffy."),
                    RecipeStep(2, "Cook Eggs", "Whisk eggs with pepper, pour over spinach in pan, and swirl on medium-low heat until edges set.", 4, false, "Low and slow heat yields creamy velvety curds without browning."),
                    RecipeStep(3, "Fold with Feta", "Scatter crumbled feta cheese over half of omelette, fold over gently, and slide onto plate.", 2, true, "Serve warm with rich melted feta inside.")
                ),
                reviews = listOf(
                    ReviewItem("r26_1", "Vanessa R.", 5, "2d ago", "The salty feta with wilted spinach is a classic breakfast match! Fluffy and rich."),
                    ReviewItem("r26_2", "Gregory P.", 5, "4d ago", "Takes 10 minutes and keeps me fueled throughout entire morning.")
                )
            ),
            Recipe(
                id = "27",
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
                ingredients = listOf(
                    ing("27_1", "All-purpose flour", "unbleached flour", 200.0, "g", "Pantry Essentials", listOf("Gluten")),
                    ing("27_2", "Fresh buttermilk", "cultured buttermilk", 250.0, "ml", "Dairy & Eggs", listOf("Dairy")),
                    ing("27_3", "Large egg", "fresh free-range egg", 1.0, "pcs", "Dairy & Eggs", listOf("Eggs")),
                    ing("27_4", "Melted butter", "unsalted butter", 2.0, "tbsp", "Dairy & Eggs", listOf("Dairy")),
                    ing("27_5", "Pure maple syrup", "grade A dark maple", 2.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Whisk Batter", "Whisk dry ingredients, then fold in buttermilk, egg, and melted butter gently.", 4, false, "Do not overmix; small lumps make the pancakes extra fluffy."),
                    RecipeStep(2, "Griddle Cooking", "Ladle 1/4 cup batter onto a buttered non-stick skillet over medium heat.", 4, false, "Flip when small bubbles pop on surface and edges look set."),
                    RecipeStep(3, "Stack & Drizzle", "Cook 2 minutes on second side until golden brown, stack high and top with maple syrup.", 2, true, "Serve steaming hot with extra whipped butter.")
                ),
                reviews = listOf(
                    ReviewItem("r27_1", "Lucas M.", 5, "1d ago", "The fluffiest pancakes I've ever made at home! Kids loved them."),
                    ReviewItem("r27_2", "Chloe R.", 5, "3d ago", "Pure weekend brunch perfection.")
                )
            ),
            Recipe(
                id = "28",
                title = "Classic Shakshuka with Feta",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 20,
                calories = 290,
                rating = 4.8,
                reviewCount = 165,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegetarian", "Gluten-Free", "Low Carb"),
                allergens = listOf("Eggs", "Dairy"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("28_1", "Large eggs", "fresh eggs", 3.0, "pcs", "Dairy & Eggs", listOf("Eggs")),
                    ing("28_2", "Crushed tomatoes", "Italian canned tomatoes", 300.0, "g", "Pantry Essentials", emptyList()),
                    ing("28_3", "Bell pepper", "diced red bell pepper", 1.0, "pcs", "Produce & Meat", emptyList()),
                    ing("28_4", "Crumbled feta", "Greek feta cheese", 40.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("28_5", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("28_6", "Ground cumin & paprika", "warm spice mix", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Simmer Tomato Sauce", "Saute diced peppers in olive oil with cumin and paprika, then simmer crushed tomatoes for 8 minutes.", 8, false, "Simmer until sauce thickens so eggs sit nicely on top."),
                    RecipeStep(2, "Poach Eggs", "Make 3 wells in sauce, crack whole eggs inside, cover with lid on low heat for 6 minutes.", 6, false, "Keep whites firm while yolks stay deliciously runny."),
                    RecipeStep(3, "Garnish & Serve", "Sprinkle crumbled feta and freshly chopped cilantro over skillet.", 2, true, "Serve directly from skillet with warm bread.")
                ),
                reviews = listOf(
                    ReviewItem("r28_1", "Daniel S.", 5, "2d ago", "Bursting with smoky spiced tomato flavor! Perfect savory breakfast."),
                    ReviewItem("r28_2", "Nadia B.", 5, "5d ago", "The runny yolks mixed into the feta tomato sauce is absolute heaven.")
                )
            ),
            Recipe(
                id = "29",
                title = "Avocado Toast with Poached Egg",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 10,
                calories = 270,
                rating = 4.9,
                reviewCount = 230,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegetarian", "Dairy-Free", "Nut-Free"),
                allergens = listOf("Gluten", "Eggs"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("29_1", "Sourdough bread", "artisan sourdough slices", 2.0, "pcs", "Pantry Essentials", listOf("Gluten")),
                    ing("29_2", "Ripe avocado", "creamy ripe Haas avocado", 1.0, "pcs", "Produce & Meat", emptyList()),
                    ing("29_3", "Fresh eggs", "large fresh eggs", 2.0, "pcs", "Dairy & Eggs", listOf("Eggs")),
                    ing("29_4", "Red pepper flakes", "crushed chili flakes", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("29_5", "Lemon juice", "freshly squeezed", 1.0, "tsp", "Produce & Meat", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Toast Sourdough", "Toast artisan sourdough slices until deeply golden and crunchy on both sides.", 3, false, "Thick crunchy toast supports the creamy avocado without sogginess."),
                    RecipeStep(2, "Mash Avocado", "Mash ripe avocado with fresh lemon juice, sea salt, and black pepper in a small bowl.", 2, false, "Leave small chunks for delightful textural contrast."),
                    RecipeStep(3, "Poach Egg & Assemble", "Gently poach eggs in simmering vinegar water for 3 minutes, slide on top of avocado toast, and sprinkle chili flakes.", 4, true, "A dash of vinegar in water keeps egg whites tidy while poaching.")
                ),
                reviews = listOf(
                    ReviewItem("r29_1", "Sarah P.", 5, "1d ago", "Cafe-quality avocado toast at home in under 10 minutes!"),
                    ReviewItem("r29_2", "Ryan K.", 5, "4d ago", "The golden runny yolk over rich avocado is unbeatable.")
                )
            ),
            Recipe(
                id = "30",
                title = "Apple Cinnamon Overnight Oats",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 5,
                calories = 240,
                rating = 4.8,
                reviewCount = 140,
                isBookmarked = false,
                category = "Breakfast",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("30_1", "Rolled oats", "gluten-free rolled oats", 60.0, "g", "Pantry Essentials", emptyList()),
                    ing("30_2", "Almond milk", "unsweetened almond milk", 150.0, "ml", "Specialty", emptyList()),
                    ing("30_3", "Honeycrisp apple", "finely diced sweet apple", 1.0, "pcs", "Produce & Meat", emptyList()),
                    ing("30_4", "Ground cinnamon", "fragrant Ceylon cinnamon", 1.0, "tsp", "Pantry Essentials", emptyList()),
                    ing("30_5", "Pure maple syrup", "dark maple syrup", 1.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("30_6", "Chia seeds", "black chia seeds", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Combine Oats & Milk", "In a mason jar, combine rolled oats, chia seeds, cinnamon, and chilled almond milk.", 2, false, "Chia seeds absorb liquid giving the oats an ultra-creamy consistency."),
                    RecipeStep(2, "Add Apples & Maple", "Stir in freshly diced sweet apples and pure maple syrup until well distributed.", 2, false, "Diced apples retain their crisp bite overnight."),
                    RecipeStep(3, "Chill Overnight", "Seal jar and refrigerate overnight. Enjoy chilled straight from jar or gently warmed.", 1, true, "Effortless grab-and-go morning fuel packed with whole grains.")
                ),
                reviews = listOf(
                    ReviewItem("r30_1", "Emma W.", 5, "2d ago", "Tastes like healthy apple pie in a jar! Saves me so much time before work."),
                    ReviewItem("r30_2", "Brian L.", 5, "6d ago", "Creamy, naturally sweet, and very satisfying.")
                )
            ),
            Recipe(
                id = "31",
                title = "Molten Chocolate Lava Cake",
                chefName = "Chef Elena Vance",
                difficulty = "Medium",
                durationMinutes = 20,
                calories = 380,
                rating = 5.0,
                reviewCount = 310,
                isBookmarked = false,
                category = "Dessert",
                dietaryBadges = listOf("Vegetarian", "Nut-Free"),
                allergens = listOf("Gluten", "Dairy", "Eggs"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("31_1", "Dark chocolate", "70% bittersweet chocolate", 120.0, "g", "Specialty", listOf("Dairy")),
                    ing("31_2", "Unsalted butter", "pure creamy butter", 60.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("31_3", "Large eggs", "whole eggs", 2.0, "pcs", "Dairy & Eggs", listOf("Eggs")),
                    ing("31_4", "Powdered sugar", "confectioners sugar", 40.0, "g", "Pantry Essentials", emptyList()),
                    ing("31_5", "All-purpose flour", "fine pastry flour", 30.0, "g", "Pantry Essentials", listOf("Gluten"))
                ),
                steps = listOf(
                    RecipeStep(1, "Melt Chocolate & Butter", "Gently melt dark chocolate and butter cubes in a heatproof bowl over simmering water until glossy smooth.", 4, false, "Do not let water touch bottom of bowl to prevent chocolate from seizing."),
                    RecipeStep(2, "Whisk Batter", "Whisk eggs and sugar until pale and fluffy, fold in melted chocolate, and sift flour gently.", 4, false, "Gentle folding keeps the cake light with a molten center."),
                    RecipeStep(3, "Bake Lava Center", "Bake in buttered ramekins at 220C for exactly 11 minutes until edges are set but center jiggles gently.", 11, true, "Invert immediately onto plate and dust with powdered sugar.")
                ),
                reviews = listOf(
                    ReviewItem("r31_1", "Isabella M.", 5, "1d ago", "Rich, gooey chocolate river poured out of the center! Absolute showstopper dessert."),
                    ReviewItem("r31_2", "David T.", 5, "3d ago", "Followed the 11-minute timer and the lava center was flawless.")
                )
            ),
            Recipe(
                id = "32",
                title = "Classic Italian Tiramisu",
                chefName = "Chef Marco",
                difficulty = "Medium",
                durationMinutes = 15,
                calories = 320,
                rating = 4.9,
                reviewCount = 275,
                isBookmarked = false,
                category = "Dessert",
                dietaryBadges = listOf("Vegetarian", "Nut-Free"),
                allergens = listOf("Gluten", "Dairy", "Eggs"),
                defaultServings = 2,
                ingredients = listOf(
                    ing("32_1", "Ladyfinger biscuits", "savoiardi biscuits", 150.0, "g", "Pantry Essentials", listOf("Gluten", "Eggs")),
                    ing("32_2", "Mascarpone cheese", "rich Italian mascarpone", 250.0, "g", "Dairy & Eggs", listOf("Dairy")),
                    ing("32_3", "Espresso coffee", "chilled fresh espresso", 150.0, "ml", "Specialty", emptyList()),
                    ing("32_4", "Heavy whipping cream", "cold cream", 120.0, "ml", "Dairy & Eggs", listOf("Dairy")),
                    ing("32_5", "Cocoa powder", "dutch processed dark cocoa", 2.0, "tbsp", "Specialty", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Whip Mascarpone Cream", "Whip cold mascarpone with heavy cream and sugar until stiff velvety peaks form.", 5, false, "Cold ingredients ensure the cream whips firmly without separating."),
                    RecipeStep(2, "Dip Ladyfingers", "Quickly dip ladyfingers into cooled espresso for 1 second per side and arrange in dish.", 4, false, "A quick dip prevents ladyfingers from turning soggy."),
                    RecipeStep(3, "Layer & Dust Cocoa", "Spread half the mascarpone cream, repeat with second layer of biscuits, and dust generously with dark cocoa powder.", 4, true, "Chill 2 hours before slicing for clean, elegant layers.")
                ),
                reviews = listOf(
                    ReviewItem("r32_1", "Marco B.", 5, "2d ago", "Tastes like authentic Roman trattoria tiramisu! Heavenly texture."),
                    ReviewItem("r32_2", "Clara K.", 5, "5d ago", "So easy to assemble and everyone asked for seconds.")
                )
            ),
            Recipe(
                id = "33",
                title = "Warm Berry Crisp",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 30,
                calories = 260,
                rating = 4.8,
                reviewCount = 160,
                isBookmarked = false,
                category = "Dessert",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("33_1", "Mixed berries", "fresh or frozen berries", 300.0, "g", "Produce & Meat", emptyList()),
                    ing("33_2", "Rolled oats", "gluten-free rolled oats", 80.0, "g", "Pantry Essentials", emptyList()),
                    ing("33_3", "Coconut oil", "melted virgin coconut oil", 2.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("33_4", "Pure maple syrup", "amber maple syrup", 3.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("33_5", "Tapioca starch", "pure tapioca starch", 1.0, "tbsp", "Pantry Essentials", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Toss Berry Base", "Toss mixed berries with 1 tbsp maple syrup and tapioca starch in baking dish.", 3, false, "Tapioca starch thickens juicy berry juices into a rich compote."),
                    RecipeStep(2, "Mix Oat Crisp", "Mix rolled oats, cinnamon, melted coconut oil, and remaining maple syrup until crumbly.", 3, false, "Rub mixture with fingers to create golden crispy oat clusters."),
                    RecipeStep(3, "Bake Golden Brown", "Scatter oat topping over berries and bake at 180C for 22 minutes until bubbling and crisp.", 22, true, "Delicious served warm with dairy-free vanilla bean ice cream.")
                ),
                reviews = listOf(
                    ReviewItem("r33_1", "Hannah G.", 5, "1d ago", "Warm, jammy berries with crunchy sweet oat topping. Super comforting!"),
                    ReviewItem("r33_2", "Samuel V.", 5, "4d ago", "Love that it is naturally sweetened and gluten-free. Will make often.")
                )
            ),
            Recipe(
                id = "34",
                title = "Mango Chia Coconut Parfait",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 10,
                calories = 190,
                rating = 4.9,
                reviewCount = 115,
                isBookmarked = false,
                category = "Dessert",
                dietaryBadges = listOf("Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free", "Nut-Free"),
                allergens = emptyList(),
                defaultServings = 2,
                ingredients = listOf(
                    ing("34_1", "Fresh mango puree", "ripe sweet mango puree", 150.0, "g", "Produce & Meat", emptyList()),
                    ing("34_2", "Coconut milk", "full fat creamy coconut milk", 150.0, "ml", "Specialty", emptyList()),
                    ing("34_3", "Black chia seeds", "raw chia seeds", 3.0, "tbsp", "Pantry Essentials", emptyList()),
                    ing("34_4", "Toasted coconut shreds", "golden toasted coconut", 20.0, "g", "Specialty", emptyList()),
                    ing("34_5", "Fresh mint", "fresh fragrant sprigs", 4.0, "pcs", "Produce & Meat", emptyList())
                ),
                steps = listOf(
                    RecipeStep(1, "Prepare Chia Cream", "Whisk chia seeds and coconut milk with a hint of vanilla until thick and creamy.", 3, false, "Coconut milk creates a rich custard-like dessert pudding."),
                    RecipeStep(2, "Puree Ripe Mango", "Blend sweet ripe mango flesh until silky smooth and brightly golden.", 3, false, "Use naturally sweet mangoes so no added sugar is needed."),
                    RecipeStep(3, "Layer & Garnish", "Alternate layers of coconut chia pudding and vibrant mango puree in glasses, topping with toasted coconut shreds and mint.", 3, true, "Light, guilt-free tropical dessert.")
                ),
                reviews = listOf(
                    ReviewItem("r34_1", "Lily A.", 5, "3d ago", "So light, refreshing, and beautifully layered! Perfect summer dessert."),
                    ReviewItem("r34_2", "George K.", 5, "1w ago", "Creamy coconut and sweet mango are a match made in paradise.")
                )
            )
        )
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

    suspend fun publishUserRecipe(recipe: Recipe) = withContext(ioDispatcher) {
        inMemoryRecipes[recipe.id] = recipe
        val currentReviews = _recipeReviewsFlow.value.toMutableMap()
        currentReviews[recipe.id] = recipe.reviews
        _recipeReviewsFlow.value = currentReviews

        val entity = RecipeEntity(
            id = recipe.id,
            title = recipe.title,
            chefName = recipe.chefName,
            difficulty = recipe.difficulty,
            durationMinutes = recipe.durationMinutes,
            calories = recipe.calories,
            rating = recipe.rating,
            reviewCount = recipe.reviewCount,
            isBookmarked = recipe.isBookmarked,
            category = recipe.category,
            dietaryBadgesRaw = recipe.dietaryBadges.joinToString(","),
            allergensRaw = recipe.allergens.joinToString(","),
            defaultServings = recipe.defaultServings
        )
        recipeDao.insertRecipe(entity)
    }

    suspend fun deleteUserRecipe(recipeId: String) = withContext(ioDispatcher) {
        inMemoryRecipes.remove(recipeId)
        val currentReviews = _recipeReviewsFlow.value.toMutableMap()
        currentReviews.remove(recipeId)
        _recipeReviewsFlow.value = currentReviews
        recipeDao.deleteRecipe(recipeId)
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
