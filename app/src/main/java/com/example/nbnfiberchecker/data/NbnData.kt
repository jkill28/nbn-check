package com.example.nbnfiberchecker.data

import kotlinx.serialization.Serializable

@Serializable
data class NbnResponse(
    val addressDetail: AddressDetail? = null
)

@Serializable
data class AddressDetail(
    var id: String? = null, // Add ID field
    val reasonCode: String? = null,
    val altReasonCode: String? = null,
    val techChangeStatus: String? = null,
    val programType: String? = null,
    val targetEligibilityQuarter: String? = null,
    val techFlip: String? = null,
    val mduFibreEligibility: Boolean? = null,
    val techType: String? = null,
    val formattedAddress: String? = null
)

@Serializable
data class AddressSuggestionResponse(
    val suggestions: List<AddressSuggestion> = emptyList()
)

@Serializable
data class AddressSuggestion(
    val id: String,
    val formattedAddress: String
)
