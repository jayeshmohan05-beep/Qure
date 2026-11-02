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

# Helper: Draw Rounded Rectangle
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

# Helper: Draw Arrow
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

# ==============================================================================
# DIAGRAM 1: Agile Functional Requirements - User Story Map & Hierarchy
# ==============================================================================
$w = 1400; $h = 920
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear($cPageBg)

# Header
$hdrBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($hdrBrush, 0, 0, $w, 85)
$hdrBrush.Dispose()

$fTitle = New-Object System.Drawing.Font("Segoe UI", 20, [System.Drawing.FontStyle]::Bold)
$fSub = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Regular)
$fBold = New-Object System.Drawing.Font("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
$fReg = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Regular)
$fSmall = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Regular)
$wBrush = New-Object System.Drawing.SolidBrush($cWhite)
$g.DrawString("QURE - Agile Requirements Gathering: Functional User Stories Map", $fTitle, $wBrush, 30, 16)
$g.DrawString("Hierarchical breakdown of Epics, Features, and Agile User Stories across Hospital Stakeholders", $fSub, $wBrush, 32, 54)

# 4 Columns for Epics
$epics = @(
    @{ Title="EPIC 1: Patient Experience Flow"; Color=$cNavy; Sub="Consultation & Queue Access"; 
       Stories=@(
           @{ Id="US-F01"; Role="Patient"; Want="Book an appointment by choosing department, doctor, and date"; So="I can secure an OPD slot before visiting the hospital"; P="High"; Sp="5" },
           @{ Id="US-F02"; Role="Patient"; Want="Check-in digitally upon arriving at the hospital"; So="My arrival time is recorded and I join the live doctor queue"; P="High"; Sp="3" },
           @{ Id="US-F03"; Role="Patient"; Want="Track my live queue status and token number"; So="I know exactly where I am in the waiting sequence without standing in line"; P="High"; Sp="5" }
       )},
    @{ Title="EPIC 2: Doctor Queue Management"; Color=$cTealDark; Sub="Outpatient Clinic Operations"; 
       Stories=@(
           @{ Id="US-F04"; Role="Doctor"; Want="View a waiting list automatically sorted by Shortest Job First (SJF)"; So="Quick consultations are cleared faster, reducing average waiting time"; P="High"; Sp="8" },
           @{ Id="US-F05"; Role="Doctor"; Want="Call the next patient with a single tap"; So="The patient's status transitions to 'In Consultation' automatically"; P="High"; Sp="3" },
           @{ Id="US-F06"; Role="Doctor"; Want="Mark the consultation as completed"; So="The session closes and the queue automatically advances to the next patient"; P="Med"; Sp="3" }
       )},
    @{ Title="EPIC 3: Admin & Hospital Operations"; Color=[System.Drawing.Color]::FromArgb(194, 24, 91); Sub="System Oversight & Simulation"; 
       Stories=@(
           @{ Id="US-F07"; Role="Admin"; Want="View real-time statistics of total users and active appointments"; So="I can monitor hospital load and outpatient volume dynamically"; P="Med"; Sp="5" },
           @{ Id="US-F08"; Role="Admin"; Want="Run an interactive OS Scheduling Simulator (FCFS, SJF, Priority)"; So="I can demonstrate and evaluate scheduling algorithms on test workloads"; P="High"; Sp="8" },
           @{ Id="US-F09"; Role="Admin"; Want="Access the master database of all historical appointments"; So="I can audit hospital records and consultation history"; P="Low"; Sp="5" }
       )},
    @{ Title="EPIC 4: Smart Hospital Utilities"; Color=[System.Drawing.Color]::FromArgb(230, 81, 0); Sub="Cross-cutting Digital Tools"; 
       Stories=@(
           @{ Id="US-F10"; Role="Patient"; Want="Upload and view medical reports (PDFs / Photos)"; So="Doctors have digital access to my previous lab investigations"; P="Med"; Sp="5" },
           @{ Id="US-F11"; Role="Patient"; Want="View hospital geolocation and launch turn-by-turn navigation"; So="I can navigate directly to the medical facility without delays"; P="Low"; Sp="3" },
           @{ Id="US-F12"; Role="Patient"; Want="Receive an instant push notification upon booking confirmation"; So="I have immediate verified proof and token details on my mobile device"; P="Med"; Sp="3" }
       )}
)

$colW = 320
$colGap = 20
$startX = 30
$startY = 105

