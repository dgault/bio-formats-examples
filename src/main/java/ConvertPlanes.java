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

        for (int series=0; series<reader.getSeriesCount(); series++) {
            // tell the reader and writer which series to work with
            reader.setSeries(series);
            
            IMetadata omexml2 = service.createOMEXMLMetadata();
            omexml2.setImageID("Image:0", 0);
            omexml2.setPixelsID("Pixels:0", 0);

            int channelCount = 1;
            omexml2.setPixelsBigEndian(omexml.getPixelsBigEndian(series), 0);
            omexml2.setPixelsDimensionOrder(omexml.getPixelsDimensionOrder(series), 0);
            omexml2.setPixelsType(omexml.getPixelsType(series), 0);
            omexml2.setPixelsSizeX(omexml.getPixelsSizeX(series), 0);
            omexml2.setPixelsSizeY(omexml.getPixelsSizeY(series), 0);
            omexml2.setPixelsSizeZ(new PositiveInteger(1), 0);
            omexml2.setPixelsSizeC(new PositiveInteger(channelCount), 0);
            omexml2.setPixelsSizeT(new PositiveInteger(1), 0);

            for (int channel=0; channel<channelCount; channel++) {
              omexml2.setChannelID("Channel:0:" + 0, 0, 0);
              omexml2.setChannelSamplesPerPixel(new PositiveInteger(1), 0, channel);
            }
            Unit<Length> unit = UNITS.MICROMETER;
            Length physicalSizeX = new Length(1.0, unit);
            Length physicalSizeY = new Length(1.5, unit);
            Length physicalSizeZ = new Length(2, unit);
            omexml2.setPixelsPhysicalSizeX(physicalSizeX, 0);
            omexml2.setPixelsPhysicalSizeY(physicalSizeY, 0);
            omexml2.setPixelsPhysicalSizeZ(physicalSizeZ, 0);
            
            // construct a buffer to hold one image's pixels
            byte[] plane = new byte[FormatTools.getPlaneSize(reader)];
            
            // convert each image in the current series
            for (int image=0; image<reader.getImageCount(); image++) {
              ImageWriter writer = new ImageWriter();
              writer.setMetadataRetrieve(omexml2);
              writer.setInterleaved(reader.isInterleaved());
              writer.setId(outputBaseFile+"_T"+image+".tif");
              writer.setSeries(0);

              reader.openBytes(image, plane);
              writer.saveBytes(0, plane);

              writer.close();
            }
        }
        
        reader.close();
    }
    
}
