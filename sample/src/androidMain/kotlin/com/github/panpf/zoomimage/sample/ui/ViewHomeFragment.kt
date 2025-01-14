/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.ArrayFragmentStateAdapter
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentViewHomeBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.gallery.LocalPhotoListFragment
import com.github.panpf.zoomimage.sample.ui.gallery.PexelsPhotoListFragment
import com.github.panpf.zoomimage.sample.ui.test.TestHomeFragment

class ViewHomeFragment : BaseBindingFragment<FragmentViewHomeBinding>() {

    private val fragments = listOf(
        "Pexels" to PexelsPhotoListFragment(),
        "Local" to LocalPhotoListFragment(),
        "Test" to TestHomeFragment(),
    )

    override fun getStatusBarInsetsView(binding: FragmentViewHomeBinding): View {
        return binding.root
    }

    override fun onViewCreated(binding: FragmentViewHomeBinding, savedInstanceState: Bundle?) {
        binding.toolbar.subtitle = "View"

        binding.composePageIconLayout.setOnClickListener {
            appSettings.composePage.value = true
        }

        binding.pager.apply {
            adapter = ArrayFragmentStateAdapter(
                fragmentManager = childFragmentManager,
                lifecycle = viewLifecycleOwner.lifecycle,
                templateFragmentList = fragments.map { it.second }
            )
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    appSettings.currentPageIndex.value = position
                    when (position) {
                        0 -> binding.navigation.selectedItemId = R.id.pexels
                        1 -> binding.navigation.selectedItemId = R.id.local
                        2 -> binding.navigation.selectedItemId = R.id.test
                    }
                }
            })
            setCurrentItem(
                /* item = */ appSettings.currentPageIndex.value
                    .coerceIn(minimumValue = 0, maximumValue = fragments.size - 1),
                /* smoothScroll = */ false
            )
        }

        binding.navigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.pexels -> binding.pager.setCurrentItem(0, false)
                R.id.local -> binding.pager.setCurrentItem(1, false)
                R.id.test -> binding.pager.setCurrentItem(2, false)
            }
            true
        }
    }
}