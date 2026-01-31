package com.example.recipegram.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.recipegram.data.RecipeFull
import com.example.recipegram.ui.viewmodel.RecipeDetailsViewModel
import androidx.compose.ui.draw.drawBehind

@Composable
fun RecipeDetailsScreen(
    recipeId: Long,
    onBack: () -> Unit,
    viewModel: RecipeDetailsViewModel = hiltViewModel<RecipeDetailsViewModel>()
) {
    val recipeFull by viewModel.recipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        recipeFull?.let { data ->
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        if (data.recipe.imagePath != null) {
                            Image(
                                painter = rememberAsyncImagePainter(data.recipe.imagePath),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, null, Modifier.size(64.dp), tint = Color.Gray)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-30).dp),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(data.recipe.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(data.recipe.time) },
                                    icon = { Icon(Icons.Default.Timer, null, Modifier.size(16.dp)) }
                                )
                                Spacer(Modifier.width(8.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(data.author.username) },
                                    icon = { Icon(Icons.Default.Person, null, Modifier.size(16.dp)) }
                                )
                            }

                            data.recipe.description?.let {
                                Text(it, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(20.dp))
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))

                            Text("Ingredients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    data.ingredients.forEach { ing ->
                                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(ing.name, modifier = Modifier.weight(1f))
                                            Text(ing.amount, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))
                                    }
                                }
                            }

                            Text("Instructions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))

                            data.steps.forEachIndexed { index, stepWithPhotos ->
                                StepDetailItem(index, stepWithPhotos)
                            }

                            Spacer(Modifier.height(50.dp))
                        }
                    }
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 40.dp, start = 16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun StepDetailItem(index: Int, stepWithPhotos: com.example.recipegram.data.StepWithPhotos) {
    val step = stepWithPhotos.step
    val photos = stepWithPhotos.photos

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            if (!step.name.isNullOrBlank()) {
                Text(step.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        Row(modifier = Modifier.padding(start = 16.dp).drawBehind {
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(0f, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }) {
            Column(modifier = Modifier.padding(start = 32.dp, top = 8.dp)) {
                Text(step.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)

                photos.firstOrNull()?.let { photo ->
                    Spacer(Modifier.height(12.dp))
                    Image(
                        painter = rememberAsyncImagePainter(photo.imagePath),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}