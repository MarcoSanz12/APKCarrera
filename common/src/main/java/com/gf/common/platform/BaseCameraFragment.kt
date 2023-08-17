package com.gf.common.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewbinding.ViewBinding
import com.gf.common.R
import com.gf.common.utils.Constants
import com.gf.common.utils.Constants.Camera.REQUEST_IMAGE_CAPTURE
import com.gf.common.utils.Constants.Camera.REQUEST_IMAGE_PICK
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


abstract class BaseCameraFragment<VB : ViewBinding> : BaseFragment<VB>() {

    lateinit var photoUri: Uri
    private var imageBitmap: Bitmap? = null
    private var camWidth = 0
    private var camHeight = 0

    protected fun uploadImage(height : Int, width: Int){
        camWidth = width
        camHeight = height
        requestPhotoPermission()
    }

    abstract fun onImageLoadedListener(img: Bitmap?)

    private fun requestPhotoPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Constants.Camera.CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            val pm = requireContext().packageManager
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
                uploadImage(pm)
            else
                Toast.makeText(context, getString(R.string.no_camera), Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",  //prefix
            ".jpg",  //suffix
            storageDir  //directory
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            photoUri = Uri.fromFile(this)
        }
    }

    private fun uploadImage(pm: PackageManager) {
        // Determine Uri of camera image to save.
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.gf.apkcarrera.provider",
                photoFile
            )
        }
        // Camera.
        val cameraIntents: MutableList<Intent> = ArrayList()

        val captureCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureCamera.resolveActivity(pm)
        captureCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraIntents.add(captureCamera)

        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        val chooserIntent = Intent.createChooser(galleryIntent, requireContext().getString(R.string.select_camera_method))
        val array = cameraIntents.toTypedArray()
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            array
        )
        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun getThumbnail(uri: Uri): Bitmap? {
        var input: InputStream
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = 2
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
        requireContext().contentResolver.openInputStream(uri).also { input = it!! }
        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
        input.close()
        return bitmap
    }

    @Throws(IOException::class)
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap? {
        val input = context.contentResolver.openInputStream(selectedImage)
        val ei: ExifInterface = input?.let { ExifInterface(it) }!!

        return when (ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val isCamera = data?.data == null
                val selectedImageUri = if (isCamera) {
                    photoUri
                } else {
                    data?.data
                }
                imageBitmap = rotateImageIfRequired(
                    requireContext(),
                    getThumbnail(selectedImageUri!!)!!,
                    selectedImageUri
                )!!


                imageBitmap = scaleBitmap(imageBitmap!!,camWidth,camHeight)
                onImageLoadedListener(imageBitmap!!)


            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                onImageLoadedListener(null)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_PICK)
            &&
            (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            )
            uploadImage(requireContext().packageManager)

    }

    companion object{

        fun scaleBitmap(src: Bitmap, width: Int, height:Int): Bitmap {

            val dimension = getSquareCropDimensionForBitmap(src)
            val bitmap = ThumbnailUtils.extractThumbnail(src, dimension, dimension)

            return Bitmap.createScaledBitmap(bitmap,width,height,false)
        }

       private fun getSquareCropDimensionForBitmap(bitmap : Bitmap) : Int
        {
            return Math.min(bitmap.width, bitmap.height);
        }
    }
}