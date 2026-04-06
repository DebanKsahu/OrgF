package com.github.orgf.core.agent.models

import com.github.orgf.utils.enums.PromptCategory

data class PromptUpdateDetail(
	val promptId: Long,
	val category: PromptCategory,
	val prompt: String
)
