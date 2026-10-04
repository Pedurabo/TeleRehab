package com.signaldesk.telerehab.core.id

import java.util.UUID
import javax.inject.Inject

class UuidAppIdGenerator @Inject constructor() : AppIdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
