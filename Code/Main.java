import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.*;

import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.Color;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Launches the window, loads sheets
 */
public class Main extends Application {

    //    replacement for CSS file for brevity
    public static String buttonStyle =
            "-fx-background-color: hotpink; " +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 10;";
    public static String bgStyle =
            "-fx-background-color: pink;";

    //    JavaFX elements of the Application
    public static Stage window;
    public static VBox vbox;
    public static Label message;
    public static javafx.scene.image.Image logo;
    public static Button inputButton, outputButton, startButton;
    public static HBox inputLine, outputLine;

    //    input, output
    public static File file, directory;

    //    Teacher objects
    public static Vector<Teacher> teachers;

    //    stores whether the program is running
    public static boolean running = false;

    /**
     * Custom JavaFX CheckBox, remembers the Teacher object it represents.
     */
    public static class SubjectBox extends CheckBox {

        Teacher teacher;

        SubjectBox (Teacher teacher) {
            this.teacher = teacher;

//            sets CheckBox Text and onAction Event
            this.setText(teacher.nameSubject);
            this.setOnAction(this::onAction);

//            sets CSS style
            this.setStyle(buttonStyle);
        }

        /**
         * changes boolean optionalSubject @ Teacher teacher when checked/unchecked
         *
         * @param actionEvent   Check/Uncheck event as a necessary Action parameter
         */
        void onAction (ActionEvent actionEvent) {
            teacher.optionalSubject = !teacher.optionalSubject;
        }
    }

    /**
     * Launches start function
     *
     * @param args  mandatory arguments
     */
    public static void main(String[] args){
        launch(args);
    }

    /**
     * Initiates the Application window
     *
     * @param window    proprietary JavaFX stage with no Nodes
     */
    @Override
    public void start(Stage window) {
        try {
            Main.window = window;

//            top bar logo
            logo = new javafx.scene.image.Image("icon.png");

//            main window frame
            window.setTitle("Lanatorator 3000");
            window.getIcons().add(logo);

//            vertical box, contains all graphics Nodes
            vbox = new VBox();
            vbox.setPrefWidth(720);
            vbox.setPrefHeight(480);
            vbox.setSpacing(10);
            vbox.setAlignment(Pos.TOP_CENTER);
            vbox.setStyle(bgStyle);

//            chooser elements for input and output
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xls", "*.xlsx"));
            DirectoryChooser directoryChooser = new DirectoryChooser();

//            labels
            Label inputLabel = new Label("no input file selected");
            Label outputLabel = new Label("no output directory selected");
            message = new Label("");

//            input button, calls fileChooser, changes inputLabel to selected filepath
            inputButton = new Button("select input file");
            inputButton.setOnAction(event -> {
                file = fileChooser.showOpenDialog(window);
                inputLabel.setText(file.getAbsolutePath());
            });
            inputButton.setStyle(buttonStyle);

//            output button, calls directoryChooser, changes outputLabel to selected dirpath
            outputButton = new Button("select output directory");
            outputButton.setOnAction(event -> {
                directory = directoryChooser.showDialog(window);
                outputLabel.setText(directory.getAbsolutePath());
            });
            outputButton.setStyle(buttonStyle);

//            begins loading from file (selected by fileChooser)
//            runs only if both input file and output directory are selected
            startButton = new Button("create");
            startButton.setOnAction(event -> {
                if (file != null && directory != null) {
                    setMessage("Reading from Excel file.");
                    running = true;
                    createHash(file);
                }
                else {
                    setMessage("Failed to read from file, input or output selection invalid.");
                }
            });
            startButton.setStyle(buttonStyle);

//            creates horizontal arrays of input (and output) buttons and labels
            inputLine = new HBox();
            inputLine.getChildren().addAll(inputButton, inputLabel);
            inputLine.setSpacing(10);
            inputLine.setAlignment(Pos.CENTER);
            inputLine.setPadding(new Insets(10, 0, 0, 0));

            outputLine = new HBox();
            outputLine.getChildren().addAll(outputButton, outputLabel);
            outputLine.setSpacing(10);
            outputLine.setAlignment(Pos.CENTER);
            outputLine.setPadding(new Insets(0, 0, 20, 0));

//            adds all nodes to the main vertical box
            vbox.getChildren().addAll(inputLine, outputLine, startButton, message);

//            creates a scene, shows
            Scene scene = new Scene(vbox);
            Main.window.setScene(scene);
            Main.window.show();
        }

        catch (Exception exception) {
            showError(exception);
        }
    }

