Add-Type -AssemblyName System.Drawing

$bmp = New-Object System.Drawing.Bitmap 800, 400
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::White)

$pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(26, 35, 126), 2)
$brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(240, 243, 250))
$g.FillRectangle($brush, 50, 50, 300, 150)
$g.DrawRectangle($pen, 50, 50, 300, 150)

$font = New-Object System.Drawing.Font("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
$textBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(26, 35, 126))
$g.DrawString("QURE Smart Hospital System", $font, $textBrush, 70, 70)

$bmp.Save("C:\Users\LENOVO\AndroidStudioProjects\QURE\test_diag.png", [System.Drawing.Imaging.ImageFormat]::Png)

$g.Dispose()
$bmp.Dispose()

Write-Output "Image created successfully at C:\Users\LENOVO\AndroidStudioProjects\QURE\test_diag.png"
