package com.example.clarity.profile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.RED
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.clarity.MainActivity
import com.example.clarity.R
import com.example.clarity.SessionManager
import com.example.clarity.databinding.FragmentProfileBinding
import com.example.clarity.sdk.ClaritySDK
import com.example.clarity.sdk.FollowerListResponse
import com.example.clarity.sdk.GetSetsByUsernameResponse
import com.example.clarity.sdk.GetUnreadResponse
import com.example.clarity.sdk.GetUserResponse
import com.example.clarity.sdk.GetUserSetProgressRequest
import com.example.clarity.sdk.GetUserSetProgressResponse
import com.example.clarity.sdk.Notification
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils


class ProfileFragment : Fragment() {

    private val api = ClaritySDK().apiService
    private var _binding: FragmentProfileBinding? = null

    private val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }

    private val binding get() = _binding!!
    //private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart


    private var username: String = ""
    private var userId: Int = 0

    private var totalSets = 0
    private var totalCards = 0
    private var completedCards = 0
    private var completedSets = 0
    private var incompleteCards = 0
    private var incompleteSets = 0
    private var cardDates = mutableListOf<LocalDate>()
    private var setDates = mutableListOf<LocalDate>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //navigation
        lifecycleScope.launch {
            username = sessionManager.getUserName()
            userId = sessionManager.getUserId()
        }
        val user = getUser()?.user
        val followingList = getFollowing()?.followers
        val followersList = getFollowers()?.followers

        val dropdownView: ImageView = binding.dropdown

        dropdownView.setOnClickListener {
            val menu = PopupMenu(view.context, dropdownView)
            menu.inflate(R.menu.profile_menu) // Replace with your menu resource file
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_settings -> {
                        settings()
                        true
                    }
                    R.id.logout -> {
                        logout()
                        true
                    }
                    // Add more menu item cases as needed
                    else -> false
                }
            }
            menu.show()
        }

        binding.firstLast.text = user?.firstname + " " + user?.lastname

        //FOLLOWERS
        println("here")
        val numFollowers = followersList?.size

        println("here")
        binding.followers.text = numFollowers.toString() + " Followers"

        if(followersList?.size!! > 0) {
            println(followersList)
            binding.followers.setOnClickListener {
                findNavController().navigate(R.id.Followers)
            }
        }



        //FOLLOWING

        val numFollowing = followingList?.size

        binding.following.text = numFollowing.toString() + " Following"

        if(followingList?.size!! > 0) {
            binding.following.setOnClickListener {
                findNavController().navigate(R.id.FollowingFragment)
            }
        }


        //STREAKS
        val streak = user?.login_streak
        binding.streak.text = "\uD83D\uDD25" + streak.toString() + " Day Streak"

        super.onViewCreated(view, savedInstanceState)

        var selectedTab = 0
        updateInfo()
        binding.noProgressInside.visibility = View.GONE

        if(setDates.size == 0 && cardDates.size == 0){
            binding.noProgress.visibility = View.VISIBLE
            binding.noProgressInside.visibility = View.GONE
            binding.chartLabel.visibility = View.GONE
            binding.lineChart.visibility = View.GONE
            binding.pieChart.visibility = View.GONE
            binding.completedNum.visibility = View.GONE
            binding.progress.visibility = View.GONE
            binding.completedText.visibility = View.GONE
        } else if (setDates.size == 0) {
            binding.noProgressInside.visibility = View.VISIBLE
            binding.noProgress.visibility = View.GONE
            binding.noProgressInside.text = "Complete Sets to View Progress!"
            binding.lineChart.visibility = View.GONE
            binding.pieChart.visibility = View.GONE
            binding.chartLabel.visibility = View.GONE
            binding.completedNum.visibility = View.GONE
            binding.progress.visibility = View.GONE
            binding.completedText.visibility = View.GONE
        } else {
            binding.noProgress.visibility = View.GONE
            binding.noProgressInside.visibility = View.GONE
            binding.lineChart.visibility = View.VISIBLE
            binding.pieChart.visibility = View.VISIBLE
            binding.completedNum.visibility = View.VISIBLE
            binding.progress.visibility = View.VISIBLE
            binding.completedText.visibility = View.VISIBLE
            sets()
        }

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.position
                if(selectedTab == 0) {
                    binding.lineChart.clear()
                    binding.pieChart.clear()
                    updateInfo()
                    if(setDates.size == 0){
                        binding.noProgressInside.visibility = View.VISIBLE
                        binding.noProgress.visibility = View.GONE
                        binding.noProgressInside.text = "Complete Sets to View Progress!"
                        binding.lineChart.visibility = View.GONE
                        binding.pieChart.visibility = View.GONE
                        binding.chartLabel.visibility = View.GONE
                        binding.completedNum.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                        binding.completedText.visibility = View.GONE
                    } else {
                        binding.noProgressInside.visibility = View.GONE
                        binding.noProgress.visibility = View.GONE
                        binding.lineChart.visibility = View.VISIBLE
                        binding.pieChart.visibility = View.VISIBLE
                        binding.completedNum.visibility = View.VISIBLE
                        binding.progress.visibility = View.VISIBLE
                        binding.completedText.visibility = View.VISIBLE
                        sets()
                    }

                } else {
                    binding.lineChart.clear()
                    binding.pieChart.clear()
                    updateInfo()
                    if(cardDates.size == 0){
                        binding.noProgressInside.visibility = View.VISIBLE
                        binding.noProgress.visibility = View.GONE
                        binding.lineChart.visibility = View.GONE
                        binding.noProgressInside.text = "Complete Cards to View Progress!"
                        binding.chartLabel.visibility = View.GONE
                        binding.pieChart.visibility = View.GONE
                        binding.completedNum.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                        binding.completedText.visibility = View.GONE
                    } else {
                        binding.noProgress.visibility = View.GONE
                        binding.noProgressInside.visibility = View.GONE
                        binding.lineChart.visibility = View.VISIBLE
                        binding.pieChart.visibility = View.VISIBLE
                        binding.completedNum.visibility = View.VISIBLE
                        binding.progress.visibility = View.VISIBLE
                        binding.completedText.visibility = View.VISIBLE
                        cards()
                    }

                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        binding.notifications.setOnClickListener {
            findNavController()?.navigate(R.id.NotificationsFragment)
        }
    }

    private fun getNotifications(): List<Notification>? {
        val response : Response<GetUnreadResponse> = runBlocking {
            return@runBlocking api.getUnread(userId)
        }
        return response.body()?.messages
    }
    private fun logout() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }
    private fun settings() {
        findNavController()?.navigate(R.id.SettingsFragment)
    }
    private fun cards() {
        binding.progress.text = "Cards Progress"
        binding.completedText.text = "Completed Cards"
        binding.completedNum.text = completedCards.toString()
        //get cards
        print("cards:")
        println(cardDates)

        var attempts = ArrayList<Entry>()
        val sortedDates = cardDates.sorted()
        val labels = ArrayList<String>()

        val dateCardCountMap = sortedDates.groupingBy { it }.eachCount()
        var index = 0f
        for ((date, count) in dateCardCountMap) {
            attempts.add(Entry(index, count.toFloat()))
            labels.add(date.toString())
            index++
        }
        if (attempts.size == 1) {
            val singleDataPoint = attempts[0]
            val fillerDataPoint = Entry(1f, singleDataPoint.y) // You can set the x-value to any value you like
            attempts.add(fillerDataPoint)
            labels.add("") // Add an empty label for the filler data point
        }
        val lineDataSet = LineDataSet(attempts, "Number of cards completed").apply {
            setDrawValues(false)
            color = Color.RED
            lineWidth = 2f
        }

        val lineData = LineData(lineDataSet)
        val lineChart: LineChart = binding.lineChart
        lineChart.data = lineData
        lineChart.apply {
            description.text = "Cards completion over time"
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                setCenterAxisLabels(true) // Center the labels
                labelRotationAngle = 45f // Rotate labels
            }
            description.isEnabled = false
            legend.isEnabled = false
            extraBottomOffset = 33f
            axisRight.isEnabled = false
            animateX(1800, Easing.EaseInOutQuart)
            invalidate() // refresh
        }
        binding.chartLabel.text = "Number of Cards Completed"


        pieChart = binding.pieChart
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(completedCards.toFloat() / totalCards, "Completed"))
        entries.add(PieEntry((incompleteCards.toFloat() / totalCards), "Incomplete"))
        //entries.add(PieEntry((notStartedCards.toFloat() / totalSavedCards), "Not Started"))
        val dataSet = PieDataSet(entries, "")
        val colourList = listOf(
            Color.parseColor("#C6C6C6"), //light grey
            Color.parseColor("#74ABFF"), //light blue
            //Color.parseColor("#3546D9") //blue
        )
        dataSet.colors = colourList
        val data = PieData(dataSet)
        dataSet.valueTextSize = 12f
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.description.isEnabled = false
        pieChart.data = data
        pieChart.invalidate()


        //number of sets completed
        binding.completedNum.text = completedCards.toString()
    }
    private fun sets(){
        println("hereS")
        binding.completedText.text = "Completed Sets"
        binding.progress.text = "Saved Sets Progress"

        binding.completedNum.text = completedSets.toString()
        print("sets:")
        println(setDates)

        var attempts = ArrayList<Entry>()
        val sortedDates = setDates.sorted()
        val labels = ArrayList<String>()

        val dateCardCountMap = sortedDates.groupingBy { it }.eachCount()
        var index = 0f
        for ((date, count) in dateCardCountMap) {
            attempts.add(Entry(index, count.toFloat()))
            labels.add(date.toString())
            index++
        }
        if (attempts.size == 1) {
            val singleDataPoint = attempts[0]
            val fillerDataPoint = Entry(1f, singleDataPoint.y) // You can set the x-value to any value you like
            attempts.add(fillerDataPoint)
            labels.add("") // Add an empty label for the filler data point
        }
        val lineDataSet = LineDataSet(attempts, "Number of sets completed").apply {
            setDrawValues(false)
            color = Color.RED
            lineWidth = 2f
        }
        val lineData = LineData(lineDataSet)
        val lineChart: LineChart = binding.lineChart
        lineChart.data = lineData
        lineChart.apply {
            description.text = "Cards completion over time"
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                setCenterAxisLabels(true) // Center the labels
                labelRotationAngle = 45f // Rotate labels
            }
            legend.isEnabled = false
            description.isEnabled = false
            extraBottomOffset = 33f
            axisRight.isEnabled = false
            animateX(1800, Easing.EaseInOutQuart)
            invalidate() // refresh
        }
        binding.chartLabel.text = "Number of Sets Completed"

        pieChart = binding.pieChart
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(completedSets.toFloat() / totalSets, "Completed"))
        entries.add(PieEntry((incompleteSets.toFloat() / totalSets), "Incomplete"))
        //entries.add(PieEntry((notStartedSets.toFloat() / totalSavedSets), "Not Started"))
        val dataSet = PieDataSet(entries, "")
        val colourList = listOf(
            Color.parseColor("#C6C6C6"), //light grey
            Color.parseColor("#74ABFF"), //light blue
            //Color.parseColor("#3546D9") //blue
        )
        dataSet.colors = colourList
        val data = PieData(dataSet)
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.description.isEnabled = false
        pieChart.data = data
        dataSet.valueTextSize = 12f
        pieChart.invalidate()


        //number of sets completed
        binding.completedNum.text = completedSets.toString()
        //get sets
    }
    private fun getFollowers(): FollowerListResponse? {
        val response : Response<FollowerListResponse> = runBlocking {
            return@runBlocking api.getFollowers(userId)
        }
        return response.body()
    }
    private fun getFollowing(): FollowerListResponse? {
        val response : Response<FollowerListResponse> = runBlocking {
            return@runBlocking api.getFollowing(userId)
        }
        return response.body()
    }
    private fun getUser(): GetUserResponse? {
        val response : Response<GetUserResponse> = runBlocking {
            return@runBlocking api.getUser(username)
        }
        return response.body()
    }
    private fun updateInfo() {
        val response : Response<GetSetsByUsernameResponse> = runBlocking {
            return@runBlocking api.getSetsByUsername(username)
        }
        val sets = response.body()?.data
        totalSets = sets?.size!!
        totalCards = 0
        completedCards = 0
        completedSets = 0
        incompleteCards = 0
        incompleteSets = 0
        cardDates.clear()
        setDates.clear()

        if (sets != null) {
            for (s in sets){
                val setReq = GetUserSetProgressRequest(s.set_id, userId)
                val res : Response<GetUserSetProgressResponse> = runBlocking {
                    return@runBlocking api.getSetProgress(setReq)
                }
                val currentSet = res.body()
                totalCards += currentSet?.numCards!!
                completedCards += currentSet?.numCompletedCards!!

                val completedList = currentSet?.completedCard

                var latestDate = LocalDate.MIN
                if (completedList != null) {
                    for (c in completedList) {
                        val date = LocalDate.parse(c.completion_date, DateTimeFormatter.ISO_DATE_TIME)
                        cardDates.add(date)
                        if(date > latestDate) {
                            latestDate = date
                        }
                    }
                }
                if(currentSet?.numCards!! == currentSet?.numCompletedCards!!) {
                    completedSets += 1
                    setDates.add(latestDate)
                }
            }
        }
        incompleteCards = totalCards - completedCards
        incompleteSets = totalSets?.minus(completedSets)!!
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}