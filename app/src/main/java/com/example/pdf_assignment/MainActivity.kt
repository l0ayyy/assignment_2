package com.example.pdf_assignment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.pdf_assignment.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var listView: ListView
    private lateinit var databaseReference: DatabaseReference
    private var uploads: MutableList<pdfClass> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floatingActionButton = binding.floatBtn

        listView = findViewById(R.id.listview)
        viewAllFiles()

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, i, l ->
            val pdfupload = uploads[i]

            val intent = Intent(Intent.ACTION_VIEW)
            intent.type = "application/pdf"
            intent.data = Uri.parse(pdfupload.pdfUrl)
            startActivity(intent)
        }

        floatingActionButton.setOnClickListener {
            val intent = Intent(applicationContext, UpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun viewAllFiles() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Uploads")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                uploads.clear()
                for (postSnapshot in snapshot.children) {
                    val pdfClass = postSnapshot.getValue(pdfClass::class.java)
                    pdfClass?.let { uploads.add(it) }
                }

                val uploadsArray = uploads.map { it.pdfName }.toTypedArray()

                val adapter = object : ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1, uploadsArray) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        val text = view.findViewById<TextView>(android.R.id.text1)
                        text.setTextColor(Color.parseColor("#FF9800"))
                        text.setTextSize(22f)
                        return view
                    }
                }
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }
}