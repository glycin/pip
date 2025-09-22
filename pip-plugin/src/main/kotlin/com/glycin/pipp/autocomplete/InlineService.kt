package com.glycin.pipp.autocomplete

import com.glycin.pipp.http.PipRestClient
import com.intellij.openapi.components.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Service(Service.Level.PROJECT)
class InlineService() {
    @Volatile
    private var canAutocomplete = true

    suspend fun autocomplete(line: String): String? {
        if (!canAutocomplete) return null

        canAutocomplete = false
        try {
            val result: String? = withContext(Dispatchers.IO) {
                val request = AutocompleteRequest(line)
                PipRestClient.doAutoComplete(request)?.autocomplete
            }
            return result
        } finally {
            canAutocomplete = true
        }
    }
}