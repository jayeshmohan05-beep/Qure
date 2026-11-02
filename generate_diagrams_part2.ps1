Add-Type -AssemblyName System.Drawing

$imgDir = "C:\Users\LENOVO\AndroidStudioProjects\QURE\docs_diagrams"

# Color Palette
$cNavyDark   = [System.Drawing.Color]::FromArgb(13, 27, 110)
$cNavy       = [System.Drawing.Color]::FromArgb(26, 35, 126)
$cNavyLight  = [System.Drawing.Color]::FromArgb(40, 53, 147)
$cTeal       = [System.Drawing.Color]::FromArgb(0, 137, 123)
$cTealDark   = [System.Drawing.Color]::FromArgb(0, 105, 92)
$cTealLight  = [System.Drawing.Color]::FromArgb(224, 242, 241)
$cCardBg     = [System.Drawing.Color]::FromArgb(255, 255, 255)
$cPageBg     = [System.Drawing.Color]::FromArgb(245, 247, 250)
$cBorder     = [System.Drawing.Color]::FromArgb(207, 216, 220)
$cTextDark   = [System.Drawing.Color]::FromArgb(33, 33, 33)
$cTextGray   = [System.Drawing.Color]::FromArgb(117, 117, 117)
$cOrange     = [System.Drawing.Color]::FromArgb(255, 152, 0)
$cGreen      = [System.Drawing.Color]::FromArgb(76, 175, 80)
$cBlueSoft   = [System.Drawing.Color]::FromArgb(232, 234, 246)
$cWhite      = [System.Drawing.Color]::White

$cPink       = [System.Drawing.Color]::FromArgb(194, 24, 91)
$cOrangeDark = [System.Drawing.Color]::FromArgb(230, 81, 0)
$cPurple     = [System.Drawing.Color]::FromArgb(106, 27, 154)
$cBlueGray   = [System.Drawing.Color]::FromArgb(55, 71, 79)

function Draw-RoundedRect($g, $pen, $brush, $x, $y, $w, $h, $r) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $d = $r * 2
    $path.AddArc($x, $y, $d, $d, 180, 90)
    $path.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $path.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $path.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $path.CloseFigure()
    if ($brush -ne $null) { $g.FillPath($brush, $path) }
    if ($pen -ne $null) { $g.DrawPath($pen, $path) }
    $path.Dispose()
}

function Draw-Arrow($g, $pen, [float]$x1, [float]$y1, [float]$x2, [float]$y2, [float]$arrowLen = 12.0) {
    $g.DrawLine($pen, $x1, $y1, $x2, $y2)
    $angle = [Math]::Atan2($y2 - $y1, $x2 - $x1)
    $dx1 = $arrowLen * [Math]::Cos($angle - ([Math]::PI / 6.0))
    $dy1 = $arrowLen * [Math]::Sin($angle - ([Math]::PI / 6.0))
    $dx2 = $arrowLen * [Math]::Cos($angle + ([Math]::PI / 6.0))
    $dy2 = $arrowLen * [Math]::Sin($angle + ([Math]::PI / 6.0))
    
    $pts = [System.Drawing.PointF[]]@(
        (New-Object System.Drawing.PointF($x2, $y2)),
        (New-Object System.Drawing.PointF(($x2 - $dx1), ($y2 - $dy1))),
        (New-Object System.Drawing.PointF(($x2 - $dx2), ($y2 - $dy2)))
    )
    $brush = New-Object System.Drawing.SolidBrush($pen.Color)
    $g.FillPolygon($brush, $pts)
    $brush.Dispose()
}

$fTitle = New-Object System.Drawing.Font("Segoe UI", 20, [System.Drawing.FontStyle]::Bold)
$fSub = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Regular)
$fBold = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
$fReg = New-Object System.Drawing.Font("Segoe UI", 9.5, [System.Drawing.FontStyle]::Regular)
$fSmall = New-Object System.Drawing.Font("Segoe UI", 8.5, [System.Drawing.FontStyle]::Regular)
$wBrush = New-Object System.Drawing.SolidBrush($cWhite)

# ==============================================================================
# DIAGRAM 5: Class Diagram for QURE
# ==============================================================================
$w = 1400; $h = 960
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear($cPageBg)

