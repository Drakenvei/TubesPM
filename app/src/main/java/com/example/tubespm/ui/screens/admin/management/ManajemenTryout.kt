package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubespm.ui.theme.TubesPMTheme

// ======================================================
// DATA MODEL TRYOUT (UI Helper)
// ======================================================
data class TryoutPackage(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val tpsSoal: Int,
    val tpsMenit: Int,
    val literasiSoal: Int,
    val literasiMenit: Int
)

// ======================================================
// SCREEN UTAMA
// ======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenTryoutScreen(
    paddingValuesFromNavHost: PaddingValues,
    // Update signature callback sesuai NavGraph: (tryoutId, questionId, paketName, questionNumber)
    onGoToEditQuestion: (String, String, String, Int) -> Unit,
    onNavigateToCreateTryout: () -> Unit, // Tambahkan ini
    onNavigateToCreateLatihan: () -> Unit, // Tambahkan ini
    viewModel: ManajemenTryoutViewModel = viewModel()
) {
    // 1. Observasi State dari ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // State lokal untuk dialog popup
    var showEditManagementDialog by remember { mutableStateOf(false) }
    var selectedPackageForEdit by remember { mutableStateOf<TryoutPackage?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Clipboard",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (selectedTab == 0) "Manajemen Tryout" else "Manajemen Latihan Soal",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9966),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        onNavigateToCreateTryout()
                    } else {
                        onNavigateToCreateLatihan()
                    }
                },
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(paddingValuesFromNavHost)
        ) {

            // TAB ROW
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Tryout", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Latihan Soal", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                }
            }

            // KONTEN UTAMA
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9966))
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        TryoutTabContent(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                            tryoutPackages = uiState.tryoutPackages,
                            onClickSettings = { pkg ->
                                selectedPackageForEdit = pkg
                                showEditManagementDialog = true
                            }
                        )
                    }
                    1 -> {
                        LatihanSoalTabContent(
                            contentPadding = PaddingValues(0.dp),
                            onAddClick = onNavigateToCreateLatihan
                        )
                    }
                }
            }

            // DIALOG EDIT
            if (showEditManagementDialog && selectedPackageForEdit != null) {
                EditManagementDialog(
                    paket = selectedPackageForEdit!!,
                    onDismiss = {
                        showEditManagementDialog = false
                        selectedPackageForEdit = null
                    },
                    onDeactivatePackage = {
                        // TODO: Panggil fungsi di ViewModel untuk update status
                        showEditManagementDialog = false
                        selectedPackageForEdit = null
                    },
                    onAddMoreSection = { },
                    // Adapter: Karena EditManagementDialog memanggil callback tanpa parameter (atau dummy),
                    // kita isi parameter yang dibutuhkan NavGraph di sini.
                    onGoToEditQuestion = {
                        onGoToEditQuestion(
                            selectedPackageForEdit!!.id, // Tryout ID
                            "q_default",                 // Default Question ID (atau nanti dari list)
                            selectedPackageForEdit!!.name, // Nama Paket
                            1                            // Nomor Soal default
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun TryoutTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    tryoutPackages: List<TryoutPackage>,
    onClickSettings: (TryoutPackage) -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search Tryout", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (tryoutPackages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada paket tryout tersedia.", color = Color.Gray)
                    }
                }
            } else {
                items(tryoutPackages) { tryoutPackage ->
                    TryoutPackageCard(
                        tryoutPackage = tryoutPackage,
                        onClickSettings = onClickSettings
                    )
                }
            }
        }
    }
}

@Composable
fun TryoutPackageCard(
    tryoutPackage: TryoutPackage,
    onClickSettings: (TryoutPackage) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tryoutPackage.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onClickSettings(tryoutPackage) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TryoutSection(title = "TPS", soal = tryoutPackage.tpsSoal, menit = tryoutPackage.tpsMenit)
            Spacer(modifier = Modifier.height(12.dp))
            TryoutSection(title = "Literasi", soal = tryoutPackage.literasiSoal, menit = tryoutPackage.literasiMenit)

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                val (bgColor, textColor, textLabel) = if (tryoutPackage.isActive) {
                    Triple(Color.White, Color(0xFFE91E63), "active")
                } else {
                    Triple(Color(0xFFB0BEC5), Color.White, "inactive")
                }
                Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
                    Text(text = textLabel, color = textColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
fun TryoutSection(title: String, soal: Int, menit: Int) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = Color.White, shape = RoundedCornerShape(4.dp)) {
            Text(text = title, color = Color(0xFFE91E63), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (soal > 0) "$soal soal" else "- soal", color = Color.White, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (menit > 0) "$menit menit" else "- menit", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManajemenTryoutScreenPreview() {
    TubesPMTheme {
        ManajemenTryoutScreen(
            paddingValuesFromNavHost = PaddingValues(0.dp),
            onGoToEditQuestion = { _, _, _, _ -> },
            onNavigateToCreateTryout = { },
            onNavigateToCreateLatihan = { }
        )
    }
}