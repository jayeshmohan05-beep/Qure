$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()
$selection = $word.Selection

$selection.Font.Name = "Segoe UI"
$selection.Font.Size = 16
$selection.Font.Bold = $true
$selection.Font.Color = 8266522 # Navy Blue
$selection.TypeText("Testing Word Table Generation")
$selection.TypeParagraph()

$table = $doc.Tables.Add($selection.Range, 3, 3)
$table.Borders.Enable = $true
$table.AutoFitBehavior(2) # wdAutoFitWindow

# Header styling
$table.Rows.Item(1).Range.Shading.BackgroundPatternColor = 8266522
$table.Rows.Item(1).Range.Font.Color = 16777215
$table.Rows.Item(1).Range.Font.Bold = $true

$table.Cell(1, 1).Range.Text = "Col 1"
$table.Cell(1, 2).Range.Text = "Col 2"
$table.Cell(1, 3).Range.Text = "Col 3"

$table.Cell(2, 1).Range.Text = "Val A"
$table.Cell(2, 2).Range.Text = "Val B"
$table.Cell(2, 3).Range.Text = "Val C"

$selection.EndKey(6) # wdStory = 6
$selection.TypeParagraph()

# Test image insertion
$imgPath = "C:\Users\LENOVO\AndroidStudioProjects\QURE\docs_diagrams\diag1_agile_functional.png"
if (Test-Path $imgPath) {
    $pic = $selection.InlineShapes.AddPicture($imgPath)
    $pic.Width = 460
}

$testPath = "C:\Users\LENOVO\AndroidStudioProjects\QURE\test_table_word.docx"
$doc.SaveAs([ref]$testPath, [ref]16)
$doc.Close()
$word.Quit()
Write-Output "Word test passed."
