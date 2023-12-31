package com.example.clarity.classroompage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class PagerAdapter(fragmentManager: FragmentManager,
                   private val classId: String,
                   private val classTeacherId: String) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = 2 // Number of tabs

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ClassAnnouncement(classId)
            1 -> ClassTask(classId, classTeacherId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}