package com.codingchallenge.locksettingsconfig

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.codingchallenge.locksettingsconfig.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: LockConfigViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LockConfigViewModel::class.java)

        setListeners()

        // Observe lockSettings LiveData
        viewModel.primaryLockSettings.observe(this) { lockSettings ->
            // Update UI with new lock settings
            binding.idTvPrimaryVoltage.text = lockSettings.lockVoltage.default
            binding.idTvPrimaryType.text = lockSettings.lockType.default
            binding.idTvPrimaryKick.text = lockSettings.lockKick.default
            binding.idTvRelease.text = lockSettings.lockRelease.default
            binding.idTvPrimaryReleaseTime.text = lockSettings.lockReleaseTime.default.toString()
            binding.idTvAngle.text = lockSettings.lockAngle.default.toString()
        }

        viewModel.secondaryLockSettings.observe(this) { lockSettings ->
            binding.idTvSecondaryVoltage.text = lockSettings.lockVoltage.default
            binding.idTvSecondaryType.text = lockSettings.lockType.default
            binding.idTvSecondaryKick.text = lockSettings.lockKick.default
            binding.idTvRelease.text = lockSettings.lockRelease.default
            binding.idTvSecondaryReleaseTime.text = lockSettings.lockReleaseTime.default.toString()
            binding.idTvAngle.text = lockSettings.lockAngle.default.toString()
        }

        // Observe filtered parameters
        viewModel.filteredLockSettings.observe(this) { parameters ->
            Log.v("Search result", parameters.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchConfig()
    }

    private fun setListeners() {
        binding.idIbEditVoltage.setOnClickListener {
            editParameter(LockConfigData::lockVoltage.name)
        }

        binding.idIbEditType.setOnClickListener {
            editParameter(LockConfigData::lockType.name)
        }

        binding.idIbEditKick.setOnClickListener {
            editParameter(LockConfigData::lockKick.name)
        }

        binding.idIbEditRelease.setOnClickListener {
            editParameter(LockConfigData::lockRelease.name)
        }

        binding.idIbEditReleaseTime.setOnClickListener {
            editParameter(LockConfigData::lockReleaseTime.name)
        }

        binding.idIbEditAngle.setOnClickListener {
            editParameter(LockConfigData::lockAngle.name)
        }

        binding.idSvParameter.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchLockSettings(query.orEmpty())
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //viewModel.searchLockSettings(newText.orEmpty())
                return true
            }
        })
    }

    private fun editParameter(parameter: String) {
        val intent = Intent(this@MainActivity, EditActivity::class.java)
        intent.putExtra(viewModel.parameterName, parameter)
        startActivity(intent)
    }
}

