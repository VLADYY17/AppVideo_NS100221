package sv.edu.ufg.fis.amb.appvideo2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import sv.edu.ufg.fis.amb.appvideo2.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_VIDEO_CAPTURE = 1
    private val REQUEST_PERMISSIONS = 100
    private var videoPath: String? = null
    private lateinit var videoDirectory: File
    private lateinit var listVideos: ListView
    private lateinit var videoListAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRecordVideo: Button = findViewById(R.id.btn_record_video)
        val btnViewVideos: Button = findViewById(R.id.btn_view_videos)
        val btnPlayLastVideo: Button = findViewById(R.id.btn_play_last_video)
        listVideos = findViewById(R.id.list_videos)

        // Directorio donde se guardarán los videos
        videoDirectory = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir

        btnRecordVideo.setOnClickListener { checkPermissionsAndRecord() }
        btnViewVideos.setOnClickListener { displaySavedVideos() }
        btnPlayLastVideo.setOnClickListener { playLastVideo() }
    }

    private fun checkPermissionsAndRecord() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_PERMISSIONS)
        } else {
            recordVideo()
        }
    }

    private fun recordVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val videoFile = createVideoFile()
        videoPath = videoFile.absolutePath

        val videoUri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            videoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
    }

    private fun createVideoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "VIDEO_$timestamp.mp4"
        return File(videoDirectory, fileName)
    }

    private fun displaySavedVideos() {
        val videos = videoDirectory.listFiles()?.map { it.name } ?: listOf()
        videoListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, videos)
        listVideos.adapter = videoListAdapter
        listVideos.visibility = View.VISIBLE

        listVideos.setOnItemClickListener { _, _, position, _ ->
            playVideo(videoDirectory.listFiles()?.get(position)?.absolutePath)
        }
    }

    private fun playLastVideo() {
        val videos = videoDirectory.listFiles()
        if (videos != null && videos.isNotEmpty()) {
            playVideo(videos.last().absolutePath)
        } else {
            Toast.makeText(this, "No hay videos grabados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playVideo(path: String?) {
        if (path != null) {
            val videoView = VideoView(this)
            setContentView(videoView)
            videoView.setVideoURI(Uri.parse(path))
            videoView.setOnPreparedListener { mediaPlayer -> mediaPlayer.isLooping = true }
            videoView.start()
        } else {
            Toast.makeText(this, "Video no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordVideo()
        } else {
            Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Video guardado en: $videoPath", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al grabar el video", Toast.LENGTH_SHORT).show()
        }
    }
}
