package com.jaqxues.sniptools.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.util.set
import androidx.core.view.isNotEmpty
import com.google.android.material.navigation.NavigationView
import com.jaqxues.sniptools.R
import com.jaqxues.sniptools.fragments.BaseFragment
import com.jaqxues.sniptools.pack.ModPack
import com.jaqxues.sniptools.utils.items
import timber.log.Timber


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 02.06.20 - Time 23:05.
 */

class DynamicNavigationView : NavigationView, NavigationView.OnNavigationItemSelectedListener {
    private var currentMenuItem: MenuItem? = null
    private val fragments = SparseArray<BaseFragment>()
    private val packFragments = SparseArray<IntArray>()
    private lateinit var activeFragment: BaseFragment
    private lateinit var listener: NavigationFragmentListener

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * @param action: Adds Fragments and returns the currently active Fragment
     */
    fun initialize(
        listener: NavigationFragmentListener,
        action: DynamicNavigationView.() -> BaseFragment?
    ) {
        this.listener = listener
        setNavigationItemSelectedListener(this)

        if (menu.isNotEmpty())
            checkMenuItem(menu.getItem(0))

        action()?.let {
            activeFragment = it
            listener.selectedFragment(it)
        }
    }

    private fun checkMenuItem(item: MenuItem) {
        item.isChecked = true
        currentMenuItem?.isChecked = false
        currentMenuItem = item
    }

    fun addFragment(fragment: BaseFragment, overriddenId: Int = Menu.NONE) {
        fragments.append(if (overriddenId == Menu.NONE) fragment.menuId else overriddenId, fragment)
    }

    fun removeFragment(menuId: Int) =
        fragments.remove(menuId)

    fun getFragmentById(@IdRes menuId: Int) =
        fragments.get(menuId)
            ?: throw IllegalArgumentException("MenuId not associated with a fragment")

    fun navigate(@IdRes menuId: Int): Boolean {
        val item = menu.findItem(menuId)
        if (item == null) {
            Timber.w("Tried to navigate to Id ($menuId) but item was not found")
            return false
        }
        return onNavigationItemSelected(item)
    }

    fun addPackFragments(menuInflater: MenuInflater, packName: String, pack: ModPack) {
        val subMenu = menu.addSubMenu(Menu.NONE, packName.hashCode(), Menu.NONE, packName)
        menuInflater.inflate(R.menu.pack_menu, subMenu)

        val staticFragments = pack.staticFragments.toSet()
        val allFragments = staticFragments +
                pack.featureManager.getActiveFeatures(true).flatMap {
                    it.getFragments().toList()
                }

        val unused = subMenu.items.toMutableList()

        val fragmentIds = allFragments.map { frag ->
            val found = unused.find {
                frag.name == it.title
            }
            if (found != null) {
                unused -= found
                addFragment(frag, found.itemId)
                found.itemId
            } else {
                subMenu.add(
                    if (frag in staticFragments) R.id.nav_group_static else Menu.NONE,
                    frag.menuId, Menu.NONE, frag.name
                )
                addFragment(frag)
                frag.menuId
            }
        }.toIntArray()
        packFragments[packName.hashCode()] = fragmentIds
        unused.forEach { it.isVisible = false }
    }

    fun removePackFragments(packName: String) {
        menu.removeItem(packName.hashCode())
        packFragments[packName.hashCode()]?.forEach {
            removeFragment(it)
        }
        packFragments.remove(packName.hashCode())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val selected = fragments.get(item.itemId) ?: return false
        if (selected == activeFragment) {
            Timber.d("Selected MenuItem of current Fragment")
            return true
        }

        checkMenuItem(item)
        listener.selectedFragment(selected)
        activeFragment = selected

        return true
    }

    operator fun BaseFragment.unaryPlus() {
        addFragment(this)
    }

    interface NavigationFragmentListener {
        fun selectedFragment(fragment: BaseFragment)
    }
}