# Header
$hdrBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($hdrBrush, 0, 0, $w, 85)
$hdrBrush.Dispose()

$g.DrawString("QURE - UML Class Diagram (Structural Code Architecture)", $fTitle, $wBrush, 30, 16)
$g.DrawString("Object-Oriented Design Models, Attributes, Operations, and Inter-Class Relationships", $fSub, $wBrush, 32, 54)

# Helper: Draw UML Class Box (3 compartments)
function Draw-UMLClass($g, $x, $y, $w, $h, $name, $pkg, $attrs, $methods, $hdrColor) {
    $pen = New-Object System.Drawing.Pen($hdrColor, 2)
    $bg = New-Object System.Drawing.SolidBrush($cWhite)
    Draw-RoundedRect $g $pen $bg $x $y $w $h 8
    $bg.Dispose()
    
    # Header bar
    $hBrush = New-Object System.Drawing.SolidBrush($hdrColor)
    $g.FillRectangle($hBrush, ($x + 1), ($y + 1), ($w - 2), 40)
    $hBrush.Dispose()
    
    $fName = New-Object System.Drawing.Font("Segoe UI", 10.5, [System.Drawing.FontStyle]::Bold)
    $fPkg = New-Object System.Drawing.Font("Segoe UI", 8, [System.Drawing.FontStyle]::Italic)
    $g.DrawString($name, $fName, $wBrush, ($x + 10), ($y + 6))
    $g.DrawString("<<$pkg>>", $fPkg, $wBrush, ($x + 10), ($y + 24))
    $fName.Dispose(); $fPkg.Dispose()
    
    # Divider 1
    $divPen = New-Object System.Drawing.Pen($cBorder, 1)
    $g.DrawLine($divPen, $x, ($y + 41), ($x + $w), ($y + 41))
    
    # Attributes
    $aY = $y + 47
    $fAttr = New-Object System.Drawing.Font("Segoe UI", 8.5, [System.Drawing.FontStyle]::Regular)
    $txtB = New-Object System.Drawing.SolidBrush($cTextDark)
    foreach ($att in $attrs) {
        $g.DrawString($att, $fAttr, $txtB, ($x + 8), $aY)
        $aY += 16
    }
    
    # Divider 2
    $g.DrawLine($divPen, $x, $aY, ($x + $w), $aY)
    $aY += 6
    
    # Methods
    foreach ($m in $methods) {
        $g.DrawString($m, $fAttr, $txtB, ($x + 8), $aY)
        $aY += 16
    }
    
    $fAttr.Dispose(); $txtB.Dispose(); $divPen.Dispose(); $pen.Dispose()
}

# 1. Appointment Class (Model)
$apptAttrs = @(
    "- appointmentId : String",
    "- patientId : String",
    "- patientName : String",
    "- doctorName : String",
    "- department : String",
    "- appointmentDate : String",
    "- symptoms : String",
    "- status : String",
    "- tokenNumber : int",
    "- arrivalTime : String",
    "- estimatedConsultationTime : int"
)
$apptMethods = @(
    "+ getAppointmentId() : String",
    "+ getPatientName() : String",
    "+ getStatus() : String",
    "+ getTokenNumber() : int",
    "+ getArrivalTime() : String",
    "+ getEstimatedConsultationTime() : int",
    "+ setStatus(status: String) : void",
    "+ setArrivalTime(time: String) : void"
)
Draw-UMLClass $g 40 110 320 330 "Appointment" "model" $apptAttrs $apptMethods $cNavy

# 2. QueueManager Class (Scheduling Engine)
$qmAttrs = @(
    " "
)
$qmMethods = @(
    "+ sortQueue(queue: List<Appointment>) : List<Appointment>",
    "- parseArrivalTime(timeStr: String) : long",
    "/* SJF OS Algorithm Implementation */",
    "/* Primary sort: Estimated Burst Time */",
    "/* Secondary fallback: Arrival Time */"
)
Draw-UMLClass $g 40 470 320 180 "QueueManager" "queue" $qmAttrs $qmMethods $cTealDark

