package com.example.tubespm.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object TextFieldColors {
    /**
     * Warna textfield standar untuk admin screens
     * Memastikan teks terlihat jelas dengan background abu-abu
     */
    @Composable
    fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFE0E0E0),
        unfocusedContainerColor = Color(0xFFE0E0E0),
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = Color(0xFF212121), // Hitam untuk kontras
        unfocusedTextColor = Color(0xFF212121), // Hitam untuk kontras
        cursorColor = Color(0xFF212121),
        focusedLabelColor = Color(0xFF757575),
        unfocusedLabelColor = Color(0xFF757575),
        focusedPlaceholderColor = Color(0xFF9E9E9E),
        unfocusedPlaceholderColor = Color(0xFF9E9E9E)
    )
}
