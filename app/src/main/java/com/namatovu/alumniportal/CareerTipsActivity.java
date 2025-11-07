package com.namatovu.alumniportal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.namatovu.alumniportal.databinding.ActivityCareerTipsBinding;
import com.namatovu.alumniportal.models.CareerTip;
import com.namatovu.alumniportal.utils.AnalyticsHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Career Tips Activity - Interactive career guidance for alumni
 * Features: Auto-rotation, swipe gestures, save/share functionality, category filtering
 */
public class CareerTipsActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String TAG = "CareerTipsActivity";
    private static final String PREFS_NAME = "CareerTipsPrefs";
    private static final String SAVED_TIPS_KEY = "saved_tips";
    private static final int AUTO_ROTATE_DELAY = 20000; // 20 seconds
    private static final int MIN_SWIPE_DISTANCE = 120;
    private static final int MIN_SWIPE_VELOCITY = 200;

    private ActivityCareerTipsBinding binding;
    private List<CareerTip> allTips;
    private List<CareerTip> filteredTips;
    private int currentTipIndex = 0;
    private String currentCategory = "All";
    private Handler autoRotateHandler;
    private Runnable autoRotateRunnable;
    private GestureDetectorCompat gestureDetector;
    private SharedPreferences sharedPreferences;
    private Set<String> savedTipIds;
    private boolean isAutoRotateEnabled = true;

    // Animation objects
    private Animation slideInRight, slideInLeft, slideOutRight, slideOutLeft, fadeIn, fadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCareerTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize components
        initializeComponents();
        setupToolbar();
        setupAnimations();
        setupGestureDetector();
        setupClickListeners();
        setupCategoryChips();
        initializeTipsData();
        setupAutoRotation();
        
        // Display first tip
        displayCurrentTip();
        
        // Log analytics
        AnalyticsHelper.logNavigation("CareerTipsActivity", "HomeActivity");
    }

    /**
     * Initialize all components and preferences
     */
    private void initializeComponents() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        savedTipIds = sharedPreferences.getStringSet(SAVED_TIPS_KEY, new HashSet<>());
        autoRotateHandler = new Handler(Looper.getMainLooper());
        allTips = new ArrayList<>();
        filteredTips = new ArrayList<>();
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Career Tips");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Initialize all animations for smooth transitions
     */
    private void setupAnimations() {
        slideInRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideInLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        
        // Set animation durations
        slideInRight.setDuration(300);
        slideInLeft.setDuration(300);
        slideOutRight.setDuration(300);
        slideOutLeft.setDuration(300);
        fadeIn.setDuration(400);
        fadeOut.setDuration(400);
    }

    /**
     * Setup gesture detector for swipe navigation
     */
    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, this);
        binding.tipCard.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    /**
     * Setup all click listeners for buttons and interactions
     */
    private void setupClickListeners() {
        // Navigation buttons
        binding.btnPrevious.setOnClickListener(v -> {
            pauseAutoRotation();
            showPreviousTip();
        });

        binding.btnNext.setOnClickListener(v -> {
            pauseAutoRotation();
            showNextTip();
        });

        // Action buttons
        binding.btnSave.setOnClickListener(v -> toggleSaveTip());
        binding.btnShare.setOnClickListener(v -> shareCurrentTip());
        binding.btnSavedTips.setOnClickListener(v -> openSavedTips());

        // Auto-rotate toggle
        binding.btnAutoRotate.setOnClickListener(v -> toggleAutoRotation());

        // Play/Pause for auto-rotation
        binding.btnPlayPause.setOnClickListener(v -> toggleAutoRotation());
    }

    /**
     * Setup category filter chips
     */
    private void setupCategoryChips() {
        String[] categories = {"All", "Networking", "Job Search", "Entrepreneurship", 
                             "Skill Development", "Productivity", "Financial Management"};
        
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            
            // Set MUST green theme for chips
            chip.setChipBackgroundColorResource(R.color.light_green);
            chip.setTextColor(getColor(R.color.must_green));
            
            if (category.equals("All")) {
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.must_green);
                chip.setTextColor(getColor(R.color.white));
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectCategory(category);
                    updateChipStyles(category);
                }
            });
            
            binding.categoryChips.addView(chip);
        }
    }

    /**
     * Update chip styles when category is selected
     */
    private void updateChipStyles(String selectedCategory) {
        for (int i = 0; i < binding.categoryChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.categoryChips.getChildAt(i);
            if (chip.getText().toString().equals(selectedCategory)) {
                chip.setChipBackgroundColorResource(R.color.must_green);
                chip.setTextColor(getColor(R.color.white));
            } else {
                chip.setChecked(false);
                chip.setChipBackgroundColorResource(R.color.light_green);
                chip.setTextColor(getColor(R.color.must_green));
            }
        }
    }

    /**
     * Initialize comprehensive tips data with categories and emojis - 100 tips per category
     */
    private void initializeTipsData() {
        allTips.clear();
        
        addNetworkingTips();
        addJobSearchTips();
        addEntrepreneurshipTips();
        addSkillDevelopmentTips();
        addProductivityTips();
        addFinancialManagementTips();

        // Load saved tips status
        loadSavedTipsStatus();
        
        // Initialize with all tips
        filteredTips.addAll(allTips);
    }

    private void addNetworkingTips() {
        String[] networkingTips = {
            "Attend alumni events regularly to grow your network. 🤝",
            "Follow up within 24 hours after meeting new contacts. ⏰",
            "Share valuable content on LinkedIn to stay visible. 📱",
            "Join professional associations in your field. 🏢",
            "Offer help before asking for favors from your network. 🤲",
            "Keep your elevator pitch under 30 seconds. 🗣️",
            "Connect with colleagues from different departments. 🔄",
            "Send personalized connection requests, not generic ones. ✉️",
            "Remember personal details about your contacts. 🧠",
            "Schedule regular coffee chats with industry peers. ☕",
            "Build relationships before you need them. 🌱",
            "Listen more than you speak in networking conversations. 👂",
            "Follow up with valuable resources after meetings. 📋",
            "Attend industry conferences and workshops. 🎓",
            "Join online communities in your field. 💻",
            "Volunteer for professional organizations. 🙋‍♀️",
            "Host networking events or meetups yourself. 🎪",
            "Connect people in your network with each other. 🔗",
            "Send congratulatory messages on career milestones. 🎉",
            "Share others' achievements on social media. 📢",
            "Ask for warm introductions instead of cold outreach. 🤗",
            "Follow up quarterly with your network. 📅",
            "Create a CRM system for your contacts. 📊",
            "Attend virtual networking events regularly. 💻",
            "Practice active networking, not passive collecting. ⚡",
            "Build relationships across different industries. 🌐",
            "Network with people at all career levels. 📈",
            "Use social media strategically for networking. 📱",
            "Attend company events and open houses. 🏠",
            "Join alumni networks from schools and companies. 🎓",
            "Participate in online discussions and forums. 💬",
            "Offer to mentor others in your field. 👨‍🏫",
            "Attend lunch-and-learn sessions. 🍽️",
            "Join professional sports leagues or clubs. ⚽",
            "Participate in charity events and fundraisers. ❤️",
            "Connect with speakers after presentations. 🎤",
            "Join mastermind groups in your industry. 🧠",
            "Attend trade shows and exhibitions. 🏪",
            "Participate in panel discussions and webinars. 📺",
            "Connect with journalists covering your industry. 📰",
            "Build relationships with recruiters. 🔍",
            "Attend startup events and pitch competitions. 🚀",
            "Join coworking spaces for networking. 🏢",
            "Participate in hackathons and competitions. 💻",
            "Attend book clubs focused on business topics. 📚",
            "Join advisory boards for startups. 💼",
            "Participate in industry research and surveys. 📊",
            "Attend networking breakfasts and happy hours. 🍻",
            "Connect with vendors and service providers. 🤝",
            "Join professional coaching circles. 👥",
            "Attend university guest lectures. 🎓",
            "Participate in industry award ceremonies. 🏆",
            "Connect with podcast hosts in your field. 🎙️",
            "Join executive roundtables. 🔄",
            "Attend innovation labs and incubators. 💡",
            "Participate in cross-industry events. 🌐",
            "Connect with thought leaders on social media. 🌟",
            "Join professional book clubs. 📖",
            "Attend leadership development programs. 👑",
            "Participate in industry forums and debates. 🗣️",
            "Connect with alumni from other schools. 🏫",
            "Join entrepreneurship meetups. 💼",
            "Attend diversity and inclusion events. 🌈",
            "Participate in skills-based volunteering. 🛠️",
            "Connect with industry influencers. 📸",
            "Join executive search firm events. 🔍",
            "Attend women in business events. 👩‍💼",
            "Participate in leadership circles. 👥",
            "Connect with board members of organizations. 🏛️",
            "Join industry-specific Slack channels. 💬",
            "Attend corporate social responsibility events. 🌍",
            "Participate in mentorship programs. 🤝",
            "Connect with venture capitalists and angels. 💰",
            "Join professional development courses. 📚",
            "Attend technology meetups and demos. 💻",
            "Participate in cultural exchange programs. 🌏",
            "Connect with international business contacts. 🌐",
            "Join chambers of commerce events. 🏛️",
            "Attend innovation and future trends events. 🔮",
            "Participate in sustainability initiatives. 🌱",
            "Connect with government and policy makers. 🏛️",
            "Join cross-functional project teams. 👥",
            "Attend customer appreciation events. 🎉",
            "Participate in industry certification programs. 📜",
            "Connect with academic researchers. 🔬",
            "Join consulting and advisory networks. 💼",
            "Attend wellness and work-life balance events. ⚖️",
            "Participate in digital transformation forums. 📱",
            "Connect with supply chain partners. 🔗",
            "Join innovation challenges. 💡",
            "Attend global business summits. 🌍",
            "Participate in thought leadership panels. 🎯",
            "Connect with media and communications experts. 📺",
            "Join financial planning and investment groups. 💹",
            "Attend customer success conferences. 🎯",
            "Participate in agile and lean methodologies events. ⚡",
            "Connect with data science and analytics experts. 📊",
            "Join cybersecurity professional networks. 🔒",
            "Attend human resources and talent events. 👥",
            "Participate in marketing and branding meetups. 📈",
            "Connect with legal and compliance professionals. ⚖️",
            "Join operations and supply chain networks. 🚚",
            "Attend product management conferences. 📦",
            "Participate in sales and business development events. 💼",
            "Connect with customer experience professionals. 😊",
            "Join quality and process improvement groups. ✅"
        };
        
        for (int i = 0; i < networkingTips.length; i++) {
            allTips.add(new CareerTip(String.valueOf(i + 1), networkingTips[i], "Networking", false));
        }
    }

    private void addJobSearchTips() {
        String[] jobSearchTips = {
            "Tailor your resume for each job application. 📄",
            "Research the company culture before interviews. 🔍",
            "Practice the STAR method for behavioral questions. ⭐",
            "Apply within the first week of job posting. 🚀",
            "Use keywords from job descriptions in your resume. 🎯",
            "Prepare thoughtful questions to ask interviewers. ❓",
            "Clean up your social media profiles before applying. 🧹",
            "Get referrals from employees at target companies. 👥",
            "Follow up on applications with a polite email. 📧",
            "Practice your interview skills with mock sessions. 🎭",
            "Create a portfolio showcasing your best work. 💼",
            "Use job search engines and aggregators effectively. 🔍",
            "Set up job alerts for your target positions. 🔔",
            "Optimize your LinkedIn profile completely. 📱",
            "Write compelling cover letters for each application. ✍️",
            "Practice salary negotiation conversations. 💰",
            "Research typical salaries for your target roles. 📊",
            "Prepare for video interviews and technical tests. 📹",
            "Build a professional email signature. ✉️",
            "Keep detailed records of your applications. 📋",
            "Follow up within one week after interviews. ⏰",
            "Dress appropriately for each interview setting. 👔",
            "Arrive 10-15 minutes early for interviews. ⏰",
            "Research your interviewers on LinkedIn. 🔍",
            "Prepare specific examples for common questions. 📝",
            "Practice explaining career gaps honestly. 🕳️",
            "Develop a 30-60-90 day plan for new roles. 📅",
            "Create different resume versions for different roles. 📄",
            "Use action verbs and quantifiable achievements. 📈",
            "Proofread everything multiple times. ✅",
            "Get professional references ready in advance. 👥",
            "Practice your handshake and body language. 🤝",
            "Research common interview questions in your field. ❓",
            "Prepare for situational and hypothetical questions. 🤔",
            "Learn about the company's competitors. 🏢",
            "Understand the company's recent news and updates. 📰",
            "Prepare questions about team dynamics. 👥",
            "Research the hiring manager's background. 👨‍💼",
            "Practice technical skills relevant to the role. 💻",
            "Prepare for group interviews and assessments. 👥",
            "Learn about the company's values and mission. 🎯",
            "Prepare examples of leadership and teamwork. 👑",
            "Practice explaining your career goals clearly. 🎯",
            "Research industry trends and challenges. 📈",
            "Prepare for stress interviews and difficult questions. 😅",
            "Learn about the company's growth and expansion plans. 📊",
            "Practice phone and video interview skills. 📞",
            "Prepare for multiple rounds of interviews. 🔄",
            "Research the company's products and services thoroughly. 📦",
            "Prepare questions about professional development. 📚",
            "Practice explaining why you want to leave your current job. 🚪",
            "Learn about the company's organizational structure. 🏗️",
            "Prepare for assessment centers and group exercises. 📊",
            "Research the local job market and opportunities. 🗺️",
            "Practice negotiating benefits beyond salary. 💼",
            "Prepare for case study interviews. 📋",
            "Learn about the company's technology stack. 💻",
            "Practice explaining your problem-solving approach. 🧩",
            "Research the company's financial performance. 📈",
            "Prepare for personality and aptitude tests. 🧠",
            "Learn about the reporting structure and team size. 👥",
            "Practice discussing your weaknesses constructively. 💪",
            "Research the company's social responsibility initiatives. 🌍",
            "Prepare for informal interviews over meals. 🍽️",
            "Learn about the company's training and onboarding process. 📚",
            "Practice explaining your interest in the specific role. ❤️",
            "Research the company's work-life balance policies. ⚖️",
            "Prepare for technical coding or skills assessments. 💻",
            "Learn about the company's remote work policies. 🏠",
            "Practice discussing your long-term career plans. 🔮",
            "Research the company's diversity and inclusion efforts. 🌈",
            "Prepare for questions about your motivation and drive. 🔥",
            "Learn about the company's performance review process. 📊",
            "Practice explaining gaps in your employment history. ⏳",
            "Research the company's promotion and advancement paths. 📈",
            "Prepare for questions about working under pressure. ⚡",
            "Learn about the company's employee benefits package. 🎁",
            "Practice discussing your communication style. 💬",
            "Research the company's market position and strategy. 🎯",
            "Prepare for questions about working with difficult people. 😤",
            "Learn about the company's innovation and R&D efforts. 💡",
            "Practice explaining your decision-making process. 🤔",
            "Research the company's customer base and market. 👥",
            "Prepare for questions about handling failure and setbacks. 💪",
            "Learn about the company's partnerships and alliances. 🤝",
            "Practice discussing your adaptability and flexibility. 🔄",
            "Research the company's future plans and vision. 🔮",
            "Prepare for questions about your work style preferences. 🎨",
            "Learn about the company's quality standards and processes. ✅",
            "Practice explaining your project management experience. 📋",
            "Research the company's regulatory environment. 📜",
            "Prepare for questions about your leadership philosophy. 👑",
            "Learn about the company's risk management approach. ⚠️",
            "Practice discussing your analytical and problem-solving skills. 🧮",
            "Research the company's sustainability and environmental policies. 🌱",
            "Prepare for questions about your ability to learn quickly. 🚀",
            "Learn about the company's vendor and supplier relationships. 🔗",
            "Practice explaining your conflict resolution skills. ⚖️",
            "Research the company's international operations and expansion. 🌐",
            "Prepare for questions about your time management abilities. ⏰",
            "Learn about the company's data security and privacy policies. 🔒",
            "Practice discussing your cross-functional collaboration experience. 👥",
            "Research the company's digital transformation initiatives. 📱",
            "Prepare for questions about your change management experience. 🔄",
            "Learn about the company's customer service philosophy. 😊",
            "Practice explaining your continuous improvement mindset. 📈",
            "Research the company's talent acquisition and retention strategies. 👥"
        };
        
        for (int i = 0; i < jobSearchTips.length; i++) {
            allTips.add(new CareerTip(String.valueOf(100 + i + 1), jobSearchTips[i], "Job Search", false));
        }
    }

    private void addEntrepreneurshipTips() {
        String[] entrepreneurshipTips = {
            "Start small and validate your business idea first. 🌱",
            "Build a strong personal brand from day one. 🏆",
            "Focus on solving real problems for real people. 💡",
            "Network with other entrepreneurs and mentors. 🤝",
            "Keep your day job while building your side business. 💼",
            "Learn to say no to opportunities that don't align. ❌",
            "Invest in learning financial management skills. 💰",
            "Test your minimum viable product early. 🧪",
            "Build systems and processes for scalability. ⚙️",
            "Customer feedback is more valuable than opinions. 👂",
            "Start with a lean business model approach. 📊",
            "Focus on cash flow management from day one. 💸",
            "Build a strong founding team with complementary skills. 👥",
            "Create a compelling value proposition. 🎯",
            "Understand your target market deeply. 🔍",
            "Develop multiple revenue streams. 💰",
            "Invest in building brand awareness early. 📢",
            "Learn from failed entrepreneurs and their mistakes. 📚",
            "Build strong relationships with suppliers. 🚚",
            "Focus on customer acquisition cost optimization. 📈",
            "Develop a strong online presence. 🌐",
            "Create systems for measuring business metrics. 📊",
            "Build partnerships with complementary businesses. 🤝",
            "Invest in legal protection for your intellectual property. ⚖️",
            "Develop crisis management and contingency plans. 🚨",
            "Focus on building a sustainable competitive advantage. 🏆",
            "Learn to pitch your business effectively. 🎤",
            "Build a strong company culture from the start. 🏢",
            "Invest in technology that scales with growth. 💻",
            "Develop strong financial forecasting skills. 📈",
            "Create effective marketing and sales funnels. 🔄",
            "Build relationships with potential investors early. 💼",
            "Focus on product-market fit before scaling. 🎯",
            "Develop strong leadership and management skills. 👑",
            "Learn to delegate effectively as you grow. 🤲",
            "Build systems for quality control and assurance. ✅",
            "Invest in cybersecurity and data protection. 🔒",
            "Develop strong vendor and supplier relationships. 🔗",
            "Create effective inventory management systems. 📦",
            "Build strong customer service capabilities. 😊",
            "Develop expertise in your industry regulations. 📜",
            "Learn to manage stress and avoid burnout. 😌",
            "Build strong business development capabilities. 🚀",
            "Invest in continuous learning and skill development. 📚",
            "Develop strong negotiation skills. 🤝",
            "Create effective project management systems. 📋",
            "Build relationships with industry analysts. 📊",
            "Focus on sustainable business practices. 🌱",
            "Develop strong public relations capabilities. 📰",
            "Learn to manage remote teams effectively. 🏠",
            "Build strong succession planning processes. 👥",
            "Invest in building strategic alliances. 🤝",
            "Develop expertise in digital marketing. 📱",
            "Create effective employee retention strategies. 👥",
            "Build strong risk management capabilities. ⚠️",
            "Learn to manage international expansion. 🌍",
            "Develop strong innovation processes. 💡",
            "Build effective performance measurement systems. 📊",
            "Invest in building thought leadership. 🧠",
            "Create strong change management capabilities. 🔄",
            "Develop expertise in mergers and acquisitions. 🏢",
            "Build strong crisis communication skills. 📢",
            "Learn to manage stakeholder relationships. 👥",
            "Develop strong competitive intelligence capabilities. 🔍",
            "Create effective knowledge management systems. 🧠",
            "Build strong business continuity planning. 🔄",
            "Invest in building social impact initiatives. 🌍",
            "Develop strong talent acquisition strategies. 👥",
            "Learn to manage regulatory compliance. 📜",
            "Build effective customer feedback systems. 👂",
            "Create strong operational efficiency processes. ⚙️",
            "Develop expertise in business model innovation. 💡",
            "Build strong market research capabilities. 🔍",
            "Learn to manage intellectual property portfolios. 📋",
            "Develop strong strategic planning processes. 🎯",
            "Create effective cost management systems. 💰",
            "Build strong supply chain management. 🚚",
            "Invest in building data analytics capabilities. 📊",
            "Develop strong customer lifecycle management. 🔄",
            "Learn to manage business transformation. 🦋",
            "Build effective governance structures. 🏛️",
            "Create strong brand management strategies. 🏆",
            "Develop expertise in digital transformation. 📱",
            "Build strong partnership management skills. 🤝",
            "Learn to manage business model pivots. 🔄",
            "Develop strong market expansion strategies. 🌐",
            "Create effective resource allocation systems. 📊",
            "Build strong stakeholder communication. 💬",
            "Invest in building ethical business practices. ⚖️",
            "Develop strong business intelligence capabilities. 🧠",
            "Learn to manage regulatory relationships. 🏛️",
            "Build effective performance management systems. 📈",
            "Create strong innovation management processes. 💡",
            "Develop expertise in sustainable growth strategies. 🌱",
            "Build strong cross-cultural management skills. 🌍",
            "Learn to manage business ecosystem partnerships. 🤝",
            "Develop strong scenario planning capabilities. 🔮",
            "Create effective business process optimization. ⚙️",
            "Build strong corporate social responsibility. 🌍",
            "Invest in building future-ready capabilities. 🚀",
            "Develop strong business model resilience. 💪",
            "Learn to manage exponential growth challenges. 📈",
            "Build effective organizational learning systems. 📚",
            "Create strong value creation strategies. 💎",
            "Develop expertise in platform business models. 🌐"
        };
        
        for (int i = 0; i < entrepreneurshipTips.length; i++) {
            allTips.add(new CareerTip(String.valueOf(200 + i + 1), entrepreneurshipTips[i], "Entrepreneurship", false));
        }
    }

    private void addSkillDevelopmentTips() {
        String[] skillDevelopmentTips = {
            "Learn a new skill every quarter to stay competitive. 📚",
            "Take online courses during your commute time. 🚇",
            "Practice public speaking at every opportunity. 🎤",
            "Read industry publications and blogs regularly. 📖",
            "Find a mentor in your field for guidance. 👨‍🏫",
            "Attend workshops and seminars in your industry. 🎓",
            "Learn basic coding skills, regardless of your field. 💻",
            "Develop your emotional intelligence daily. 💝",
            "Practice active listening in all conversations. 👂",
            "Get certified in relevant technologies or methods. 📜",
            "Join professional development programs. 🎯",
            "Practice critical thinking and problem-solving. 🧩",
            "Develop strong communication skills. 💬",
            "Learn project management methodologies. 📋",
            "Improve your data analysis capabilities. 📊",
            "Develop cross-cultural competency. 🌍",
            "Practice time management techniques. ⏰",
            "Learn conflict resolution strategies. ⚖️",
            "Develop leadership skills at every level. 👑",
            "Practice creative thinking and innovation. 💡",
            "Learn financial literacy and business acumen. 💰",
            "Develop strong research and analytical skills. 🔍",
            "Practice negotiation and persuasion techniques. 🤝",
            "Learn digital literacy and technology skills. 📱",
            "Develop adaptability and resilience. 💪",
            "Practice mindfulness and stress management. 😌",
            "Learn collaborative teamwork skills. 👥",
            "Develop customer service excellence. 😊",
            "Practice ethical decision-making. ⚖️",
            "Learn strategic thinking and planning. 🎯",
            "Develop cultural intelligence and awareness. 🌏",
            "Practice effective feedback giving and receiving. 🔄",
            "Learn agile and lean methodologies. ⚡",
            "Develop strong writing and documentation skills. ✍️",
            "Practice systems thinking and analysis. 🔄",
            "Learn change management principles. 🦋",
            "Develop entrepreneurial mindset and skills. 🚀",
            "Practice design thinking methodologies. 🎨",
            "Learn quality management principles. ✅",
            "Develop risk assessment and management skills. ⚠️",
            "Practice continuous improvement mindset. 📈",
            "Learn supply chain and operations knowledge. 🚚",
            "Develop marketing and brand management skills. 📢",
            "Practice sales and business development. 💼",
            "Learn human resources and talent management. 👥",
            "Develop cybersecurity awareness and skills. 🔒",
            "Practice sustainability and environmental thinking. 🌱",
            "Learn regulatory compliance knowledge. 📜",
            "Develop international business capabilities. 🌐",
            "Practice innovation and creativity techniques. 💡",
            "Learn performance measurement and analytics. 📊",
            "Develop stakeholder management skills. 👥",
            "Practice scenario planning and forecasting. 🔮",
            "Learn process improvement methodologies. ⚙️",
            "Develop competitive intelligence capabilities. 🔍",
            "Practice knowledge management principles. 🧠",
            "Learn business continuity planning. 🔄",
            "Develop social impact and CSR knowledge. 🌍",
            "Practice talent development and coaching. 👨‍🏫",
            "Learn regulatory and legal fundamentals. ⚖️",
            "Develop customer experience design skills. 😊",
            "Practice operational excellence principles. ⚙️",
            "Learn business model innovation techniques. 💡",
            "Develop market research and insights skills. 🔍",
            "Practice intellectual property management. 📋",
            "Learn strategic partnership development. 🤝",
            "Develop cost management and optimization. 💰",
            "Practice supply chain optimization. 🚚",
            "Learn data science and analytics fundamentals. 📊",
            "Develop customer lifecycle management. 🔄",
            "Practice business transformation leadership. 🦋",
            "Learn governance and compliance frameworks. 🏛️",
            "Develop brand strategy and management. 🏆",
            "Practice digital transformation leadership. 📱",
            "Learn partnership and alliance management. 🤝",
            "Develop business model adaptation skills. 🔄",
            "Practice market expansion strategies. 🌐",
            "Learn resource allocation and optimization. 📊",
            "Develop stakeholder communication excellence. 💬",
            "Practice ethical business leadership. ⚖️",
            "Learn business intelligence and insights. 🧠",
            "Develop regulatory relationship management. 🏛️",
            "Practice performance optimization techniques. 📈",
            "Learn innovation ecosystem development. 💡",
            "Develop sustainable business practices. 🌱",
            "Practice cross-cultural leadership skills. 🌍",
            "Learn ecosystem partnership management. 🤝",
            "Develop scenario planning expertise. 🔮",
            "Practice business process excellence. ⚙️",
            "Learn corporate responsibility leadership. 🌍",
            "Develop future-ready skill anticipation. 🚀",
            "Practice business resilience building. 💪",
            "Learn exponential growth management. 📈",
            "Develop organizational learning facilitation. 📚",
            "Practice value creation optimization. 💎",
            "Learn platform economy principles. 🌐",
            "Develop quantum leap thinking skills. 🚀",
            "Practice ecosystem orchestration capabilities. 🎼",
            "Learn exponential technology integration. ⚡",
            "Develop regenerative business practices. 🌱",
            "Practice conscious leadership development. 🧘",
            "Learn circular economy principles. 🔄",
            "Develop biomimicry innovation skills. 🦋",
            "Practice integral systems thinking. 🌐"
        };
        
        for (int i = 0; i < skillDevelopmentTips.length; i++) {
            allTips.add(new CareerTip(String.valueOf(300 + i + 1), skillDevelopmentTips[i], "Skill Development", false));
        }
    }

    private void addProductivityTips() {
        String[] productivityTips = {
            "Use the Pomodoro Technique for focused work sessions. 🍅",
            "Set boundaries between work and personal time. ⚖️",
            "Plan your week every Sunday evening. 📅",
            "Take regular breaks to maintain peak performance. ⏸️",
            "Eliminate distractions during deep work hours. 🔇",
            "Delegate tasks that others can do better. 🤲",
            "Exercise regularly to boost mental clarity. 🏃‍♀️",
            "Batch similar tasks together for efficiency. 📦",
            "Say no to meetings without clear agendas. 🚫",
            "Review and reflect on your week every Friday. 🤔",
            "Use time-blocking to schedule your priorities. 📋",
            "Create morning and evening routines. 🌅",
            "Minimize multitasking to improve focus. 🎯",
            "Use the two-minute rule for quick tasks. ⏰",
            "Organize your workspace for maximum efficiency. 🗂️",
            "Practice single-tasking for better results. 🎯",
            "Use productivity apps to track your time. 📱",
            "Set realistic daily and weekly goals. 🎯",
            "Learn to say no to non-essential commitments. ❌",
            "Create templates for repetitive tasks. 📄",
            "Use keyboard shortcuts to save time. ⌨️",
            "Automate routine tasks whenever possible. 🤖",
            "Keep a daily journal for reflection. 📔",
            "Practice mindfulness to improve focus. 🧘",
            "Use the Eisenhower Matrix for prioritization. 📊",
            "Create checklists for complex processes. ✅",
            "Schedule your most important work during peak hours. ⏰",
            "Use standing meetings to keep them short. 🚶",
            "Implement a digital filing system. 📁",
            "Practice deep breathing for stress relief. 🌬️",
            "Use noise-canceling headphones for focus. 🎧",
            "Take walking meetings when appropriate. 🚶‍♀️",
            "Use project management tools effectively. 📋",
            "Create a distraction-free work environment. 🔇",
            "Practice the 80/20 rule for maximum impact. 📊",
            "Schedule buffer time between meetings. ⏰",
            "Use email filters and folders for organization. 📧",
            "Implement a weekly review process. 📅",
            "Practice saying no gracefully. 🙏",
            "Use visual reminders for important tasks. 👁️",
            "Create energy-based schedules, not just time-based. ⚡",
            "Practice gratitude to maintain positive mindset. 🙏",
            "Use the PARA method for information organization. 📚",
            "Implement inbox zero for email management. 📧",
            "Practice active recovery during breaks. 🏃‍♂️",
            "Use collaborative tools for team productivity. 👥",
            "Create standard operating procedures. 📋",
            "Practice decision-making frameworks. 🤔",
            "Use habit stacking to build new routines. 🔗",
            "Implement a capture system for ideas. 💡",
            "Practice energy management over time management. ⚡",
            "Use color coding for visual organization. 🌈",
            "Create accountability partnerships. 🤝",
            "Practice digital minimalism. 📱",
            "Use the Getting Things Done methodology. ✅",
            "Implement regular digital detoxes. 📵",
            "Practice batch processing for similar tasks. 📦",
            "Use meditation for mental clarity. 🧘‍♀️",
            "Create weekly themes for focused work. 📅",
            "Practice deliberate practice for skill development. 🎯",
            "Use natural light to regulate energy cycles. ☀️",
            "Implement a shutdown ritual for work. 🔚",
            "Practice constraint-based productivity. 🎯",
            "Use music strategically for different tasks. 🎵",
            "Create contingency plans for disruptions. 🛡️",
            "Practice active listening in all interactions. 👂",
            "Use visualization for goal achievement. 🎯",
            "Implement regular skill maintenance sessions. 🔧",
            "Practice saying no to perfectionism. ❌",
            "Use deadlines as motivation tools. ⏰",
            "Create reward systems for completed goals. 🏆",
            "Practice stress inoculation techniques. 💪",
            "Use technology to enhance, not replace, thinking. 🧠",
            "Implement regular learning breaks. 📚",
            "Practice outcome-based thinking. 🎯",
            "Use natural breaks in your schedule. ⏰",
            "Create systems for continuous improvement. 📈",
            "Practice mindful transitions between tasks. 🔄",
            "Use environmental design for productivity. 🏢",
            "Implement regular performance reviews. 📊",
            "Practice selective ignorance of non-essentials. 🙈",
            "Use documentation to capture knowledge. 📋",
            "Create feedback loops for improvement. 🔄",
            "Practice patience with long-term goals. ⏳",
            "Use calendar blocking for deep work. 📅",
            "Implement regular energy audits. ⚡",
            "Practice graceful failure and recovery. 🤸‍♀️",
            "Use peer pressure positively for accountability. 👥",
            "Create rituals for entering flow state. 🌊",
            "Practice strategic rest and recovery. 😴",
            "Use constraints to boost creativity. 🎨",
            "Implement regular technology updates. 💻",
            "Practice presence in all activities. 🧘",
            "Use natural rhythms for scheduling. 🌙",
            "Create systems for managing overwhelm. 🌊",
            "Practice intentional attention management. 👁️",
            "Use gamification for mundane tasks. 🎮",
            "Implement regular relationship maintenance. 👥",
            "Practice sustainable productivity habits. 🌱",
            "Use scenario planning for better preparation. 🔮",
            "Create networks for knowledge sharing. 🕸️",
            "Practice adaptive productivity methods. 🔄",
            "Use technology for cognitive augmentation. 🧠",
            "Implement regular horizon scanning. 🔭",
            "Practice regenerative work practices. 🌱"
        };
        
        for (int i = 0; i < productivityTips.length; i++) {
            allTips.add(new CareerTip(String.valueOf(400 + i + 1), productivityTips[i], "Productivity", false));
        }
    }

    private void addFinancialManagementTips() {
        String[] financialTips = {
            "Negotiate your salary every 1-2 years. 💵",
            "Invest in your 401k from your first paycheck. 🏦",
            "Build an emergency fund of 6 months expenses. 💰",
            "Track your expenses with budgeting apps. 📱",
            "Diversify your income with side projects. 🔄",
            "Research market rates before salary negotiations. 📊",
            "Automate your savings and investments. 🤖",
            "Invest in index funds for long-term growth. 📈",
            "Review your credit report annually. 📋",
            "Consider professional financial planning advice. 👨‍💼",
            "Create and stick to a monthly budget. 📊",
            "Maximize employer 401k matching contributions. 🎯",
            "Pay off high-interest debt first. 💳",
            "Consider Roth IRA for tax-free retirement growth. 🏦",
            "Negotiate bills and recurring payments. 💰",
            "Invest in your professional development. 📚",
            "Consider real estate as investment option. 🏠",
            "Understand tax implications of investments. 📊",
            "Build multiple streams of passive income. 💰",
            "Review insurance coverage annually. 🛡️",
            "Set specific financial goals with deadlines. 🎯",
            "Learn about cryptocurrency and blockchain. ₿",
            "Consider dollar-cost averaging for investments. 📈",
            "Understand compound interest and time value. ⏰",
            "Create a will and estate planning documents. 📜",
            "Learn about tax-advantaged investment accounts. 🏦",
            "Consider hiring a fee-only financial advisor. 👨‍💼",
            "Understand your employee benefits thoroughly. 📋",
            "Learn about international investment opportunities. 🌍",
            "Consider HSA as retirement investment vehicle. 🏥",
            "Understand correlation between risk and return. 📊",
            "Learn about alternative investment strategies. 💎",
            "Consider socially responsible investing options. 🌱",
            "Understand impact of inflation on wealth. 📈",
            "Learn about tax loss harvesting strategies. 📊",
            "Consider life insurance as investment tool. 🛡️",
            "Understand difference between active and passive investing. 📊",
            "Learn about commodity and precious metal investing. 🥇",
            "Consider peer-to-peer lending platforms. 🤝",
            "Understand impact of fees on investment returns. 💰",
            "Learn about venture capital and angel investing. 🚀",
            "Consider international tax implications. 🌍",
            "Understand behavioral finance and cognitive biases. 🧠",
            "Learn about options and derivatives trading. 📊",
            "Consider impact investing for social returns. 🌍",
            "Understand liquidity needs and investment horizon. ⏰",
            "Learn about hedge fund strategies and risks. 📊",
            "Consider family financial planning and education. 👨‍👩‍👧‍👦",
            "Understand credit scores and improvement strategies. 📊",
            "Learn about mortgage strategies and refinancing. 🏠",
            "Consider business ownership and equity participation. 🏢",
            "Understand foreign exchange and currency risks. 💱",
            "Learn about retirement withdrawal strategies. 🏦",
            "Consider long-term care insurance needs. 🏥",
            "Understand charitable giving tax strategies. 🎁",
            "Learn about trust structures and wealth transfer. 🏛️",
            "Consider disability insurance for income protection. 🛡️",
            "Understand market cycles and economic indicators. 📊",
            "Learn about private equity investment opportunities. 🏢",
            "Consider fractional real estate investing. 🏠",
            "Understand bond investing and fixed income. 📊",
            "Learn about emerging market investment opportunities. 🌍",
            "Consider environmental, social, governance investing. 🌱",
            "Understand sector rotation and tactical allocation. 🔄",
            "Learn about regulatory changes affecting investments. 📜",
            "Consider robo-advisors for automated investing. 🤖",
            "Understand alternative retirement strategies. 🏖️",
            "Learn about family office and wealth management. 🏛️",
            "Consider international retirement planning. 🌍",
            "Understand succession planning for business owners. 👥",
            "Learn about philanthropic strategies and vehicles. 🎁",
            "Consider cross-border tax optimization strategies. 🌍",
            "Understand wealth protection and asset structuring. 🛡️",
            "Learn about next-generation wealth transfer. 👨‍👩‍👧‍👦",
            "Consider impact of technology on financial services. 💻",
            "Understand regulatory compliance for investments. 📜",
            "Learn about behavioral coaching and investor psychology. 🧠",
            "Consider sustainable investing and climate risks. 🌱",
            "Understand geopolitical risks and portfolio impacts. 🌍",
            "Learn about digital assets and blockchain technology. 💻",
            "Consider multi-generational wealth planning. 👥",
            "Understand liquidity management for high net worth. 💰",
            "Learn about private market investment opportunities. 🏢",
            "Consider tax-efficient wealth transfer strategies. 📊",
            "Understand family governance and wealth education. 👨‍👩‍👧‍👦",
            "Learn about impact measurement and social returns. 📊",
            "Consider cross-asset allocation and diversification. 🔄",
            "Understand regulatory arbitrage opportunities. 📜",
            "Learn about next-generation investment platforms. 💻",
            "Consider alternative credit and lending strategies. 🏦",
            "Understand demographic trends and investment implications. 👥",
            "Learn about artificial intelligence in investing. 🤖",
            "Consider space economy and frontier investments. 🚀",
            "Understand quantum computing impact on finance. 💻",
            "Learn about biotechnology investment opportunities. 🧬",
            "Consider renewable energy investment strategies. 🌱",
            "Understand autonomous vehicles and mobility investing. 🚗",
            "Learn about virtual reality and metaverse investments. 🥽",
            "Consider water scarcity and resource investing. 💧",
            "Understand aging population investment themes. 👴",
            "Learn about precision medicine investment trends. 🏥",
            "Consider cybersecurity investment opportunities. 🔒",
            "Understand food technology and agriculture investing. 🌾",
            "Learn about education technology investment trends. 📚",
            "Consider circular economy investment strategies. ♻️",
            "Understand ocean economy and blue investing. 🌊",
            "Learn about space technology investment opportunities. 🛸",
            "Consider longevity and life extension investing. ⏳"
        };
        
        for (int i = 0; i < financialTips.length; i++) {
            allTips.add(new CareerTip(String.valueOf(500 + i + 1), financialTips[i], "Financial Management", false));
        }
    }

    /**
     * Load saved tips status from preferences
     */
    private void loadSavedTipsStatus() {
        for (CareerTip tip : allTips) {
            tip.setSaved(savedTipIds.contains(tip.getId()));
        }
    }

    /**
     * Filter tips by selected category
     */
    private void selectCategory(String category) {
        currentCategory = category;
        filteredTips.clear();
        
        if ("All".equals(category)) {
            filteredTips.addAll(allTips);
        } else {
            for (CareerTip tip : allTips) {
                if (category.equals(tip.getCategory())) {
                    filteredTips.add(tip);
                }
            }
        }
        
        currentTipIndex = 0;
        displayCurrentTip();
        
        // Log category selection
        AnalyticsHelper.logEvent("career_tips_category_selected", "category", category);
    }

    /**
     * Display the current tip with animation
     */
    private void displayCurrentTip() {
        if (filteredTips.isEmpty()) {
            showEmptyState();
            return;
        }
        
        hideEmptyState();
        CareerTip currentTip = filteredTips.get(currentTipIndex);
        
        // Update UI elements
        binding.tipText.setText(currentTip.getText());
        binding.tipCategory.setText(currentTip.getCategory());
        
        // Update save button icon
        updateSaveButton(currentTip.isSaved());
        
        // Update navigation buttons
        binding.btnPrevious.setEnabled(currentTipIndex > 0);
        binding.btnNext.setEnabled(currentTipIndex < filteredTips.size() - 1);
        
        // Apply fade-in animation
        binding.tipCard.startAnimation(fadeIn);
        
        // Log tip view
        AnalyticsHelper.logEvent("career_tip_viewed", "tip_id", currentTip.getId());
    }

    /**
     * Show empty state when no tips are available
     */
    private void showEmptyState() {
        binding.tipCard.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
        binding.btnPrevious.setEnabled(false);
        binding.btnNext.setEnabled(false);
        binding.btnSave.setEnabled(false);
        binding.btnShare.setEnabled(false);
    }

    /**
     * Hide empty state and show tip card
     */
    private void hideEmptyState() {
        binding.tipCard.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.btnSave.setEnabled(true);
        binding.btnShare.setEnabled(true);
    }


    /**
     * Show previous tip with slide animation
     */
    private void showPreviousTip() {
        if (currentTipIndex > 0) {
            binding.tipCard.startAnimation(slideOutRight);
            currentTipIndex--;
            
            slideOutRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    displayCurrentTip();
                    binding.tipCard.startAnimation(slideInLeft);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    /**
     * Show next tip with slide animation
     */
    private void showNextTip() {
        if (currentTipIndex < filteredTips.size() - 1) {
            binding.tipCard.startAnimation(slideOutLeft);
            currentTipIndex++;
            
            slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    displayCurrentTip();
                    binding.tipCard.startAnimation(slideInRight);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        } else {
            // Reached end, restart from beginning
            binding.tipCard.startAnimation(slideOutLeft);
            currentTipIndex = 0;
            
            slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    displayCurrentTip();
                    binding.tipCard.startAnimation(slideInRight);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    /**
     * Setup auto-rotation functionality
     */
    private void setupAutoRotation() {
        autoRotateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoRotateEnabled && !filteredTips.isEmpty()) {
                    showNextTip();
                    autoRotateHandler.postDelayed(this, AUTO_ROTATE_DELAY);
                }
            }
        };
        
        startAutoRotation();
    }

    /**
     * Start auto-rotation
     */
    private void startAutoRotation() {
        if (isAutoRotateEnabled) {
            autoRotateHandler.postDelayed(autoRotateRunnable, AUTO_ROTATE_DELAY);
            binding.btnPlayPause.setText("⏸️");
        }
    }

    /**
     * Pause auto-rotation temporarily
     */
    private void pauseAutoRotation() {
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
        // Resume after 10 seconds of inactivity
        autoRotateHandler.postDelayed(() -> {
            if (isAutoRotateEnabled) {
                startAutoRotation();
            }
        }, 10000);
    }

    /**
     * Toggle auto-rotation on/off
     */
    private void toggleAutoRotation() {
        isAutoRotateEnabled = !isAutoRotateEnabled;
        
        if (isAutoRotateEnabled) {
            startAutoRotation();
            binding.btnPlayPause.setText("⏸️");
            binding.btnAutoRotate.setText("Auto: ON");
        } else {
            autoRotateHandler.removeCallbacks(autoRotateRunnable);
            binding.btnPlayPause.setText("▶️");
            binding.btnAutoRotate.setText("Auto: OFF");
        }
    }

    /**
     * Toggle save status of current tip
     */
    private void toggleSaveTip() {
        if (filteredTips.isEmpty()) return;
        
        CareerTip currentTip = filteredTips.get(currentTipIndex);
        boolean newSavedStatus = !currentTip.isSaved();
        currentTip.setSaved(newSavedStatus);
        
        // Update saved tips set
        if (newSavedStatus) {
            savedTipIds.add(currentTip.getId());
            Toast.makeText(this, "Tip saved! 💾", Toast.LENGTH_SHORT).show();
        } else {
            savedTipIds.remove(currentTip.getId());
            Toast.makeText(this, "Tip removed from saved", Toast.LENGTH_SHORT).show();
        }
        
        // Update preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SAVED_TIPS_KEY, savedTipIds);
        editor.apply();
        
        // Update UI
        updateSaveButton(newSavedStatus);
        
        // Log save action
        AnalyticsHelper.logEvent("career_tip_saved", "tip_id", currentTip.getId());
    }

    /**
     * Update save button appearance
     */
    private void updateSaveButton(boolean isSaved) {
        if (isSaved) {
            binding.btnSave.setText("💾 Saved");
            binding.btnSave.setBackgroundTintList(getColorStateList(R.color.must_green));
        } else {
            binding.btnSave.setText("💾 Save");
            binding.btnSave.setBackgroundTintList(getColorStateList(R.color.light_green));
        }
    }

    /**
     * Share current tip via intent
     */
    private void shareCurrentTip() {
        if (filteredTips.isEmpty()) return;
        
        CareerTip currentTip = filteredTips.get(currentTipIndex);
        String shareText = "💡 Career Tip: " + currentTip.getText() + 
                          "\n\n🏷️ Category: " + currentTip.getCategory() + 
                          "\n\nShared from Alumni Portal Career Tips";
        
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");
        
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share Career Tip");
        startActivity(chooserIntent);
        
        // Log share action
        AnalyticsHelper.logEvent("career_tip_shared", "tip_id", currentTip.getId());
        Toast.makeText(this, "Tip shared! 📤", Toast.LENGTH_SHORT).show();
    }

    /**
     * Open saved tips activity
     */
    private void openSavedTips() {
        Intent intent = new Intent(this, SavedCareerTipsActivity.class);
        startActivity(intent);
    }

    // Gesture Detector Methods for Swipe Navigation

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) return false;
        
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        
        if (Math.abs(diffX) > Math.abs(diffY) && 
            Math.abs(diffX) > MIN_SWIPE_DISTANCE && 
            Math.abs(velocityX) > MIN_SWIPE_VELOCITY) {
            
            pauseAutoRotation();
            
            if (diffX > 0) {
                // Swipe right - previous tip
                showPreviousTip();
            } else {
                // Swipe left - next tip
                showNextTip();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause auto-rotation when activity is not visible
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume auto-rotation when activity becomes visible
        if (isAutoRotateEnabled) {
            startAutoRotation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handlers
        if (autoRotateHandler != null) {
            autoRotateHandler.removeCallbacks(autoRotateRunnable);
        }
    }
}