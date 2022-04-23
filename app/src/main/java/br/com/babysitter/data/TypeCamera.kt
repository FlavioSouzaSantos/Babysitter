package br.com.babysitter.data

enum class TypeCamera(val value:Int) {
    BACK(0), FRONT(1);

    fun exchange():TypeCamera{
        return when(this){
            BACK -> FRONT
            FRONT -> BACK
        }
    }
}