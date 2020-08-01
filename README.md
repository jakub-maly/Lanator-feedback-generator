# Release 1.0
This version is designed for our client's personal use only, and is not ready for further distribution.

## How to use:
The application is found in the `Application` folder. The `.jar` version will only work if Java is installed; the `.exe` version should run on all Windows machines. Read the **known issues** section before running the application. Both versions are ready to download and run.

## Known issues:
 - The application may freeze after `create` has been pressed. The application should still be creating PDFs in the background, and will unfreeze once finished (this may take a few minutes). Do not terminate the process while new PDFs are being created. Once all generation is finished, the application will produce a success message.
 - The application temporarily creates images and then deletes them in the same folder as itself. **To prevent other images with the same name being deleted, run this application from an empty directory!** 
 - The application may falsely trigger some anti-virus software warnings for ransomware (due to modifying a large amount of pictures described above).

## Changelog:

### Optional Subjects UI Support (01 August 2020)

```
Teacher
   +	(v) public boolean optionalSubject

Main
   +	(m) void drawSubjectList ()
		-- draws all CheckBoxes @ SubjectBox

   +	(m) void setOptionalSubjects (ActionEvent actionEvent)
		-- calls PDF generation after startButton is pressed @ PdfGenerator

   ~    (m) void createHash (File file)
		-- Vector<Teacher> teachers is now a static variable
		-- removed File directory from @param as an unused variable

SubjectBox
   + 	(m) SubjectBox (Teacher teacher)
		-- custom CheckBox to toggle @ Teacher optionalSubject

   + 	(m) onAction (ActionEvent actionEvent)
		-- triggers a boolean switch operation @ Teacher optionalSubject
```
