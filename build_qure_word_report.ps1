# ==============================================================================
# build_qure_word_report.ps1
# Generates the complete QURE Software Engineering Word Document (.docx)
# Guaranteed page break after every single question!
# ==============================================================================

$targetDocx = "C:\Users\LENOVO\AndroidStudioProjects\QURE\QURE_Software_Engineering_Diagrams_and_Requirements.docx"
$desktopDocx = "C:\Users\LENOVO\Desktop\QURE_Software_Engineering_Diagrams_and_Requirements.docx"
$imgDir = "C:\Users\LENOVO\AndroidStudioProjects\QURE\docs_diagrams"

# RGB Color values for Word COM
# RGB = R + (G * 256) + (B * 65536)
$cRGBNavy     = 26 + (35 * 256) + (126 * 65536)      # #1A237E
$cRGBNavyDark = 13 + (27 * 256) + (110 * 65536)      # #0D1B6E
$cRGBTeal     = 0 + (137 * 256) + (123 * 65536)      # #00897B
$cRGBTealDark = 0 + (105 * 256) + (92 * 65536)       # #00695C
$cRGBDarkText = 33 + (33 * 256) + (33 * 65536)       # #212121
$cRGBGrayText = 100 + (100 * 256) + (100 * 65536)
$cRGBWhite    = 255 + (255 * 256) + (255 * 65536)
$cRGBLightBg  = 245 + (247 * 256) + (250 * 65536)

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()

# Page Margins: 0.75 inch (54 points)
$doc.PageSetup.TopMargin    = 54
$doc.PageSetup.BottomMargin = 54
$doc.PageSetup.LeftMargin   = 54
$doc.PageSetup.RightMargin  = 54

$selection = $word.Selection

# Helper: Insert Question Heading
function Insert-QuestionHeading($num, $title) {
    $selection.Font.Name = "Segoe UI"
    $selection.Font.Size = 18
    $selection.Font.Bold = $true
    $selection.Font.Color = $cRGBNavyDark
    $selection.ParagraphFormat.SpaceBefore = 0
    $selection.ParagraphFormat.SpaceAfter = 6
    $selection.ParagraphFormat.Alignment = 0 # Left
    $headingText = "QUESTION " + $num + ": " + $title
    $selection.TypeText($headingText)
    $selection.TypeParagraph()
    
    # Subtitle divider line / tag
    $selection.Font.Size = 10
    $selection.Font.Bold = $false
    $selection.Font.Color = $cRGBTeal
    $selection.ParagraphFormat.SpaceAfter = 10
    $selection.TypeText("QURE: Smart Outpatient Department (OPD) Queue Management System | Academic Specification")
    $selection.TypeParagraph()
}

# Helper: Insert Paragraph
function Insert-Paragraph($text, $boldPrefix = "", $spaceAfter = 8) {
    $selection.Font.Name = "Segoe UI"
    $selection.Font.Size = 10.5
    $selection.ParagraphFormat.SpaceBefore = 0
    $selection.ParagraphFormat.SpaceAfter = $spaceAfter
    $selection.ParagraphFormat.LineSpacingRule = 0 # Single
    $selection.ParagraphFormat.Alignment = 0 # Left
    
    if ($boldPrefix -ne "") {
        $selection.Font.Bold = $true
        $selection.Font.Color = $cRGBNavyDark
        $selection.TypeText($boldPrefix + " ")
    }
    
    $selection.Font.Bold = $false
    $selection.Font.Color = $cRGBDarkText
    $selection.TypeText($text)
    $selection.TypeParagraph()
}

# Helper: Insert Diagram Image
function Insert-DiagramImage($fileName, $caption) {
    $filePath = "$imgDir\$fileName"
    if (Test-Path $filePath) {
        $selection.ParagraphFormat.Alignment = 1 # Center
        $selection.ParagraphFormat.SpaceBefore = 6
        $selection.ParagraphFormat.SpaceAfter = 4
        
        $shape = $selection.InlineShapes.AddPicture($filePath)
        $shape.Width = 490 # Fits 0.75-inch margins
        
        $selection.TypeParagraph()
        
        # Caption
        $selection.Font.Name = "Segoe UI"
        $selection.Font.Size = 9
        $selection.Font.Italic = $true
        $selection.Font.Bold = $true
        $selection.Font.Color = $cRGBNavyDark
        $selection.ParagraphFormat.SpaceAfter = 10
        $selection.TypeText("Figure: $caption")
        $selection.Font.Italic = $false
        $selection.TypeParagraph()
        
        $selection.ParagraphFormat.Alignment = 0 # Reset to Left
    }
}

