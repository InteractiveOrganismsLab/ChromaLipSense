package com.example.lipsensor.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.core.ImageCapture
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lipsensor.databinding.FragmentGalleryBinding
import java.io.File
import java.util.concurrent.ExecutorService

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: ExecutorService
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}