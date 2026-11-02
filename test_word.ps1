$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()

$selection = $word.Selection
$selection.TypeText("Hello World QURE Test Document")
$selection.TypeParagraph()

# Add a page break
$selection.InsertBreak([Microsoft.Office.Interop.Word.WdBreakType]::wdPageBreak)
$selection.TypeText("Page 2 Content")

$docPath = "C:\Users\LENOVO\AndroidStudioProjects\QURE\test_word.docx"
$doc.SaveAs([ref]$docPath, [ref]16) # 16 = wdFormatXMLDocument (.docx)
$doc.Close()
$word.Quit()

Write-Output "Word document created successfully at $docPath"
