package com.salahguard.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Root Application class.
 * @HiltAndroidApp triggers Hilt's code generation, creating a dependency
 * container that is attached to the application lifecycle. Every other
 * Hilt-annotated class (ViewModels, Repositories, Workers) hangs off this.
 */
@HiltAndroidApp
class SalahGuardApp : Application()
