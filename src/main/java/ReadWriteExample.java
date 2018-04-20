/*
 * #%L
 * OME Bio-Formats package for reading and converting biological file formats.
 * %%
 * Copyright (C) 2005 - 2017 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.io.IOException;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.FormatTools;
import loci.formats.meta.IMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;

/**
 * Example class for reading and writing a file in a tiled OME-Tiff format.
 *
 * @author David Gault dgault at dundee.ac.uk
 */
public class ReadWriteExample {


  public static void main(String[] args) throws FormatException, IOException, DependencyException, ServiceException {
    // modified based on user submitted code at https://shrib.com/#RjFIQ2X6UyqkAvv-eyCl
    
    //IO
    IFormatReader reader = null;            
    IFormatWriter writer = null;
    int images, fps, num_series;
    String seriesname, route;
      
    //METADATA
    MetadataStore metadata;
    ServiceFactory factory;
    OMEXMLService service;
    Hashtable<String, Object> metaseries;

    num_series = 10; //I previously know how many series there are in the .lif project

    //setup reader
    factory = new ServiceFactory();
    service = factory.getInstance(OMEXMLService.class);
    metadata = service.createOMEXMLMetadata();
    reader = new ImageReader(); 
    reader.setMetadataStore(metadata);
        
    //for each row, we get the path and the video number
    filetoopen = "route to .lif project";
        
    //we create a temp folder
    folder = new File(filetoopen.substring(0, filetoopen.lastIndexOf("\\"))+"\\temp");
    folder.mkdirs();
        
    //start reading
    reader.setId(filetoopen);

    //setup writer
    writer = new ImageWriter();
    writer.setMetadataRetrieve(service.asRetrieve(reader.getMetadataStore()));

    File temporal = new File(filetoopen.substring(0, filetoopen.lastIndexOf("\\")+1)+"temp\\temp.tif");
    if (temporal.exists()) temporal.delete();
    temporal.createNewFile();
    writer.setId(temporal.getAbsolutePath());

    for (int jj = 0; jj<num_series ; jj++){

      //we set the series that we want to read
      reader.setSeries(jj);
      writer.setSeries(jj);
        
      //Get series name and fps
      metaseries = reader.getSeriesMetadata();    

      seriename = metaseries.get("Image name").toString();
      if (seriename == null) seriename = jj+"";

      fps = Double.valueOf(metaseries.get("Image|ATLCameraSettingDefinition|CycleTime").toString());
      if (fps == Double.NaN) fps = 0.04;
      
      //Start extracting each time step
      slides = reader.getImageCount();
      filename = reader.getCurrentFile().substring(reader.getCurrentFile().lastIndexOf("\\")+1,reader.getCurrentFile().length()-4);
      route = filetoopen.substring(0, filetoopen.lastIndexOf("\\"))+"\\temp\\"+filename+"_"+seriename+"_”;

      for (image=0; image<slides; image++) {
        writer.changeOutputFile(route+image+".tif");
        writer.saveBytes(image, reader.openBytes(image));
      }
    }

    // Close readers and writers
    writer.close();
    reader.close();
  }

}
