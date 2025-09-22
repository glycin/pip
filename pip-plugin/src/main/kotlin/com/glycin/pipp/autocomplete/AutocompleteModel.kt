package com.glycin.pipp.autocomplete

data class AutocompleteRequest(
    val singleLineText: String,
)

data class AutocompleteResponse(
    val autocomplete: String,
)