# Helper: Insert Table with Headers
function Insert-SimpleTable($headers, $rowsData, $colWidths = $null) {
    $rowCount = $rowsData.Count + 1
    $colCount = $headers.Count
    
    $table = $doc.Tables.Add($selection.Range, $rowCount, $colCount)
    $table.Borders.Enable = $true
    $table.AutoFitBehavior(2) # wdAutoFitWindow
    $table.Rows.Alignment = 1 # Center
    
    # Header Row
    $headRow = $table.Rows.Item(1)
    $headRow.Range.Shading.BackgroundPatternColor = $cRGBNavy
    $headRow.Range.Font.Name = "Segoe UI"
    $headRow.Range.Font.Size = 9.5
    $headRow.Range.Font.Bold = $true
    $headRow.Range.Font.Color = $cRGBWhite
    
    for ($c = 0; $c -lt $colCount; $c++) {
        $table.Cell(1, ($c + 1)).Range.Text = $headers[$c]
    }
    
    # Data Rows
    for ($r = 0; $r -lt $rowsData.Count; $r++) {
        $dataRow = $table.Rows.Item($r + 2)
        $dataRow.Range.Font.Name = "Segoe UI"
        $dataRow.Range.Font.Size = 9
        $dataRow.Range.Font.Bold = $false
        $dataRow.Range.Font.Color = $cRGBDarkText
        
        if ($r % 2 -eq 1) {
            $dataRow.Range.Shading.BackgroundPatternColor = $cRGBLightBg
        }
        
        for ($c = 0; $c -lt $colCount; $c++) {
            $table.Cell(($r + 2), ($c + 1)).Range.Text = $rowsData[$r][$c]
        }
    }
    
    $selection.EndKey(6) # wdStory
    $selection.ParagraphFormat.SpaceAfter = 8
    $selection.TypeParagraph()
}

# Helper: Insert Page Break
function Insert-NewPage() {
    $selection.InsertBreak(7) # 7 = wdPageBreak
}

Write-Output "Building Word Document..."

# ==============================================================================
# PAGE 1: QUESTION 1 - Agile Functional Requirements with User Stories
# ==============================================================================
Insert-QuestionHeading "1" "Agile Requirements Gathering with User Stories (Functional Requirements)"

Insert-Paragraph "Agile Requirements Gathering is a modern software engineering technique where system features are defined from the direct viewpoint of actual end-users rather than technical jargon. Instead of long, rigid specification documents, Agile breaks requirements into small, self-contained units called User Stories. Every user story answers three straightforward questions: (1) Who wants the feature? (2) What do they want to accomplish? and (3) What real-world benefit does it provide? The industry-standard agile template used is: 'As a [Role], I want to [Perform an Action], so that [Achieve a Benefit]'." "What is Agile Requirements Gathering?"

Insert-Paragraph "In the QURE Smart Hospital System, functional requirements define the actual features that the application performs for its three key user roles: Patients, Doctors, and Hospital Administrators. These features streamline the outpatient department (OPD) by replacing disorganized physical queues with digital appointment booking, smart check-in, real-time token tracking, and Shortest Job First (SJF) doctor queues." "Functional Requirements in QURE:"

Insert-DiagramImage "diag1_agile_functional.png" "Agile Functional Requirements User Story Hierarchy & Map for QURE"

$headersQ1 = @("Story ID", "User Role", "Agile User Story (As a... I want... So that...)", "Acceptance Criteria", "Priority", "Story Points")
$rowsQ1 = @(
    @("US-F01", "Patient", "As a Patient, I want to book an OPD appointment by selecting a department, doctor, and date, so that I can secure a consultation slot ahead of time.", "User selects department & doctor from dropdown; system saves appointment record to Cloud Firestore with initial status 'Registered'.", "High", "5"),
    @("US-F02", "Patient", "As a Patient, I want to digitally check in when I physically arrive at the hospital, so that my arrival timestamp is recorded and I join the live doctor queue.", "Patient taps 'Check In at Hospital'; system records arrival time and transitions appointment status from 'Registered' to 'Waiting'.", "High", "3"),
    @("US-F03", "Patient", "As a Patient, I want to track my live queue status and token number on my dashboard, so that I know where I stand without waiting in crowded physical lines.", "Dashboard displays high-contrast token number, current status badge (Registered/Waiting/In Consultation), and doctor details.", "High", "5"),
    @("US-F04", "Doctor", "As a Doctor, I want my patient queue to be automatically organized using the Shortest Job First (SJF) algorithm, so that shorter consultations are cleared faster and average wait times drop.", "App queries all 'Waiting' appointments for the logged-in doctor and sorts them via QueueManager in ascending order of estimated consultation time.", "High", "8"),
    @("US-F05", "Doctor", "As a Doctor, I want to call the next patient with a single button click, so that the patient is immediately pulled into my consultation room.", "Tapping 'Call Next Patient' selects the top SJF patient, updates their database status to 'In Consultation', and displays their details in the active card.", "High", "3"),
    @("US-F06", "Doctor", "As a Doctor, I want to mark a consultation as completed, so that the patient leaves the queue and the system prepares for the next patient.", "Tapping 'Complete Consultation' marks status as 'Completed' and reloads the remaining waiting queue.", "Medium", "3"),
    @("US-F07", "Admin", "As an Administrator, I want to view live system statistics (total users and total appointments), so that I have real-time visibility over hospital traffic.", "Admin dashboard connects to Firestore and dynamically displays live counts of registered users and booked appointments.", "Medium", "5"),
    @("US-F08", "Admin", "As an Administrator, I want to test and compare FCFS, SJF, and Priority algorithms in an interactive simulator, so that I can demonstrate and evaluate OS queue scheduling concepts.", "Interactive simulator screen allows toggling between FCFS, SJF, and Priority radio buttons to observe instant list reordering on test workloads.", "High", "8"),
    @("US-F09", "Patient", "As a Patient, I want to upload and view PDF/image medical reports, so that my doctor can digitally access my previous diagnostic history.", "Patient picks a file from mobile storage; file uploads to Firebase Storage, download URL is saved in Firestore, and can be viewed with one tap.", "Medium", "5"),
    @("US-F10", "Patient", "As a Patient, I want to receive an instant push notification when my appointment is confirmed, so that I have immediate proof and token details on my mobile phone.", "App triggers a high-priority Android notification banner upon successful booking displaying doctor name and date.", "Medium", "3")
)
Insert-SimpleTable $headersQ1 $rowsQ1

