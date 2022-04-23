package br.com.babysitter.data.repositories

import br.com.babysitter.data.Configuration
import br.com.babysitter.data.OperationType
import br.com.babysitter.data.TypeCamera

class ConfigRepository {

    fun getConfiguration():Configuration {
        return Configuration(OperationType.TRANSMITTER, TypeCamera.FRONT,
        0.0f, true, true)
    }
}