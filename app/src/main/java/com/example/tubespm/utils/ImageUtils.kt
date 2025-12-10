package com.example.tubespm.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object ImageUtils {
    // Ubah Uri -> Base64
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
//            val bitmap = BitmapFactory.decodeStream(inputStream)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            // Resize gambar jika perlu agar tidak melebihi 1MB Firestore limit
            // val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true)

            // Ubah ukuran gambar agar sisi terpanjang max 800px
            val scaledBitmap = getResizedBitmap(originalBitmap, 800)

            bitmapToBase64(scaledBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Ubah Bitmap -> Base64 String
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Kompres JPEG kualitas 50%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Helper untuk mengubah ukuran gambar secara proporsional
     * (Agar gambar tidak gepeng/stretching)
     */
    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            // Gambar Landscape (Lebar > Tinggi)
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            // Gambar Portrait (Tinggi > Lebar)
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }


    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Upload gambar ke Firebase Storage dan return download URL
     * @param uri URI gambar dari device
     * @param folderPath Path folder di storage (default: "soal_images")
     * @return Download URL string
     */
    suspend fun uploadImageToFirebaseStorage(
        uri: Uri,
        folderPath: String = "soal_images"
    ): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child("$folderPath/$fileName")

        val uploadTask = imageRef.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    /**
     * Delete gambar dari Firebase Storage
     */
    suspend fun deleteImageFromFirebaseStorage(imageUrl: String) {
        try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
            // Ignore error jika file tidak ditemukan
        }
    }
}