# read in and display ImagePlus object(s)
from loci.plugins import BF
file = "/Users/dgault/Documents/Sample Images/vsi/test/R6_EXP001_AQPO4_WFA_0"+str(1)+".vsi"


# parse metadata
from loci.formats import ImageReader
from loci.formats import ImageWriter
from loci.formats import MetadataTools
from loci.formats.services import OMEXMLService
from loci.common.services import ServiceFactory
reader = ImageReader()
omeMeta = MetadataTools.createOMEXMLMetadata()
reader.setMetadataStore(omeMeta)
reader.setOriginalMetadataPopulated(True)


# create a writer that will automatically handle any supported output format
writer = ImageWriter()

factory = ServiceFactory();
service = factory.getInstance(OMEXMLService);

# create initial writer metadata from first file
file = "/Path/to/R6_EXP001_AQPO4_WFA_01.vsi"
reader.setId(file)
initialMetadata = service.asRetrieve(reader.getMetadataStore())
writerMetadata = service.createOMEXMLMetadata(initialMetadata.dumpXML());
reader.close()

# add additional writer metadata for remaining file
writerRoot = writerMetadata.getRoot()
writerIndex = 2
for fileIndex in range(1, 4):
  file = "/Path/to/R6_EXP001_AQPO4_WFA_0"+str(fileIndex + 1)+".vsi"
  reader.setId(file)
  readerMetadata = service.asRetrieve(reader.getMetadataStore())
  root = readerMetadata.getRoot()
  seriesCount = reader.getSeriesCount()

  for series in range(reader.getSeriesCount()):
    reader.setSeries(series)
    seriesMetadata = reader.getSeriesMetadata()
    image = root.getImage(series)
    image.setID("Image:"+str(writerIndex))
    # TODO: copy original metadata
    writerRoot.addImage(image)
    writerIndex = writerIndex + 1
  reader.close()

writerMetadata.setRoot(writerRoot)
writer.setMetadataRetrieve(writerMetadata)

# initialize the writer
writer.setId("/Path/to/combinedVSI.ome.tiff")


# write the pixels
writerIndex = 0
for fileIndex in range(4):
  file = "/Path/to/R6_EXP001_AQPO4_WFA_0"+str(fileIndex + 1)+".vsi"
  reader.setId(file)
  seriesCount = reader.getSeriesCount()
  for series in range(reader.getSeriesCount()):
    reader.setSeries(series)

    writer.setSeries(writerIndex)
    writerIndex = writerIndex + 1
    seriesMetadata = reader.getSeriesMetadata()
    for image in range(reader.getImageCount()):
      writer.saveBytes(image, reader.openBytes(image))
  reader.close()

writer.close()