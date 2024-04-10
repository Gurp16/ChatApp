package com.message.messengerapp.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.message.messengerapp.databinding.FragmentSettingsBinding
import com.message.messengerapp.ModelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var userReference: DatabaseReference? = null // Reference to the current user's database node
    private var firebaseUser: FirebaseUser? = null // Current Firebase user
    private val RequestCode = 438 // Request code for picking an image
    private var imageUri: Uri? = null // Uri of the selected image
    private var storageRef: StorageReference? = null // Reference to Firebase Storage for storing images
    private var coverChecker: String? = "" // Indicates whether the user is setting a cover image or profile image
    private var socialChecker: String? = "" // Indicates the type of social link being set (e.g., Facebook, Instagram)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Image")

        // Listen for changes to the current user's data in the database
        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (_binding != null) {
                        _binding!!.usernamesettings.text = user?.getUsername()
                        val profileImageUrl = user?.getProfile()
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Load the profile and cover images using Picasso if available
                            if (context != null) {
                                Picasso.get().load(profileImageUrl).into(_binding!!.profileImage)
                                Picasso.get().load(profileImageUrl).into(_binding!!.coverImage)
                            }
                        } else {
                            // Handle case when profile image URL is empty or null
                            // For example, you can set a default image or show an error message
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })

        // Set click listeners for profile and cover images to allow the user to change them
        binding.profileImage.setOnClickListener{
            pickImage()
        }

        binding.coverImage.setOnClickListener{
            coverChecker = "cover"
            pickImage()
        }

        // Set click listeners for social links buttons
        binding.setFacebook.setOnClickListener{
            socialChecker = "facebook"
            setSocialLinks()
        }
        binding.setInstagram.setOnClickListener{
            socialChecker = "instagram"
            setSocialLinks()
        }
        binding.setWebsite.setOnClickListener{
            socialChecker = "website"
            setSocialLinks()
        }

        return view
    }

    // Function to set social links (e.g., Facebook, Instagram, Website)
    private fun setSocialLinks() {

        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context!!, com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        // Determine the title based on the socialChecker value
        if(socialChecker == "website")
        {
            builder.setTitle("Write Url")
        }
        else{
            builder.setTitle("write username")
        }

        val editText = EditText(context)

        // Set hint text based on the socialChecker value
        if (socialChecker == "website")
        {
            editText.hint = "e.g www.google.com"
        }
        else {
            editText.hint = "e.g Gurpreet"
        }
        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener{
                dialog, which ->
            val str = editText.text.toString()

            if(str == ""){
                Toast.makeText(context, "Please write something....", Toast.LENGTH_SHORT).show()
            }
            else{
                saveSociallink(str)
            }
        })
        builder.setNegativeButton("Create", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        builder.show()
    }

    // Function to save social links to the database
    private fun saveSociallink(str: String) {

        val mapSocial= HashMap<String, Any>()

        when (socialChecker)
        {
            "facebook" ->{
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" ->{
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "website" ->{
                mapSocial["facebook"] = "https:///$str"
            }
        }

        userReference!!.updateChildren(mapSocial).addOnCompleteListener{
                task->
            if(task.isSuccessful)
            {
                Toast.makeText(context, "saved Successfully", Toast.LENGTH_SHORT).show()

            }
        }
    }

    // Function to pick an image from the device storage
    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    // Handle the result of picking an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            Toast.makeText(context, "Uploading.....", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

    // Function to upload the selected image to Firebase Storage
    private fun uploadImageToDatabase() {
        if (imageUri != null) {
            val progressBar = ProgressDialog(context)
            progressBar.setMessage("Image is uploading, please wait.....")
            progressBar.show()

            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            val uploadTask: UploadTask
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@continueWithTask fileRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover") {
                        val mapCoverImg = HashMap<String, Any>()
                        mapCoverImg["cover"] = url
                        userReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                    } else {
                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        userReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                } else {
                    // Handle unsuccessful upload
                    progressBar.dismiss()
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
