package com.example.recipegram.repository

import com.example.recipegram.data.Ingredient
import com.example.recipegram.data.Recipe
import com.example.recipegram.data.RecipeFull
import com.example.recipegram.data.RecipeGramDao
import com.example.recipegram.data.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val dao: RecipeGramDao,
    private val fileRepository: FileRepository
) {


    fun getAllRecipes(query: String = ""): Flow<List<RecipeFull>> {
        return dao.getAllRecipes().map { list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.recipe.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    suspend fun getRecipeById(id: Long): RecipeFull? {
        return dao.getRecipeById(id)
    }

    fun getUserRecipes(userId: Long): Flow<List<RecipeFull>> {
        return dao.getAllRecipes().map { list ->
            list.filter { it.recipe.authorId == userId }
        }
    }

    suspend fun createRecipe(
        title: String,
        description: String?,
        time: String,
        authorId: Long,
        coverUri: String?,
        ingredients: List<Ingredient>,
        steps: List<StepDataUI>
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val savedCoverPath = fileRepository.saveImageToInternalStorage(coverUri)

                val recipe = Recipe(
                    name = title,
                    description = description,
                    time = time,
                    authorId = authorId,
                    imagePath = savedCoverPath
                )

                val stepsWithSavedPhotos = steps.mapIndexed { index, stepUI ->
                    val savedStepPath = fileRepository.saveImageToInternalStorage(stepUI.imageUri)

                    val stepEntity = Step(
                        recipeId = 0,
                        name = stepUI.name,
                        description = stepUI.description,
                        stepOrder = index + 1
                    )

                    Pair(stepEntity, savedStepPath)
                }

                dao.createFullRecipe(
                    recipe = recipe,
                    ingredients = ingredients,
                    stepsWithPhotos = stepsWithSavedPhotos
                )

                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun deleteRecipe(recipeId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                dao.deleteRecipe(recipeId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

data class StepDataUI(
    val name: String?,
    val description: String,
    val imageUri: String?
)