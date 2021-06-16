package com.jasonwanjs.bestprice

import android.animation.ArgbEvaluator
import android.app.AlertDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlin.properties.Delegates

private const val TAG = "MainActivity"
private var badCase = false
private const val DEFAULT_RESULT = "A / B"
private const val DEFAULT_RATIO = "0.0"
private const val DEFAULT_DECIMAL = "%.4f"
class MainActivity : AppCompatActivity() {
    private lateinit var etUnitA : EditText
    private lateinit var etUnitB : EditText
    private lateinit var etPriceA : EditText
    private lateinit var etPriceB : EditText
    private lateinit var tvResult : TextView
    private lateinit var tvCommentA : TextView
    private lateinit var tvCommentB : TextView
    private lateinit var tvRatio : TextView
    private var defaultColor by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUnitA = findViewById(R.id.etUnitA)
        etUnitB = findViewById(R.id.etUnitB)
        etPriceA = findViewById(R.id.etPriceA)
        etPriceB = findViewById(R.id.etPriceB)
        tvResult = findViewById(R.id.tvResult)
        tvCommentA = findViewById(R.id.tvCommentA)
        tvCommentB = findViewById(R.id.tvCommentB)
        tvRatio = findViewById(R.id.tvRatio)

        tvResult.text = DEFAULT_RESULT
        tvRatio.text = DEFAULT_RATIO
        defaultColor = ContextCompat.getColor(this, R.color.black)
        tvCommentA.setTextColor(defaultColor)
        tvCommentB.setTextColor(defaultColor)

        etUnitA.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                preventDoubleZero(s, etUnitA)
                computeRatio()
            }
        })
        etUnitB.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                preventDoubleZero(s, etUnitB)
                computeRatio()
            }
        })

        etPriceA.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                preventDoubleZero(s, etPriceA)
                computeRatio()
            }
        })
        etPriceB.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                preventDoubleZero(s, etPriceB)
                computeRatio()
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_select, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.btnClear) {
            Log.i(TAG, "Tapped on clear!")

            if (!(etUnitA.text.isEmpty() && etUnitB.text.isEmpty() && etPriceA.text.isEmpty() && etPriceB.text.isEmpty())) {
                showAlertDialog("Clear all values?", null, View.OnClickListener {
                    clearAllValues()
                })
            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun computeRatio() {
        if (computeRatioA() == -1.0 || computeRatioB() == -1.0) {
            tvResult.text = DEFAULT_RESULT
            tvCommentA.setTextColor(defaultColor)
            tvCommentB.setTextColor(defaultColor)
            return
        }

        val ratioA = computeRatioA()
        val ratioB = computeRatioB()

        Log.i(TAG, "afterTextChanged: $ratioA and $ratioB")

        val colorA = if (ratioA > ratioB) ContextCompat.getColor(this, R.color.red) else ContextCompat.getColor(this, R.color.green)
        val colorB = if (ratioA < ratioB) ContextCompat.getColor(this, R.color.red) else ContextCompat.getColor(this, R.color.green)

        tvCommentA.setTextColor(colorA)
        tvCommentB.setTextColor(colorB)

        if( ratioA < ratioB ) {
            tvRatio.text = String.format(DEFAULT_DECIMAL, ratioA)
            tvResult.text = "A"
        } else if( ratioA > ratioB ){
            tvRatio.text = String.format(DEFAULT_DECIMAL, ratioB)
            tvResult.text = "B"
        } else if ( badCase ) {
            tvRatio.text = DEFAULT_RATIO
            tvResult.text = DEFAULT_RESULT
        } else {
            tvRatio.text = DEFAULT_RATIO
            tvResult.text = DEFAULT_RESULT
        }
    }

    private fun computeRatioA(): Double {
        if (etUnitA.text.isEmpty() || etPriceA.text.isEmpty()) {
            return -1.0
        }
        val ratioA = etPriceA.text.toString().toDouble() / etUnitA.text.toString().toDouble()
        Log.i(TAG, "afterTextChanged ratioA: $ratioA")
        Log.i(TAG, "afterTextChanged text: ${etUnitA.text}")

        if(ratioA.isInfinite() || ratioA.isNaN() ) {
            badCase = true
            Log.e(TAG, "Got infinity or NaN")
            Toast.makeText(this, "Sorry, ratio cannot be determined", Toast.LENGTH_SHORT).show()
                etUnitA.setTextColor(ContextCompat.getColor(this, R.color.red))
                etPriceA.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            badCase = false
        }

        if(etUnitA.text.toString() != "0" && etUnitA.text.toString() != "0.") {
            etUnitA.setTextColor(defaultColor)
        }
        if(etPriceA.text.toString() != "0" && etPriceA.text.toString() != "0.") {
            etPriceA.setTextColor(defaultColor)
        }

        if( !badCase ) {
            tvCommentA.text = String.format(DEFAULT_DECIMAL, ratioA)
        } else {
            tvCommentA.text = ""
        }

        return ratioA
    }

    private fun computeRatioB(): Double {
        if (etUnitB.text.isEmpty() || etPriceB.text.isEmpty()) {
            return -1.0
        }
        val ratioB = etPriceB.text.toString().toDouble() / etUnitB.text.toString().toDouble()
        Log.i(TAG, "afterTextChanged ratioB: $ratioB")

        if(ratioB.isInfinite() || ratioB.isNaN() ) {
            badCase = true
            Log.e(TAG, "Got infinity or NaN")
            Toast.makeText(this, "Sorry, ratio cannot be determined", Toast.LENGTH_SHORT).show()
            etUnitB.setTextColor(ContextCompat.getColor(this, R.color.red))
            etPriceB.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            badCase = false
        }

        if(etUnitB.text.toString() != "0" && etUnitB.text.toString() != "0.") {
            etUnitB.setTextColor(defaultColor)
        }
        if(etPriceB.text.toString() != "0" && etPriceB.text.toString() != "0.") {
            etPriceB.setTextColor(defaultColor)
        }

        if( !badCase ) {
            tvCommentB.text = String.format(DEFAULT_DECIMAL, ratioB)
        } else {
            tvCommentB.text = ""
        }

        return ratioB
    }

    private fun preventDoubleZero( s:Editable?, target: EditText )  {
        if(s.toString().length == 2
            && s.toString().startsWith("0")
            && !s.toString().startsWith(".",1)) {
                target.setText("")
        } else if(s.toString().length == 1
            && s.toString().startsWith(".")) {
                target.setText("0.")
                target.setSelection(target.text.length)
        }

    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok") { _,_ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun clearAllValues() {
        etUnitA.text.clear()
        etUnitB.text.clear()
        etPriceA.text.clear()
        etPriceB.text.clear()
        tvCommentA.text = ""
        tvCommentB.text = ""
        tvResult.text = DEFAULT_RESULT
        tvRatio.text = DEFAULT_RATIO
        badCase = false
        tvCommentA.setTextColor(defaultColor)
        tvCommentB.setTextColor(defaultColor)
    }
}