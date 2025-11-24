package com.example.tubespm.ui.screens.siswa.homepage

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.tubespm.R
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.ui.theme.TubesPMTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController : NavController,
    viewModel: HomeViewModel = hiltViewModel() // inject viewmodel
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    var userName by remember { mutableStateOf("User") }

    // DAFTAR KATEGORI (7 subtes UTBK)
    val categories = listOf(
        CategoryModel("Penalaran Umum", Icons.Default.Lightbulb, Color(0xFFE91E63)),
        CategoryModel("Pengetahuan Kuantitatif", Icons.Default.Calculate, Color(0xFF9C27B0)),
        CategoryModel("Literasi Bahasa Indonesia", Icons.Default.MenuBook, Color(0xFF673AB7)),
        CategoryModel("Pengetahuan Umum", Icons.Default.Public, Color(0xFF3F51B5)),
        CategoryModel("Literasi Bahasa Inggris", Icons.Default.Book, Color(0xFF2196F3)),
        CategoryModel("Penalaran Matematika", Icons.Default.AutoGraph, Color(0xFF4CAF50)),
        CategoryModel("Pemahaman Konsep", Icons.Default.School, Color(0xFFFF9800))
    )

    // Selected: selalu ada satu (default = pertama). User tidak bisa membatalkan (hanya ganti ke yang lain).
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Filter Latihan Soal berdasarkan Kategori
    val filteredLatihan = remember(selectedCategory, uiState.latestLatihan) {
        if (selectedCategory == null) uiState.latestLatihan
        else uiState.latestLatihan.filter { it.subtest.contains(selectedCategory!!, ignoreCase = true)}
    }

    val decodedBitmap = remember(uiState.profilePicture) {
        if (uiState.profilePicture.isNotEmpty()) {
            try {
                val bytes = Base64.decode(uiState.profilePicture, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER with gradient + search (sedikit dipadatkan)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE91E63), Color(0xFFD81B60))
                    )
                )
                .padding(top = 40.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Picture
                    if (decodedBitmap != null) {
                        // Tampilkan gambar dari Base64
                        Image(
                            bitmap = decodedBitmap.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Tampilkan Placeholder Default jika Base64 kosong/gagal decode
                        Image(
                            painter = painterResource(id = R.drawable.user_default_profile),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hi!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = uiState.userName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    // Notification Bell with Badge
                    Box {
                        IconButton(
                            onClick = {
                                navController.navigate("notification")
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                        // Notification Badge
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEB3B))
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search Tryout...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), //Tambahkan Tombol Search di Keyboard
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if(searchQuery.isNotBlank()) {
                                navController.navigate("exercises?type=tryout&query=$searchQuery")
                            }
                        }
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.clickable {
                                if(searchQuery.isNotBlank()) {
                                    navController.navigate("exercises?type=tryout&query=$searchQuery")
                                }
                            }
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rekomendasi Untuk Kamu Section (tetap horizontal)
        Text(
            text = "Rekomendasi Untuk Kamu!",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF212121)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if(uiState.isLoading){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if(uiState.tryoutRecommendation.isEmpty()){
            Text("Belum ada tryout tersedia.", modifier = Modifier.padding(horizontal = 20.dp), color = Color.Gray)
        } else{
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(uiState.tryoutRecommendation) {index, tryout ->
                    // Warna card looping
                    val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7))
                    val cardColor = colors[index % colors.size]

                    RecommendedTryoutCard(
                        tryout = tryout,
                        color = cardColor,
                        onClick = {
                            // Navigasi ke Exercise Screen atau Detail Tryout
                            navController.navigate("exercises")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // =======================
        // KATEGORI SUBTES (HORIZONTAL SCROLL). See-all sudah dihapus.
        // =======================
        Text(
            text = "Kategori Subtes",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF212121)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat.title
                CategoryCardHorizontal(
                    title = cat.title,
                    icon = cat.icon,
                    color = cat.color,
                    isSelected = isSelected,
                    onClick = {
                        selectedCategory = if (selectedCategory == cat.title) null else cat.title
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // =======================
        // SOAL LATIHAN TERBARU (FILTERED)
        // Gunakan AnimatedContent agar transisi saat ganti kategori terasa halus
        // =======================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Soal Latihan Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF212121)
            )
            Text(
                text = selectedCategory ?: "Semua Kategori",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Animated Content: saat selectedCategory berubah, konten soal berubah dengan fade
        AnimatedContent(
            targetState = filteredLatihan,
            transitionSpec = { fadeIn() with fadeOut() },
            label = "LatihanList"
        ) { latihanList ->
            if (latihanList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if(uiState.isLoading) "Memuat..." else "Tidak ada soal untuk kategori ini", color = Color.Gray)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(latihanList) { latihan ->

                        // Jika subtest latihan sama dengan judul kategori, ambil warnanya.
                        // Jika tidak ketemu, pakai warna default.
                        val categoryColor = categories.find { category ->
                            latihan.subtest.contains(category.title, ignoreCase = true)
                        }?.color ?: Color(0xFFE91E63)

                        LatestQuestionCard(
                            latihan = latihan, // Passing LatihanSoal Object
                            color = categoryColor,
                            onClick = {
                                // Navigasi ke Exercise Screen bagian latihan
                                navController.navigate("exercises?type=latihan")
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

    //yg ngerjain backend tolong ini dibuat warna nya looping yah, tiap 5 tryout warna nya looping

        if (uiState.scoreHistory.isNotEmpty()) {
            CustomBarChart(
                data = uiState.scoreHistory,
                title = "Statistik Skor Tryout Kamu",
                maxValue = 1000,
                barColor = Color(0xFF3F51B5)
            )
        } else if (!uiState.isLoading){
            // Placeholder jika belum ada data
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "Belum ada riwayat skor. Ayo kerjakan tryout!",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class CategoryModel(
    val title: String,
    val icon: ImageVector,
    val color: Color
)



@Composable
fun CustomBarChart(
    data: List<ChartData>,
    title: String = "Statistik Skor Tryout",
    modifier: Modifier = Modifier,
    maxValue: Int = 1000,
    barWidth: Dp = 24.dp,
    barColor: Color = Color(0xFF2196F3),
    labelColor: Color = Color.DarkGray,
    showValues: Boolean = true
) {
    // Pastikan maxScore minimal sama dengan nilai tertinggi di data
    val maxScore = maxOf(data.maxOfOrNull { it.value } ?: 1, maxValue)
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header dengan icon dan jumlah data
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF212121)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicator scroll (jika ada lebih dari 5 tryout)
            if (data.size > 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwipeLeft,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Geser untuk melihat semua",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Chart area with horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Spacer untuk padding awal
                Spacer(modifier = Modifier.width(4.dp))

                data.forEach { item ->
                    val barHeightRatio = item.value / maxScore.toFloat()
                    val animatedHeight by animateFloatAsState(
                        //coerceAtLeast lebih kecil (0.02f) agar nilai 0 terlihat tipis/di bawah
                        targetValue = barHeightRatio.coerceAtLeast(0.02f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "barHeight"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
//                        modifier = Modifier.width(barWidth * 1.8f)
//                        Lebar kolom fix agar teks panjang bisa wrap dengan rapi
                        modifier = Modifier.width(60.dp)
                    ) {
                        // Value di atas bar
                        if (showValues) {
                            Text(
                                text = "${item.value}",
                                fontSize = 12.sp,
                                color = if(item.value == 0) Color.Gray else (item.color ?: barColor),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                        }

                        // Bar dengan shadow dan gradient
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .fillMaxHeight(animatedHeight.coerceIn(0.02f, 1f))
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp)
                                )
                                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            item.color ?: barColor,
                                            (item.color ?: barColor).copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Label di bawah bar
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            color = labelColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp, // Jarak antar baris teks
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth() // Isi lebar kolom 60dp
                        )
                    }
                }

                // Spacer untuk padding akhir
                Spacer(modifier = Modifier.width(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informasi tambahan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoLabel(
                    label = "Rata-rata",
                    value = "${data.map { it.value }.average().toInt()}",
                    color = Color(0xFF4CAF50)
                )
                InfoLabel(
                    label = "Tertinggi",
                    value = "${data.maxOfOrNull { it.value } ?: 0}",
                    color = Color(0xFFE91E63)
                )
                InfoLabel(
                    label = "Terendah",
                    value = "${data.minOfOrNull { it.value } ?: 0}",
                    color = Color(0xFFFF9800)
                )
                InfoLabel(
                    label = "Jumlah Tryout",
                    value = "${data.size}",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun InfoLabel(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ----------------------------
// Reusable components (dipertahankan / disesuaikan)
// ----------------------------
@Composable
fun RecommendedTryoutCard(
    tryout: Tryout, // Menerima Data Asli
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "Scale"
    )
    Card(
        modifier = Modifier
            .width(280.dp)
            .scale(scale)
            .clickable {
                isPressed = !isPressed
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tryout.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "NEW",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

//            Text(
//                text = tryout.description.ifBlank { "Latihan simulasi UTBK lengkap." },
//                color = Color.White.copy(alpha = 0.95f),
//                fontSize = 13.sp,
//                lineHeight = 18.sp,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(icon = Icons.Default.Description, text = "${tryout.totalQuestionCount} Soal")
                InfoChip(icon = Icons.Default.Schedule, text = "${tryout.totalDuration} Menit")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Detail", color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CategoryCardHorizontal(
    title: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .size(width = 140.dp, height = 120.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) color else Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LatestQuestionCard(
    latihan: LatihanSoal, // Menerima Data Asli
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "Scale"
    )

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(220.dp)
            .scale(scale)
            .clickable { isPressed = !isPressed },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = latihan.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = latihan.subtest,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text(text = "${latihan.questionCount} Soal", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Ayo Mulai", color = color, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