Insert-NewPage
Write-Output "Page 1 complete."

# ==============================================================================
# PAGE 2: QUESTION 2 - Agile Non-Functional Requirements with User Stories
# ==============================================================================
Insert-QuestionHeading "2" "Agile Requirements Gathering with User Stories (Non-Functional Requirements)"

Insert-Paragraph "While functional requirements explain what the software does, Non-Functional Requirements (NFRs) describe how well the software performs those functions. Non-functional requirements represent the quality attributes, security boundaries, performance thresholds, reliability standards, and user-experience constraints of the system. In Agile development, writing NFRs as User Stories ensures that quality and performance are treated as first-class backlog deliverables rather than afterthoughts." "What are Non-Functional Requirements?"

Insert-Paragraph "In QURE, non-functional requirements are vital because hospitals deal with sensitive medical information and time-critical outpatient operations. The system must never crash during doctor consultations, must synchronize queue updates across devices in under two seconds, must protect medical reports with secure authentication, and must feature a clean, high-contrast interface easily readable by elderly patients." "Non-Functional Requirements in QURE:"

Insert-DiagramImage "diag2_agile_nonfunctional.png" "QURE Non-Functional Requirements (NFR) Architecture Model across 5 Quality Pillars"

$headersQ2 = @("NFR ID", "Quality Category", "Agile Non-Functional User Story", "Target Metric & Operational Constraint")
$rowsQ2 = @(
    @("NFR-01", "Performance & Latency", "As a Doctor, I want queue updates to synchronize in under 1.5 seconds, so that I never call a patient who has already left or cancelled.", "Cloud Firestore snapshot listeners and real-time updates propagate across client devices in <= 1.5 seconds under standard 4G/WiFi connections."),
    @("NFR-02", "Algorithm Efficiency", "As a System Architect, I want the QueueManager SJF sorting to execute in O(n log n) time complexity, so that processing hundreds of patients does not freeze the UI.", "In-memory Java Collections.sort() algorithm sorts up to 500 active patients in under 15 milliseconds without blocking the Android main UI thread."),
    @("NFR-03", "Security & RBAC", "As a Hospital Admin, I want strict Role-Based Access Control (RBAC), so that patients cannot view other patients' records or doctor controls.", "Firebase Authentication manages user tokens; users collection strictly validates roles (Patient, Doctor, Admin) during login and restricts dashboard routing."),
    @("NFR-04", "Data Privacy", "As a Patient, I want my uploaded lab reports to be securely stored with unique cloud paths, so that unauthorized users cannot access my medical files.", "Medical documents stored in Firebase Storage are partitioned under /medical_reports/{patientId}/ and protected by Firebase Security Rules."),
    @("NFR-05", "Reliability & Uptime", "As a Clinic Manager, I want local caching and automatic session resumption, so that temporary network dropouts do not log users out or crash the app.", "Firebase offline persistence caches reads locally; SharedPreferences maintains login state synchronously to eliminate screen flicker during launch."),
    @("NFR-06", "Usability & Accessibility", "As a Sick or Elderly Patient, I want high-contrast cards, large typography, and emoji cues, so that I can easily navigate the app without technical confusion.", "Material Design 3 standards implemented with a 2x2 grid layout, minimum 48dp touch targets, clear color badges (Gray=Registered, Orange=Waiting, Blue=Consulting)."),
    @("NFR-07", "Portability", "As an Android User, I want the QURE app to install and run smoothly on both older and modern smartphones, so that all patients can access care.", "Configured minSdk = 24 (Android 7.0 Nougat) and targetSdk = 37 (Android 14+), ensuring compatibility across more than 95% of active Android devices worldwide."),
    @("NFR-08", "Fault Tolerance", "As a User, I want friendly error messages when inputs are invalid or servers are unreachable, so that I know exactly what action to take.", "Form validations display inline TextInputLayout error messages and friendly Toast notifications instead of silent failures or technical stack traces.")
)
Insert-SimpleTable $headersQ2 $rowsQ2

Insert-NewPage
Write-Output "Page 2 complete."

# ==============================================================================
# PAGE 3: QUESTION 3 - Use Case Diagram
# ==============================================================================
Insert-QuestionHeading "3" "User's View Analysis for Qure: Use Case Diagram"

