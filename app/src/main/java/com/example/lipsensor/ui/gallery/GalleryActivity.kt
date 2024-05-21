package com.example.lipsensor.ui.gallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lipsensor.R

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GalleryFragment())
                .commitNow()
        }
    }
}