    /**
     * Redraws the UI with SubjectBox Nodes for each subject (from Teacher objects)
     */
    void drawSubjectList () {
        setMessage("Loading subjects, please check optional subjects.");

        vbox.getChildren().clear();
        startButton.setOnAction(this::setOptionalSubjects);
        vbox.getChildren().addAll(inputLine, outputLine, startButton, message);

//        creates a SubjectBox for each Teacher
        for (Teacher teacher : teachers) {
            vbox.getChildren().add(new SubjectBox(teacher));
        }

        ScrollPane pane = new ScrollPane();
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.setContent(vbox);

        Scene scene = new Scene(pane);
        window.setScene(scene);
    }

    /**
     * Multithreading class
     */
    public class Dolphin extends Thread {

        //        used to terminate thread if necessary
        public volatile boolean running = true;

        /**
         * Terminates thread
         */
        public void terminate () {
            running = false;
        }

        /**
         * Starts all PDF generation
         */
        public void run () {
            try {
//                    loops through all teachers, creates all ratings
                for (Teacher teacher : teachers) {

//                    stops if thread is terminated
                    if (!running)
                        return;

                    PdfGenerator pdfGenerator = new PdfGenerator(teachers);
                    String currentSubjectName = teacher.getNameSubject();

                    pdfGenerator.generateRatings(teacher, directory);
                    System.out.println("Generated ratings for " + currentSubjectName);

                    pdfGenerator.generateSubEval(teacher, directory);
                    System.out.println("Generated subject evaluation for " + currentSubjectName);

                    pdfGenerator.generateTeachEval(teacher, directory);
                    System.out.println("Generated teacher evaluation for " + currentSubjectName);
                }

            } catch (Exception exception) {
                showError(exception);
                exception.printStackTrace();
                running = false;
            } finally {
                System.exit(0);
            }
        }
    }

    /**
     * Calls for PDFs to be generated
     *
     * @param actionEvent   mandatory ActionEvent parameter
     */
    void setOptionalSubjects(ActionEvent actionEvent) {

        try {

            setMessage("Optional subjects selected, generating feedback.");

//            removes all interactive GUI elements
            vbox.getChildren().clear();
            vbox.getChildren().add(message);

            Pane pane = new Pane(vbox);
            Scene scene = new Scene(pane);
            window.setScene(scene);

//            starts second thread that generates PDFs
            Dolphin thread = new Dolphin();
            thread.start();
            setMessage("PDF files will generate in the background.\nThe application closes once finished.\nTo terminate the process, close the application.");

//            ends thread if window is closed
            window.setOnCloseRequest(event -> {
                thread.terminate();
                window.close();
            });

        } catch (Exception exception) {
            exception.printStackTrace();
            showError(exception);
        }
    }

