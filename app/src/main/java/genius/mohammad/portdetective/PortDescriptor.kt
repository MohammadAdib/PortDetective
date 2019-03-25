package genius.mohammad.portdetective

import java.io.IOException
import java.io.InputStream
import java.util.*

class PortDescriptor(stream: InputStream) {

    private val database: List<String>

    init {
        database = readDatabase(stream)
    }

    private fun readDatabase(stream: InputStream): List<String> {
        val contents = ArrayList<String>()
        try {
            contents.addAll(stream.bufferedReader().readLines())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return contents
    }

    fun getDescription(p: Int): String {
        val port = Integer.toString(p)
        var description = " - Description not available."
        for (s in database) {
            if (s.startsWith(port + " -")) {
                description = "	" + s.substring(s.indexOf(" -") + 1).trim { it <= ' ' }
            }
        }
        return description
    }
}
