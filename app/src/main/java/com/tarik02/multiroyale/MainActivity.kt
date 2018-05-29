package com.tarik02.multiroyale

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.main_activity.*
import java.io.*

class MainActivity : AppCompatActivity(), MainProfilesAdapter.Listener {
    val adapter = MainProfilesAdapter(this)
    lateinit var script: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)

        script = File(filesDir, "clashroyale-1.0.0.sh")
        if (!script.exists()) {
            val out = FileOutputStream(script)
            val `in` = assets.open("clashroyale.sh")
            val temp = ByteArray(1024)

            while (true) {
                val i = `in`.read(temp)
                if (i <= 0) break

                out.write(temp, 0, i)
            }

            out.close()
            `in`.close()
        }

        profiles.isLongClickable = true
        profiles.adapter = adapter
        profiles.layoutManager = LinearLayoutManager(this)

        loadProfiles()

        profile_add.setOnClickListener { _ ->
            AlertDialog.Builder(this).apply {
                setTitle(R.string.enter_new_profile_name)

                val input = EditText(this@MainActivity)
                input.inputType = InputType.TYPE_CLASS_TEXT
                setView(input)

                setPositiveButton(R.string.ok) { _, _ ->
                    val name = input.text.toString()
                    val systemName = systemProfileName(name)

                    if (systemName.isEmpty()) {
                        Toast.makeText(this@MainActivity, R.string.wrong_profile_name, Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    if (adapter.items.find { systemProfileName(it.name) == systemName } != null) {
                        Toast.makeText(this@MainActivity, R.string.profile_already_exists, Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    adapter.items.add(Profile(name))
                    adapter.notifyItemInserted(adapter.items.size)

                    saveProfiles()
                }

                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }

                show()
            }
        }
    }

    override fun onClick(profile: Profile) {
        ProgressDialog.show(this, "Applying profile...", "Applying profile...", true, false)

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val task = StartProfileTask(script.absolutePath, systemProfileName(profile.name))
        task.execute()
    }

    private fun systemProfileName(name: String) = name.replace("[^a-zA-Z0-9_\\-.]".toRegex(), "")

    override fun onLongClick(profile: Profile) {
        val index = adapter.items.indexOf(profile)
        if (index != -1) {
            AlertDialog.Builder(this).apply {
                setMessage(R.string.profile_remove_sure)
                setPositiveButton(R.string.yes) { _, _ ->
                    adapter.items.removeAt(index)
                    adapter.notifyItemRemoved(index)

                    saveProfiles()
                }
                setNegativeButton(R.string.no) { _, _ -> }
            }.show()
        }
    }

    fun loadProfiles() {
        adapter.items.clear()

        try {
            FileInputStream(File(filesDir, "profiles.txt")).use { stream ->
                val reader = InputStreamReader(stream)
                reader.readLines().forEach { adapter.items.add(Profile(it)) }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        adapter.notifyDataSetChanged()
    }

    fun saveProfiles() {
        try {
            FileOutputStream(File(filesDir, "profiles.txt")).use { stream ->
                val writer = OutputStreamWriter(stream)
                adapter.items.forEach { writer.appendln(it.name) }
                writer.close()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
