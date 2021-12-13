package com.example.imagepreviewertc

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.imagepreviewertc.databinding.ActivityMainBinding
import android.graphics.BitmapFactory

import android.R.attr.data
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import iamutkarshtiwari.github.io.ananas.editimage.EditImageActivity
import iamutkarshtiwari.github.io.ananas.editimage.ImageEditorIntentBuilder
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import android.provider.MediaStore.Images
import com.example.imagepreviewertc.utils.generateEditFile
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var requestPermissionCamera: ActivityResultLauncher<String>
    private lateinit var requestPermissionGallery: ActivityResultLauncher<String>
    private var bitMap: Bitmap? = null
    private var picturePath: String? = null
    private lateinit var actionPhoto: ActivityResultLauncher<Intent>
    private lateinit var actionCamera: ActivityResultLauncher<Intent>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        getActionPhoto()
        getCameraAction()
        val getActionGallery =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // get bitmap data for image
                val selectedImage: Uri? = result.data?.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                if (selectedImage != null) {
                    val cursor: Cursor? =
                        contentResolver.query(selectedImage, filePathColumn, null, null, null)
                    if (cursor != null) {
                        cursor.moveToFirst()
                        val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                        val picturePath: String = cursor.getString(columnIndex)
                        val bitmap = BitmapFactory.decodeFile(picturePath)
                        this.picturePath = picturePath
                        binding.photoPreviewer.setImageBitmap(bitmap)
                        bitMap = bitmap
                        cursor.close()
                    }
                }
            }
        //get camera permission
        requestPermissionCamera =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {

                if (it) {
                    //start an intent to capture image
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    //start the result
                    //check if the task can be performed or not
                    if (intent.resolveActivity(packageManager) != null) {
                        //startActivityForResult(intent, our_request_code)
                        actionCamera.launch(intent)
                    }
                } else {
                    Toast.makeText(applicationContext, "Permission not granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        requestPermissionGallery =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {

                if (it) {
                    //start an intent to get photo from gallery

                    val intent = Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    //start the result
                    //check if the task can be performed or not
                    if (intent.resolveActivity(packageManager) != null) {
                        //startActivityForResult(intent, our_request_code)
                        getActionGallery.launch(intent)
                    }
                } else {
                    Toast.makeText(applicationContext, "Permission not granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }


        binding.cameraBtn.setOnClickListener {
            requestPermissionCamera.launch(Manifest.permission.CAMERA)

        }
        binding.galleryBtn.setOnClickListener {
            requestPermissionGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        binding.editBtn.setOnClickListener {
            picturePath?.let {
                try {
                    val intent =
                        ImageEditorIntentBuilder(this, it, generateEditFile(this)?.absolutePath)
                            .withRotateFeature()
                            .withCropFeature()
                            .forcePortrait(true)
                            .setSupportActionBarVisibility(false)
                            .withEditorTitle("Edit")
                            .build()
                    EditImageActivity.start(actionPhoto, intent, this)
                } catch (e: Exception) {
                }


            }
        }
    }



    private fun getCameraAction(){
        actionCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // get bitmap data for image

            Log.d("MainActivity", "picture path: $picturePath")
            val bitmap = result?.data?.extras?.get("data") as Bitmap
            val tempUri = getImageUri(applicationContext, bitmap)
            picturePath = getRealPathFromURI(tempUri)
            binding.photoPreviewer.setImageBitmap(bitmap)
            bitMap = bitmap
        }
    }

    private fun getActionPhoto(){
        actionPhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // get bitmap data for image
            val newFilePath = result.data?.getStringExtra(ImageEditorIntentBuilder.OUTPUT_PATH)
            val isImageEdit =
                result.data?.getBooleanExtra(EditImageActivity.IS_IMAGE_EDITED, false)
            val bitmap = BitmapFactory.decodeFile(newFilePath)
            binding.photoPreviewer.setImageBitmap(bitmap)
            val path = Images.Media.insertImage(applicationContext.contentResolver, bitmap, System.currentTimeMillis().toString(), null)
            Toast.makeText(this, "Photo Saved in Gallery", Toast.LENGTH_SHORT).show()

        }
    }


    private fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (contentResolver != null) {
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val path = Images.Media.insertImage(inContext.contentResolver, inImage, System.currentTimeMillis().toString(), null)
        return Uri.parse(path)
    }


}