Insert-Paragraph "A Use Case Diagram represents the user's view of the system. In software engineering, it is used during system analysis to define the external boundaries of the application and illustrate the high-level services provided to each stakeholder. It consists of: (1) Actors (human users or external systems that interact with the application), (2) Use Cases (ovals representing discrete tasks or goals the system accomplishes), and (3) Relationships (lines showing associations, <<include>> dependencies for mandatory sub-tasks, and <<extend>> relationships for optional workflows)." "What is a Use Case Diagram?"

Insert-Paragraph "For the QURE Smart Hospital System, the actors are: Patient (books appointments, checks in, monitors queue, uploads lab reports), Doctor (views waiting queue, reviews SJF ordering, conducts consultations), Administrator (monitors live hospital metrics, evaluates scheduling algorithms), and Firebase Cloud Services (external backend supporting authentication, cloud database, cloud storage, and messaging)." "Actors & Capabilities in QURE:"

Insert-DiagramImage "diag3_use_case.png" "QURE UML Use Case Diagram illustrating Stakeholder Interactions and System Boundary"

$headersQ3 = @("Use Case ID", "Use Case Name", "Primary Actor", "Description & Operational Scope", "Relationship")
$rowsQ3 = @(
    @("UC-01", "Register & Login", "Patient / Doctor / Admin", "User registers a new account or signs into an existing account using email and password.", "<<include>> Authenticate User"),
    @("UC-02", "Authenticate User", "Firebase Auth (Secondary)", "Validates credentials, issues secure tokens, and retrieves user profile with role flag.", "Included by UC-01"),
    @("UC-03", "Book OPD Appointment", "Patient", "Patient fills appointment form selecting Department, Doctor, Date, and entering Symptoms.", "<<include>> Select Dept & Doctor"),
    @("UC-04", "Select Dept & Doctor", "Patient", "Provides dropdown lists of medical departments and verified doctors.", "Included by UC-03"),
    @("UC-05", "Check-In at Hospital", "Patient", "Patient taps Check In upon physical arrival, recording arrival time and joining live queue.", "<<extend>> Track Queue Status"),
    @("UC-06", "Track Live Queue Status", "Patient", "Displays live token number, waiting status badge, doctor name, and arrival instructions.", "Direct Association"),
    @("UC-07", "Upload Medical Reports", "Patient", "Patient selects PDF/Image files from mobile storage to upload directly to Cloud Storage.", "Direct Association"),
    @("UC-08", "View Hospital Location", "Patient", "Displays hospital address and launches turn-by-turn navigation in Google Maps.", "Direct Association"),
    @("UC-09", "View Live Waiting Queue", "Doctor", "Retrieves all patients checked in for the doctor, ordered by Shortest Job First (SJF).", "<<include>> Sort Queue by SJF"),
    @("UC-10", "Sort Queue by SJF", "System / QueueManager", "Sorts waiting patients primarily by estimated consultation time and secondarily by arrival time.", "Included by UC-09"),
    @("UC-11", "Call Next Patient", "Doctor", "Selects the first patient in the SJF queue and updates their status to 'In Consultation'.", "Direct Association"),
    @("UC-12", "Complete Consultation", "Doctor", "Closes the current consultation session and updates patient status to 'Completed'.", "Direct Association"),
    @("UC-13", "View Hospital Statistics", "Administrator", "Displays real-time counters of total registered users and total hospital appointments.", "Direct Association"),
    @("UC-14", "Execute OS Simulator", "Administrator", "Runs interactive simulation comparing FCFS, SJF, and Priority algorithms on test processes.", "Direct Association"),
    @("UC-15", "Access Master Records", "Administrator", "Provides read-only access to all historical hospital appointment records.", "Direct Association"),
    @("UC-16", "Send Push Notification", "System (NotificationHelper)", "Sends instant high-priority banner alert to the patient upon successful appointment booking.", "<<extend>> Book Appointment")
)
Insert-SimpleTable $headersQ3 $rowsQ3

Insert-NewPage
Write-Output "Page 3 complete."

# ==============================================================================
# PAGE 4: QUESTION 4 - Activity Diagram
# ==============================================================================
Insert-QuestionHeading "4" "Operational Workflow: Activity Diagram for Qure"

Insert-Paragraph "An Activity Diagram is a behavioral UML diagram that models the dynamic operational workflow of a system. Similar to a sophisticated flowchart, it visualizes the flow of control from activity to activity, highlighting decision branches, parallel steps, and transitions across different system participants. In multi-user systems, Swimlanes are used to divide activities into columns representing who is responsible for each step." "What is an Activity Diagram?"

Insert-Paragraph "The QURE Activity Diagram depicts the complete outpatient patient journey divided into three clear swimlanes: (1) Patient (Client App), (2) QURE Backend & SJF Scheduler, and (3) Doctor (Clinical App). It shows how a patient books an appointment, arrives at the clinic, triggers the smart SJF queue calculation upon check-in, and undergoes consultation with the doctor until completion." "Workflow Description for QURE:"

