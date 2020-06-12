import org.apache.poi.ss.usermodel.*;

import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class zeby {    // netusim ako sa ma robit grafika
    public static void main(String[] args){

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame jframe = new MainFrame("Ľubo automatizuje");
                jframe.setSize(400,400);
                jframe.setResizable(true);
                jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jframe.setVisible(true);
            }
        });
    }
}

class MainFrame extends JFrame {

    JButton button1, button3, button2;
    JLabel label1, label3, label2;
    JPanel North, Center, South;
    File file, directory;

    public MainFrame(String title) {
        super(title);

        North = new JPanel();
        South = new JPanel();
        Center = new JPanel();
        button1 = new JButton("browse");
        button3 = new JButton("create");
        button2 = new JButton("browse");
        label1 = new JLabel("/filename/");
        label3 = new JLabel("/status/");
        label2 = new JLabel("/directory/");

        this.setLayout(new BorderLayout());
        this.add(North, BorderLayout.NORTH);
        this.add(South, BorderLayout.SOUTH);
        this.add(Center, BorderLayout.CENTER);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int option = fileChooser.showOpenDialog(MainFrame.super.rootPane);
                if (option == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    label1.setText(file.getName());
                } else {
                    label1.setText("/pick file/");
                }
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(MainFrame.super.rootPane);
                if (option == JFileChooser.APPROVE_OPTION) {
                    directory = fileChooser.getSelectedFile();
                    label2.setText(directory.getPath());
                } else {
                    label2.setText("/pick directory/");
                }
            }
        });

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file != null & directory != null) {
                    createHash(file, directory);
                } else {
                    label1.setText("/pick file/");
                    label2.setText("/pick directory/");
                }
            }
        });

        North.add(button1);
        North.add(label1);
        South.add(button3);
        South.add(label3);
        Center.add(button2);
        Center.add(label2);
    }

    private void createHash(File file, File directory) {
        Row rowCurrent;
        Cell cellMaster;
        Teacher teacherCurrent = null;
        HashMap<String, Teacher> teachers = new HashMap<>();

        try {
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);    // pozor na cislo sheetu

            Iterator<Row> iteratorRow = sheet.iterator();

            iteratorRow.next();      //
            iteratorRow.next();      // aby sme zacali na riadku index 2
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
                    } else if (teacherCurrent != null && sheet.getRow(1).getCell(cellMaster.getColumnIndex()) != null && // aby sme vedeli ze ci numericka otazka a jej maxScore
                            rowCurrent.getCell(cellMaster.getColumnIndex()) != null) {
                        if (sheet.getRow(1).getCell(cellMaster.getColumnIndex()).getNumericCellValue() == 0) {
                            if (cellMaster.getStringCellValue().contains("Characterize")) {   // characterize je na subject
                                teacherCurrent.addSubjectEval(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                            } else if (cellMaster.getStringCellValue().contains("What are")) {      // what are je na Teacher
                                teacherCurrent.addTeacherEval(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                            }
                        } else if (sheet.getRow(1).getCell(cellMaster.getColumnIndex()).getNumericCellValue() > 0) {
                            teacherCurrent.addScore(cellMaster.getStringCellValue(), rowCurrent.getCell(cellMaster.getColumnIndex()).getNumericCellValue(), 5);
                        }
                        teachers.replace(teacherCurrent.getNameSubject(), teacherCurrent);
                    }
                }
            }

            Vector<Teacher> teachersVector = new Vector<>(5, 5);
            Iterator hmIterator = teachers.entrySet().iterator();
            while (hmIterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) hmIterator.next();
                teachersVector.add((Teacher) mapElement.getValue());
            }
            
            iteratorRow.remove();
            inputStream.close();
            workbook.close();

            /*
            TODO: Print message "Done reading Excel file."
             */

            PdfGenerator pdfGenerator = new PdfGenerator();

            for (int i = 0; i < teachersVector.size(); i++) {

                pdfGenerator.createPdf(teachersVector.get(i), directory);

                /*
                TODO: Print message "ratings_[subject_name].pdf generated"
                TODO: Print message "subjecteval_[subject_name].pdf generated" after I write the function that generates
                TODO: subject evaluation pdfs
                TODO: Print message "teachereval_[subject_name].pdf generated" after I write the function that generates
                TODO: teacher evaluation pdfs
                */

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class PdfGenerator {

    PdfGenerator() {}

    public void createPdf(Teacher teacher, File directory) throws IOException, DocumentException {

        //Vezmem reku hodnotenia ucitela
        HashMap ratings = teacher.getHashMap();
        //Vytvorim si tuto vec aby som mohol iterovat cez hash mapu
        Iterator it = ratings.entrySet().iterator();

        //Na nazvy suborov
        int filenumber = 1;

        //Veci na path kde sa to ulozi a nazov suboru
        String path = directory.getPath()+"\\";
        String filename = "ratings_" + teacher.getNameSubject();

        //Veci na vytvorenie pdfka
        Document document = new Document();
        FileOutputStream fileOutputStream = new FileOutputStream(path + filename + ".pdf");
        PdfWriter.getInstance(document, fileOutputStream);
        document.open();
        //Toto som sem musel dat lebo si to myslelo ze dokument je prazdny
        document.add(new Chunk(""));

        while (it.hasNext()) {

            //Vezmem value teda array frekvencii hodnoteni a nazov predmetu z objektu na ktoroj sme teraz
            Map.Entry pair = (Map.Entry) it.next();
            int[] frequencies = (int[]) pair.getValue();
            String question = (String) pair.getKey();

            //Vytvorim dataset na graf
            DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

            //Vytvorim sablonu tabulky
            PdfPTable table = new PdfPTable(6);
            table.addCell("Hodnota/ value");

            //Pridam to tabulky cisla hodnoteni
            for (int i = 1; i <= frequencies.length; i++) {
                table.addCell(Integer.toString(i));
            }

            table.addCell("Koľkokrát/ frequency");

            //Intermediate premenne na priemer
            int totalScore = 0;
            int totalRespondents = 0;
            float average = 0;

            //Prebehnem pole s frekvenciami a dam ich do datasetu
            for (int i = 0; i < frequencies.length; i++) {

                //Pridam frekvencie do datasetu na fraf
                dataSet.setValue(frequencies[i], "Frequency", Integer.toString(i + 1));
                //Pridam frekvencie do tabulky
                table.addCell(Integer.toString(frequencies[i]));

                totalScore += (i + 1) * frequencies[i];
                totalRespondents += frequencies[i];

            }

            //Vyratam priemer
            average = (float) totalScore / (float) totalRespondents;
            //A pridam ho do tabulky
            table.addCell("Priemer/ average");
            table.addCell(Float.toString(average));
            //Doplnim prazdne bunky do riadku s priemerom lebo inak to nechce spravit tabulku
            for (int i = 2; i <= frequencies.length; i++) {
                table.addCell("");
            }

            //Toto tu zevraj treba podla StackOverflow
            it.remove();

            //Spravim chart objekt s datasetu
            JFreeChart chart = ChartFactory.createBarChart(
                    "Vaše hodnotenie/ your evaluation", "Hodnota/ value", "Koľkokrát/ frequency",
                    dataSet, PlotOrientation.VERTICAL, false, true, false);

            //A nakreslim z toho pekny obrazok
            String chartFileName = "chart" + Integer.toString(filenumber) + ".png";
            File barChart = new File(chartFileName);
            ChartUtils.saveChartAsPNG(barChart, chart, 640, 480);

            //Dam otazku a za nou graf a tabulku to pdfka
            Image chartImage = Image.getInstance(chartFileName);
            chartImage.scalePercent(75);
            document.add(new Paragraph(question));
            document.add(chartImage);
            document.add(table);
            document.newPage();

            //Zvysim cislo grafu o 1
            filenumber++;

        }

        //Koniec prace s pdfkom
        document.close();
        fileOutputStream.close();

    }

    public HashMap calculateAverages(Vector teachers) {

        HashMap<String, LinkedList<Float>> averages = new HashMap<>();

        for (Object teacher : teachers) {

            HashMap ratings = ((Teacher) teacher).getHashMap();
            Iterator it = ratings.entrySet().iterator();

            while (it.hasNext()) {

                Map.Entry pair = (Map.Entry) it.next();
                int[] frequencies = (int[]) pair.getValue();
                String question = (String) pair.getKey();

                int totalScore = 0;
                int totalRespondents = 0;
                float currentAverage = 0;

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

                it.remove();
            }

        }

        return averages;

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
    private String nameSubject;
    private HashMap<String, int[]> numQuestions;
    private Vector<String> subjectEval, teacherEval;

    Teacher(String nameSubject){
        this.nameSubject = nameSubject;
        numQuestions = new HashMap<>();
        subjectEval = new Vector<>(5,5);
        teacherEval = new Vector<>(5,5);
    }

    void addScore(String question, double score, double maxScore){
        if (numQuestions.containsKey(question)){
            int[] frequency = numQuestions.get(question);
            frequency[(int)(score-1)]+=1;
            numQuestions.replace(question, frequency);
        }
        else{
            int[] frequency = new int[(int)maxScore];
            for (int i=0; i<maxScore; i++){
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
