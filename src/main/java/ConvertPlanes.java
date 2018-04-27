import java.io.IOException;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;

import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;


public class ConvertPlanes {
    
    
    public static void main(String[] args) {
        String inputFile = "path/to/fileToConvert.lif";
        String outputBaseFile = "path/to/output";
        
        // construct the object that stores OME-XML metadata
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        IMetadata omexml = service.createOMEXMLMetadata();
        
        // set up the reader and associate it with the input file
        ImageReader reader = new ImageReader();
        reader.setMetadataStore(omexml);
        reader.setId(inputFile);
        
        // set up the writer and associate it with the output file
        ImageWriter writer = new ImageWriter();
        writer.setMetadataRetrieve(omexml);
        writer.setInterleaved(reader.isInterleaved());
        writer.setId(outputBaseFile+"_s0_0.tif");
        
        
        for (int series=0; series<reader.getSeriesCount(); series++) {
            // tell the reader and writer which series to work with
            reader.setSeries(series);
            writer.setSeries(series);
            
            // construct a buffer to hold one image's pixels
            byte[] plane = new byte[FormatTools.getPlaneSize(reader)];
            
            // convert each image in the current series
            for (int image=0; image<reader.getImageCount(); image++) {
                //change output file for each plane
                writer.changeOutputFile(outputBaseFile+"_s"+series+"_"+image+".tif");
                
                reader.openBytes(image, plane);
                writer.saveBytes(image, plane);
            }
        }
        
        reader.close();
        writer.close();
    }
    
}
