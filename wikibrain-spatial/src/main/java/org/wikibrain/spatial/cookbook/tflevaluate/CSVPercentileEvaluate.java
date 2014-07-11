package org.wikibrain.spatial.cookbook.tflevaluate;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.wikibrain.core.dao.Dao;
import org.wikibrain.core.lang.Language;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by toby on 6/18/14.
 */
class EvaluationEntry {
    public String itemName1;
    public Integer itemID1;
    public String itemName2;
    public Integer itemID2;
    public double spatialDistance;
    public double ordinalDistance;
    public double SESDistance;
    public double topoDistance;
    public double SR;
    public double SR_percentile;
    public double dist_percentile;
}


public class CSVPercentileEvaluate {
    public static int round(double i, int v){
        return (int)Math.round(i/v) * v;
    }
    public static void main(String[] args) throws Exception {
        List<Language> langList = new LinkedList<Language>();
        langList.add(Language.getByLangCode("en"));
        langList.add(Language.getByLangCode("ar"));
        langList.add(Language.getByLangCode("ca"));
        langList.add(Language.getByLangCode("ceb"));
        langList.add(Language.getByLangCode("cs"));
        langList.add(Language.getByLangCode("de"));
        langList.add(Language.getByLangCode("es"));
        langList.add(Language.getByLangCode("fa"));
        langList.add(Language.getByLangCode("fi"));
        langList.add(Language.getByLangCode("fr"));
        langList.add(Language.getByLangCode("hu"));
        langList.add(Language.getByLangCode("id"));
        langList.add(Language.getByLangCode("it"));
        langList.add(Language.getByLangCode("ja"));
        langList.add(Language.getByLangCode("ko"));
        langList.add(Language.getByLangCode("nl"));
        langList.add(Language.getByLangCode("no"));
        langList.add(Language.getByLangCode("pl"));
        langList.add(Language.getByLangCode("pt"));
        langList.add(Language.getByLangCode("ru"));
        langList.add(Language.getByLangCode("sv"));
        langList.add(Language.getByLangCode("uk"));
        langList.add(Language.getByLangCode("vi"));
        langList.add(Language.getByLangCode("war"));
        langList.add(Language.getByLangCode("zh"));
        for(Language langu : langList){

            String lang = langu.getLangCode();

            CSVReader reader = new CSVReader(new FileReader("/Users/toby/Dropbox/Tobler2/Data/Basic TFL/TFL-"+lang+".csv"), ',');
            CSVWriter writer = new CSVWriter(new FileWriter("/Users/toby/Dropbox/Tobler2/Data/Basic TFL_bin/TFL-"+lang+"_bin.csv"), ',');
            String[] temp;
            List<EvaluationEntry> entryList = new ArrayList<EvaluationEntry>();
            List<Double> SRList = new ArrayList<Double>();
            Map<Double, Integer> SRMap = new HashMap<Double, Integer>();
            Map<Double, Integer> SRCounter = new HashMap<Double, Integer>();
            temp = reader.readNext();
            while((temp = reader.readNext()) != null){
                EvaluationEntry entry = new EvaluationEntry();
                entry.itemName1 = temp[0];
                entry.itemID1 = Integer.valueOf(temp[1]);
                entry.itemName2 = temp[2];
                entry.itemID2 = Integer.valueOf(temp[3]);
                entry.spatialDistance = Double.valueOf(temp[4]);
                entry.SR = Double.valueOf(temp[5]);
                SRList.add(entry.SR);
                entryList.add(entry);
            }
            Collections.sort(SRList);
            int counter = 0;
            for(Double d : SRList){
                if(SRMap.containsKey(d)){
                    SRMap.put(d, counter + SRMap.get(d));
                    SRCounter.put(d, SRCounter.get(d) + 1);
                }
                else{
                    SRMap.put(d, counter);
                    SRCounter.put(d, 1);
                }
                counter ++;
            }
            for(Double d : SRList){
                SRMap.put(d, SRMap.get(d) / SRCounter.get(d));
            }

            String[] headerEntries = new String[8];
            headerEntries[0] = "ITEM_NAME_1";
            headerEntries[1] = "ITEM_ID_1";
            headerEntries[2] = "ITEM_NAME_2";
            headerEntries[3] = "ITEM_ID_2";
            headerEntries[4] = "SPATIAL_DISTANCE";
            headerEntries[5] = "DISTANCE_BIN";
            headerEntries[6] = "SR";
            headerEntries[7] = "SR_Percentile";
            writer.writeNext(headerEntries);
            writer.flush();

            for(EvaluationEntry e : entryList){
                e.SR_percentile = SRMap.get(e.SR);
                String[] rowEntries = new String[8];
                rowEntries[0] = e.itemName1;
                rowEntries[1] = e.itemID1.toString();
                rowEntries[2] = e.itemName2;
                rowEntries[3] = e.itemID2.toString();
                rowEntries[4] = String.format("%.2f", e.spatialDistance);
                rowEntries[5] = String.format("%d", round(e.spatialDistance, 50));
                rowEntries[6] = Double.valueOf(e.SR).toString();
                rowEntries[7] = String.format("%.2f", e.SR_percentile/SRList.size());
                writer.writeNext(rowEntries);
                writer.flush();
            }

            reader.close();
            writer.close();
        }




    }


}
