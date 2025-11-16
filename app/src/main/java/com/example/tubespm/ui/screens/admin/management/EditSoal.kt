package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues

data class AnswerOption(
    val label: Char,
    val placeholder: String = "Add Here"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionScreen(
    paketName: String,          // contoh: "TO-001 (Penalaran Umum)"
    questionNumber: Int,        // contoh: 1
    paddingValuesFromNavHost: PaddingValues, // padding dari AdminMainScreen (bottom bar)
    onBackClick: () -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var discussionText by remember { mutableStateOf("") }

    val options = remember {
        listOf(
            AnswerOption('A'),
            AnswerOption('B'),
            AnswerOption('C'),
            AnswerOption('D'),
            AnswerOption('E')
        )
    }

    var answersText by remember {
        mutableStateOf(
            options.associate { it.label to "" }
        )
    }
    var correctAnswer by remember { mutableStateOf('A') }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Edit Question",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = paketName,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9966),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                // padding dari top appbar (Scaffold di EditSoal)
                .padding(innerPadding)
                // padding dari bottom bar (Scaffold di AdminMainScreen)
                .padding(paddingValuesFromNavHost)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {

                // ------------------ TITLE "QUESTION 1" ------------------
                Text(
                    text = "Question $questionNumber",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ------------------ INPUT QUESTION ------------------
                Text(
                    text = "Input Question",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Add Here", color = Color(0xFF9E9E9E))
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ------------------ ADD PICTURE ------------------
                Text(
                    text = "Add Picture",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                        .border(
                            width = 1.dp,
                            color = Color(0xFFBDBDBD),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Picture",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ------------------ INPUT ANSWER ------------------
                Text(
                    text = "Input Answer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                ) {
                    options.forEach { option ->
                        val isCorrect = correctAnswer == option.label

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Label (A) (B) ...
                            Text(
                                text = "(${option.label})",
                                fontSize = 13.sp,
                                color = Color(0xFF757575),
                                modifier = Modifier.width(32.dp)
                            )

                            // Jawaban
                            OutlinedTextField(
                                value = answersText[option.label] ?: "",
                                onValueChange = { newText ->
                                    answersText = answersText.toMutableMap().apply {
                                        put(option.label, newText)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                placeholder = {
                                    Text(
                                        text = option.placeholder,
                                        color = Color(0xFFB0B0B0),
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color.Black
                                ),
                                shape = RoundedCornerShape(4.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFE0E0E0),
                                    unfocusedContainerColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Tombol plus (dummy – bisa dipakai nanti untuk media, dsb.)
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = Color(0xFFD5D5D5)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add something",
                                        tint = Color(0xFF757575),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Tombol cek jawaban benar
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD5D5D5)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .noRippleClickable {
                                            correctAnswer = option.label
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // garis pemisah antar baris
                        if (option.label != 'E') {
                            Divider(color = Color(0xFFCCCCCC), thickness = 0.5.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ------------------ INPUT DISCUSSION ------------------
                Text(
                    text = "Input Discussion",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = discussionText,
                    onValueChange = { discussionText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text("Add Here", color = Color(0xFF9E9E9E))
                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ------------------ PREV / NEXT ------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Prev",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = questionNumber.toString(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF424242)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Next",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Helper: clickable tanpa ripple
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditQuestionScreenPreview() {
    EditQuestionScreen(
        paketName = "TO-001 (Penalaran Umum)",
        questionNumber = 1,
        paddingValuesFromNavHost = PaddingValues(0.dp),
        onBackClick = {}
    )
}
