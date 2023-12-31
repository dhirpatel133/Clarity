package com.example.clarity.sets

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColorStateList
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clarity.R
import com.example.clarity.SessionManager
import com.example.clarity.databinding.FragmentSetsBinding
import com.example.clarity.sdk.ClaritySDK
import com.example.clarity.sdk.GetCardsInSetRequest
import com.example.clarity.sdk.GetCardsInSetResponse
import com.example.clarity.sdk.GetSetsByUsernameResponse
import com.example.clarity.sdk.GetUserSetProgressRequest
import com.example.clarity.sdk.GetUserSetProgressResponse
import com.example.clarity.sets.activities.PracticeSetActivity
import com.example.clarity.sets.activities.TestSetActivity
import com.example.clarity.sets.data.Card
import com.example.clarity.sets.data.Set
import com.example.clarity.sets.data.SetCategory
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Response


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val USER_ID = "userId"

/**
 * A simple [Fragment] subclass.
 * Use the [SetsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SetsFragment : Fragment() {
    // Fragment binding
    private var _binding: FragmentSetsBinding? = null

    // Set Adapter for list of sets
    private lateinit var setAdapter: SetAdapter
    private lateinit var sets: MutableList<Set>
    private lateinit var localFilteredSets: MutableList<Set>

    // Variables to store username and userid
    private lateinit var username: String
    private var userid: Int = 0

    // ClaritySDK api for endpoint calls
    private val api = ClaritySDK().apiService

    // sessionManager to interact with global datastore
    private val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }

    // Filter stuff
    private var showFilters = false
    private var filterText = ""
    private var showCompleted = true

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun filter() {
        val filteredSets: MutableList<Set> = mutableListOf()

        for (set in sets) {
            if (set.title.lowercase()
                    .contains(filterText.lowercase()) && (showCompleted || set.progress != set.cards.size)
            ) {
                filteredSets.add(set)
            }
        }

        localFilteredSets = filteredSets

        setAdapter.filterList(filteredSets)
    }

    private fun onSetClick(position: Int) {
        // Get all Variables
        val btnTest = binding.btnTest
        val btnPractice = binding.btnPractice
        val btnCancel = binding.iBtnCancel
        val cvStartActivity = binding.cvStartActivity
        val tvPopupSetTitle = binding.tvPopupSetTitle
        val tvNumCards = binding.tvNumCards

        // Make sure that cvStartActivity is not already up, otherwise the button won't work
        if (cvStartActivity.visibility != VISIBLE) {
            //
            tvPopupSetTitle.text = localFilteredSets[position].title
            if (localFilteredSets[position].cards.size == 1) {
                tvNumCards.text = "${localFilteredSets[position].cards.size} card"
            } else {
                tvNumCards.text = "${localFilteredSets[position].cards.size} cards"
            }
            cvStartActivity.visibility = VISIBLE

            btnTest.setOnClickListener {
                cvStartActivity.visibility = INVISIBLE
                val set = localFilteredSets[position]
                val gson = Gson()
                val setJson = gson.toJson(set)
                val intent = Intent(activity, TestSetActivity::class.java).apply {
                    putExtra("set", setJson)
                }
                startActivity(intent)
            }

            btnPractice.setOnClickListener {
                cvStartActivity.visibility = INVISIBLE
                val set = localFilteredSets[position]
                val gson = Gson()
                val setJson = gson.toJson(set)
                val intent = Intent(activity, PracticeSetActivity::class.java).apply {
                    putExtra("set", setJson)
                }
                startActivity(intent)
            }

            btnCancel.setOnClickListener {
                cvStartActivity.visibility = INVISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillLayout()
    }

    override fun onResume() {
        super.onResume()
        fillLayout()
    }

    private fun fillLayout() {
        lifecycleScope.launch {
            username = sessionManager.getUserName()
            userid = sessionManager.getUserId()
            Log.d("Username is: ", username)
        }
        sets = mutableListOf()
        // TODO: Replace following lines with a query for all sets with our userId, and then parse
        //  through the object returned, creating a set data class for each set, and appending it
        //  to the sets array


        val response : Response<GetSetsByUsernameResponse> = runBlocking {
            return@runBlocking api.getSetsByUsername(username)
        }
        println(response.body())

        if (response.isSuccessful) {
            val size = response.body()!!.data.size

            for(i in 0 until size) {
                val setData = response.body()?.data?.get(i)!!
                val setId = setData.set_id
                val setTitle = setData.title

                val progressResponse : Response<GetUserSetProgressResponse> = runBlocking {
                    return@runBlocking api.getSetProgress(GetUserSetProgressRequest(setId, userid))
                }

                val progress = progressResponse.body()!!.numCompletedCards

                val set = Set(setId, setTitle, userid, mutableListOf<Card>(), progress, SetCategory.CREATED_SET)

                val cards : Response<GetCardsInSetResponse> = runBlocking {
                    return@runBlocking api.getCards(GetCardsInSetRequest(setId))
                }
                if (cards.isSuccessful) {
                    for (card in cards.body()!!.cards) {
                        set.cards.add(Card(card.card_id, card.phrase, false))
                    }
                }
                sets.add(set)
            }
        }

        localFilteredSets = sets

        setAdapter = SetAdapter(sets) { position -> onSetClick(position) }
        binding.rvSets.adapter = setAdapter
        binding.rvSets.layoutManager = LinearLayoutManager(context)
        binding.btnCreateSet.setOnClickListener {
            val intent = Intent(activity, CreateSetActivity::class.java)
            startActivity(intent)
        }

        binding.btnFilterToggle.setBackgroundResource(R.drawable.baseline_filter_list_off_24)
        binding.btnToggleCompleted.visibility = GONE
        binding.etSearchSets.visibility = GONE
        showFilters = false
        binding.btnFilterToggle.setOnClickListener {
            showFilters = !showFilters
            if (showFilters) {
                binding.btnFilterToggle.setBackgroundResource(R.drawable.baseline_filter_list_24)
                binding.btnToggleCompleted.visibility = VISIBLE
                binding.etSearchSets.visibility = VISIBLE
            } else {
                binding.btnFilterToggle.setBackgroundResource(R.drawable.baseline_filter_list_off_24)
                binding.btnToggleCompleted.visibility = GONE
                binding.etSearchSets.visibility = GONE
            }
        }

        showCompleted = true
        binding.btnToggleCompleted.text = "Hide Completed"
        binding.btnToggleCompleted.backgroundTintList = getColorStateList(requireContext(), R.color.hideCompleted)
        binding.btnToggleCompleted.setOnClickListener {
            showCompleted = !showCompleted
            if (showCompleted) {
                binding.btnToggleCompleted.text = "Hide Completed"
                binding.btnToggleCompleted.backgroundTintList = getColorStateList(requireContext(), R.color.hideCompleted)

            } else {
                binding.btnToggleCompleted.text = "Show Completed"
                binding.btnToggleCompleted.backgroundTintList = getColorStateList(requireContext(), R.color.showCompleted)
            }
            filter()
        }

        binding.etSearchSets.text.clear()
        binding.etSearchSets.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filterText = s.toString()
                filter()
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param userId User ID of user that logged in
         * @return A new instance of fragment SetsFragment.
         */
        @JvmStatic
        fun newInstance() =
            SetsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}