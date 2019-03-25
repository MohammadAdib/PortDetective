package genius.mohammad.portdetective

import android.text.Html
import android.text.Spanned

import java.io.InputStream
import java.net.Socket

class PortScanner(stream: InputStream) {

    internal var progress = 0
    internal var currentPort = -2
    private var consoleOutput: String? = null
    internal var running = true
    private var timedOut = false
    private val portDescriptor: PortDescriptor = PortDescriptor(stream)

    fun scan(ip: String, portStart: Int, portEnd: Int) {
        stop()
        consoleOutput = "No open ports found..."
        val r = Runnable {
            running = true
            for (port in portStart until portEnd + 1) {
                if (!timedOut && running) {
                    var open = false
                    try {
                        val s = Socket(ip, port)
                        open = true
                        s.close()
                    } catch (e: Exception) {
                    }

                    if (open) {
                        if (consoleOutput == "No open ports found...")
                            consoleOutput = ""
                        consoleOutput += (if (consoleOutput!!.isEmpty()) "" else "<br><br>") + "<b>Port " + port + "</b> is open.<br>" + portDescriptor.getDescription(port)
                    }
                    progress = ((port - portStart) / (1.0 * (portEnd - portStart)) * 100.0 + 0.5).toInt()
                    currentPort = port
                } else {
                    break
                }
            }
            if (!timedOut) {
                currentPort = -1
            }
            running = false
        }
        val t = Thread(r)
        t.start()
    }

    fun getOutput(): Spanned {
        return Html.fromHtml(consoleOutput!!.trim { it <= ' ' })
    }

    fun timeOut() {
        timedOut = true
        running = false
        currentPort = -2
    }

    fun stop() {
        running = false
    }
}