for ($i = 0; $i -lt $epics.Length; $i++) {
    $ep = $epics[$i]
    $x = $startX + ($i * ($colW + $colGap))
    
    # Epic Header Card
    $epBrush = New-Object System.Drawing.SolidBrush($ep.Color)
    $epPen = New-Object System.Drawing.Pen($ep.Color, 2)
    Draw-RoundedRect $g $epPen $epBrush $x $startY $colW 75 8
    $g.DrawString($ep.Title, $fBold, $wBrush, ($x + 10), ($startY + 12))
    $g.DrawString($ep.Sub, $fSmall, $wBrush, ($x + 10), ($startY + 42))
    $epBrush.Dispose()
    $epPen.Dispose()
    
    # Stories
    $cardY = $startY + 90
    foreach ($st in $ep.Stories) {
        $cPen = New-Object System.Drawing.Pen($cBorder, 1)
        $cBg = New-Object System.Drawing.SolidBrush($cWhite)
        Draw-RoundedRect $g $cPen $cBg $x $cardY $colW 225 8
        $cPen.Dispose(); $cBg.Dispose()
        
        # Story ID badge
        $badgeBrush = New-Object System.Drawing.SolidBrush($ep.Color)
        $g.FillRectangle($badgeBrush, ($x + 12), ($cardY + 12), 70, 24)
        $g.DrawString($st.Id, $fSmall, $wBrush, ($x + 18), ($cardY + 15))
        $badgeBrush.Dispose()
        
        # Priority / SP badge
        $pText = "Priority: " + $st.P + " | SP: " + $st.Sp
        $pBrush = New-Object System.Drawing.SolidBrush($cTextGray)
        $g.DrawString($pText, $fSmall, $pBrush, ($x + 95), ($cardY + 16))
        $pBrush.Dispose()
        
        # User Story formula text
        $txtBrush = New-Object System.Drawing.SolidBrush($cTextDark)
        $boldDark = New-Object System.Drawing.SolidBrush($cNavyDark)
        $g.DrawString("AS A: " + $st.Role, $fBold, $boldDark, ($x + 12), ($cardY + 45))
        
        $wantRect = New-Object System.Drawing.RectangleF(($x + 12), ($cardY + 75), ($colW - 24), 60)
        $g.DrawString("I WANT: " + $st.Want, $fReg, $txtBrush, $wantRect)
        
        $soRect = New-Object System.Drawing.RectangleF(($x + 12), ($cardY + 140), ($colW - 24), 70)
        $soBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(46, 125, 50))
        $g.DrawString("SO THAT: " + $st.So, $fSmall, $soBrush, $soRect)
        $soBrush.Dispose()
        
        $txtBrush.Dispose(); $boldDark.Dispose()
        $cardY += 240
    }
}

