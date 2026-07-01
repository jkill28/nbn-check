package com.example.nbnfiberchecker.data

import kotlinx.serialization.Serializable

@Serializable
data class NbnResponse(
    val addressDetail: AddressDetail? = null
)

@Serializable
data class AddressDetail(
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
