import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

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

    public MainFrame(String title){
        super(title);

        North=new JPanel();
        South=new JPanel();
        Center=new JPanel();
        button1=new JButton("browse");
        button3 =new JButton("create");
        button2 =new JButton("browse");
        label1=new JLabel("/filename/");
        label3 =new JLabel("/status/");
        label2 =new JLabel("/directory/");

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
                if(option == JFileChooser.APPROVE_OPTION){
                    file = fileChooser.getSelectedFile();
                    label1.setText(file.getName());
                }else{
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
                if(option == JFileChooser.APPROVE_OPTION){
                    directory = fileChooser.getSelectedFile();
                    label2.setText(directory.getPath());
                }else{
                    label2.setText("/pick directory/");
                }
            }
        });

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file!=null & directory!=null){
                    tryCreate(file, directory);
                }
                else{
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

    private void tryCreate(File file, File directory){   // velice inefficient, just wanted to see if i can do something at all
        Row rowCurrent;
        Cell cellMaster;
        teacher teacherCurrent;
        HashMap<String, Integer> teachersIndex = new HashMap<>();
        Vector<teacher> teachersVector = new Vector<>(5,5);
        int index=0;

        try {
            FileInputStream inputStream1 = new FileInputStream(file);
            Workbook workbook1 = WorkbookFactory.create(inputStream1);
            Sheet sheet = workbook1.getSheetAt(0);    // pozor na cislo sheetu

            Iterator<Row> iteratorRow = sheet.iterator();

            iteratorRow.next();
            iteratorRow.next();      // aby sme zacali na riadku index 2
            while (iteratorRow.hasNext()){
                rowCurrent = iteratorRow.next();

                teacherCurrent = null;

                Iterator<Cell> iteratorCell = sheet.getRow(0).cellIterator();
                iteratorCell.next();    // aby sme zacali na cell index 1
                while (iteratorCell.hasNext()){
                    cellMaster = iteratorCell.next();

                    if (cellMaster.getStringCellValue().contains("Pick your")){
                        if (teachersIndex.containsKey(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue())){
                            teacherCurrent = teachersVector.get(teachersIndex.get(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue()));
                        }
                        else if (!rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue().contains("not")){
                            teachersIndex.put(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue(), index);
                            teacherCurrent = new teacher(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                            teachersVector.add(teacherCurrent);
                            index+=1;
                        }
                    }
                    else{
                        if (sheet.getRow(1).getCell(cellMaster.getColumnIndex())!=null && sheet.getRow(1).getCell(cellMaster.getColumnIndex()).getNumericCellValue()==0){
                            if (rowCurrent.getCell(cellMaster.getColumnIndex())!=null && teacherCurrent!=null &&
                                    cellMaster.getStringCellValue().contains("Characterize")){   // characterize je na subject
                                teacherCurrent.addSubjectEval(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                            }
                            else if(rowCurrent.getCell(cellMaster.getColumnIndex())!=null && teacherCurrent!=null &&
                                    cellMaster.getStringCellValue().contains("What are")){      // what are je na teacher
                                teacherCurrent.addTeacherEval(rowCurrent.getCell(cellMaster.getColumnIndex()).getStringCellValue());
                            }
                        }
                        else if(sheet.getRow(1).getCell(cellMaster.getColumnIndex())!=null && sheet.getRow(1).getCell(cellMaster.getColumnIndex()).getNumericCellValue()>0
                                && rowCurrent.getCell(cellMaster.getColumnIndex())!=null && teacherCurrent!=null){
                            teacherCurrent.addScore(cellMaster.getStringCellValue(), rowCurrent.getCell(cellMaster.getColumnIndex()).getNumericCellValue(), sheet.getRow(1).getCell(cellMaster.getColumnIndex()).getNumericCellValue());
                        }
                        if (teacherCurrent!=null){
                            teachersVector.set(teachersIndex.get(teacherCurrent.getNameSubject()), teacherCurrent);
                        }
                    }
                }
            }

            //potialto to spracuvava excel, odtialto to vyraba wordy a pdfka
            
            String dirOutput = directory.getPath()+"\\";
            String dirDocxTeacherEval, dirDocxSubjectEval, dirDocxNumberEval;
            String dirPdfTeacherEval, dirPdfSubjectEval, dirPdfNumberEval;
            XWPFDocument docxTeacherEval, docxSubjectEval, docxNumberEval;
            FileOutputStream fosTeacherEval, fosSubjectEval, fosNumberEval;
            for (int i=0; i<teachersVector.size(); i++){
                dirDocxTeacherEval = dirOutput+teachersVector.get(i).getNameSubject()+" teacherEvaluation.docx";
                dirDocxSubjectEval = dirOutput+teachersVector.get(i).getNameSubject()+" subjectEvaluation.docx";
                dirDocxNumberEval = dirOutput+teachersVector.get(i).getNameSubject()+" numberEvaluation.docx";
                dirPdfTeacherEval = dirOutput+teachersVector.get(i).getNameSubject()+" teacherEvaluation.pdf";
                dirPdfSubjectEval = dirOutput+teachersVector.get(i).getNameSubject()+" subjectEvaluation.pdf";
                dirPdfNumberEval = dirOutput+teachersVector.get(i).getNameSubject()+" numberEvaluation.pdf";
                docxTeacherEval = new XWPFDocument();
                docxSubjectEval = new XWPFDocument();
                docxNumberEval = new XWPFDocument();
                fosTeacherEval = new FileOutputStream(new File(dirDocxTeacherEval));
                fosSubjectEval = new FileOutputStream(new File(dirDocxSubjectEval));
                fosNumberEval = new FileOutputStream(new File(dirDocxNumberEval));

                XWPFParagraph paraOneTeacherEval = docxTeacherEval.createParagraph();

                XWPFRun runOneTeacherEval = paraOneTeacherEval.createRun();
                runOneTeacherEval.setText("Prečo je/nie je učiteľ pre mňa vzorom? Čo sú jeho silné stránky a na čom by mohol popracovať?");
                runOneTeacherEval.setBold(true);
                runOneTeacherEval.addBreak();
                runOneTeacherEval.addBreak();

                XWPFRun runTwoTeacherEval = paraOneTeacherEval.createRun();
                runTwoTeacherEval.setText("What are the reasons that the teacher is/is not positive role model for me? What are the teacher’s " +
                        "strengths and what could he/she improve?");
                runTwoTeacherEval.setBold(true);
                runTwoTeacherEval.addBreak();

                XWPFTable tableTeacherEval = docxTeacherEval.createTable();

                for (int j=0; j<teachersVector.get(i).getTeacherEval().length; j++){
                    if (j==0){
                        XWPFTableRow tableRow = tableTeacherEval.getRow(0);
                        tableRow.getCell(0).setText(teachersVector.get(i).getTeacherEval()[j]);
                    }
                    else{
                        XWPFTableRow tableRow = tableTeacherEval.createRow();
                        tableRow.getCell(0).setText(teachersVector.get(i).getTeacherEval()[j]);
                    }
                }

                XWPFParagraph paraOneSubjectEval = docxSubjectEval.createParagraph();

                XWPFRun runOneSubjectEval = paraOneSubjectEval.createRun();
                runOneSubjectEval.setText("Popíš typickú hodinu a čo sa ti na hodinách páči/nepáči? Ako by sa dali hodiny zlepšiť?");
                runOneSubjectEval.setBold(true);
                runOneSubjectEval.addBreak();
                runOneSubjectEval.addBreak();

                XWPFRun runTwoSubjectEval = paraOneSubjectEval.createRun();
                runTwoSubjectEval.setText("Characterize typical lessons and what you like/dislike about them? What would you suggest to " +
                        "improve the lessons?");
                runTwoSubjectEval.setBold(true);
                runTwoSubjectEval.addBreak();

                XWPFTable tableSubjectEval = docxSubjectEval.createTable();

                for (int j=0; j<teachersVector.get(i).getSubjectEval().length; j++){
                    if (j==0){
                        XWPFTableRow tableRow = tableSubjectEval.getRow(0);
                        tableRow.getCell(0).setText(teachersVector.get(i).getSubjectEval()[j]);
                    }
                    else{
                        XWPFTableRow tableRow = tableSubjectEval.createRow();
                        tableRow.getCell(0).setText(teachersVector.get(i).getSubjectEval()[j]);
                    }
                }

                XWPFParagraph paraOneNumberEval = docxNumberEval.createParagraph();

                XWPFRun runOneNumberEval = paraOneNumberEval.createRun();
                runOneNumberEval.setText(teachersVector.get(i).getNameSubject());
                runOneNumberEval.setBold(true);
                runOneNumberEval.setFontSize(20);
                runOneNumberEval.addBreak();

                XWPFRun runTwoNumberEval = paraOneNumberEval.createRun();
                runTwoNumberEval.setText("Hodnotenie hodín a predmetu / lesson and subject evaluation");
                runTwoNumberEval.setBold(true);
                runTwoNumberEval.setFontSize(15);
                runTwoNumberEval.addBreak();
                runTwoNumberEval.addBreak();

                // Zobrat existujuci subor a len do neho vlozit potrebne udaje

                docxTeacherEval.write(fosTeacherEval);
                docxSubjectEval.write(fosSubjectEval);
                docxNumberEval.write(fosNumberEval);

                fosTeacherEval.close();
                fosSubjectEval.close();
                fosNumberEval.close();

                com.aspose.words.Document docTeacherEval = new com.aspose.words.Document(dirDocxTeacherEval);
                com.aspose.words.Document docSubjectEval = new com.aspose.words.Document(dirDocxSubjectEval);
                com.aspose.words.Document docNumberEval = new com.aspose.words.Document(dirDocxNumberEval);

                docTeacherEval.save(dirPdfTeacherEval);
                docSubjectEval.save(dirPdfSubjectEval);
                docNumberEval.save(dirPdfNumberEval);
            }
            label3.setText("finished");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class teacher{
    private String nameSubject;
    private HashMap<String, int[]> hashMap;
    private Vector<String> subjectEval, teacherEval;

    teacher(String nameSubject){
        this.nameSubject = nameSubject;
        hashMap = new HashMap<>();
        subjectEval = new Vector<>(5,5);
        teacherEval = new Vector<>(5,5);
    }

    public void addScore(String question, double score, double maxScore){
        if (hashMap.containsKey(question)){
            int[] frequency = hashMap.get(question);
            frequency[(int)(score-1)]+=1;
            hashMap.replace(question, frequency);
        }
        else{
            int[] frequency = new int[(int)maxScore];
            for (int i=0; i<maxScore; i++){
                frequency[i]=0;
            }
            frequency[(int)(score-1)]+=1;
            hashMap.put(question, frequency);
        }
    }

    public void addSubjectEval(String evaluation){
        subjectEval.add(evaluation);
    }

    public String[] getSubjectEval(){
        return subjectEval.toArray(new String[0]);
    }

    public void addTeacherEval(String evaluation){
        teacherEval.add(evaluation);
    }

    public String[] getTeacherEval(){
        return teacherEval.toArray(new String[0]);
    }

    public String getNameSubject(){
        return nameSubject;
    }
}
