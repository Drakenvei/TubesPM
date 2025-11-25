package com.example.tubespm.ui.screens.siswa.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

// ============================================
// 1. DATA MODELS
// ============================================

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String = "",
    val time: String,
    val date: Date = Date(),
    val isRead: Boolean = false,
    val category: NotificationCategory = NotificationCategory.GENERAL
)

enum class NotificationCategory {
    TRYOUT, GENERAL
}

// ============================================
// 2. EMPTY STATE COMPONENT
// ============================================

@Composable
fun EmptyNotificationState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mailbox Illustration
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.tubespm.R.drawable.mailboxicon),
                contentDescription = "MailBoxNotification",
                modifier = Modifier
                    .size(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Belum Ada Notifikasi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Notifikasi kamu bakalan muncul disini\nketika kamu menerima sebuah\nnotifikasi!",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

// ============================================
// 3. NOTIFICATION ITEM COMPONENT
// ============================================

@Composable
fun NotificationItemCard(
    notification: NotificationItem,
    onItemClick: (String) -> Unit // Callback saat diklik
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable{ onItemClick(notification.id)},
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFFFF8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon indicator
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.tubespm.R.drawable.notificonpast),
                    contentDescription = "Notif Icon sudah dibaca",
                    modifier = Modifier.size(25.dp)
                )
                // Badge icon
                if (!notification.isRead) {
                    Image(
                        painter = painterResource(id = com.example.tubespm.R.drawable.notificonnew),
                        contentDescription = "Notif Icon belum dibaca",
                        modifier = Modifier
                            .size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Notification content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.7f),
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis // Tambahkan "..." jika teks terlalu panjang
                )

                Spacer(modifier = Modifier.height(8.dp)) // Beri jarak ke waktu

                Text(
                    text = notification.time,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// ============================================
// 4. NOTIFICATION SECTION HEADER
// ============================================

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        if (actionText != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

// ============================================
// 5. MAIN NOTIFICATION PAGE
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel() // Inject ViewModel
) {
    // Ambil data dari ViewModel
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifikasi",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
            )
        }
    ) { paddingValues ->

        //Handle loading
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (notifications.isEmpty()) {
            // Show empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                EmptyNotificationState()
            }
        } else {
            // Show notification list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFFAFAFA))
            ) {
                // Group notifications by date
                val groupedNotifications = notifications.groupBy { notification ->
                    val calendar = Calendar.getInstance()
                    calendar.time = notification.date

                    val today = Calendar.getInstance()
                    val yesterday = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -1)
                    }

                    when {
                        isSameDay(calendar, today) -> "Hari ini"
                        isSameDay(calendar, yesterday) -> "Kemarin"
                        else -> SimpleDateFormat("MMM dd", Locale("id", "ID")).format(notification.date)
                    }
                }

                groupedNotifications.forEach { (dateLabel, notificationsInGroup) ->
                    item {
                        SectionHeader(
                            title = dateLabel,
                            // Tampilkan tombol "Tandai dibaca" hanya jika ada yang belum dibaca di grup ini
                            actionText = if (notificationsInGroup.any { !it.isRead }) "Tandai telah dibaca" else null,
                            onActionClick = {
                                // Panggil fungsi ViewModel untuk update batch
                                val unreadIds = notificationsInGroup.filter { !it.isRead }.map { it.id }
                                viewModel.markAllAsRead(unreadIds)
                            }
                        )
                    }

                    items(notificationsInGroup) { notification ->
                        NotificationItemCard(
                            notification = notification,
                            onItemClick = { id ->
                                // Panggil fungsi ViewModel untuk update satu item
                                viewModel.markAsRead(id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Helper function
private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

// ============================================
// 6. PREVIEW
// ============================================

@Preview(showBackground = true)
@Composable
fun PreviewNotificationPageEmpty() {
    MaterialTheme {
        NotificationScreen()
    }
}


