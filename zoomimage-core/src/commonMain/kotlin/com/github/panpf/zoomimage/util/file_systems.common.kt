/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.util

import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path

/**
 * Get default file system.
 */
internal expect fun defaultFileSystem(): FileSystem

/**
 * Create a new empty file.
 */
internal fun FileSystem.createFile(file: Path, mustCreate: Boolean = false) {
    if (mustCreate) {
        sink(file, mustCreate = true).closeQuietly()
    } else if (!exists(file)) {
        sink(file).closeQuietly()
    }
}

/**
 * Tolerant delete, try to clear as many files as possible even after a failure.
 */
internal fun FileSystem.deleteContents(directory: Path) {
    var exception: IOException? = null
    val files = try {
        list(directory)
    } catch (_: FileNotFoundException) {
        return
    }
    for (file in files) {
        try {
            if (metadata(file).isDirectory) {
                deleteContents(file)
            }
            delete(file)
        } catch (e: IOException) {
            if (exception == null) {
                exception = e
            }
        }
    }
    if (exception != null) {
        throw exception
    }
}