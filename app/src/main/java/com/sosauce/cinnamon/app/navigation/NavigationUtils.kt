package com.sosauce.cinnamon.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.navigate(element: NavKey) {
    remove(element)
    add(element)
}

fun NavBackStack<NavKey>.navigateBack() = navigateBack(Screen.Conversations)

fun NavBackStack<NavKey>.navigateBack(fallback: NavKey) {
    if (size == 1) {
        add(fallback)
    } else {
        removeLastOrNull()
    }
}