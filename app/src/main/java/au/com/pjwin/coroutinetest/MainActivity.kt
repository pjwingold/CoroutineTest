package au.com.pjwin.coroutinetest

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    val TAG = MainActivity::class.simpleName
    val job = Job() //+ Dispatchers.Main
    val lock = Object()
    var count = 0

    override val coroutineContext: CoroutineContext
        get() = job //+ Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_threads.setOnClickListener {
            testManyThread()
        }

        btn_coroutines.setOnClickListener { testManyCoroutine() }

        btn_launch_await.setOnClickListener { launchAwait() }

        btn_launch_concur.setOnClickListener { launchConcurrent() }

        btn_runbloacking.setOnClickListener { testRunBlocking() }

        btn_non_bloacking.setOnClickListener { testNonBlocking() }
    }

    private fun testManyCoroutine() {
        for (i in 1..100000) {
            launch {}
        }
    }

    private fun testManyThread() {
        for (i in 1..100000) {
            Thread(Runnable {
                synchronized(lock) {
                    count++
                    Log.d(TAG, "Thread count $count" + Thread.currentThread())
                }
                SystemClock.sleep(100)
            }).start()
        }
    }

    private fun launchAwait() {
        launch {
            Log.d(TAG, "launch " + Thread.currentThread())
            val data = async {
                getData()
            }.await()

            val deferred = testSuspend()

            val result = async(Dispatchers.Main) {
                computeResult()
            }.await()

            val combine = "combine: $data $result $deferred" + Thread.currentThread()
            Log.d(TAG, combine)
            //result_txt.text = combine
            withContext(Dispatchers.Main) {
                result_txt.text = combine
            }
        }
        Log.d(TAG, "after launch")
    }

    private fun launchConcurrent() {
        launch(Dispatchers.IO) {
            Log.d(TAG, "launchConcurrent " + Thread.currentThread())

            //val deferred = testSuspend()

            val work1 = async {
                getData()
            }

            val work2 = async {
                computeResult()
            }

            val asyncWork = asyncParallel()
            val launchWork = launchParallel()

            val work3 = deferredResult()
            val deferred = work3.await()

            val data = work1.await()
            val compute = work2.await()
            val para = asyncWork.await()

            val combine = "combine: $data $compute $deferred $para " + Thread.currentThread()
            Log.d(TAG, combine)

            withContext(Dispatchers.Main) {
                result_txt.text = combine
            }
        }
    }

    private fun testRunBlocking() {

        runBlocking {
            Log.d(TAG, " testRunBlocking start ")
            SystemClock.sleep(1000)
            Log.d(TAG, " testRunBlocking ${Thread.currentThread()}")
        }
        Log.d(TAG, " testRunBlocking done")
    }

    private fun testNonBlocking() {
        launch {
            Log.d(TAG, " testLaunch start ")
            SystemClock.sleep(1000)
            Log.d(TAG, " testLaunch ${Thread.currentThread()}")
        }
        Log.d(TAG, " testLaunch 1")
    }

    private fun getData(): String {
        Log.d(TAG, "getData 1 ${this.coroutineContext}")
        SystemClock.sleep(3000)
        Log.d(TAG, "getData " + Thread.currentThread())
        return "getData"
    }

    private fun computeResult(): Int {
        Log.d(TAG, "computeResult 1 ${this.coroutineContext}")
        SystemClock.sleep(1000)
        Log.d(TAG, "computeResult " + Thread.currentThread())
        return 100
    }

    private fun deferredResult(): Deferred<Int> {
        Log.d(TAG, "deferred computeResult 1 ${this.coroutineContext}")
        SystemClock.sleep(2000)
        Log.d(TAG, "deferred computeResult " + Thread.currentThread())
        return CompletableDeferred(200)
    }

    private suspend fun testSuspend() =
            deferredResult().await()

    private fun asyncParallel() =
            async {
                Log.d(TAG, "parallel 1 ${this.coroutineContext}")
                SystemClock.sleep(3000)
                Log.d(TAG, "parallel " + Thread.currentThread())
                "Parallel"
            }

    private fun launchParallel() =
            launch {
                Log.d(TAG, "parallel launch ${this.coroutineContext}")
                SystemClock.sleep(4000)
                Log.d(TAG, "parallel launch " + Thread.currentThread())
            }
}