$bmp.Save("$imgDir\diag1_agile_functional.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 1 generated."

# ==============================================================================
# DIAGRAM 2: Agile Non-Functional Requirements (NFR) Classification
# ==============================================================================
$w = 1400; $h = 920
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear($cPageBg)

# Header
$hdrBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($hdrBrush, 0, 0, $w, 85)
$hdrBrush.Dispose()

$g.DrawString("QURE - Non-Functional Requirements (NFR) Architecture Model", $fTitle, $wBrush, 30, 16)
$g.DrawString("Quality Attributes, Operational Constraints, Security, and System Performance Standards", $fSub, $wBrush, 32, 54)

# Central Core Hub
$hubX = 520; $hubY = 380; $hubW = 360; $hubH = 150
$hubPen = New-Object System.Drawing.Pen($cNavy, 3)
$hubBrush = New-Object System.Drawing.SolidBrush($cBlueSoft)
Draw-RoundedRect $g $hubPen $hubBrush $hubX $hubY $hubW $hubH 16
$hubTitleFont = New-Object System.Drawing.Font("Segoe UI", 16, [System.Drawing.FontStyle]::Bold)
$hubTxtBrush = New-Object System.Drawing.SolidBrush($cNavyDark)
$g.DrawString("QURE SYSTEM NFR CORE", $hubTitleFont, $hubTxtBrush, ($hubX + 35), ($hubY + 30))
$g.DrawString("Software Quality Attributes", $fBold, $hubTxtBrush, ($hubX + 80), ($hubY + 65))
$g.DrawString("ISO/IEC 25010 Quality Model Standards", $fSmall, $hubTxtBrush, ($hubX + 55), ($hubY + 95))
$hubPen.Dispose(); $hubBrush.Dispose(); $hubTxtBrush.Dispose()

# 5 Surrounding Pillars
$nfrPillars = @(
    @{ Title="1. PERFORMANCE & EFFICIENCY"; Color=$cTealDark; X=40; Y=110; W=380; H=210;
       ConnectX=420; ConnectY=215;
       Points=@(
           "• Real-time synchronization: Database changes propagate < 1.5s",
           "• Low computational overhead: O(n log n) SJF queue reordering",
           "• Sub-second Activity launches & instant navigation",
           "• Low bandwidth consumption on 3G/4G/WiFi mobile networks"
       )},
    @{ Title="2. SECURITY & PRIVACY"; Color=[System.Drawing.Color]::FromArgb(194, 24, 91); X=980; Y=110; W=380; H=210;
       ConnectX=980; ConnectY=215;
       Points=@(
           "• Secure Firebase Authentication with encrypted token sessions",
           "• Role-Based Access Control (RBAC): Patient, Doctor, Admin",
           "• SharedPreferences stores only essential session keys locally",
           "• Cloud Storage rules restrict medical report access by UID"
       )},
    @{ Title="3. RELIABILITY & ROBUSTNESS"; Color=[System.Drawing.Color]::FromArgb(46, 125, 50); X=40; Y=590; W=380; H=210;
       ConnectX=420; ConnectY=695;
       Points=@(
           "• Cloud Firestore offline persistence & automatic sync",
           "• Synchronous session auto-check avoids screen flicker",
           "• Graceful exception handling with user-friendly Toast alerts",
           "• Zero crashes on missing network or empty dataset states"
       )},
    @{ Title="4. USABILITY & ACCESSIBILITY"; Color=[System.Drawing.Color]::FromArgb(230, 81, 0); X=980; Y=590; W=380; H=210;
       ConnectX=980; ConnectY=695;
       Points=@(
           "• Clean Material Design with high-contrast Navy & Teal palette",
           "• Intuitive 2x2 grid navigation with self-explanatory emojis",
           "• Touch target sizes conform to Android accessibility (>= 48dp)",
           "• Interactive date picker & drop-downs prevent typing errors"
       )},
    @{ Title="5. PORTABILITY & SCALABILITY"; Color=[System.Drawing.Color]::FromArgb(106, 27, 154); X=520; Y=670; W=360; H=200;
       ConnectX=700; ConnectY=670;
       Points=@(
           "• Android OS support: API 24 (Android 7.0) to API 37",
           "• Horizontal scalability enabled by Google Cloud Firestore",
           "• Modular Java architecture with clean package boundaries",
           "• Dynamic queue sizing accommodates busy multi-doctor clinics"
       )}
)

foreach ($pil in $nfrPillars) {
    # Connector line to central core
    $connPen = New-Object System.Drawing.Pen($pil.Color, 2)
    $connPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
    $centerCoreX = $hubX + ($hubW / 2)
    $centerCoreY = $hubY + ($hubH / 2)
    $g.DrawLine($connPen, $centerCoreX, $centerCoreY, $pil.ConnectX, $pil.ConnectY)
    $connPen.Dispose()
    
    # Pillar Card
    $pPen = New-Object System.Drawing.Pen($pil.Color, 2)
    $pBg = New-Object System.Drawing.SolidBrush($cWhite)
    Draw-RoundedRect $g $pPen $pBg $pil.X $pil.Y $pil.W $pil.H 10
    $pBg.Dispose()
    
    # Header bar inside card
    $hBar = New-Object System.Drawing.SolidBrush($pil.Color)
    $g.FillRectangle($hBar, ($pil.X + 2), ($pil.Y + 2), ($pil.W - 4), 38)
    $g.DrawString($pil.Title, $fBold, $wBrush, ($pil.X + 14), ($pil.Y + 10))
    $hBar.Dispose()
    
    # Text bullets
    $bY = $pil.Y + 48
    $bBrush = New-Object System.Drawing.SolidBrush($cTextDark)
    foreach ($pt in $pil.Points) {
        $ptRect = New-Object System.Drawing.RectangleF(($pil.X + 12), $bY, ($pil.W - 24), 36)
        $g.DrawString($pt, $fSmall, $bBrush, $ptRect)
        $bY += 36
    }
    $bBrush.Dispose()
    $pPen.Dispose()
}

$bmp.Save("$imgDir\diag2_agile_nonfunctional.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 2 generated."

# ==============================================================================
# DIAGRAM 3: Use Case Diagram for QURE
# ==============================================================================
$w = 1400; $h = 950
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear($cPageBg)

# Header
$hdrBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($hdrBrush, 0, 0, $w, 85)
$hdrBrush.Dispose()

$g.DrawString("QURE - UML Use Case Diagram (User's View Analysis)", $fTitle, $wBrush, 30, 16)
$g.DrawString("Complete interaction boundary between Stakeholder Actors and Hospital System Capabilities", $fSub, $wBrush, 32, 54)

# System Boundary Box
$sysX = 300; $sysY = 110; $sysW = 800; $sysH = 810
$sysPen = New-Object System.Drawing.Pen($cNavy, 2)
$sysBg = New-Object System.Drawing.SolidBrush($cWhite)
Draw-RoundedRect $g $sysPen $sysBg $sysX $sysY $sysW $sysH 12
$sysPen.Dispose(); $sysBg.Dispose()

$sysTagBrush = New-Object System.Drawing.SolidBrush($cNavyLight)
$g.DrawString("<<system>> QURE Smart Hospital Queue Management System", $fBold, $sysTagBrush, ($sysX + 20), ($sysY + 15))
$sysTagBrush.Dispose()

# Helper: Draw Actor Stick Figure
function Draw-ActorFigure($g, $x, $y, $name, $color) {
    $aPen = New-Object System.Drawing.Pen($color, 3)
    $aBrush = New-Object System.Drawing.SolidBrush($color)
    $fName = New-Object System.Drawing.Font("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
    
    # Head
    $g.DrawEllipse($aPen, ($x - 18), $y, 36, 36)
    # Body
    $g.DrawLine($aPen, $x, ($y + 36), $x, ($y + 90))
    # Arms
    $g.DrawLine($aPen, ($x - 30), ($y + 55), ($x + 30), ($y + 55))
    # Legs
    $g.DrawLine($aPen, $x, ($y + 90), ($x - 25), ($y + 140))
    $g.DrawLine($aPen, $x, ($y + 90), ($x + 25), ($y + 140))
    
    # Name label
    $size = $g.MeasureString($name, $fName)
    $g.DrawString($name, $fName, $aBrush, ($x - ($size.Width / 2)), ($y + 150))
    
    $aPen.Dispose(); $aBrush.Dispose(); $fName.Dispose()
}

# Helper: Draw Use Case Oval
function Draw-UseCaseOval($g, $x, $y, $w, $h, $name, $color, $borderCol) {
    $pen = New-Object System.Drawing.Pen($borderCol, 2)
    $brush = New-Object System.Drawing.SolidBrush($color)
    $g.FillEllipse($brush, $x, $y, $w, $h)
    $g.DrawEllipse($pen, $x, $y, $w, $h)
    
    $fUC = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)
    $tBrush = New-Object System.Drawing.SolidBrush($cTextDark)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF($x, $y, $w, $h)
    $g.DrawString($name, $fUC, $tBrush, $rect, $sf)
    
    $pen.Dispose(); $brush.Dispose(); $fUC.Dispose(); $tBrush.Dispose(); $sf.Dispose()
}

# Draw Actors
$cAdmin = [System.Drawing.Color]::FromArgb(194, 24, 91)
# Patient (Left)
Draw-ActorFigure $g 140 260 "Patient" $cNavy
# Doctor (Right Top)
Draw-ActorFigure $g 1250 200 "Doctor" $cTealDark
# Administrator (Right Bottom)
Draw-ActorFigure $g 1250 600 "Administrator" $cAdmin

# Draw Use Cases inside boundary
$ucW = 210; $ucH = 50
$ucList = @(
    @{ Id="UC1"; Name="Register & Login"; X=340; Y=150; Bg=$cBlueSoft; B=$cNavy },
    @{ Id="UC2"; Name="Authenticate User`n<<include>>"; X=600; Y=150; Bg=$cPageBg; B=$cBorder },
    @{ Id="UC3"; Name="Book OPD Appointment"; X=340; Y=240; Bg=$cBlueSoft; B=$cNavy },
    @{ Id="UC4"; Name="Select Dept & Doctor`n<<include>>"; X=600; Y=240; Bg=$cPageBg; B=$cBorder },
    @{ Id="UC5"; Name="Check-In at Hospital"; X=340; Y=330; Bg=$cBlueSoft; B=$cNavy },
    @{ Id="UC6"; Name="Track Live Queue Status"; X=340; Y=420; Bg=$cBlueSoft; B=$cNavy },
    @{ Id="UC7"; Name="Upload Medical Reports"; X=340; Y=510; Bg=$cBlueSoft; B=$cNavy },
    @{ Id="UC8"; Name="View Hospital Navigation"; X=340; Y=600; Bg=$cBlueSoft; B=$cNavy },
    @{ Id="UC9"; Name="View Live Waiting Queue"; X=840; Y=220; Bg=$cTealLight; B=$cTealDark },
    @{ Id="UC10"; Name="Sort Queue by SJF`n<<include>>"; X=590; Y=330; Bg=$cTealLight; B=$cTealDark },
    @{ Id="UC11"; Name="Call Next Patient"; X=840; Y=310; Bg=$cTealLight; B=$cTealDark },
    @{ Id="UC12"; Name="Complete Consultation"; X=840; Y=400; Bg=$cTealLight; B=$cTealDark },
    @{ Id="UC13"; Name="View Hospital Statistics"; X=840; Y=570; Bg=[System.Drawing.Color]::FromArgb(252, 228, 236); B=[System.Drawing.Color]::FromArgb(194, 24, 91) },
    @{ Id="UC14"; Name="Execute OS Simulator"; X=840; Y=660; Bg=[System.Drawing.Color]::FromArgb(252, 228, 236); B=[System.Drawing.Color]::FromArgb(194, 24, 91) },
    @{ Id="UC15"; Name="Access Master Records"; X=840; Y=750; Bg=[System.Drawing.Color]::FromArgb(252, 228, 236); B=[System.Drawing.Color]::FromArgb(194, 24, 91) },
    @{ Id="UC16"; Name="Send Push Notification`n<<extend>>"; X=340; Y=720; Bg=$cPageBg; B=$cBorder }
)

foreach ($u in $ucList) {
    Draw-UseCaseOval $g $u.X $u.Y $ucW $ucH $u.Name $u.Bg $u.B
}

# Connect Actors to Use Cases (Association Lines)
$assocPen = New-Object System.Drawing.Pen($cNavy, 2)
# Patient associations
$g.DrawLine($assocPen, 180, 310, 340, 175) # to UC1
$g.DrawLine($assocPen, 180, 320, 340, 265) # to UC3
$g.DrawLine($assocPen, 180, 335, 340, 355) # to UC5
$g.DrawLine($assocPen, 180, 350, 340, 445) # to UC6
$g.DrawLine($assocPen, 180, 365, 340, 535) # to UC7
$g.DrawLine($assocPen, 180, 380, 340, 625) # to UC8
$assocPen.Dispose()

# Doctor associations
$docPen = New-Object System.Drawing.Pen($cTealDark, 2)
$g.DrawLine($docPen, 1210, 260, 1050, 245) # to UC9
$g.DrawLine($docPen, 1210, 275, 1050, 335) # to UC11
$g.DrawLine($docPen, 1210, 290, 1050, 425) # to UC12
$docPen.Dispose()

# Admin associations
$admPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(194, 24, 91), 2)
$g.DrawLine($admPen, 1210, 660, 1050, 595) # to UC13
$g.DrawLine($admPen, 1210, 675, 1050, 685) # to UC14
$g.DrawLine($admPen, 1210, 690, 1050, 775) # to UC15
$admPen.Dispose()

# Include / Extend Relationships (Dashed Arrows)
$dashPen = New-Object System.Drawing.Pen($cTextGray, 2)
$dashPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
# UC1 -> UC2 (include)
Draw-Arrow $g $dashPen 550 175 600 175
# UC3 -> UC4 (include)
Draw-Arrow $g $dashPen 550 265 600 265
# UC9 -> UC10 (include)
Draw-Arrow $g $dashPen 840 245 800 330
# UC3 -> UC16 (extend)
Draw-Arrow $g $dashPen 445 290 445 720
$dashPen.Dispose()

$bmp.Save("$imgDir\diag3_use_case.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 3 generated."

# ==============================================================================
# DIAGRAM 4: Activity Diagram for QURE
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

$g.DrawString("QURE - UML Activity Diagram (Operational Workflow)", $fTitle, $wBrush, 30, 16)
$g.DrawString("End-to-End Dynamic Workflow across Patient, Smart Hospital Queue Backend, and Doctor Clinic", $fSub, $wBrush, 32, 54)

# 3 Swimlanes
$laneW = 440
$lane1X = 30; $lane2X = 490; $lane3X = 950
$laneY = 100; $laneH = 830

# Swimlane 1: Patient
$l1Brush = New-Object System.Drawing.SolidBrush($cNavyLight)
$g.FillRectangle($l1Brush, $lane1X, $laneY, $laneW, 40)
$g.DrawString("PATIENT (Client App)", $fBold, $wBrush, ($lane1X + 120), ($laneY + 10))
$l1Pen = New-Object System.Drawing.Pen($cBorder, 1)
$g.DrawRectangle($l1Pen, $lane1X, $laneY, $laneW, $laneH)

# Swimlane 2: QURE System & Queue Backend
$l2Brush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillRectangle($l2Brush, $lane2X, $laneY, $laneW, 40)
$g.DrawString("QURE BACKEND & SJF SCHEDULER", $fBold, $wBrush, ($lane2X + 80), ($laneY + 10))
$g.DrawRectangle($l1Pen, $lane2X, $laneY, $laneW, $laneH)

# Swimlane 3: Doctor Clinic
$l3Brush = New-Object System.Drawing.SolidBrush($cTealDark)
$g.FillRectangle($l3Brush, $lane3X, $laneY, ($w - $lane3X - 30), 40)
$g.DrawString("DOCTOR (Clinical App)", $fBold, $wBrush, ($lane3X + 100), ($laneY + 10))
$g.DrawRectangle($l1Pen, $lane3X, $laneY, ($w - $lane3X - 30), $laneH)
$l1Brush.Dispose(); $l2Brush.Dispose(); $l3Brush.Dispose(); $l1Pen.Dispose()

# Helper: Activity Action Node
function Draw-ActionNode($g, $x, $y, $w, $h, $text, $bgColor, $borderColor) {
    $pen = New-Object System.Drawing.Pen($borderColor, 2)
    $brush = New-Object System.Drawing.SolidBrush($bgColor)
    Draw-RoundedRect $g $pen $brush $x $y $w $h 10
    
    $fAct = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)
    $tBrush = New-Object System.Drawing.SolidBrush($cTextDark)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF($x, $y, $w, $h)
    $g.DrawString($text, $fAct, $tBrush, $rect, $sf)
    
    $pen.Dispose(); $brush.Dispose(); $fAct.Dispose(); $tBrush.Dispose(); $sf.Dispose()
}

# Helper: Decision Diamond
function Draw-DecisionDiamond($g, $x, $y, $w, $h, $label) {
    $p1 = New-Object System.Drawing.PointF(($x + $w / 2), $y)
    $p2 = New-Object System.Drawing.PointF(($x + $w), ($y + $h / 2))
    $p3 = New-Object System.Drawing.PointF(($x + $w / 2), ($y + $h))
    $p4 = New-Object System.Drawing.PointF($x, ($y + $h / 2))
    
    $pen = New-Object System.Drawing.Pen($cOrange, 2)
    $brush = New-Object System.Drawing.SolidBrush($cWhite)
    $pts = [System.Drawing.PointF[]]@($p1, $p2, $p3, $p4)
    $g.FillPolygon($brush, $pts)
    $g.DrawPolygon($pen, $pts)
    
    $fDec = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Bold)
    $tBrush = New-Object System.Drawing.SolidBrush($cTextDark)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF($x, $y, $w, $h)
    $g.DrawString($label, $fDec, $tBrush, $rect, $sf)
    
    $pen.Dispose(); $brush.Dispose(); $fDec.Dispose(); $tBrush.Dispose(); $sf.Dispose()
}

