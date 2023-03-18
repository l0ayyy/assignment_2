package com.example.pdf_assignment

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UpActivity : AppCompatActivity() {
        private val REQUEST_CODE_SELECT_PDF = 1

        private lateinit var uploadBtn: Button
        private lateinit var viewPDF_btn: Button
        private lateinit var pdfName: EditText

        private lateinit var storageReference: StorageReference
        private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_up)
        uploadBtn = findViewById(R.id.upload_btn)
        viewPDF_btn = findViewById(R.id.viewPDF_btn)
        pdfName = findViewById(R.id.name)

        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Uploads")

        uploadBtn.setOnClickListener {
            selectPdfFile()
        }

        viewPDF_btn.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Select PDF File"), REQUEST_CODE_SELECT_PDF)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PDF && resultCode == RESULT_OK && data != null) {
            uploadPdfFile(data.data)
        }
    }
    private fun uploadPdfFile(pdfUri: Uri?) {
        if (pdfUri == null) {
            Toast.makeText(this, "Please select a PDF file to upload", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfNameStr = pdfName.text.toString().trim()
        if (TextUtils.isEmpty(pdfNameStr)) {
            pdfName.error = "PDF Name is required"
            pdfName.requestFocus()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val pdfRef = storageReference.child("Uploads/" + System.currentTimeMillis() + ".pdf")

        val uploadTask = pdfRef.putFile(pdfUri)
        uploadTask.addOnCompleteListener(this) { task ->
            progressDialog.dismiss()
        }
        uploadTask.addOnSuccessListener { taskSnapshot ->
            pdfRef.downloadUrl.addOnSuccessListener { uri ->
                val pdf = pdfClass(pdfNameStr, uri.toString())
                val uploadId = databaseReference.push().key
                databaseReference.child(uploadId!!).setValue(pdf).addOnSuccessListener {
                    pdfName.setText("")
                    pdfName.clearFocus()
                    Toast.makeText(this@UpActivity, "File uploaded successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@UpActivity,
                        "Error uploading file: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded: " + progress.toInt() + "%")
        }
    }
}