Insert-DiagramImage "diag4_activity.png" "QURE UML Activity Diagram depicting the Complete Outpatient Journey across 3 Swimlanes"

$headersQ4 = @("Phase", "Actor / Swimlane", "Action Node", "Detailed Operational Behavior", "Next State")
$rowsQ4 = @(
    @("1. Booking", "Patient", "Fill Appointment Form", "Patient selects department, doctor, date from calendar, and types medical symptoms.", "Backend Booking"),
    @("2. Registration", "Backend", "Generate Token & Record", "System generates unique appointmentId, assigns token number, sets status = 'Registered'.", "Notification"),
    @("3. Alerting", "Backend", "Trigger Push Notification", "NotificationHelper creates notification channel and fires high-priority banner to device.", "Patient Mobile"),
    @("4. Confirmation", "Patient", "Receive Confirmation", "Patient reviews assigned token number and appointment details on patient dashboard.", "Hospital Travel"),
    @("5. Arrival", "Patient", "Tap 'Check In at Hospital'", "Upon reaching hospital premises, patient taps Check In button on Queue Status screen.", "Arrival Logging"),
    @("6. Timestamping", "Backend", "Record Arrival & Set 'Waiting'", "System records System.currentTimeMillis() arrival timestamp and sets status = 'Waiting'.", "SJF Sorting"),
    @("7. Scheduling", "Backend / OS", "QueueManager.sortQueue()", "Applies Shortest Job First (SJF) algorithm: sorts waiting patients by burst time ascending.", "Doctor Queue"),
    @("8. Clinic View", "Doctor", "Load SJF Waiting Queue", "Doctor dashboard loads waiting patients in optimal order (shortest consultation first).", "Call Action"),
    @("9. Call Next", "Doctor", "Tap 'Call Next Patient'", "Doctor calls the first patient from the sorted queue; button disabled if queue empty.", "Status Transition"),
    @("10. Consultation", "Doctor & Patient", "Conduct Consultation", "Status changes to 'In Consultation' (Blue badge). Doctor examines patient in clinic room.", "Completion"),
    @("11. Completion", "Doctor", "Tap 'Complete Consultation'", "Doctor finishes session. Backend updates status = 'Completed' and removes patient from queue.", "Loop Decision"),
    @("12. Decision", "Doctor", "More Patients in Queue?", "If Yes, doctor calls next patient. If No, doctor dashboard returns to 'You are free' state.", "End of OPD Day")
)
Insert-SimpleTable $headersQ4 $rowsQ4

Insert-NewPage
Write-Output "Page 4 complete."

# ==============================================================================
# PAGE 5: QUESTION 5 - Class Diagram
# ==============================================================================
Insert-QuestionHeading "5" "Structural Architecture: Class Diagram for Qure"

Insert-Paragraph "A Class Diagram is the central structural diagram in object-oriented software engineering. It describes the static structure of the application by showing the system's classes, their internal attributes (data fields), their operations (methods and functions), and the relationships between them. In UML class notation, each class is drawn as a box divided into three compartments: (1) Class Name at the top, (2) Attributes in the middle, and (3) Methods at the bottom. Visibility symbols indicate access levels: '+' means public, '-' means private, and '#' means protected." "What is a Class Diagram?"

Insert-Paragraph "The QURE codebase follows a clean, modular Java architecture organized into specialized packages: models (data encapsulation classes like Appointment and Report), database (FirebaseHelper managing cloud database and authentication operations), queue (QueueManager encapsulating the Shortest Job First OS algorithm), utils (Constants and NotificationHelper), and activities (Android UI controllers for each dashboard and screen)." "Class Architecture in QURE:"

Insert-DiagramImage "diag5_class.png" "QURE UML Class Diagram showing Models, Controllers, OS Scheduling Engine, and UI Layer"