# 3. FirebaseHelper Class (Database & Auth Controller)
$fbAttrs = @(
    "- mAuth : FirebaseAuth",
    "- mFirestore : FirebaseFirestore"
)
$fbMethods = @(
    "+ registerUser(email, pass, name, role, cb) : void",
    "+ loginUser(email, pass, cb) : void",
    "+ bookAppointment(patientId, doc, dept, date, symp, cb)",
    "+ getActiveAppointment(patientId, cb) : void",
    "+ checkInAppointment(apptId, cb) : void",
    "+ getDoctorQueue(doctorName, cb) : void",
    "+ getDoctorCurrentPatient(doctorName, cb) : void",
    "+ startConsultation(apptId, cb) : void",
    "+ completeConsultation(apptId, cb) : void",
    "+ getSystemStatistics(cb) : void",
    "+ getAllAppointments(cb) : void",
    "+ logoutUser() : void"
)
Draw-UMLClass $g 440 110 440 330 "FirebaseHelper" "database" $fbAttrs $fbMethods $cNavyDark

# 4. Report Class (Model)
$repAttrs = @(
    "- reportId : String",
    "- patientId : String",
    "- fileName : String",
    "- fileUrl : String",
    "- uploadDate : String"
)
$repMethods = @(
    "+ getReportId() : String",
    "+ getFileName() : String",
    "+ getFileUrl() : String",
    "+ getUploadDate() : String"
)
Draw-UMLClass $g 440 470 230 180 "Report" "model" $repAttrs $repMethods $cPink

# 5. NotificationHelper Class (Utility)
$notifAttrs = @(
    "+ CHANNEL_ID : String",
    "+ CHANNEL_NAME : String"
)
$notifMethods = @(
    "+ createNotificationChannel(context) : void",
    "+ showNotification(context, title, msg, id) : void"
)
Draw-UMLClass $g 700 470 260 180 "NotificationHelper" "utils" $notifAttrs $notifMethods $cOrangeDark

# 6. SimulatorProcess Class (Model)
$simAttrs = @(
    "- processName : String",
    "- arrivalTime : int",
    "- burstTime : int",
    "- priority : int",
    "- priorityLabel : String"
)
$simMethods = @(
    "+ getProcessName() : String",
    "+ getArrivalTime() : int",
    "+ getBurstTime() : int",
    "+ getPriority() : int"
)
Draw-UMLClass $g 980 470 230 180 "SimulatorProcess" "models" $simAttrs $simMethods $cPurple

# 7. Constants Class (Utility)
$constAttrs = @(
    "+ COLLECTION_APPOINTMENTS : String",
    "+ COLLECTION_USERS : String",
    "+ STATUS_REGISTERED : String",
    "+ STATUS_WAITING : String",
    "+ STATUS_IN_CONSULTATION : String",
    "+ STATUS_COMPLETED : String",
    "+ ALGO_SJF : String = 'SJF'"
)
$constMethods = @(
    "/* Global configuration registry */"
)
Draw-UMLClass $g 980 110 380 200 "Constants" "utils" $constAttrs $constMethods $cBlueGray

# 8. Activity Layer Box (Lower section)
$actAttrs = @(
    "+ PatientDashboardActivity",
    "+ BookAppointmentActivity",
    "+ QueueStatusActivity",
    "+ MedicalReportsActivity",
    "+ HospitalLocationActivity"
)
$actMethods = @(
    "+ DoctorDashboardActivity",
    "+ AdminDashboardActivity",
    "+ OsSimulatorActivity",
    "+ LoginActivity",
    "+ RegisterActivity"
)
Draw-UMLClass $g 40 680 920 180 "Activities Layer (Android UI Components)" "activities" $actAttrs $actMethods $cNavyLight

# Draw Relationship Lines
$relPen = New-Object System.Drawing.Pen($cNavy, 2)
# FirebaseHelper <---> Appointment (Dependency/Association)
Draw-Arrow $g $relPen 440 250 360 250
$g.DrawString("1..* manages", $fSmall, $relPen.Brush, 370, 230)

# QueueManager ---> Appointment (Uses)
Draw-Arrow $g $relPen 170 470 170 440
$g.DrawString("<<sorts>>", $fSmall, $relPen.Brush, 180, 450)

# FirebaseHelper ---> Report
Draw-Arrow $g $relPen 550 440 550 470
$g.DrawString("1..* stores", $fSmall, $relPen.Brush, 560, 448)