# Start Node
$startBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillEllipse($startBrush, 230, 160, 26, 26)
$startBrush.Dispose()

$flowPen = New-Object System.Drawing.Pen($cNavy, 2)

# Flow 1: Start -> Book Appointment
Draw-Arrow $g $flowPen 243 186 243 210
Draw-ActionNode $g 130 210 220 50 "Fill Appointment Form`n(Doctor, Dept, Date, Symptoms)" $cBlueSoft $cNavy

# Flow 2: Book Appt -> Backend Creates Record
Draw-Arrow $g $flowPen 350 235 530 235
Draw-ActionNode $g 530 210 260 50 "Generate Token & Save Record`nStatus = 'Registered', Est. Time" $cCardBg $cBorder

# Flow 3: Push Notification
Draw-Arrow $g $flowPen 660 260 660 285
Draw-ActionNode $g 530 285 260 45 "Trigger Instant Notification`n(NotificationHelper)" $cCardBg $cBorder

# Flow 4: Return notification to Patient
Draw-Arrow $g $flowPen 530 307 350 307
Draw-ActionNode $g 130 285 220 45 "Receive Booking Confirmation`n& Token on Mobile Screen" $cBlueSoft $cNavy

# Flow 5: Patient reaches Hospital & Check-In
Draw-Arrow $g $flowPen 243 330 243 360
Draw-ActionNode $g 130 360 220 50 "Arrive at Hospital & Tap`n'CHECK IN AT HOSPITAL'" $cBlueSoft $cNavy

