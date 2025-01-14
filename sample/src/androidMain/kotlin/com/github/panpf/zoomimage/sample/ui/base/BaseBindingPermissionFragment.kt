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

package com.github.panpf.zoomimage.sample.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseBindingPermissionFragment<VIEW_BINDING : ViewBinding> :
    BasePermissionFragment() {

    protected var binding: VIEW_BINDING? = null

    @Suppress("UNCHECKED_CAST")
    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = (createViewBinding(inflater, container) as VIEW_BINDING).apply {
        this@BaseBindingPermissionFragment.binding = this
    }.root

    final override fun onPermissionPassed(view: View, savedInstanceState: Bundle?) {
        onViewCreated(this.binding!!, savedInstanceState)
    }

    abstract fun onViewCreated(binding: VIEW_BINDING, savedInstanceState: Bundle?)

    final override fun getStatusBarInsetsView(): View? {
        return getStatusBarInsetsView(binding!!) ?: super.getStatusBarInsetsView()
    }

    open fun getStatusBarInsetsView(binding: VIEW_BINDING): View? = null

    final override fun getNavigationBarInsetsView(): View? {
        return getNavigationBarInsetsView(binding!!) ?: super.getNavigationBarInsetsView()
    }

    open fun getNavigationBarInsetsView(binding: VIEW_BINDING): View? = null

    override fun onDestroyView() {
        this.binding = null
        super.onDestroyView()
    }
}