$headersQ5 = @("Class Name", "Package", "Key Attributes (Fields)", "Key Methods (Operations)", "Role & Architectural Responsibility")
$rowsQ5 = @(
    @("Appointment", "com.example.qure.models", "appointmentId, patientId, patientName, doctorName, department, appointmentDate, symptoms, status, tokenNumber, arrivalTime, estimatedConsultationTime", "getters and setters for all attributes, empty constructor for Firestore serialization", "POJO data model representing a medical appointment record in Firestore."),
    @("Report", "com.example.qure.models", "reportId, patientId, fileName, fileUrl, uploadDate", "getReportId(), getFileName(), getFileUrl(), getUploadDate()", "POJO data model representing a medical document stored in Firebase Storage."),
    @("SimulatorProcess", "com.example.qure.models", "processName, arrivalTime, burstTime, priority, priorityLabel", "getProcessName(), getArrivalTime(), getBurstTime(), getPriority()", "Data model representing a test process in the OS Scheduling Simulator."),
    @("QueueManager", "com.example.qure.queue", "None (Utility / Engine)", "+ sortQueue(queue: List<Appointment>): List<Appointment>, - parseArrivalTime(timeStr: String): long", "Core OS algorithm engine. Implements Shortest Job First (SJF) comparator sorting on waiting queues."),
    @("FirebaseHelper", "com.example.qure.database", "- mAuth: FirebaseAuth, - mFirestore: FirebaseFirestore", "+ registerUser(), + loginUser(), + bookAppointment(), + checkInAppointment(), + getDoctorQueue(), + startConsultation(), + completeConsultation(), + getSystemStatistics(), + getAllAppointments()", "Central database controller. Handles all asynchronous Firebase Authentication and Firestore CRUD operations with callback interfaces."),
    @("NotificationHelper", "com.example.qure.utils", "+ CHANNEL_ID: String, + CHANNEL_NAME: String", "+ createNotificationChannel(context: Context): void, + showNotification(context: Context, title: String, message: String, id: int): void", "Android utility managing notification channels and firing high-priority notification banners."),
    @("Constants", "com.example.qure.utils", "COLLECTION_APPOINTMENTS, COLLECTION_USERS, STATUS_REGISTERED, STATUS_WAITING, STATUS_IN_CONSULTATION, STATUS_COMPLETED, ALGO_SJF", "Static constant declarations (no methods)", "Central repository for collection names, intent extras, shared preference keys, and statuses."),
    @("Activities Layer", "com.example.qure.activities", "UI widgets, adapters, listeners", "onCreate(), initUI(), loadData(), onClick()", "Android Activity controllers (PatientDashboard, DoctorDashboard, AdminDashboard, BookAppointment, QueueStatus, etc.).")
)
Insert-SimpleTable $headersQ5 $rowsQ5

Insert-NewPage
Write-Output "Page 5 complete."

# ==============================================================================
# PAGE 6: QUESTION 6 - Sequence Diagram
# ==============================================================================
Insert-QuestionHeading "6" "Behavioral View Diagram: Sequence Diagram for Qure"

Insert-Paragraph "A Sequence Diagram is an interaction diagram that details how operations are carried out over time. It captures the dynamic behavior of a specific use case by illustrating the objects involved, their vertical 'lifelines' that extend downwards across time, and the horizontal arrows representing messages (method calls, events, and return data) passed between them in strict chronological order from top to bottom. Dashed return arrows indicate responses or asynchronous callback completions." "What is a Sequence Diagram?"

Insert-Paragraph "In QURE, the primary behavioral sequence represents the core end-to-end hospital scenario: 'Patient Check-In, SJF Queue Reordering, and Doctor Consultation Session'. It demonstrates how the Patient UI communicates with FirebaseHelper, how Cloud Firestore updates arrival timestamps, how the Doctor Dashboard queries waiting patients, and how QueueManager automatically executes the Shortest Job First algorithm before presenting patients to the doctor." "Scenario Description for QURE:"

Insert-DiagramImage "diag6_sequence.png" "QURE UML Sequence Diagram showing the Chronological Execution Flow of Check-In & Consultation"

$headersQ6 = @("Step #", "Sender Object", "Receiver Object", "Message / Method Invocation", "Purpose & Operational Effect")
$rowsQ6 = @(
    @("1", "Patient (User)", ":QueueStatusActivity", "tap 'CHECK IN AT HOSPITAL'", "User indicates physical arrival at the hospital outpatient department."),
    @("2", ":QueueStatusActivity", ":FirebaseHelper", "checkInAppointment(apptId)", "UI initiates database update via controller method."),
    @("3", ":FirebaseHelper", ":CloudFirestore", "update(status='Waiting', arrivalTime=now)", "Firestore sets arrival timestamp and changes status from 'Registered' to 'Waiting'."),
    @("4", ":CloudFirestore", ":FirebaseHelper", "onSuccess() [Callback]", "Cloud Firestore confirms document update successfully committed."),
    @("5", ":FirebaseHelper", ":QueueStatusActivity", "callback.onSuccess()", "Helper triggers UI callback to refresh screen state."),
    @("6", ":QueueStatusActivity", "Patient (User)", "display 'Status: Waiting' (Orange badge)", "Patient screen confirms successful check-in and instructs patient to wait in lobby."),
    @("7", "Doctor (User)", ":DoctorDashboardActivity", "opens Doctor Dashboard", "Doctor logs into clinic terminal to begin outpatient hours."),
    @("8", ":DoctorDashboardActivity", ":FirebaseHelper", "getDoctorQueue(doctorName)", "Activity requests all waiting appointments assigned to the logged-in doctor."),
    @("9", ":FirebaseHelper", ":CloudFirestore", "where(doctor=name, status='Waiting')", "Database queries all checked-in patients awaiting consultation."),
    @("10", ":CloudFirestore", ":FirebaseHelper", "return raw waiting list", "Firestore returns unordered list of matching appointment documents."),
    @("11", ":FirebaseHelper", ":DoctorDashboardActivity", "onQueueLoaded(rawQueue)", "Helper passes raw patient collection back to Doctor Activity."),
    @("12", ":DoctorDashboardActivity", ":QueueManager", "sortQueue(rawQueue) [SJF]", "Activity delegates queue reordering to the OS algorithm engine."),
    @("13", ":QueueManager", ":DoctorDashboardActivity", "return sortedQueue", "QueueManager sorts list by estimated burst time ascending and returns optimal list."),
    @("14", ":DoctorDashboardActivity", "Doctor (User)", "render SJF sorted queue in RecyclerView", "Doctor visualizes patient list with shortest consultations at the top of the queue."),
    @("15", "Doctor (User)", ":DoctorDashboardActivity", "tap 'CALL NEXT PATIENT'", "Doctor summons the top patient from the sorted queue."),
    @("16", ":DoctorDashboardActivity", ":FirebaseHelper", "startConsultation(apptId)", "Activity requests status promotion for selected patient."),
    @("17", ":FirebaseHelper", ":CloudFirestore", "update(status='In Consultation')", "Database marks patient as currently in doctor consultation room."),
    @("18", "Doctor (User)", ":DoctorDashboardActivity", "tap 'COMPLETE CONSULTATION'", "Doctor finishes medical examination and closes consultation session."),
    @("19", ":DoctorDashboardActivity", ":CloudFirestore", "update(status='Completed')", "Database finalizes visit; patient is removed from active waiting list.")
)
Insert-SimpleTable $headersQ6 $rowsQ6