# Flow 6: Backend updates Status & Arrival Time
Draw-Arrow $g $flowPen 350 385 530 385
Draw-ActionNode $g 530 360 260 50 "Record Arrival Timestamp`nUpdate Status = 'Waiting'" $cCardBg $cBorder

# Flow 7: SJF Queue Sorting
Draw-Arrow $g $flowPen 660 410 660 440
Draw-ActionNode $g 530 440 260 55 "QueueManager.sortQueue()`nApply SJF (Shortest Job First)`nOrder by Burst Time" $cTealLight $cTealDark

# Flow 8: Doctor fetches queue
Draw-Arrow $g $flowPen 790 467 980 467
Draw-ActionNode $g 980 440 260 55 "Doctor Logs In & Loads Queue`nDisplays SJF Sorted Patient List" $cTealLight $cTealDark

# Flow 9: Doctor calls next patient
Draw-Arrow $g $flowPen 1110 495 1110 525
Draw-ActionNode $g 980 525 260 45 "Click 'CALL NEXT PATIENT'`n(Highest Priority / Shortest Burst)" $cTealLight $cTealDark

# Flow 10: Backend updates Status = In Consultation
Draw-Arrow $g $flowPen 980 547 790 547
Draw-ActionNode $g 530 525 260 45 "Update Status = 'In Consultation'`nShow Active Consultation Card" $cCardBg $cBorder