    /**
     * Creates a HashMap with all spreadsheet data; calls functions to generate PDFs
     *
     * @param file      input file, excel format
     */
    void createHash (File file) {
        Row rowCurrent;
        Cell cellMaster;
        Teacher teacherCurrent = null;
        HashMap<String, Teacher> teachers = new HashMap<>();

        try {
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);    // pozor na cislo sheetu

            Iterator<Row> iteratorRow = sheet.iterator();

            iteratorRow.next();      // aby sme zacali na riadku index 1
            while (iteratorRow.hasNext()) {
                rowCurrent = iteratorRow.next();

                Iterator<Cell> iteratorCell = sheet.getRow(0).cellIterator();   // tam su otazky
                iteratorCell.next();    // aby sme zacali na cell index 1
                while (iteratorCell.hasNext()) {
                    cellMaster = iteratorCell.next();

                    if (rowCurrent.getCell(cellMaster.getColumnIndex()) != null && cellMaster.getStringCellValue().contains("Pick your")) {
                        if (teachers.containsKey(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue())) {
                            teacherCurrent = teachers.get(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                        } else if (!rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue().contains(" not ")) {
                            teacherCurrent = new Teacher(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                            teachers.put(teacherCurrent.getNameSubject(), teacherCurrent);
                        } else {
                            teacherCurrent = null;
                        }
                    } else if (teacherCurrent != null && rowCurrent.getCell(cellMaster.getColumnIndex()) != null) {
                        if (cellMaster.getStringCellValue().contains("Characterize")) {   // characterize je na subject
                            teacherCurrent.addSubjectEval(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                        } else if (cellMaster.getStringCellValue().contains("What are")) {      // what are je na Teacher
                            teacherCurrent.addTeacherEval(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                        } else{
                            teacherCurrent.addScore(cellMaster.getStringCellValue(), rowCurrent.getCell(cellMaster.getColumnIndex()).getNumericCellValue());
                        }
                        teachers.replace(teacherCurrent.getNameSubject(), teacherCurrent);
                    }
                }
            }

            Vector<Teacher> teachersVector = new Vector<>(5, 5);
            for (Map.Entry<String, Teacher> stringTeacherEntry : teachers.entrySet()) {
                teachersVector.add((Teacher) ((Map.Entry) stringTeacherEntry).getValue());
            }

            iteratorRow.remove();
            inputStream.close();
            workbook.close();

            Main.teachers = teachersVector;

            setMessage("Done reading Excel file.");

            drawSubjectList();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Creates error window, prints Exception
     *
     * @param exception     Exception that is printed
     */
    void showError (Exception exception) {
        setMessage(exception.toString());
    }

    static void setMessage(String message) {
        Main.message.setText(String.format("[" + "%1$TH:%1$TM:%1$TS", System.currentTimeMillis()) + "] " + message);
    }
}

class PdfGenerator {

    private HashMap averagesMandatory;
    private HashMap averagesOptional;
    private ArrayList<String> mandatorySubjectList;
    private ArrayList<String> optionalSubjectList;
    private Font font;

    PdfGenerator(Vector teachers) {
//    PdfGenerator(Vector teachers) throws IOException, DocumentException {
        try {
            mandatorySubjectList = new ArrayList<>();
            optionalSubjectList = new ArrayList<>();

            averagesMandatory = calculateAverages(teachers);
            averagesOptional = calculateAveragesOptional(teachers);

            //        creates directories
            File mandatoryDir = new File(Main.directory.getPath() + "\\mandatory\\");
            File optionalDir = new File(Main.directory.getPath() + "\\optional\\");
            mandatoryDir.mkdir();
            optionalDir.mkdir();

            BaseFont baseFont = BaseFont.createFont("roboto.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            font = new Font(baseFont);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void generateRatings(Teacher teacher, File directory) throws IOException, DocumentException {

        //Vezmem reku hodnotenia ucitela
        HashMap ratings = teacher.getHashMap();

//        get and sort all keys for teacher ratings
        LinkedList keys = new LinkedList<>(ratings.keySet());
        Collections.sort(keys);

        //Na nazvy suborov
        int filenumber = 1;

        //Veci na path kde sa to ulozi a nazov suboru
        String path;

        //Povinne predmety idu do mandatory priecinku a nepovinne do optional
        if (teacher.optionalSubject) {
            path = directory.getPath() + "\\optional\\";
        } else {
            path = directory.getPath() + "\\mandatory\\";
        }

        String subjectName = fixFileName(teacher.getNameSubject());
        String filename = subjectName + "_ratings";

        //Veci na vytvorenie pdfka
        Document document = new Document();
        FileOutputStream fileOutputStream = new FileOutputStream(path + filename + ".pdf");
        PdfWriter.getInstance(document, fileOutputStream);
        document.open();
        //Toto som sem musel dat lebo si to myslelo ze dokument je prazdny
        document.add(new Chunk(""));

        //Na koniec pridam zoznam vsetkych porovnavanych predmetov podla toho ci je mandatory alebo optional
        ArrayList<String> comparedSubjects;

        if (teacher.optionalSubject) {
            comparedSubjects = optionalSubjectList;
        } else {
            comparedSubjects = mandatorySubjectList;
        }

        document.add(new Paragraph("Porovnané predmety:",font));
        document.add(new Paragraph("Compared subjects:",font));

        for (String comparedSubject : comparedSubjects) {
            document.add(new Paragraph(comparedSubject, font));
        }

        document.newPage();

        document.add(new Paragraph(teacher.getNameSubject(),font));

        for (Object key : keys) {

            //Vezmem value teda array frekvencii hodnoteni a nazov predmetu z objektu na ktorom sme teraz
            int[] frequencies = (int[]) ratings.get(key);

            //Vytvorim dataset na graf
            DefaultCategoryDataset ratingsSet = new DefaultCategoryDataset();

            //Vytvorim sablonu tabulky
            PdfPTable table = new PdfPTable(6);
            table.addCell("Hodnota / value");

            //Pridam to tabulky cisla hodnoteni
            for (int i = 1; i <= frequencies.length; i++) {
                table.addCell(Integer.toString(i));
            }

            table.addCell("Koľkokrát / frequency");

            //Intermediate premenne na priemer
            int totalScore = 0;
            int totalRespondents = 0;
            float average;

            //Prebehnem pole s frekvenciami a dam ich do datasetu
            for (int i = 0; i < frequencies.length; i++) {

                //Pridam frekvencie do datasetu na fraf
                ratingsSet.setValue(frequencies[i], "Frequency", Integer.toString(i + 1));
                //Pridam frekvencie do tabulky
                table.addCell(Integer.toString(frequencies[i]));

                totalScore += (i + 1) * frequencies[i];
                totalRespondents += frequencies[i];

            }

            //Vyratam priemer
            average = (float) totalScore / (float) totalRespondents;
            //A pridam ho do tabulky
            table.addCell("Priemer / average");
            table.addCell(Float.toString(average));
            //Doplnim prazdne bunky do riadku s priemerom lebo inak to nechce spravit tabulku
            for (int i = 2; i <= frequencies.length; i++) {
                table.addCell("");
            }

            //Spravim chart objekt s datasetu
            JFreeChart ratingsChart = ChartFactory.createBarChart(
                    "Vaše hodnotenie/ your evaluation", "Hodnota / value", "Koľkokrát / frequency",
                    ratingsSet, PlotOrientation.VERTICAL, false, true, false);

            //A nakreslim z toho pekny obrazok
            String ratingsChartFileName = "ratings" + filenumber + ".png";
            File ratingsChartFile = new File(ratingsChartFileName);
            ChartUtils.saveChartAsPNG(ratingsChartFile, ratingsChart, 640, 480);

            //Dam otazku a za nou graf a tabulku to pdfka
            Image ratingsChartImage = Image.getInstance(ratingsChartFileName);
            ratingsChartImage.scalePercent(60);
            document.add(new Paragraph((String) key,font));
            document.add(ratingsChartImage);
            File toRemove = new File(ratingsChartFileName);
            toRemove.delete();

            document.add(table);

            //Pridam nadpisy na priemery
            document.add(new Chunk(""));
            document.add(new Paragraph("Hodnotenia všetkých predmetov/učiteľov od najnižšieho po najvyššie",font));
            document.add(new Paragraph("Evaluation of all subject/teachers from the lowest to the highest",font));

            //Vezmem LinkedList priemerov na danu otazku (optional a mandatory subjects sa porovnavaju osobitne) a spravim z toho array

            Object[] averagesArray;

            if (!teacher.optionalSubject) {
                LinkedList averagesTemp = (LinkedList) averagesMandatory.get(key);
                averagesArray = averagesTemp.toArray();
            } else {
                LinkedList averagesTemp = (LinkedList) averagesOptional.get(key);
                averagesArray = averagesTemp.toArray();
            }

            //Vytvorim dataset na priemery
            DefaultCategoryDataset averagesSet = new DefaultCategoryDataset();

            //Nahadzem priemery do datasetu
            boolean teacherFound = false;
            for (int i = 0; i < averagesArray.length; i++) {
                if (averagesArray[i].equals(average) && !teacherFound) {
                    averagesSet.setValue((float)averagesArray[i], "thisteacher", Integer.toString(i+1));
                    teacherFound = true;
                } else {
                    averagesSet.setValue((float)averagesArray[i], "Average", Integer.toString(i+1));
                }
            }

            //Spravim chart z priemerov
            JFreeChart averagesChart = ChartFactory.createBarChart(
                    "", "Hodnota/ value", "",
                    averagesSet, PlotOrientation.VERTICAL, false, true, false);

            CategoryPlot plot = averagesChart.getCategoryPlot();
            plot.getDomainAxis().setVisible(false);
            plot.getDomainAxis().setCategoryMargin(0.0);
            BarRenderer barRenderer = (BarRenderer)plot.getRenderer();

            barRenderer.setItemMargin(0);
//            barRenderer.setMaximumBarWidth(200);
            barRenderer.setSeriesPaint(1, Color.ORANGE);
            barRenderer.setSeriesPaint(0, Color.RED);

            barRenderer.setDrawBarOutline(false);

            String averagesChartFileName = "averages" + filenumber + ".png";
            File averagesChartFile = new File(averagesChartFileName);
            ChartUtils.saveChartAsPNG(averagesChartFile, averagesChart, 640, 480);

            //Dam chart do PDFka
            Image averagesChartImage = Image.getInstance(averagesChartFileName);
            averagesChartImage.scalePercent(60);
            document.add(averagesChartImage);
            toRemove = new File(averagesChartFileName);
            toRemove.delete();

            document.newPage();

            //Zvysim cislo grafu o 1
            filenumber++;
        }

        //Koniec prace s pdfkom
        document.close();
        fileOutputStream.close();
    }

    public void generateSubEval(Teacher teacher, File directory) throws IOException, DocumentException {

        String[] subjectEval = teacher.getSubjectEval();

        //Veci na path kde sa to ulozi a nazov suboru
        String path;

        //Povinne predmety idu do mandatory priecinku a nepovinne do optional
        if (!teacher.optionalSubject) {
            path = directory.getPath() + "\\mandatory\\";
        } else {
            path = directory.getPath() + "\\optional\\";
        }

        String subjectName = fixFileName(teacher.getNameSubject());
        String filename = subjectName + "_subjecteval";

        //Veci na vytvorenie pdfka
        Document document = new Document();
        FileOutputStream fileOutputStream = new FileOutputStream(path + filename + ".pdf");
        PdfWriter.getInstance(document, fileOutputStream);
        document.open();
        //Toto som sem musel dat lebo si to myslelo ze dokument je prazdny
        document.add(new Chunk(""));
        document.add(new Paragraph(teacher.getNameSubject(),font));

        //Nadpisy
        document.add(new Paragraph("Popíš typickú hodinu a čo sa ti na hodinách páči/nepáči? Ako by sa dali hodiny zlepšiť?",font));
        document.add(new Paragraph("Characterize typical lessons and what you like/dislike about them? What would you suggest to improve the lessons?",font));
        document.add(new Chunk(""));

        //Vytvorim sablonu tabulky
        PdfPTable table = new PdfPTable(1);

        //Do tabulky nahadzem vsetky hodnotenia na dany predmet
        for (String eval : subjectEval) {
            table.addCell(eval);
        }

        //Pridam tabulku do suboru
        document.add(table);

        //Koniec prace s pdfkom
        document.close();
        fileOutputStream.close();
    }

    public void generateTeachEval(Teacher teacher, File directory) throws IOException, DocumentException {

        String[] teachEval = teacher.getTeacherEval();

        //Veci na path kde sa to ulozi a nazov suboru
        String path;

        //Povinne predmety idu do mandatory priecinku a nepovinne do optional
        if (!teacher.optionalSubject) {
            path = directory.getPath() + "\\mandatory\\";
        } else {
            path = directory.getPath() + "\\optional\\";
        }

        String subjectName = fixFileName(teacher.getNameSubject());
        String filename = subjectName + "_teachereval";

        //Veci na vytvorenie pdfka
        Document document = new Document();
        FileOutputStream fileOutputStream = new FileOutputStream(path + filename + ".pdf");
        PdfWriter.getInstance(document, fileOutputStream);
        document.open();
        //Toto som sem musel dat lebo si to myslelo ze dokument je prazdny
        document.add(new Chunk(""));
        document.add(new Paragraph(teacher.getNameSubject(),font));

        //Nadpisy
        document.add(new Paragraph("Prečo je/nie je učiteľ pre mňa vzorom? Čo sú jeho silné stránky a na čom by mohol popracovať?",font));
        document.add(new Paragraph("What are the reasons that the teacher is/is not positive role model for me? What are the teacher’s strengths and what could he/she improve?",font));
        document.add(new Chunk(""));

        //Vytvorim sablonu tabulky
        PdfPTable table = new PdfPTable(1);

        //Do tabulky nahadzem vsetky hodnotenia na dany predmet
        for (String eval : teachEval) {
            table.addCell(eval);
        }

        //Pridam tabulku do suboru
        document.add(table);

        //Koniec prace s pdfkom
        document.close();
        fileOutputStream.close();
    }

    public HashMap calculateAverages(Vector teachers) {

        HashMap<String, LinkedList<Float>> averages = new HashMap<>();

        for (Object teacher : teachers) {

            if (!((Teacher) teacher).optionalSubject) {

                mandatorySubjectList.add(((Teacher)teacher).getNameSubject());

                HashMap ratings = ((Teacher) teacher).getHashMap();
                LinkedList keys = new LinkedList<>(ratings.keySet());
                Collections.sort(keys);

                for (Object key : keys) {

                    int[] frequencies = (int[]) ratings.get(key);
                    String question = (String) key;

                    int totalScore = 0;
                    int totalRespondents = 0;
                    float currentAverage;

                    for (int i = 0; i < frequencies.length; i++) {
                        totalScore += (i + 1) * frequencies[i];
                        totalRespondents += frequencies[i];
                    }

                    currentAverage = (float) totalScore / (float) totalRespondents;

                    if (averages.containsKey(question)) {
                        LinkedList temp = averages.get(question);
                        addValue(temp, currentAverage);
                        averages.put(question, temp);
                    } else {
                        LinkedList<Float> temp = new LinkedList<>();
                        addValue(temp, currentAverage);
                        averages.put(question, temp);
                    }
                }
            }
        }
        return averages;
    }

    public HashMap calculateAveragesOptional(Vector teachers) {

        HashMap<String, LinkedList<Float>> averages = new HashMap<>();

        for (Object teacher : teachers) {

            if (((Teacher) teacher).optionalSubject) {

                optionalSubjectList.add(((Teacher)teacher).getNameSubject());

                HashMap ratings = ((Teacher) teacher).getHashMap();
                LinkedList<String> keys = new LinkedList<>(ratings.keySet());
                Collections.sort(keys);

                for (Object key : keys) {

                    int[] frequencies = (int[]) ratings.get(key);
                    String question = (String) key;

                    int totalScore = 0;
                    int totalRespondents = 0;
                    float currentAverage;

                    for (int i = 0; i < frequencies.length; i++) {
                        totalScore += (i + 1) * frequencies[i];
                        totalRespondents += frequencies[i];
                    }

                    currentAverage = (float) totalScore / (float) totalRespondents;

                    if (averages.containsKey(question)) {
                        LinkedList temp = averages.get(question);
                        addValue(temp, currentAverage);
                        averages.put(question, temp);
                    } else {
                        LinkedList<Float> temp = new LinkedList<>();
                        addValue(temp, currentAverage);
                        averages.put(question, temp);
                    }
                }
            }
        }
        return averages;
    }

    private String fixFileName(String filename) {
        return filename.replaceAll("(\\Q/\\E)|(\\Q?\\E)|(\\Q<\\E)|(\\Q>\\E)|(\\Q\\\\E)|(\\Q:\\E)|(\\Q*\\E)(\\Q|\\E)|(\\Q\"\\E)","+");
    }

    private static void addValue(LinkedList<Float> llist, float val) {

        if (llist.size() == 0) {
            llist.add(val);
        } else if (llist.get(0) > val) {
            llist.add(0, val);
        } else if (llist.get(llist.size() - 1) < val) {
            llist.add(llist.size(), val);
        } else {
            int i = 0;
            while (llist.get(i) < val) {
                i++;
            }
            llist.add(i, val);
        }
    }
}

class Teacher {
    public String nameSubject;
    public HashMap<String, int[]> numQuestions;
    public Vector<String> subjectEval, teacherEval;
    public boolean optionalSubject;

    Teacher(String nameSubject){
        this.nameSubject = nameSubject;
        numQuestions = new HashMap<>();
        subjectEval = new Vector<>(5,5);
        teacherEval = new Vector<>(5,5);
        optionalSubject = false;
    }

    void addScore(String question, double score){
        if (numQuestions.containsKey(question)){
            int[] frequency = numQuestions.get(question);
            frequency[(int)(score-1)]+=1;
            numQuestions.replace(question, frequency);
        }
        else{
            int[] frequency = new int[(int) (double) 5];
            for (int i = 0; i< (double) 5; i++){
                frequency[i]=0;
            }
            frequency[(int)(score-1)]+=1;
            numQuestions.put(question, frequency);
        }
    }

    void addSubjectEval(String evaluation){
        subjectEval.add(evaluation);
    }

    String[] getSubjectEval(){
        return subjectEval.toArray(new String[0]);
    }

    void addTeacherEval(String evaluation){
        teacherEval.add(evaluation);
    }

    String[] getTeacherEval(){
        return teacherEval.toArray(new String[0]);
    }

    String getNameSubject(){
        return nameSubject;
    }

    HashMap getHashMap() {
        return numQuestions;
    }
}
