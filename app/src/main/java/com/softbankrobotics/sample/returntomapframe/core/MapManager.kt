/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.returntomapframe.core

import android.content.Context
import android.util.Log
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.ExplorationMap
import com.aldebaran.qi.sdk.`object`.streamablebuffer.StreamableBuffer
import com.aldebaran.qi.sdk.`object`.streamablebuffer.StreamableBufferFactory
import com.aldebaran.qi.sdk.builder.ExplorationMapBuilder
import com.aldebaran.qi.sdk.util.copyToStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

/**
 * Manager that provides and saves the [ExplorationMap].
 */
object MapManager {

    private const val TAG = "MapManager"
    private const val MAP_FILENAME = "map.txt"

    // The cached map.
    private var explorationMap: ExplorationMap? = null

    /**
     * Save the specified map to a file.
     *
     * @param context the context
     * @param map     the map to save
     * @return A [Future] wrapping the operation.
     */
    @JvmStatic
    fun saveMap(context: Context, map: ExplorationMap): Future<Void> {
        // Cache the map.
        this.explorationMap = map

        // Serialize the map and write it to the file.
        Log.d(TAG, "Serializing map...")
        return map.async().serializeAsStreamableBuffer()
                .andThenConsume { streamableBuffer: StreamableBuffer ->
                    Log.d(TAG, "Map serialized successfully")
                    writeMapToFile(context, streamableBuffer)
                }
    }

    /**
     * Indicate if there is an existing map or not.
     *
     * @param context the context
     * @return `true` if there is a map, `false` otherwise.
     */
    @JvmStatic
    fun hasMap(context: Context): Boolean {
        // If cached map available, return true directly.
        if (this.explorationMap != null) {
            return true
        }

        // Check if the map file exists.
        val mapFile = File(context.filesDir, MAP_FILENAME)
        return mapFile.exists()
    }

    /**
     * Provide the map.
     *
     * @param qiContext the qiContext
     * @return A [Future] wrapping the operation.
     */
    @JvmStatic
    fun retrieveMap(qiContext: QiContext): Future<ExplorationMap> {
        // If cached map available, return it directly.
        val explorationMap = this.explorationMap
        return if (explorationMap != null) {
            Future.of(explorationMap)
        } else {
            Future.of(qiContext)
                    .andThenApply { context ->
                        val mapFile = File(context.filesDir, MAP_FILENAME)
                        StreamableBufferFactory.fromFile(mapFile)
                    }
                    .andThenApply {
                        ExplorationMapBuilder.with(qiContext)
                                .withStreamableBuffer(it)
                                .build()
                                // Cache the map.
                                .also { explorationMap -> this.explorationMap = explorationMap }
                    }
        }

        // Read the file and create the ExplorationMap.
    }

    private fun writeMapToFile(context: Context, streamableBuffer: StreamableBuffer) {
        val mapFile = File(context.filesDir, MAP_FILENAME)
        mapFile.outputStream().use {
            streamableBuffer.copyToStream(it)
        }
    }

    private fun StreamableBufferFactory.fromFile(file: File): StreamableBuffer {
        return fromFunction(file.length()) { offset, size ->
            RandomAccessFile(file, "r").use {
                val byteArray = ByteArray(size.toInt())
                it.seek(offset)
                it.read(byteArray)

                ByteBuffer.wrap(byteArray)
            }
        }
    }
}
