
from loci.plugins import BF
from loci.formats import ImageReader
from loci.formats import ImageWriter
from loci.formats import MetadataTools
from loci.formats.services import OMEXMLService
from loci.common.services import ServiceFactory
from loci.plugins.in import ImporterOptions

reader = ImageReader()
omeMeta = MetadataTools.createOMEXMLMetadata()
reader.setMetadataStore(omeMeta)
reader.setOriginalMetadataPopulated(True)


# create a writer that will automatically handle any supported output format
writer = ImageWriter()

factory = ServiceFactory();
service = factory.getInstance(OMEXMLService);

# create initial writer metadata from first file
file = "/Users/dgault/Documents/Sample Images/vsi/test/R6_EXP001_AQPO4_WFA_01.vsi"
reader.setId(file)
allMetadata = reader.getGlobalMetadata()
initialMetadata = service.asRetrieve(reader.getMetadataStore())
writerMetadata = service.createOMEXMLMetadata(initialMetadata.dumpXML());
reader.close()

# add additional writer metadata for remaining file
writerRoot = writerMetadata.getRoot()
writerIndex = 2
for fileIndex in range(1, 4):
  file = "/Users/dgault/Documents/Sample Images/vsi/test/R6_EXP001_AQPO4_WFA_0"+str(fileIndex + 1)+".vsi"
  reader.setId(file)
  readerMetadata = service.asRetrieve(reader.getMetadataStore())
  root = readerMetadata.getRoot()
  seriesCount = reader.getSeriesCount()

  for series in range(reader.getSeriesCount()):
    reader.setSeries(series)
    seriesMetadata = reader.getSeriesMetadata()
    image = root.getImage(series)
    image.setID("Image:"+str(writerIndex))
    writerRoot.addImage(image)
    writerIndex = writerIndex + 1

    writerMetadata.setRoot(writerRoot)
    seriesMeta = reader.getSeriesMetadata()

    name = "Series " + str(writerIndex)
    realName = reader.getMetadataStore().getImageName(series)
    if realName is None and realName.trim().length() != 0:
      name = realName
    MetadataTools.merge(seriesMeta, allMetadata, name + " ")
    
  reader.close()

service.populateOriginalMetadata(writerMetadata, allMetadata)
writer.setMetadataRetrieve(writerMetadata)

# initialize the writer
combinedFile = "/Users/dgault/Documents/Sample Images/vsi/test/R6_EXP001_AQPO4_WFA_combined.ome.tiff"
writer.setId(combinedFile)


# write the pixels
writerIndex = 0
for fileIndex in range(4):
  file = "/Users/dgault/Documents/Sample Images/vsi/test/R6_EXP001_AQPO4_WFA_0"+str(fileIndex + 1)+".vsi"
  reader.setId(file)
  seriesCount = reader.getSeriesCount()
  for series in range(reader.getSeriesCount()):
    reader.setSeries(series)

    writer.setSeries(writerIndex)
    writerIndex = writerIndex + 1
    for image in range(reader.getImageCount()):
      writer.saveBytes(image, reader.openBytes(image))
  reader.close()

writer.close()

# read in the combined file and split the channels
options = ImporterOptions()
options.setSplitChannels(True)
options.setOpenAllSeries(True)
options.setShowOMEXML(True)
options.setId(combinedFile)
imps = BF.openImagePlus(options)
for imp in imps:
    imp.show()