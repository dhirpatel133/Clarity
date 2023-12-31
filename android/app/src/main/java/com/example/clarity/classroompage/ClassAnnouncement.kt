package com.example.clarity.classroompage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clarity.IndexActivity
import com.example.clarity.R
import com.example.clarity.databinding.FragmentClassAnnouncementBinding
import com.example.clarity.databinding.FragmentFirstBinding
import com.example.clarity.sdk.ClaritySDK
import com.example.clarity.sdk.GetAnnouncementsResponse
import com.example.clarity.sdk.StatusResponse
import kotlinx.coroutines.runBlocking
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [ClassAnnouncement.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClassAnnouncement(private val classId: String) : Fragment() {
    private var _binding: FragmentClassAnnouncementBinding? = null

    private lateinit var announcementAdapter: AnnouncementAdapter
    private val api = ClaritySDK().apiService

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentClassAnnouncementBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val announcements: MutableList<Pair<String, String>> = mutableListOf()

        // update list with current announcements
        val response: Response<GetAnnouncementsResponse> = runBlocking {
            return@runBlocking api.getAnnouncements(classId)
        }
        if (response.body()?.response  == StatusResponse.Success) {
            val announcementList = response?.body()?.result
            if (announcementList != null) {
                announcements.clear()
                for (announcement in announcementList) {
                    announcements.add(Pair(announcement.text, announcement.description))
                }
            }
        }
        announcementAdapter = AnnouncementAdapter(announcements) {announcement ->
        }
        binding.rvClasses.adapter = announcementAdapter
        binding.rvClasses.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}