# Activities Layer ---> FirebaseHelper
Draw-Arrow $g $relPen 600 680 600 440
$g.DrawString("<<invokes APIs>>", $fSmall, $relPen.Brush, 610, 560)

# Activities Layer ---> QueueManager
Draw-Arrow $g $relPen 200 680 200 650
$g.DrawString("<<invokes SJF>>", $fSmall, $relPen.Brush, 210, 660)

# Activities Layer ---> NotificationHelper
Draw-Arrow $g $relPen 830 680 830 650
$g.DrawString("<<triggers>>", $fSmall, $relPen.Brush, 840, 660)

$relPen.Dispose()

$bmp.Save("$imgDir\diag5_class.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 5 generated."

# ==============================================================================
# DIAGRAM 6: Sequence Diagram for QURE
# ==============================================================================
$w = 1400; $h = 960
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear($cPageBg)

# Header
$hdrBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($hdrBrush, 0, 0, $w, 85)
$hdrBrush.Dispose()

$g.DrawString("QURE - UML Sequence Diagram (Behavioral Interaction)", $fTitle, $wBrush, 30, 16)
$g.DrawString("Chronological message flow for Patient Check-In, SJF Queue Sorting, and Doctor Consultation", $fSub, $wBrush, 32, 54)

# 7 Lifelines
$lifeLines = @(
    @{ Name="Patient`n(User)"; X=100; Color=$cNavy },
    @{ Name=":QueueStatus`nActivity"; X=300; Color=$cNavyLight },
    @{ Name=":Firebase`nHelper"; X=510; Color=$cNavyDark },
    @{ Name=":Cloud`nFirestore"; X=720; Color=$cOrangeDark },
    @{ Name=":Queue`nManager"; X=920; Color=$cTealDark },
    @{ Name=":DoctorDashboard`nActivity"; X=1120; Color=$cTeal },
    @{ Name="Doctor`n(User)"; X=1300; Color=$cTealDark }
)

$lineTop = 150
$lineBottom = 880
$dashPen = New-Object System.Drawing.Pen($cBorder, 1.5)
$dashPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash

foreach ($lf in $lifeLines) {
    # Header box
    $boxPen = New-Object System.Drawing.Pen($lf.Color, 2)
    $boxBrush = New-Object System.Drawing.SolidBrush($cWhite)
    Draw-RoundedRect $g $boxPen $boxBrush ($lf.X - 65) 100 130 50 8
    $boxBrush.Dispose(); $boxPen.Dispose()
    
    $tBrush = New-Object System.Drawing.SolidBrush($lf.Color)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF(($lf.X - 65), 100, 130, 50)
    $g.DrawString($lf.Name, $fSmall, $tBrush, $rect, $sf)
    $tBrush.Dispose(); $sf.Dispose()
    
    # Vertical dashed line
    $g.DrawLine($dashPen, $lf.X, $lineTop, $lf.X, $lineBottom)
}
$dashPen.Dispose()

# Helper: Draw Message Arrow
function Draw-SeqMsg($g, $x1, $y1, $x2, $y2, $num, $text, $isReturn = $false, $color = $cNavy) {
    $pen = New-Object System.Drawing.Pen($color, 1.8)
    if ($isReturn) {
        $pen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
    }
    Draw-Arrow $g $pen $x1 $y1 $x2 $y2 10.0
    $pen.Dispose()
    
    $fMsg = New-Object System.Drawing.Font("Segoe UI", 8.5, [System.Drawing.FontStyle]::Bold)
    $tBrush = New-Object System.Drawing.SolidBrush($cTextDark)
    $lbl = "$num. $text"
    $midX = ($x1 + $x2) / 2
    $g.DrawString($lbl, $fMsg, $tBrush, ($midX - 110), ($y1 - 16))
    $fMsg.Dispose(); $tBrush.Dispose()
}

# Message sequence
$mY = 180
# 1. Patient taps Check In
Draw-SeqMsg $g 100 $mY 300 $mY "1" "tap 'CHECK IN AT HOSPITAL'"
$mY += 45
# 2. Activity calls helper
Draw-SeqMsg $g 300 $mY 510 $mY "2" "checkInAppointment(apptId)"
$mY += 45
# 3. Helper updates Firestore
Draw-SeqMsg $g 510 $mY 720 $mY "3" "update(status='Waiting', arrivalTime=now)"
$mY += 45
# 4. Firestore confirms
Draw-SeqMsg $g 720 $mY 510 $mY "4" "onSuccess()" $true
$mY += 45
# 5. Helper notifies UI
Draw-SeqMsg $g 510 $mY 300 $mY "5" "callback.onSuccess()" $true
$mY += 45
# 6. UI updates status to Waiting
Draw-SeqMsg $g 300 $mY 100 $mY "6" "display 'Status: Waiting' (Orange Badge)" $true
$mY += 60

# Part B: Doctor side
# 7. Doctor opens dashboard
Draw-SeqMsg $g 1300 $mY 1120 $mY "7" "opens Doctor Dashboard"
$mY += 45
# 8. Activity requests queue
Draw-SeqMsg $g 1120 $mY 510 $mY "8" "getDoctorQueue(doctorName)"
$mY += 45
# 9. Helper queries Firestore
Draw-SeqMsg $g 510 $mY 720 $mY "9" "where(status='Waiting', doctor=name)"
$mY += 45
# 10. Firestore returns raw appointments
Draw-SeqMsg $g 720 $mY 510 $mY "10" "return raw waiting list" $true
$mY += 45
# 11. Helper returns to Activity
Draw-SeqMsg $g 510 $mY 1120 $mY "11" "onQueueLoaded(rawQueue)" $true
$mY += 45
# 12. Activity passes to QueueManager
Draw-SeqMsg $g 1120 $mY 920 $mY "12" "QueueManager.sortQueue(rawQueue)"
$mY += 45
# 13. QueueManager sorts via SJF and returns
Draw-SeqMsg $g 920 $mY 1120 $mY "13" "return SJF sorted list (Shortest Burst first)" $true
$mY += 45
# 14. Activity displays queue to Doctor
Draw-SeqMsg $g 1120 $mY 1300 $mY "14" "render SJF sorted queue in RecyclerView" $true
$mY += 55

# Part C: Consultation
# 15. Doctor taps Call Next Patient
Draw-SeqMsg $g 1300 $mY 1120 $mY "15" "tap 'CALL NEXT PATIENT'"
$mY += 45
# 16. Start consultation
Draw-SeqMsg $g 1120 $mY 510 $mY "16" "startConsultation(apptId)"
$mY += 45
# 17. Firestore update
Draw-SeqMsg $g 510 $mY 720 $mY "17" "update(status='In Consultation')"
$mY += 45
# 18. Complete consultation
Draw-SeqMsg $g 1300 $mY 1120 $mY "18" "tap 'COMPLETE CONSULTATION'"
$mY += 45
# 19. Finish in Firestore
Draw-SeqMsg $g 1120 $mY 720 $mY "19" "update(status='Completed')"

$bmp.Save("$imgDir\diag6_sequence.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 6 generated."

# ==============================================================================
# DIAGRAM 7: Collaboration (Communication) Diagram for QURE
# ==============================================================================
$w = 1400; $h = 960
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear($cPageBg)

# Header
$hdrBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($hdrBrush, 0, 0, $w, 85)
$hdrBrush.Dispose()

$g.DrawString("QURE - UML Collaboration (Communication) Diagram", $fTitle, $wBrush, 30, 16)
$g.DrawString("Structural Architecture, Object Links, and Numbered Message Interactions across Modules", $fSub, $wBrush, 32, 54)

# Helper: Draw Object Node Box
function Draw-CollabObject($g, $x, $y, $w, $h, $name, $role, $col) {
    $pen = New-Object System.Drawing.Pen($col, 2)
    $bg = New-Object System.Drawing.SolidBrush($cWhite)
    Draw-RoundedRect $g $pen $bg $x $y $w $h 10
    $bg.Dispose(); $pen.Dispose()
    
    $fObj = New-Object System.Drawing.Font("Segoe UI", 10.5, [System.Drawing.FontStyle]::Bold)
    $fSubO = New-Object System.Drawing.Font("Segoe UI", 8.5, [System.Drawing.FontStyle]::Regular)
    $tB = New-Object System.Drawing.SolidBrush($col)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    
    $rect = New-Object System.Drawing.RectangleF($x, $y, $w, ($h * 0.6))
    $g.DrawString($name, $fObj, $tB, $rect, $sf)
    
    $rectSub = New-Object System.Drawing.RectangleF($x, ($y + ($h * 0.5)), $w, ($h * 0.45))
    $g.DrawString("<<$role>>", $fSubO, $tB, $rectSub, $sf)
    
    $fObj.Dispose(); $fSubO.Dispose(); $tB.Dispose(); $sf.Dispose()
}

# 8 Objects positioned strategically
Draw-CollabObject $g 60 140 220 75 ":Patient" "Client Actor" $cNavy
Draw-CollabObject $g 360 140 250 75 ":QueueStatusActivity" "Boundary / UI" $cNavyLight
Draw-CollabObject $g 700 320 250 75 ":FirebaseHelper" "Controller" $cNavyDark
Draw-CollabObject $g 1050 320 250 75 ":CloudFirestore" "Database Service" $cOrangeDark
Draw-CollabObject $g 700 600 250 75 ":QueueManager" "OS Algorithm Engine" $cTealDark
Draw-CollabObject $g 360 760 260 75 ":DoctorDashboardActivity" "Boundary / UI" $cTeal
Draw-CollabObject $g 60 760 220 75 ":Doctor" "Clinical Actor" $cTealDark
Draw-CollabObject $g 1050 760 250 75 ":NotificationHelper" "Utility Service" $cPink

# Helper: Draw Link with messages
$linkPen = New-Object System.Drawing.Pen($cNavy, 2)
$fMsg = New-Object System.Drawing.Font("Segoe UI", 8.5, [System.Drawing.FontStyle]::Bold)
$mBrush = New-Object System.Drawing.SolidBrush($cTextDark)

# Link 1: Patient <---> QueueStatusActivity
$g.DrawLine($linkPen, 280, 177, 360, 177)
$g.DrawString("1: checkIn()`n1.4: displayWaiting()", $fMsg, $mBrush, 240, 200)

# Link 2: QueueStatusActivity <---> FirebaseHelper
$g.DrawLine($linkPen, 610, 177, 730, 320)
$g.DrawString("1.1: checkInAppointment()`n1.3: onSuccess()", $fMsg, $mBrush, 580, 230)

# Link 3: FirebaseHelper <---> CloudFirestore
$g.DrawLine($linkPen, 950, 357, 1050, 357)
$g.DrawString("1.2: update(status='Waiting')`n2.2: queryWaitingList()`n3.2: update('In Consultation')`n4.2: update('Completed')", $fMsg, $mBrush, 960, 380)

# Link 4: Doctor <---> DoctorDashboardActivity
$g.DrawLine($linkPen, 280, 797, 360, 797)
$g.DrawString("2: loadDashboard()`n3: callNextPatient()`n4: completeConsultation()", $fMsg, $mBrush, 200, 825)

# Link 5: DoctorDashboardActivity <---> FirebaseHelper
$g.DrawLine($linkPen, 500, 760, 750, 395)
$g.DrawString("2.1: getDoctorQueue()`n3.1: startConsultation()`n4.1: completeConsultation()", $fMsg, $mBrush, 530, 520)

# Link 6: DoctorDashboardActivity <---> QueueManager
$g.DrawLine($linkPen, 500, 760, 720, 675)
$g.DrawString("2.3: sortQueue(queue) [SJF]`n2.4: return sortedQueue", $fMsg, $mBrush, 520, 680)

# Link 7: FirebaseHelper <---> NotificationHelper
$g.DrawLine($linkPen, 850, 395, 1100, 760)
$g.DrawString("5: showNotification(title, msg)", $fMsg, $mBrush, 980, 600)

$linkPen.Dispose(); $fMsg.Dispose(); $mBrush.Dispose()

$bmp.Save("$imgDir\diag7_collaboration.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 7 generated."

# ==============================================================================
# DIAGRAM 8: Data Flow Diagram (DFD Level 0 & Level 1) for QURE
# ==============================================================================
$w = 1400; $h = 980
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear($cPageBg)

# Header
$hdrBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($hdrBrush, 0, 0, $w, 85)
$hdrBrush.Dispose()

$g.DrawString("QURE - Data Flow Diagram (DFD: Level 0 Context & Level 1 System)", $fTitle, $wBrush, 30, 16)
$g.DrawString("Information Flows, Process Transformations, Data Stores, and External Entities", $fSub, $wBrush, 32, 54)

# Divider line separating Level 0 from Level 1
$divPen = New-Object System.Drawing.Pen($cBorder, 2)
$g.DrawLine($divPen, 30, 380, ($w - 30), 380)
$divPen.Dispose()

# Subtitles
$fSection = New-Object System.Drawing.Font("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
$sBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.DrawString("LEVEL 0: CONTEXT ANALYSIS DIAGRAM", $fSection, $sBrush, 40, 95)
$g.DrawString("LEVEL 1: SYSTEM DECOMPOSITION & DATA STORE FLOWS", $fSection, $sBrush, 40, 390)
$sBrush.Dispose(); $fSection.Dispose()

# Helper: Draw External Entity (Rectangle)
function Draw-DFDEntity($g, $x, $y, $w, $h, $name) {
    $pen = New-Object System.Drawing.Pen($cNavyDark, 2)
    $bg = New-Object System.Drawing.SolidBrush($cWhite)
    Draw-RoundedRect $g $pen $bg $x $y $w $h 6
    $pen.Dispose(); $bg.Dispose()
    
    $fE = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)
    $tB = New-Object System.Drawing.SolidBrush($cNavyDark)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF($x, $y, $w, $h)
    $g.DrawString($name, $fE, $tB, $rect, $sf)
    $fE.Dispose(); $tB.Dispose(); $sf.Dispose()
}

# Helper: Draw Process Circle / Oval
function Draw-DFDProcess($g, $x, $y, $w, $h, $id, $name, $col = $cTealDark) {
    $pen = New-Object System.Drawing.Pen($col, 2)
    $bg = New-Object System.Drawing.SolidBrush($cWhite)
    $g.FillEllipse($bg, $x, $y, $w, $h)
    $g.DrawEllipse($pen, $x, $y, $w, $h)
    $pen.Dispose(); $bg.Dispose()
    
    $fP = New-Object System.Drawing.Font("Segoe UI", 8.5, [System.Drawing.FontStyle]::Bold)
    $tB = New-Object System.Drawing.SolidBrush($cTextDark)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF($x, $y, $w, $h)
    $g.DrawString("$id`n$name", $fP, $tB, $rect, $sf)
    $fP.Dispose(); $tB.Dispose(); $sf.Dispose()
}

# Helper: Draw Data Store (Open Rectangle)
function Draw-DFDDataStore($g, $x, $y, $w, $h, $id, $name) {
    $pen = New-Object System.Drawing.Pen($cOrange, 2)
    $bg = New-Object System.Drawing.SolidBrush($cWhite)
    $g.FillRectangle($bg, $x, $y, $w, $h)
    # Draw top and bottom borders with left divider line
    $g.DrawLine($pen, $x, $y, ($x + $w), $y)
    $g.DrawLine($pen, $x, ($y + $h), ($x + $w), ($y + $h))
    $g.DrawLine($pen, ($x + 35), $y, ($x + 35), ($y + $h))
    $pen.Dispose(); $bg.Dispose()
    
    $fS = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Bold)
    $tB = New-Object System.Drawing.SolidBrush($cTextDark)
    $g.DrawString($id, $fS, $tB, ($x + 6), ($y + 8))
    $g.DrawString($name, $fS, $tB, ($x + 45), ($y + 8))
    $fS.Dispose(); $tB.Dispose()
}

# ==================== LEVEL 0 DIAGRAM ====================
Draw-DFDEntity $g 60 210 160 60 "PATIENT"
Draw-DFDEntity $g 1180 150 160 55 "DOCTOR"
Draw-DFDEntity $g 1180 260 160 55 "ADMINISTRATOR"
Draw-DFDProcess $g 520 160 360 160 "0.0" "QURE SMART HOSPITAL`nQUEUE MANAGEMENT SYSTEM" $cNavy

$dfdPen = New-Object System.Drawing.Pen($cNavy, 1.8)
# Patient <---> Process 0.0
Draw-Arrow $g $dfdPen 220 225 520 225
$g.DrawString("Booking details, Check-in signal", $fSmall, $dfdPen.Brush, 260, 208)

Draw-Arrow $g $dfdPen 520 255 220 255
$g.DrawString("Token No, Queue Status, Push Alert", $fSmall, $dfdPen.Brush, 260, 260)

# Doctor <---> Process 0.0
Draw-Arrow $g $dfdPen 1180 170 880 200
$g.DrawString("Doctor availability, Consultation actions", $fSmall, $dfdPen.Brush, 910, 165)

Draw-Arrow $g $dfdPen 880 220 1180 190
$g.DrawString("SJF Sorted Waiting Queue", $fSmall, $dfdPen.Brush, 930, 225)

# Admin <---> Process 0.0
Draw-Arrow $g $dfdPen 880 260 1180 280
$g.DrawString("Live hospital statistics, Global records", $fSmall, $dfdPen.Brush, 910, 285)

# ==================== LEVEL 1 DIAGRAM ====================
# Data Stores
Draw-DFDDataStore $g 580 430 240 36 "D1" "users (Credentials & Roles)"
Draw-DFDDataStore $g 580 650 240 36 "D2" "appointments (Token & Queue)"
Draw-DFDDataStore $g 580 870 240 36 "D3" "reports (Cloud Documents)"

# Entities Left
Draw-DFDEntity $g 40 490 140 50 "PATIENT"
# Entities Right
Draw-DFDEntity $g 1220 540 140 50 "DOCTOR"
Draw-DFDEntity $g 1220 730 140 50 "ADMIN"

# Sub-Processes
Draw-DFDProcess $g 260 430 140 100 "1.0" "User Login`n& Auth" $cNavy
Draw-DFDProcess $g 260 580 140 100 "2.0" "Book Appt &`nGen Token" $cNavy
Draw-DFDProcess $g 260 740 140 100 "3.0" "Hospital`nCheck-In" $cNavy

Draw-DFDProcess $g 940 450 150 100 "4.0" "SJF Queue`nSort Engine" $cTealDark
Draw-DFDProcess $g 940 610 150 100 "5.0" "Consultation`nManagement" $cTealDark
Draw-DFDProcess $g 940 770 150 100 "6.0" "Live System`nAnalytics" $cPink

# Flow lines Level 1
# Patient -> 1.0 (Login)
Draw-Arrow $g $dfdPen 180 500 260 470
# 1.0 <-> D1
Draw-Arrow $g $dfdPen 400 450 580 450
$g.DrawString("Validate credentials", $fSmall, $dfdPen.Brush, 430, 432)

# Patient -> 2.0 (Book)
Draw-Arrow $g $dfdPen 180 515 260 610
# 2.0 -> D2 (Store new appointment)
Draw-Arrow $g $dfdPen 400 630 580 660
$g.DrawString("Store appointment record", $fSmall, $dfdPen.Brush, 420, 630)

# Patient -> 3.0 (Check In)
Draw-Arrow $g $dfdPen 180 530 260 780
# 3.0 -> D2 (Update arrival & status=Waiting)
Draw-Arrow $g $dfdPen 400 780 580 680
$g.DrawString("Update status='Waiting'", $fSmall, $dfdPen.Brush, 420, 725)

# D2 -> 4.0 (Fetch Waiting Patients)
Draw-Arrow $g $dfdPen 820 660 940 510
$g.DrawString("Waiting patients query", $fSmall, $dfdPen.Brush, 825, 570)

# 4.0 -> Doctor (Display SJF Queue)
Draw-Arrow $g $dfdPen 1090 490 1220 550
$g.DrawString("SJF Sorted Queue", $fSmall, $dfdPen.Brush, 1100, 505)

# Doctor -> 5.0 (Call Next / Complete)
Draw-Arrow $g $dfdPen 1220 570 1090 640
$g.DrawString("Call Next / Finish", $fSmall, $dfdPen.Brush, 1110, 595)

# 5.0 -> D2 (Update status = In Consultation / Completed)
Draw-Arrow $g $dfdPen 940 660 820 665
$g.DrawString("Update status", $fSmall, $dfdPen.Brush, 840, 645)

# D2 -> 6.0 & D1 -> 6.0 (Admin Stats)
Draw-Arrow $g $dfdPen 820 680 940 810
Draw-Arrow $g $dfdPen 1090 820 1220 760
$g.DrawString("Real-time Stats", $fSmall, $dfdPen.Brush, 1110, 780)

$dfdPen.Dispose()

$bmp.Save("$imgDir\diag8_dfd.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 8 generated."
