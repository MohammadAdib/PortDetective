package genius.mohammad.portdetective

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    internal lateinit var mHandler: Handler
    internal lateinit var scanner: PortScanner
    internal var startTime = System.currentTimeMillis()
    internal var lastPortChecked = 0
    internal lateinit var scanDialog: AlertDialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var ip: String

    private val mUpdate = object : Runnable {
        override fun run() {
            val label = scanDialog.findViewById<TextView>(R.id.label)
            val console = scanDialog.findViewById<TextView>(R.id.portconsole)
            val progressBar = scanDialog.findViewById<ProgressBar>(R.id.progressBar)
            if (scanner.running) {
                if (lastPortChecked != scanner.currentPort) {
                    lastPortChecked = scanner.currentPort
                    startTime = System.currentTimeMillis()
                }
                // Add ...
                val dots = ((System.currentTimeMillis() - startTime) / 1000.0 + .5).toInt()
                var s = ""
                for (i in 0 until dots % 4) {
                    s += "."
                }
                progressBar!!.progress = scanner.progress
                console!!.text = scanner.getOutput()
                if (scanner.currentPort > -1) {
                    label!!.setText(String.format(getString(R.string.querying_port), scanner.progress, scanner.currentPort, s))
                } else if (scanner.currentPort == -2) {
                    label!!.setText(String.format(getString(R.string.resolving_host), s))
                    console.text = getString(R.string.host_not_reachable)
                }
                if (System.currentTimeMillis() - startTime > 7000 && lastPortChecked == scanner.currentPort) {
                    scanner.timeOut()
                    label!!.text = getString(R.string.timeout)
                    progressBar.progress = 100
                    startTime = System.currentTimeMillis()
                }
                // Repeat thread execution
                mHandler.post(this)
            } else if (scanner.currentPort == -1) {
                label!!.text = getString(R.string.scan_completed)
                console!!.text = scanner.getOutput()
                progressBar!!.progress = 100
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        mHandler = Handler()
        scanner = PortScanner(this.applicationContext.resources.openRawResource(R.raw.ports))
        (findViewById<View>(R.id.current_ip) as TextView).text = Html.fromHtml(String.format(getString(R.string.current_ip_display), getIPAddress(true)))
        addListeners()
    }

    private fun addListeners() {
        ip1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 2) {
                    ip2.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 3) {
                    ip1.setText(s.toString().substring(0, s.length - 1))
                }
            }
        })
        ip2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 2) {
                    ip3.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 3) {
                    ip2.setText(s.toString().substring(0, s.length - 1))
                }
            }
        })
        ip3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 2) {
                    ip4.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 3) {
                    ip3.setText(s.toString().substring(0, s.length - 1))
                }
            }
        })
        ip4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 2) {
                    portStart.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 3) {
                    ip4.setText(s.toString().substring(0, s.length - 1))
                }
            }
        })
        portStart.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 4) {
                    portEnd.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 5) {
                    portStart.setText(s.toString().substring(0, s.length - 1))
                }
            }
        })
        portEnd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 4) {
                    startButton.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 5) {
                    portEnd.setText(s.toString().substring(0, s.length - 1))
                }
            }
        })

        ip2.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (ip2.text.toString().isEmpty()) {
                    ip1.requestFocus()
                }
            }
            false
        }

        ip3.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (ip3.text.toString().isEmpty()) {
                    ip2.requestFocus()
                }
            }
            false
        }

        ip4.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (ip4.text.toString().isEmpty()) {
                    ip3.requestFocus()
                }
            }
            false
        }

        portStart.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (portStart.text.toString().isEmpty()) {
                    ip4.requestFocus()
                }
            }
            false
        }

        portEnd.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (portEnd.text.toString().isEmpty()) {
                    portStart.requestFocus()
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        ip1.setText(sharedPreferences.getString("ip1", ""))
        ip2.setText(sharedPreferences.getString("ip2", ""))
        ip3.setText(sharedPreferences.getString("ip3", ""))
        ip4.setText(sharedPreferences.getString("ip4", ""))
        portStart.setText(sharedPreferences.getString("port1", ""))
        portEnd.setText(sharedPreferences.getString("port2", ""))
    }

    override fun onPause() {
        super.onPause()
        saveInputs()
    }

    private fun saveInputs() {
        sharedPreferences.edit()
                .putString("ip1", ip1.text.toString())
                .putString("ip2", ip2.text.toString())
                .putString("ip3", ip3.text.toString())
                .putString("ip4", ip4.text.toString())
                .putString("port1", portStart.text.toString())
                .putString("port2", portEnd.text.toString())
                .apply()
    }

    fun startScanning(view: View) {
        saveInputs()
        var error = getString(R.string.error)
        try {
            val port1: Int
            val port2: Int
            ip = ip1.text.toString() + "." + ip2.text + "." + ip3.text + "." + ip4.text
            //Sentinels
            if (ip1.text.isEmpty() || ip2.text.isEmpty() || ip3.text.isEmpty() || ip4.text.isEmpty() || portStart.text.toString().isEmpty() || portEnd.text.toString().isEmpty()) {
                error = getString(R.string.invalid_ip_port)
                throw Exception()
            }
            if (ip1.text.length > 3 || ip2.text.length > 3 || ip3.text.length > 3 || ip4.text.length > 3) {
                error = getString(R.string.invalid_ip)
                throw Exception()
            }
            try {
                port1 = Integer.parseInt(portStart.text.toString())
                port2 = Integer.parseInt(portEnd.text.toString())
                if (port1 > 65535 || port1 < 1 || port2 > 65535 || port2 < 1) {
                    throw Exception()
                }
            } catch (e: Exception) {
                error = getString(R.string.invalid_port)
                throw Exception()
            }

            if (port2 - port1 <= 0) {
                error = getString(R.string.invalid_range)
                throw Exception()
            }
            // Start scanning
            scanner.scan(ip, port1, port2)
            scanDialog = AlertDialog.Builder(this)
                    .setPositiveButton(R.string.close) { dialog, _ ->
                        scanner.stop()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.export) { _, _ -> }
                    .setView(R.layout.dialog_scan).create()
            scanDialog.setOnShowListener {
                val button = scanDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                button.setOnClickListener { export() }
            }
            scanDialog.show()
            mHandler.post(mUpdate)
        } catch (e: Exception) {
            val toast = Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT)
            toast.show()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            export()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun export() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0)
        } else {
            try {
                if (!scanner.running) {
                    val sdCard = Environment.getExternalStorageDirectory()
                    val dir = File(sdCard.absolutePath + "/Port Detective/")
                    dir.mkdir()
                    @SuppressLint("SimpleDateFormat")
                    val dateFormat = SimpleDateFormat(getString(R.string.date_format))
                    val date = Date()
                    val file = File(dir, ip + "@" + dateFormat.format(date) + ".txt")
                    val f = FileOutputStream(file)
                    val data = ("Host " + ip + "\nTime:" + dateFormat.format(date) + "\n\n" + scanner.getOutput()).toByteArray()
                    f.write(data)
                    f.flush()
                    f.close()
                    Toast.makeText(applicationContext, String.format(getString(R.string.saved_log), dir, ip, dateFormat.format(date)), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, getString(R.string.scanning_not_finished), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, getString(R.string.error_saving), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.lookup -> showLookupDialog()
            R.id.about -> showAboutDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLookupDialog() {
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.port_enc)
                .setView(R.layout.dialog_lookup)
                .create()
        dialog.show()
        val et = dialog.findViewById<EditText>(R.id.input)
        val search = dialog.findViewById<Button>(R.id.search)
        search!!.setOnClickListener {
            try {
                val port = et!!.text.toString()
                val pd = PortDescriptor(resources.openRawResource(R.raw.ports))
                val desc = pd.getDescription(Integer.parseInt(port))
                if (Integer.parseInt(port) < 49152) {
                    val console = dialog.findViewById<TextView>(R.id.info)
                    console!!.text = desc.replace("- ", "").trim({ it <= ' ' })
                    val lookUp = dialog.findViewById<TextView>(R.id.lookup)
                    lookUp!!.visibility = View.VISIBLE
                    lookUp.text = String.format(getString(R.string.lookup_web), port)
                    lookUp.setOnClickListener {
                        val url = String.format(getString(R.string.port_website), port)
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    }
                } else {
                    Toast.makeText(applicationContext, getString(R.string.invalid_entry), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, getString(R.string.error_parse), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAboutDialog() {
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setView(R.layout.dialog_about)
                .create()
        dialog.show()
    }

    private fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        val isIPv4 = !sAddr.contains(":")

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        return ""
    }
}
