package com.blackjack.ru_okay.setting

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.ActivityPsychologistProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.sendbird.android.handler.CompletionHandler
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.uikit.SendbirdUIKit
import java.io.IOException

class PsychologistProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPsychologistProfileBinding

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPsychologistProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance("https://ru-okaydemo-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        binding.changeAvatarButton.setOnClickListener { openFileChooser() }
        binding.saveButton.setOnClickListener { savePsychologistProfile() }

        loadPsychologistProfile()
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
                    // Update the psychologist's profile with the new avatar URL
                    database.child("psychologists").child(userId).child("avatarUrl").setValue(uri.toString())
                }
            }
            .addOnFailureListener {
                // Handle unsuccessful uploads
            }
    }

    private fun savePsychologistProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val gender = if (binding.maleRadioButton.isChecked) "Male" else "Female"
        val psychologistProfile = mapOf(
            "username" to binding.usernameEditText.text.toString(),
            "realName" to binding.realNameEditText.text.toString(),
            "specialization" to binding.specializationEditText.text.toString(),
            "experience" to binding.experienceEditText.text.toString(),
            "location" to binding.locationEditText.text.toString(),
            "gender" to gender,
            "aboutMe" to binding.aboutMeEditText.text.toString(),
            "phoneNumber" to binding.phoneNumberEditText.text.toString()
        )
        database.child("psychologists").child(userId).child("profile").setValue(psychologistProfile)
            .addOnSuccessListener {
                val username = psychologistProfile["username"].toString()
                val params = UserUpdateParams()
                params.nickname = username

                SendbirdUIKit.updateUserInfo(params, CompletionHandler { e ->
                    if (e != null) {
                        Log.e("Sendbird", "Failed to update user info: ${e.message}")
                        // Handle error, show toast or alert
                    } else {
                        Log.d("Sendbird", "User info updated successfully")
                        finish()
                    }
                })
                // Navigate back to the previous activity
            }
            .addOnFailureListener {
                // Failed to save profile
            }
    }

    private fun loadPsychologistProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child("psychologists").child(userId).child("profile").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.usernameEditText.setText(snapshot.child("username").value as? String)
                binding.realNameEditText.setText(snapshot.child("realName").value as? String)
                binding.specializationEditText.setText(snapshot.child("specialization").value as? String)
                binding.experienceEditText.setText(snapshot.child("experience").value as? String)
                binding.locationEditText.setText(snapshot.child("location").value as? String)
                val gender = snapshot.child("gender").value as? String
                if (gender == "Male") {
                    binding.maleRadioButton.isChecked = true
                } else {
                    binding.femaleRadioButton.isChecked = true
                }
                binding.aboutMeEditText.setText(snapshot.child("aboutMe").value as? String)
                binding.phoneNumberEditText.setText(snapshot.child("phoneNumber").value as? String)
                val avatarUrl = snapshot.child("avatarUrl").value as? String
                avatarUrl?.let {
                    Glide.with(this@PsychologistProfileActivity)
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
