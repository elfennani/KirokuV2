package com.elfennani.kiroku.utils

fun Double.clean(): String =
    if (this % 1 == 0.0) this.toInt().toString() else this.toString()