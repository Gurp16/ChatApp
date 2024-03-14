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

    private var userReference: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Image")

        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    binding.usernamesettings.text = user?.getUsername()
                    val profileImageUrl = user?.getProfile()
                    if (!profileImageUrl.isNullOrEmpty()) {
                        if (context != null) {
                            Picasso.get().load(profileImageUrl).into(binding.profileImage)
                            Picasso.get().load(profileImageUrl).into(binding.coverImage)
                        }
                    } else {
                        // Handle case when profile image URL is empty or null
                        // For example, you can set a default image or show an error message
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })

        binding.profileImage.setOnClickListener{
            pickImage()
        }

        binding.coverImage.setOnClickListener{
            coverChecker = "cover"
            pickImage()
        }

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

    private fun setSocialLinks() {

        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context!!, com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if(socialChecker == "website")
        {
            builder.setTitle("Write Url")
        }
        else{
            builder.setTitle("write usernmae")
        }

        val editText = EditText(context)

        if(socialChecker == "website")
        {
            editText.hint = "e.g www.google.com"
        }
        else{
            editText.hint = "e.g Gurpreet"
        }
        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener{
            dialog, which ->
            val str = editText.text.toString()

            if(str == ""){
                Toast.makeText(context, "Please write someting....", Toast.LENGTH_SHORT).show()
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

    private fun saveSociallink(str: String) {


        val mapSocial= HashMap<String, Any>()
//        mapSocial["cover"] = url
//        userReference!!.updateChildren(mapCoverImg)

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
                Toast.makeText(context, "saved Sucessfully", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            Toast.makeText(context, "Uploading.....", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

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
