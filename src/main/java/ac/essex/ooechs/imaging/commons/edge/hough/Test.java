package ac.essex.ooechs.imaging.commons.edge.hough;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

/**
 * Created by willtemperley@gmail.com on 21-Jul-15.
 */
public class Test {
    Dataset hDataset;
    int numBands;

    public Test(String filename){
        gdal.AllRegister();
        hDataset = gdal.Open(filename, gdalconstConstants.GA_ReadOnly);
        this.numBands = hDataset.getRasterCount();
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
            Test instance = new Test("src/test/resources/test.tiff");
            System.out.println(instance.numBands);
    }
}