package eu.fbk.dh.CAT_R_Kappa_Cohen;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by giovannimoretti on 12/02/17.
 */
public class Main {
    public static void main(String[] args) {
        File folder = new File(args[0]);
        File folder2 = new File(args[1]);



        for (String classe  : args[2].split(",")) {
            System.out.println("Markable class: " + classe);
            try {
                java.nio.file.Files.walk(folder.toPath()).collect(Collectors.toList())//.parallelStream()
                        .forEach(filePath -> {
                            try {
                                if (java.nio.file.Files.isRegularFile(filePath)) {

                                    File file1 = filePath.toFile();

                                    File file2 = Paths.get(folder2.getAbsolutePath(), file1.getName()).toFile();


                                    CATFIle_Analyzer ca1 = new CATFIle_Analyzer(file1, classe);
                                    CATFIle_Analyzer ca2 = new CATFIle_Analyzer(file2, classe);


                                    ca1.analize();
                                    ca2.analize();

                                    if (ca1.compareTo(ca2) != 0) {
                                        System.out.println("Somethig wrog with cat file name or file token number");
                                        System.exit(1);
                                    }


                                    if (!ca1.isAnnotated() && !ca2.isAnnotated()) {
                                        System.out.println(file1.getName() + " not annotated with class " + classe);
                                    } else {


                                        try {
                                            BufferedWriter writer = new BufferedWriter(new FileWriter("Raters_CSV.tsv"));
                                            CSVFormat csvFileFormat = CSVFormat.TDF.withRecordSeparator("\n");
                                            CSVPrinter printer = new CSVPrinter(writer, csvFileFormat);
                                            final Object[] FILE_HEADER = {"R1", "R2"};
                                            printer.printRecord(FILE_HEADER);


                                            for (Integer s : ca1.getSubjects().keySet()) {
                                                List row = new ArrayList();
                                                row.add(ca1.getSubjects().get(s).toString());
                                                row.add(ca2.getSubjects().get(s).toString());

                                                printer.printRecord(row);
                                            }

                                            printer.flush();
                                            printer.close();

                                            ProcessBuilder builder = new ProcessBuilder("Rscript", "Kappa.r");

                                            final Process process = builder.start();
                                            InputStream is = process.getInputStream();
                                            InputStreamReader isr = new InputStreamReader(is);
                                            BufferedReader br = new BufferedReader(isr);
                                            String line;
                                            while ((line = br.readLine()) != null) {
                                                System.out.println(file1.getName() + line.replace("[1]", "").replace("NaN", "1.0").replace(" ", "\t"));
                                            }
                                            //    System.out.println("Program terminated!");


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }


                                }
                            } catch (Exception e) {
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("");
        }





    }
}
