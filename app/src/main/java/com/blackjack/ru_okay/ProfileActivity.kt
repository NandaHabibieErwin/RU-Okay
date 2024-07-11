package com.blackjack.ru_okay

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.blackjack.ru_okay.databinding.ActivityProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        binding.changeAvatarButton.setOnClickListener { openFileChooser() }
        binding.saveButton.setOnClickListener { saveUserProfile() }

        loadUserProfile()
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                binding.avatarImageView.setImageBitmap(bitmap)
                uploadImageToFirebaseStorage(imageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageReference = storage.reference.child("avatars/$userId.jpg")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    // Update the user's profile with the new avatar URL
                    database.child("users").child(userId).child("avatarUrl").setValue(uri.toString())
                }
            }
            .addOnFailureListener {
                // Handle unsuccessful uploads
            }
    }

    private fun saveUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val gender = if (binding.maleRadioButton.isChecked) "Male" else "Female"
        val userProfile = mapOf(
            "username" to binding.usernameEditText.text.toString(),
            "realName" to binding.realNameEditText.text.toString(),
            "age" to binding.ageEditText.text.toString(),
            "location" to binding.locationEditText.text.toString(),
            "gender" to gender,
            "aboutMe" to binding.aboutMeEditText.text.toString(),
            "hobbies" to binding.hobbiesEditText.text.toString(),
            "phoneNumber" to binding.phoneNumberEditText.text.toString()
        )
        database.child("users").child(userId).child("profile").setValue(userProfile)
            .addOnSuccessListener {
                // Profile saved successfully
                finish() // Navigate back to the previous activity
            }
            .addOnFailureListener {
                // Failed to save profile
            }
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child("users").child(userId).child("profile").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.usernameEditText.setText(snapshot.child("username").value as? String)
                binding.realNameEditText.setText(snapshot.child("realName").value as? String)
                binding.ageEditText.setText(snapshot.child("age").value as? String)
                binding.locationEditText.setText(snapshot.child("location").value as? String)
                val gender = snapshot.child("gender").value as? String
                if (gender == "Male") {
                    binding.maleRadioButton.isChecked = true
                } else {
                    binding.femaleRadioButton.isChecked = true
                }
                binding.aboutMeEditText.setText(snapshot.child("aboutMe").value as? String)
                binding.hobbiesEditText.setText(snapshot.child("hobbies").value as? String)
                binding.phoneNumberEditText.setText(snapshot.child("phoneNumber").value as? String)
                val avatarUrl = snapshot.child("avatarUrl").value as? String
                avatarUrl?.let {
                    Glide.with(this@ProfileActivity)
                        .load(it)
                        .placeholder(R.drawable.ic_user_temporary)
                        .into(binding.avatarImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}
