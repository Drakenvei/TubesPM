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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.tubespm.ui.theme.TubesPMTheme
import com.example.tubespm.ui.screens.admin.management.EditManagementDialog

// ======================================================
// DATA MODEL TRYOUT
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
// SCREEN UTAMA MANAGEMEN TRYOUT + LATIHAN SOAL (TAB)
// ======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenTryoutScreen(
    // padding dari AdminRoot / AdminMainScreen (Scaffold dengan BottomNavbarAdmin)
    paddingValuesFromNavHost: PaddingValues
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    // state untuk dialog edit paket
    var showEditManagementDialog by remember { mutableStateOf(false) }
    var selectedPackageForEdit by remember { mutableStateOf<TryoutPackage?>(null) }

    // >>> DATA INI MASIH DUMMY, HARD-CODED DI DALAM KODE <<<
    // Kalau mau ambil dari Firebase, bagian ini nanti diganti dengan hasil query.
    val tryoutPackages = remember {
        listOf(
            TryoutPackage(
                id = "TO-001",
                name = "Paket Tryout Sakti (TO-001)",
                isActive = true,
                tpsSoal = 80,
                tpsMenit = 80,
                literasiSoal = 80,
                literasiMenit = 120
            ),
            TryoutPackage(
                id = "TO-002",
                name = "Paket Tryout Sakti (TO-002)",
                isActive = false,
                tpsSoal = 80,
                tpsMenit = 80,
                literasiSoal = 80,
                literasiMenit = 120
            ),
            TryoutPackage(
                id = "TO-003",
                name = "Paket Tryout Sakti (TO-003)",
                isActive = false,
                tpsSoal = 0,
                tpsMenit = 0,
                literasiSoal = 0,
                literasiMenit = 0
            )
        )
    }

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
                            text = if (selectedTab == 0)
                                "Manajemen Tryout"
                            else
                                "Manajemen Latihan Soal",
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
                    // TODO:
                    // if (selectedTab == 0) { tambah paket tryout }
                    // else { tambah paket latihan soal }
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
                .padding(innerPadding)            // padding dari Scaffold (top bar, FAB)
                .padding(paddingValuesFromNavHost) // padding dari NavHost / bottom bar
        ) {

            // ==================================================
            // TABROW: TRYOUT & LATIHAN SOAL
            // ==================================================
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                ) {
                    Text(
                        text = "Tryout",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) {
                    Text(
                        text = "Latihan Soal",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // ==================================================
            // KONTEN PER TAB
            // ==================================================
            when (selectedTab) {
                0 -> {
                    TryoutTabContent(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        tryoutPackages = tryoutPackages,
                        onClickSettings = { pkg ->
                            selectedPackageForEdit = pkg
                            showEditManagementDialog = true
                        }
                    )
                }

                1 -> {
                    LatihanSoalTabContent(
                        contentPadding = PaddingValues(0.dp)
                    )
                }
            }

            // ==================================================
            // DIALOG EDIT MANAGEMENT (POPU PAKET)
            // ==================================================
            if (showEditManagementDialog && selectedPackageForEdit != null) {
                EditManagementDialog(
                    paket = selectedPackageForEdit!!,
                    onDismiss = {
                        showEditManagementDialog = false
                        selectedPackageForEdit = null
                    },
                    onDeactivatePackage = {
                        // TODO: logika nonaktifkan paket di sini (update ke Firebase, dsb.)
                        showEditManagementDialog = false
                        selectedPackageForEdit = null
                    },
                    onAddMoreSection = {
                        // TODO: logika tambah section baru
                    }
                )
            }
        }
    }
}

// ======================================================
// KONTEN TAB "TRYOUT"
// ======================================================
@Composable
fun TryoutTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    tryoutPackages: List<TryoutPackage>,
    onClickSettings: (TryoutPackage) -> Unit
) {
    Column {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search Tryout", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // List Tryout
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                tryoutPackages.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }
            ) { tryoutPackage ->
                TryoutPackageCard(
                    tryoutPackage = tryoutPackage,
                    onClickSettings = onClickSettings
                )
            }
        }
    }
}

// ======================================================
// CARD PAKET TRYOUT
// ======================================================
@Composable
fun TryoutPackageCard(
    tryoutPackage: TryoutPackage,
    onClickSettings: (TryoutPackage) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE91E63)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: title + settings icon
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
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TPS Section
            TryoutSection(
                title = "TPS",
                soal = tryoutPackage.tpsSoal,
                menit = tryoutPackage.tpsMenit
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Literasi Section
            TryoutSection(
                title = "Literasi",
                soal = tryoutPackage.literasiSoal,
                menit = tryoutPackage.literasiMenit
            )

            // Status badge di pojok kanan bawah (hanya jika ada data soal/menit)
            val hasData = tryoutPackage.tpsSoal > 0 || tryoutPackage.literasiSoal > 0
            if (hasData) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val bgColor: Color
                    val textColor: Color
                    val textLabel: String

                    if (tryoutPackage.isActive) {
                        bgColor = Color.White
                        textColor = Color(0xFFE91E63)
                        textLabel = "active"
                    } else {
                        bgColor = Color(0xFFB0BEC5)
                        textColor = Color.White
                        textLabel = "inactive"
                    }

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = textLabel,
                            color = textColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

// ======================================================
// SECTION TPS / LITERASI
// ======================================================
@Composable
fun TryoutSection(
    title: String,
    soal: Int,
    menit: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFFE91E63),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Soal",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (soal > 0) "$soal soal" else "- soal",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Waktu",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (menit > 0) "$menit menit" else "- menit",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ManajemenTryoutScreenPreview() {
    TubesPMTheme {
        ManajemenTryoutScreen(
            paddingValuesFromNavHost = PaddingValues(0.dp)
        )
    }
}
