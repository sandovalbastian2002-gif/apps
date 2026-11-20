package com.focusgate.app.blocker

import java.util.concurrent.atomic.AtomicBoolean

/** Tracks whether the user currently holds an "unlocked" pass into Instagram.
 *
 *  The pass is granted exactly once a challenge session is completed successfully, and is
 *  consumed (locking the gate again) as soon as [InstagramAccessibilityService] observes the
 *  user leaving Instagram for anything other than FocusGate's own challenge screen — i.e.
 *  going back to Instagram after switching away always requires a fresh challenge, per spec.
 *
 *  A single process-wide object is enough here: the accessibility service and the challenge
 *  activity both run in FocusGate's own app process. */
object UnlockManager {
    private val unlocked = AtomicBoolean(false)

    fun isUnlocked(): Boolean = unlocked.get()

    fun grantUnlock() {
        unlocked.set(true)
    }

    fun consumeUnlock() {
        unlocked.set(false)
    }
}