# Flow 11: Notify Patient status change
Draw-Arrow $g $flowPen 530 547 350 547
Draw-ActionNode $g 130 525 220 45 "Queue Status changes to`n'In Consultation' (Blue)" $cBlueSoft $cNavy

# Flow 12: Doctor conducts session & completes
Draw-Arrow $g $flowPen 1110 570 1110 600
Draw-ActionNode $g 980 600 260 50 "Conduct Medical Consultation`n& Click 'COMPLETE'" $cTealLight $cTealDark

# Flow 13: Backend marks Completed
Draw-Arrow $g $flowPen 980 625 790 625
Draw-ActionNode $g 530 600 260 50 "Update Status = 'Completed'`nRemove from Active Waiting Queue" $cCardBg $cBorder

# Flow 14: Decision - Are there more patients?
Draw-Arrow $g $flowPen 1110 650 1110 685
Draw-DecisionDiamond $g 1045 685 130 60 "More Patients`nin Queue?"

# Yes branch (Loop back to Call Next Patient)
$g.DrawLine($flowPen, 1175, 715, 1280, 715)
$g.DrawLine($flowPen, 1280, 715, 1280, 547)
Draw-Arrow $g $flowPen 1280 547 1240 547
$g.DrawString("[Yes]", $fBold, $flowPen.Brush, 1195, 695)

# No branch (To End Node)
Draw-Arrow $g $flowPen 1110 745 1110 790
$g.DrawString("[No]", $fBold, $flowPen.Brush, 1118, 755)

Draw-ActionNode $g 980 790 260 45 "Doctor Free State`n('You are currently free')" $cCardBg $cBorder

# Final Node
Draw-Arrow $g $flowPen 1110 835 1110 865
# Bullseye outer circle
$g.DrawEllipse($flowPen, 1097, 865, 26, 26)
$endBrush = New-Object System.Drawing.SolidBrush($cNavy)
$g.FillEllipse($endBrush, 1102, 870, 16, 16)
$endBrush.Dispose()

$flowPen.Dispose()

$bmp.Save("$imgDir\diag4_activity.png", [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Output "Diagram 4 generated."
