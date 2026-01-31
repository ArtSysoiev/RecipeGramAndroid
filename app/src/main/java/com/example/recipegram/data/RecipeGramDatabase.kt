package com.example.recipegram.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeGramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY created_at DESC")
    fun getAllRecipes(): Flow<List<RecipeFull>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: Long): RecipeFull?

    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Insert
    suspend fun insertStep(step: Step): Long

    @Insert
    suspend fun insertStepPhoto(photo: StepPhoto)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: Long)

    @Transaction
    suspend fun createFullRecipe(
        recipe: Recipe,
        ingredients: List<Ingredient>,
        stepsWithPhotos: List<Pair<Step, String?>>
    ) {
        val recipeId = insertRecipe(recipe)

        val ingredientsWithId = ingredients.map { it.copy(recipeId = recipeId) }
        insertIngredients(ingredientsWithId)

        stepsWithPhotos.forEach { (step, photoPath) ->
            val stepWithId = step.copy(recipeId = recipeId)
            val stepId = insertStep(stepWithId)

            if (photoPath != null) {
                insertStepPhoto(StepPhoto(stepId = stepId, imagePath = photoPath, caption = null))
            }
        }
    }
}

@Database(
    entities = [User::class, Recipe::class, Ingredient::class, Step::class, StepPhoto::class],
    version = 1,
    exportSchema = false
)
abstract class RecipeGramDatabase : RoomDatabase() {
    abstract fun dao(): RecipeGramDao

    companion object {
        @Volatile
        private var Instance: RecipeGramDatabase? = null

        fun getDatabase(context: Context): RecipeGramDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    RecipeGramDatabase::class.java,
                    "recipegram.db"
                )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}