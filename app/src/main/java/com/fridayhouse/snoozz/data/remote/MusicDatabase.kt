package com.fridayhouse.snoozz.data.remote

import com.fridayhouse.snoozz.data.entities.sound
import com.fridayhouse.snoozz.others.Constants.SOUND_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val soundCollection = firestore.collection(SOUND_COLLECTION)

    suspend fun getAllSounds(): List<sound> {
        return try {
            soundCollection.get().await().toObjects(sound::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}