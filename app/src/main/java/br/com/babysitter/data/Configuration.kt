package br.com.babysitter.data

data class Configuration(val operationType:OperationType, val typeCamera: TypeCamera,
                         val zoom:Float, val audioEnable:Boolean, val flashEnable:Boolean)