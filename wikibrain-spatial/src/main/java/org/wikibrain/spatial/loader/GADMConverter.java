//package org.wikibrain.spatial.loader;
//
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import com.vividsolutions.jts.geom.*;
//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import net.lingala.zip4j.core.ZipFile;
//import net.lingala.zip4j.exception.ZipException;
//import org.apache.commons.cli.*;
//import org.apache.commons.io.FilenameUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.io.FileUtils;
//import org.geotools.data.shapefile.dbf.DbaseFileHeader;
//import org.geotools.data.shapefile.dbf.DbaseFileReader;
//import org.geotools.data.shapefile.dbf.DbaseFileWriter;
//import org.geotools.data.shapefile.files.ShpFiles;
//import org.geotools.data.shapefile.shp.ShapefileException;
//import org.geotools.data.shapefile.shp.ShapefileReader;
//import org.geotools.data.simple.SimpleFeatureIterator;
//import org.geotools.feature.SchemaException;
//import org.geotools.geometry.jts.*;
//import org.geotools.data.DataUtilities;
//import org.geotools.data.DefaultTransaction;
//import org.geotools.data.Transaction;
//import org.geotools.data.collection.ListFeatureCollection;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.ShapefileDataStoreFactory;
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.data.simple.SimpleFeatureStore;
//import org.geotools.data.*;
//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
//import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.geotools.referencing.crs.DefaultGeographicCRS;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.feature.simple.SimpleFeatureType;
//import org.geotools.referencing.crs.DefaultGeographicCRS;
//import org.opengis.geometry.*;
//import org.opengis.geometry.coordinate.*;
//import org.geotools.geometry.jts.*;
//import org.hibernate.annotations.SourceType;
//import org.wikibrain.conf.ConfigurationException;
//import org.wikibrain.conf.Configurator;
//import org.wikibrain.conf.DefaultOptionBuilder;
//import org.wikibrain.core.WikiBrainException;
//import org.wikibrain.core.cmd.Env;
//import org.wikibrain.core.cmd.EnvBuilder;
//import org.wikibrain.core.dao.DaoException;
//import org.wikibrain.core.dao.LocalPageDao;
//import org.wikibrain.phrases.PhraseAnalyzer;
//import org.wikibrain.spatial.core.dao.SpatialDataDao;
//import org.wikibrain.spatial.core.dao.postgis.PostGISDB;
//import org.wikibrain.wikidata.WikidataDao;
//import org.wikibrain.download.*;
//import net.lingala.zip4j.*;
//
//import java.awt.*;
//import java.io.*;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.channels.FileChannel;
//import java.nio.channels.WritableByteChannel;
//import java.nio.charset.Charset;
//import java.util.*;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//
///**
// * Created by aaroniidx on 4/13/14.
// */
//public class GADMConverter {
//
//
//    public static final Logger LOG = Logger.getLogger(GADMConverter.class.getName());
//
//    public static void downloadAndConvert(SpatialDataFolder folder) throws WikiBrainException{
//
//        try {
//
//            String tmpFolder = "_gadmdownload";
//
//
//            // check to see if GADM layers already exist
//            // TODO: this check should be made more sophisticated, esp. in the case of partial downloads
//            if (folder.hasLayer("GADM0", "earth")){ // if layers already exist, do nothing and retur
//                return;
//            }
//
//            // Download to a temp folder (Note that WikiBrain will ignore all reference systems that begin with "_"
//            // TODO: It would be better to have a temp folder elsewhere, but this shouldn't be a hack and shouldn't be platform dependent
//            folder.createNewReferenceSystemIfNotExists(tmpFolder);
//            File rawFile = downloadGADMShapeFile(folder.getRefSysFolder(tmpFolder));
//
//            // convert file and save as layer in earth reference system
//            convertShpFile(rawFile, folder.getRefSysFolder("earth"));
//
//            // delete the temp folder
//            folder.deleteReferenceSystem(tmpFolder);
//
//        }catch(IOException e){
//            throw new WikiBrainException(e);
//        }
//
//    }
//
//    /**
//     * Download GADM shape file
//     *
//     */
//    public static File downloadGADMShapeFile(File tmpFolder) {
//
//        String baseFileName = "gadm_v2_shp";
//        String zipFileName = baseFileName + ".zip";
//        String gadmURL = "http://biogeo.ucdavis.edu/data/gadm2/" + zipFileName;
//        File gadmShapeFile = new File(tmpFolder+"/"+zipFileName);
//        FileDownloader downloader = new FileDownloader();
//        try {
//            if (gadmShapeFile.exists() && !gadmShapeFile.isDirectory())
//                gadmShapeFile.delete();
//            downloader.download(new URL(gadmURL),gadmShapeFile);
//            ZipFile zipFile = new ZipFile(gadmShapeFile.getAbsolutePath());
//            LOG.log(Level.INFO, "Extracting to " + gadmShapeFile.getParent() + "/gadm_v2_shp/" );
//            //System.out.println("Extracting to " + gadmShapeFile.getParent() + "/gadm_v2_shp/");
//            zipFile.extractAll(gadmShapeFile.getParent());
//            File f = new File(tmpFolder+"/"+baseFileName +".shp");
//            LOG.log(Level.INFO, "Extraction complete." );
//            //System.out.println("Extraction complete.");
//            gadmShapeFile.delete();
//            return f;
//        } catch (MalformedURLException e){
//            e.printStackTrace();
//        } catch (IOException e){
//            e.printStackTrace();
//        } catch (ZipException e){
//            e.printStackTrace();
//        } catch (InterruptedException e){
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     *
//     * @param fileName
//     * Takes in the GADM shape file and converts it into the kind of shape file we can read
//     * Recommended JVM max heapsize = 4G
//     *
//     */
//    public static void convertShpFile(File rawFile, File outputFolder) {
//        File file = rawFile;
//        Map map = new HashMap();
//        HashMap<String, List<Geometry>> stateShape = new HashMap<String, List<Geometry>>();
//        HashMap<String, String> stateCountry = new HashMap<String, String>();
//        new File("newgadm").mkdir(); //Must have this if you want to put the output file in a new directory
//
//        ShapefileReader shpReader;
//        GeometryFactory geometryFactory;
//        SimpleFeatureTypeBuilder typeBuilder;
//        SimpleFeatureBuilder featureBuilder;
//        DataStore inputDataStore;
//        List<SimpleFeature> features = new ArrayList<SimpleFeature>();
//
//        try{
//            map.put("url", file.toURI().toURL());
//            inputDataStore = DataStoreFinder.getDataStore(map);
//            SimpleFeatureSource inputFeatureSource = inputDataStore.getFeatureSource(inputDataStore.getTypeNames()[0]);
//            SimpleFeatureCollection inputCollection = inputFeatureSource.getFeatures();
//            SimpleFeatureIterator inputFeatures = inputCollection.features();
//
//            LOG.log(Level.INFO, "Mapping polygons..." );
//            while (inputFeatures.hasNext()) {
//                SimpleFeature feature = inputFeatures.next();
//                String country = ((String)feature.getAttribute(4)).intern();
//
//                if (!stateShape.containsKey(feature.getAttribute(6))) {
//                    stateShape.put((String) feature.getAttribute(6), new ArrayList<Geometry>());
//                    stateCountry.put((String) feature.getAttribute(6), country); //set up the state-country map
//                }
//
//                stateShape.get(feature.getAttribute(6)).add((Geometry)feature.getAttribute(0)); //and put all the polygons under a state into another map
//            }
//            inputFeatures.close();
//            inputDataStore.dispose();
//
//            LOG.log(Level.INFO, "Mapping complete." );
//
//            typeBuilder = new SimpleFeatureTypeBuilder();  //build the output feature type
//            typeBuilder.setName("WIKITYPE");
//            typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
//            typeBuilder.add("the_geom", MultiPolygon.class);
//            typeBuilder.add("TITLE1_EN", String.class);
//            typeBuilder.add("TITLE2_EN", String.class);
//            typeBuilder.setDefaultGeometry("the_geom");
//
//
//            final SimpleFeatureType WIKITYPE = typeBuilder.buildFeatureType();
//
//            geometryFactory = JTSFactoryFinder.getGeometryFactory();
//            featureBuilder = new SimpleFeatureBuilder(WIKITYPE);
//
//            LOG.log(Level.INFO, "Processing polygons...");
//
//            int count = 0;
//            int total = stateShape.keySet().size();
//            for (String state: stateCountry.keySet()){    //create the feature collection for the new shpfile
//                count++;
//                Geometry newGeom = geometryFactory.buildGeometry(stateShape.get(state)).union();
//                featureBuilder.add(newGeom);
//                featureBuilder.add(state);
//                featureBuilder.add(state + ", " + stateCountry.get(state));
//                SimpleFeature feature = featureBuilder.buildFeature(null);
//                if (count % 50 == 0)
//                    LOG.log(Level.INFO, count + "/" + total + " states processed.");
//                features.add(feature);
//                stateShape.remove(state);
//                System.gc();
//            }
//
//
//            LOG.log(Level.INFO, "Processing complete. " + count + " states processed.");
//
//            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();  //create the output datastore
//
//            Map<String, Serializable> outputParams = new HashMap<String, Serializable>();
//            outputParams.put("url", outputFile.toURI().toURL());
//            outputParams.put("create spatial index", Boolean.TRUE);
//
//            ShapefileDataStore outputDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(outputParams);
//
//            outputDataStore.createSchema(WIKITYPE);
//
//            Transaction transaction = new DefaultTransaction("create");
//
//            String typeName = outputDataStore.getTypeNames()[0];
//            SimpleFeatureSource outputFeatureSource = outputDataStore.getFeatureSource(typeName);
//            SimpleFeatureType SHAPE_TYPE = outputFeatureSource.getSchema();
//
//            LOG.log(Level.INFO, "Writing to " + outputFile.getCanonicalPath());
//
//            if (outputFeatureSource instanceof SimpleFeatureStore) {
//                SimpleFeatureStore featureStore = (SimpleFeatureStore) outputFeatureSource;
//
//                SimpleFeatureCollection collection = new ListFeatureCollection(WIKITYPE, features);
//                featureStore.setTransaction(transaction);
//                try {
//                    featureStore.addFeatures(collection);
//                    transaction.commit();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    transaction.rollback();
//                } finally {
//                    transaction.close();
//                }
//                LOG.log(Level.INFO, "Writing success.");
//                System.exit(0); // success!
//            } else {
//                LOG.log(Level.INFO, typeName + " does not support read/write access");
//                System.exit(1);
//            }
//
//
//        } catch (MalformedURLException e){
//            e.printStackTrace();
//
//        } catch (IOException e){
//            e.printStackTrace();
//
//        }
//
//
//
//    }
//
//}
