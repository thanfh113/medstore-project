package com.example.nhathuoc.data.remote

/**
 * Normalizes backend-local file URLs for real Android devices.
 *
 * Files stored by the BE under /static/uploads may be saved in DB with a host
 * that only works from the uploader machine, for example localhost from Desktop.
 * The app should always open those files through ApiConstants.BASE_URL.
 */
object BackendUrlResolver {
    fun resolveFileUrl(rawUrl: String?): String {
        val url = rawUrl?.trim().orEmpty()
        if (url.isBlank()) return url

        val baseUrl = ApiConstants.BASE_URL.trimEnd('/')
        val normalized = url.replace('\\', '/')

        val staticMarker = "/static/uploads/"
        val staticIndex = normalized.indexOf(staticMarker, ignoreCase = true)
        if (staticIndex >= 0) {
            return baseUrl + normalized.substring(staticIndex)
        }

        if (normalized.startsWith("static/uploads/", ignoreCase = true)) {
            return "$baseUrl/$normalized"
        }

        if (normalized.startsWith("/")) {
            return baseUrl + normalized
        }

        return normalized
    }
}
