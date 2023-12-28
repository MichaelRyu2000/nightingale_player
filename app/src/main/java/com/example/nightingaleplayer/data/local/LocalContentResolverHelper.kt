package com.example.nightingaleplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.nightingaleplayer.data.local.model.Audio
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/*
This file is meant to retrieve the metadata of LOCAL files.

TODO: Consider implementing an interface for ContentResolverHelper to retrieve metadata from anywhere, not just local files specifically
 */

// Using Hilt DI
// @ApplicationContext annotation to not need a provider for the application context
class LocalContentResolverHelper @Inject
constructor(@ApplicationContext val context: Context) {

    // Using MediaStore to create a query for what metadata we wish to retrieve
    private val audioColumns: Array<String> = arrayOf(
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DURATION
        )

    private var lCursor: Cursor? = null;

    // the purpose of "?" at the end of the sql statement is that first, it is a parameterized query
    // it means that program will fill it in, rather than myself hardcoding it, and is an example of dynamic SQL
    // it also prevents SQL injections
    private var checkMusic: String? =
        "${MediaStore.Audio.AudioColumns.IS_MUSIC} = ?"

    private var selectionArg = arrayOf("1")

    // sorting by display names
    // TODO: Consider adding functionality to filter, figure out if making the sortOrder a function and running the query is efficient or not
    private val sortOrder = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} ASC"

    @WorkerThread
    fun getAudioData(): List<Audio> {
        return getCursorData()
    }

    private fun getCursorData(): MutableList<Audio> {
        val audioList = mutableListOf<Audio>()

        // look at the 2nd required parameter for query and look at the use of (out), related to Java wildcards and how Kotlin handles them
        // same with the exclamation mark: platform types
        lCursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            audioColumns,
            checkMusic,
            selectionArg,
            sortOrder
        )

        lCursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)

            cursor.apply {
                if (count == 0) {
                    Log.e("Cursor", "getCursorData: Data is empty")
                } else {
                    while(cursor.moveToNext()) {
                        val id = getLong(idColumn)
                        val displayName = getString(displayNameColumn)
                        val artist = getString(artistColumn)
                        val title = getString(titleColumn)
                        val data = getString(dataColumn)
                        val duration = getInt(durationColumn)
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        audioList.add(
                            Audio(
                                uri, displayName, id, artist, title, data, duration
                            )
                        )
                    }
                }
            }
        }

        return audioList
    }
}