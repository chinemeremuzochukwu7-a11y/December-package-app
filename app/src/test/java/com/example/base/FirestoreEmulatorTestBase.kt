package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import org.junit.Assume
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.net.InetSocketAddress
import java.net.Socket

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {

  protected lateinit var firestore: FirebaseFirestore
  protected lateinit var databaseId: String

  private fun isEmulatorReachable(): Boolean {
    return try {
      Socket().use { socket ->
        socket.connect(InetSocketAddress(EMULATOR_HOST, FIRESTORE_PORT), 250)
        true
      }
    } catch (_: Exception) {
      false
    }
  }

  @Before
  open fun setUpFirebase() {
    Assume.assumeTrue(
      "Skipping Firestore rule test because Firestore emulator is not running on $EMULATOR_HOST:$FIRESTORE_PORT",
      isEmulatorReachable()
    )
    val context = ApplicationProvider.getApplicationContext<Context>()
    val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
    databaseId = if (resId != 0) context.getString(resId) else "demo-database"

    val app = if (FirebaseApp.getApps(context).isEmpty()) {
      val newApp = FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("com.geegroup.holidaywish")
          .setProjectId(PROJECT_ID)
          .setApiKey("fake-api-key-for-emulator")
          .build()
      )
      val newFirestore = FirebaseFirestore.getInstance(newApp, databaseId)
      newFirestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
      newFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        .build()
      newApp
    } else {
      FirebaseApp.getInstance()
    }

    firestore = FirebaseFirestore.getInstance(app, databaseId)
    try {
      firestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
      firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        .build()
    } catch (_: Exception) {}
  }

  protected fun Throwable.unwrapFirestoreException(): FirebaseFirestoreException? {
    var current: Throwable? = this
    while (current != null) {
      if (current is FirebaseFirestoreException) return current
      current = current.cause
    }
    return null
  }

  protected companion object {
    const val EMULATOR_HOST = "127.0.0.1"
    const val FIRESTORE_PORT = 8085
    const val PROJECT_ID = "demo-no-project"
  }
}
