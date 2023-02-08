package tomocomd;


import silis.api.ambit.AppDomainFactory;
import silis.api.ambit.AppDomainMethod;
import silis.api.ambit.AppDomainModel;

import java.io.*;
import java.sql.Array;
import java.util.*;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        String pathTrain = "C:\\Users\\potter\\OneDrive - CICESE\\Documentos\\Doctorado\\Articulos\\3DILI\\Ejecuciones" +
                "\\MejoresModelos_rendimiento_todas_las_datas\\Bases\\CNN\\trainCSV\\Train_MFE_2D_OK.sdf_279_best_mfe_RandomForest_36004-2.csv";

        String pathTest = "C:\\Users\\potter\\OneDrive - CICESE\\Documentos\\Doctorado\\Articulos\\3DILI\\Ejecuciones" +
                "\\MejoresModelos_rendimiento_todas_las_datas\\Bases\\CNN\\extCSV\\Test_MFE_2D_OK_sin_repetir.sdf_279_best_mfe_RandomForest_36004-2.csv";

        File trainingFile = new File(pathTrain);
        DataInputStream fis = new DataInputStream(new FileInputStream(trainingFile));
        String descriptors = fis.readLine();
        descriptors = descriptors.substring(descriptors.indexOf(",") + 1);
        File csvDataset = new File(pathTest);
        System.out.println();
        AppDomainMethod[] appDomainMethods = AppDomainMethod.values();
        Map<Integer, List<String[]>> predsTotal = new LinkedHashMap();


        for (AppDomainMethod method : appDomainMethods) {
            String[] parameters = new String[]{"-f", descriptors, "-m", method.toString(), "-t", trainingFile.getAbsolutePath()};
            AppDomainModel model = AppDomainFactory.getAppDomain((AppDomainModel) null, parameters, 2);
            model = AppDomainFactory.getAppDomain(model, new String[]{"-s", csvDataset.getAbsolutePath()}, 2);
            fis = new DataInputStream(new FileInputStream(csvDataset));
            fis.readLine();

            int pos = 0;
            while (fis.available() > 0) {
                String line = fis.readLine();
                String[] mds = line.substring(line.indexOf(",") + 1).split(",");
                String[] preds = model.estimateAD(0, mds);

                if (predsTotal.get(pos) == null) {
                    List<String[]> collections = new LinkedList<String[]>();
                    collections.add(preds);
                    predsTotal.put(pos++, collections);
                } else
                    predsTotal.get(pos++).add(preds);
            }
            fis.close();
        }

        for (AppDomainMethod method : appDomainMethods) {
            System.out.print(","+method+",");
        }
        System.out.println();

        predsTotal.forEach( (pos,list) ->{
            System.out.print(pos);
            list.stream().forEach(preds -> System.out.print(","+preds[0]+","+preds[1]));
            System.out.println();
        } );
    }
}