Insert-NewPage
Write-Output "Page 6 complete."

# ==============================================================================
# PAGE 7: QUESTION 7 - Collaboration (Communication) Diagram
# ==============================================================================
Insert-QuestionHeading "7" "Behavioral View (Collaboration) Diagram for Qure"

Insert-Paragraph "A Collaboration Diagram (formally designated as a Communication Diagram in UML 2.0) is a structural and behavioral diagram that models object interactions. While a Sequence Diagram emphasizes the chronological timeline of events, a Collaboration Diagram emphasizes the structural architecture and organizational layout of communicating objects. Objects are connected by association links, and interactions are represented by numbered directional messages (such as 1, 1.1, 1.2, 2) drawn alongside the links. This numbering scheme clearly shows the hierarchy and nesting of requests across system tiers." "What is a Collaboration Diagram?"

Insert-Paragraph "In QURE, the Collaboration Diagram illustrates how mobile client UI activities, database controllers, operating system queue engines, utility services, and cloud datastores cooperate as a coherent network. It demonstrates that the UI never accesses the database directly; instead, all communications pass through the FirebaseHelper controller and QueueManager engine, preserving clean separation of concerns and MVC architecture." "Collaboration Structure in QURE:"

Insert-DiagramImage "diag7_collaboration.png" "QURE UML Collaboration Diagram showing Architecture Links and Hierarchical Message Passing"

$headersQ7 = @("Message Sequence", "Source Object", "Target Object", "Operation Invoked", "Message Semantic Description")
$rowsQ7 = @(
    @("1", ":Patient", ":QueueStatusActivity", "checkIn()", "Patient taps the check-in button on the mobile client screen."),
    @("1.1", ":QueueStatusActivity", ":FirebaseHelper", "checkInAppointment(id)", "Activity delegates check-in processing to the central database helper."),
    @("1.2", ":FirebaseHelper", ":CloudFirestore", "update(status='Waiting', arrivalTime)", "Helper issues asynchronous document update to the appointments collection."),
    @("1.3", ":FirebaseHelper", ":QueueStatusActivity", "onSuccess()", "Callback notifies the activity that database update committed successfully."),
    @("1.4", ":QueueStatusActivity", ":Patient", "displayWaiting()", "UI updates visual status badge to Orange ('Waiting') and hides check-in button."),
    @("2", ":Doctor", ":DoctorDashboardActivity", "loadDashboard()", "Doctor launches outpatient clinic session on doctor dashboard."),
    @("2.1", ":DoctorDashboardActivity", ":FirebaseHelper", "getDoctorQueue(docName)", "Activity requests current list of waiting outpatient appointments."),
    @("2.2", ":FirebaseHelper", ":CloudFirestore", "queryWaitingList()", "Helper executes Firestore query for appointments where status == 'Waiting'."),
    @("2.3", ":DoctorDashboardActivity", ":QueueManager", "sortQueue(list) [SJF]", "Activity sends unsorted appointments to QueueManager for OS scheduling."),
    @("2.4", ":QueueManager", ":DoctorDashboardActivity", "return sortedList", "QueueManager returns sorted collection with shortest consultations first."),
    @("3", ":Doctor", ":DoctorDashboardActivity", "callNextPatient()", "Doctor clicks 'CALL NEXT PATIENT' button on active consultation card."),
    @("3.1", ":DoctorDashboardActivity", ":FirebaseHelper", "startConsultation(id)", "Activity instructs helper to advance the top patient into consultation."),
    @("3.2", ":FirebaseHelper", ":CloudFirestore", "update(status='In Consultation')", "Helper updates Firestore document to active consultation state."),
    @("4", ":Doctor", ":DoctorDashboardActivity", "completeConsultation()", "Doctor clicks 'COMPLETE CONSULTATION' after examining the patient."),
    @("4.1", ":DoctorDashboardActivity", ":FirebaseHelper", "completeConsultation(id)", "Activity instructs helper to close the consultation session."),
    @("4.2", ":FirebaseHelper", ":CloudFirestore", "update(status='Completed')", "Helper updates Firestore document to finalized completed state."),
    @("5", ":BookAppointmentActivity", ":NotificationHelper", "showNotification()", "Upon appointment booking, activity triggers push notification alert to device.")
)
Insert-SimpleTable $headersQ7 $rowsQ7

