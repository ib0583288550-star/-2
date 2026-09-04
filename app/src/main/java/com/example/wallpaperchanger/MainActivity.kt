package com.example.wallpaperchanger

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {

    private val intervalOptions = listOf(
        "15 דקות" to 15L,
        "שעה" to 60L,
        "6 שעות" to 360L,
        "יום" to 1440L,
        "שבוע" to 10080L
    )

    private lateinit var recycler: RecyclerView
    private lateinit var spinnerInterval: Spinner
    private lateinit var switchAutoRotate: Switch
    private lateinit var imagePreviewLarge: ImageView
    private var selectedItem: WallpaperItem? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) addPhotoUri(uri)
    }

    private val pickDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) addPhotoUri(uri)
    }

    private fun addPhotoUri(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }

        WallpaperRepository.addUserPhoto(this, uri)
        refreshGrid()
        Toast.makeText(this, "התמונה נוספה לרשימת הטפטים", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recyclerWallpapers)
        recycler.layoutManager = GridLayoutManager(this, 3)

        imagePreviewLarge = findViewById(R.id.imagePreviewLarge)

        spinnerInterval = findViewById(R.id.spinnerInterval)
        spinnerInterval.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, intervalOptions.map { it.first }
        )

        switchAutoRotate = findViewById(R.id.switchAutoRotate)
        switchAutoRotate.isChecked = WallpaperRepository.isAutoRotateEnabled(this)

        val savedMinutes = WallpaperRepository.getIntervalMinutes(this)
        val savedPosition = intervalOptions.indexOfFirst { it.second == savedMinutes }
        if (savedPosition >= 0) spinnerInterval.setSelection(savedPosition)

        findViewById<Button>(R.id.btnAddPhoto).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btnAddFromFiles).setOnClickListener {
            pickDocumentLauncher.launch(arrayOf("image/*"))
        }

        findViewById<Button>(R.id.btnChangeNow).setOnClickListener {
            applySelectedOrRandom()
        }

        switchAutoRotate.setOnCheckedChangeListener { _, isChecked ->
            WallpaperRepository.setAutoRotateEnabled(this, isChecked)
            val minutes = intervalOptions[spinnerInterval.selectedItemPosition].second
            if (isChecked) {
                RotationScheduler.schedule(this, minutes)
                Toast.makeText(this, "החלפה אוטומטית הופעלה", Toast.LENGTH_SHORT).show()
            } else {
                RotationScheduler.cancel(this)
            }
        }

        spinnerInterval.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val minutes = intervalOptions[position].second
                WallpaperRepository.setIntervalMinutes(this@MainActivity, minutes)
                if (switchAutoRotate.isChecked) {
                    RotationScheduler.schedule(this@MainActivity, minutes)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        refreshGrid()
    }

    private fun refreshGrid() {
        val all = WallpaperRepository.getAllWallpapers(this)
        recycler.adapter = WallpaperAdapter(
            items = all,
            onClick = { item, _ ->
                selectedItem = item
                showInPreview(item)
            },
            onLongClickDelete = { item -> confirmDeletePhoto(item) }
        )
        if (selectedItem == null) {
            all.firstOrNull()?.let { showInPreview(it) }
        }
    }

    private fun showInPreview(item: WallpaperItem) {
        when (item) {
            is WallpaperItem.Builtin -> imagePreviewLarge.setImageResource(item.drawableRes)
            is WallpaperItem.UserPhoto -> imagePreviewLarge.setImageURI(item.uri)
        }
    }

    private fun confirmDeletePhoto(item: WallpaperItem.UserPhoto) {
        AlertDialog.Builder(this)
            .setTitle("מחיקת תמונה")
            .setMessage("למחוק את התמונה הזו מרשימת הטפטים?")
            .setPositiveButton("מחק") { _, _ ->
                WallpaperRepository.removeUserPhoto(this, item.uri)
                if (selectedItem == item) {
                    selectedItem = null
                }
                refreshGrid()
                Toast.makeText(this, "התמונה נמחקה", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun applySelectedOrRandom() {
        val item = selectedItem ?: WallpaperRepository.getAllWallpapers(this).randomOrNull()
        if (item == null) {
            Toast.makeText(this, "אין טפטים זמינים", Toast.LENGTH_SHORT).show()
            return
        }
        val success = WallpaperSetter.apply(this, item)
        val message = if (success) "הטפט הוחלף בהצלחה!" else "שגיאה בהחלפת הטפט"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
