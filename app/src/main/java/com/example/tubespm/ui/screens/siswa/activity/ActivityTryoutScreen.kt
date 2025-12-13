package com.example.tubespm.ui.screens.siswa.activity

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Diperlukan untuk TryoutDetailDialog jika dipindahkan
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Diperlukan untuk TryoutDetailDialog
import androidx.compose.foundation.lazy.items // Diperlukan untuk TryoutDetailDialog
import androidx.compose.foundation.shape.CircleShape // Diperlukan untuk TryoutDetailDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close // Diperlukan untuk TryoutDetailDialog
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // Diperlukan untuk TryoutDetailDialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Diperlukan untuk TryoutDetailDialog
import androidx.compose.ui.window.Dialog // Diperlukan untuk TryoutDetailDialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tubespm.data.model.Section // Diperlukan untuk TryoutDetailDialog
import com.example.tubespm.data.model.Tryout // Diperlukan untuk TryoutDetailDialog
import androidx.compose.ui.draw.clip // Diperlukan untuk TryoutDetailDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTryoutScreen(
    navController : NavController,
    viewModel: ActivityTryoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Belum Dikerjakan", "Dalam Proses", "Selesai")

    // State untuk mengontrol dialog detail
    var showDetailDialogFor by remember { mutableStateOf<ActivityTryoutDetail?>(null) }

    // STATE BARU UNTUK KONFIRMASI PEMBATALAN
    var tryoutToConfirmCancel by remember { mutableStateOf<ActivityTryoutDetail?>(null) }

    val context = LocalContext.current

    // LOGIKA MENERIMA PESAN (TOAST) DARI QUIZ
    val resultMessageState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("submit_message")
        ?.observeAsState()

    val resultMessage = resultMessageState?.value

    LaunchedEffect(resultMessage) {
        resultMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("submit_message")
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Daftar Tryout",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE61C5D)
            )
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            indicator = { TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(it[selectedTabIndex]), height = 3.dp, color = Color.Black)}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    selectedContentColor = Color.Black,
                    unselectedContentColor = Color.Gray,
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                )
            }
        }

        // Konten berdasarkan state
        if (uiState.isLoading){
            Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            // Tampilkan konten tab berdasarkan uiState
            when (selectedTabIndex) {
                0 -> TryoutBelumDikerjakanContent(
                    activities = uiState.notStarted,
                    onCardClick = { activityDetail ->
                        showDetailDialogFor = activityDetail
                    },
                    onTryoutCanceled = { activityDetail ->
                        // Pindah Tryout yang akan dibatalkan ke state konfirmasi
                        tryoutToConfirmCancel = activityDetail
                    }
                )
                1 -> TryoutDalamProsesContent(
                    activities = uiState.inProgress,
                    onContinueClick = { activityDetail ->
                        navController.navigate("tryout_quiz/${activityDetail.userActivity.id}")
                    }
                )
                2 -> TryoutSelesaiContent(
                    activities = uiState.completed,
                    onResultClick = { activityDetail ->
                        navController.navigate("analisis/${activityDetail.userActivity.id}")
                    }
                )
            }
        }
    }

    // --- 1. Tampilkan dialog detail (Trigger Pembatalan Tahap 1) ---
    showDetailDialogFor?.let { activityDetail ->
        TryoutDetailDialog( // <<< SEKARANG TryoutDetailDialog SUDAH TERDEFINISI DI FILE INI
            tryout = activityDetail.tryout,
            onDismiss = { showDetailDialogFor = null },
            onStart = {
                showDetailDialogFor = null
                navController.navigate("tryout_quiz/${activityDetail.userActivity.id}")
            },
            onCancel = {
                // Saat tombol Batalkan di Dialog Detail ditekan, pindahkan ke state konfirmasi
                tryoutToConfirmCancel = activityDetail
                showDetailDialogFor = null // Tutup dialog detail
            }
        )
    }

    // --- 2. Tampilkan dialog KONFIRMASI Pembatalan (Trigger Pembatalan Tahap 2) ---
    tryoutToConfirmCancel?.let { activityDetail ->
        AlertDialog(
            onDismissRequest = { tryoutToConfirmCancel = null },
            title = {
                Text(text = "Batalkan Tryout?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Apakah Anda yakin ingin membatalkan pengambilan tryout '${activityDetail.tryout.title}' ini? Aksi ini tidak dapat diurungkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelTryout(activityDetail.userActivity.id)

                        Toast.makeText(context, "Tryout '${activityDetail.tryout.title}' berhasil dibatalkan.", Toast.LENGTH_SHORT).show()

                        tryoutToConfirmCancel = null // Tutup dialog konfirmasi
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D))
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { tryoutToConfirmCancel = null }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// InfoRow adalah component (Diperlukan di sini jika tidak ada di file lain)
@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

// =======================================================================
// DEFINISI TryoutDetailDialog DIPINDAHKAN KE SINI UNTUK MENGATASI ERROR
// =======================================================================
@Composable
fun TryoutDetailDialog(
    tryout: Tryout,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit // Sekarang onCancel hanya Memicu Dialog Konfirmasi di Parent
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.Black, modifier = Modifier.size(28.dp).background(Color.LightGray.copy(alpha = 0.3f), CircleShape).padding(4.dp))
                    }
                    Text(
                        text = tryout.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(16.dp))

                Text("Detail Tryout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("Kode Paket: ${tryout.code}", style = MaterialTheme.typography.bodyMedium)
                Text("Jumlah Soal: ${tryout.totalQuestionCount}", style = MaterialTheme.typography.bodyMedium)
                Text("Durasi: ${tryout.totalDuration} menit", style = MaterialTheme.typography.bodyMedium)
                Divider(Modifier.padding(vertical = 12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(tryout.sections) { section ->
                        Text(section.sectionName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            section.subtests.forEachIndexed { index, subtest ->
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(
                                        "${index + 1}. ${subtest.subtestName} (${subtest.questionCount} soal, ${subtest.duration} menit)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (subtest.topics.isNotEmpty()) {
                                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                                            subtest.topics.forEach { topic ->
                                                Text(text = "- ${topic.name}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Tombol Kerjakan
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Kerjakan Tryout", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(8.dp))
                // Tombol Batalkan
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Batalkan Tryout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}