Insert-NewPage
Write-Output "Page 7 complete."

# ==============================================================================
# PAGE 8: QUESTION 8 - Data Flow Diagram (DFD Level 0 & Level 1)
# ==============================================================================
Insert-QuestionHeading "8" "Functional Information Flows: Data Flow Diagram (DFD) for Qure"

Insert-Paragraph "A Data Flow Diagram (DFD) is a traditional structured analysis technique that models how data flows through a system, where data originated, how it is processed or transformed, and where it is stored. Unlike UML diagrams (which focus on software objects and classes), a DFD focuses strictly on information movement and business logic. A DFD uses four standard notations: (1) External Entities (rectangles representing external people or systems exchanging data), (2) Processes (circles/rounded boxes representing data transformations), (3) Data Stores (open-ended rectangles representing persistent databases), and (4) Data Flows (labeled directional arrows showing the path of data packets)." "What is a Data Flow Diagram?"

Insert-Paragraph "For QURE, data flow analysis is divided into two structured levels: Level 0 (Context Diagram) establishes the overarching system boundary and shows raw data exchanges between the central QURE system and external stakeholders (Patient, Doctor, Admin). Level 1 (Decomposition Diagram) expands the system into its six core sub-processes (1.0 Authentication, 2.0 Appointment Booking, 3.0 Check-In, 4.0 SJF Queue Engine, 5.0 Consultation Processing, 6.0 System Analytics) and maps interactions with persistent datastores (D1: users, D2: appointments, D3: reports)." "DFD Levels in QURE:"

Insert-DiagramImage "diag8_dfd.png" "QURE Data Flow Diagrams: Level 0 Context Analysis (Top) and Level 1 Functional Decomposition (Bottom)"

$headersQ8 = @("DFD Element", "Element Type", "Name / Identifier", "Data Flow / Transformation Description")
$rowsQ8 = @(
    @("Entity 1", "External Entity", "PATIENT", "Inputs appointment details, symptoms, check-in signals, report files; receives tokens and queue updates."),
    @("Entity 2", "External Entity", "DOCTOR", "Inputs clinic availability, call signals, completion marks; receives SJF-sorted waiting lists."),
    @("Entity 3", "External Entity", "ADMINISTRATOR", "Inputs simulation parameters and query filters; receives live hospital statistics and global audit records."),
    @("Process 0.0", "Context Process", "QURE Smart Hospital System", "Central Level 0 processing hub managing all hospital outpatient operations, queue logic, and cloud storage."),
    @("Process 1.0", "Sub-Process (Level 1)", "User Authentication & Roles", "Validates user login credentials against D1 (users) and directs session to authorized dashboard role."),
    @("Process 2.0", "Sub-Process (Level 1)", "Book Appointment & Token Gen", "Accepts appointment inputs, generates token numbers, simulates burst times, and writes record to D2."),
    @("Process 3.0", "Sub-Process (Level 1)", "Hospital Check-In & Arrival", "Receives check-in trigger, captures arrival timestamp, and updates appointment state in D2 to 'Waiting'."),
    @("Process 4.0", "Sub-Process (Level 1)", "SJF Queue Scheduling Engine", "Extracts waiting appointments from D2, sorts via Shortest Job First algorithm, and streams output to Doctor."),
    @("Process 5.0", "Sub-Process (Level 1)", "Consultation Management", "Processes 'In Consultation' and 'Completed' transitions, updating appointment lifecycle states in D2."),
    @("Process 6.0", "Sub-Process (Level 1)", "Live System Analytics", "Aggregates document counts across D1 (users) and D2 (appointments) to produce real-time administrative metrics."),
    @("Store D1", "Persistent Data Store", "users Collection", "Stores user account records (userId, email, name, role) in Cloud Firestore."),
    @("Store D2", "Persistent Data Store", "appointments Collection", "Stores OPD appointment documents (appointmentId, patientId, doctorName, date, token, status, arrivalTime, burstTime)."),
    @("Store D3", "Persistent Data Store", "reports Collection", "Stores medical document metadata (reportId, patientId, fileName, fileUrl, uploadDate) and Cloud Storage URLs.")
)
Insert-SimpleTable $headersQ8 $rowsQ8

# Save the Document
$doc.SaveAs([ref]$targetDocx, [ref]16) # 16 = wdFormatXMLDocument (.docx)
$doc.Close()
$word.Quit()

Write-Output "Saved document to: $targetDocx"

# Copy to Desktop if available
if (Test-Path "C:\Users\LENOVO\Desktop") {
    Copy-Item -Path $targetDocx -Destination $desktopDocx -Force
    Write-Output "Copied to Desktop: $desktopDocx"
}

Write-Output "ALL 8 QUESTIONS COMPLETED WITH NEW PAGES AND EMBEDDED DIAGRAMS!"
