package ru.csbi.transport.service.fileexchange.schedule;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;

import ru.csbi.transport.service.fileexchange.controller.FileController;
import ru.csbi.transport.service.fileexchange.schedule.converter.CommandResolver;

/**
 * @author d.pogorelov
 */
public class ScheduleImporter implements Importer
{
  private static final Logger log = Logger.getLogger(ScheduleImporter.class);

  private FileController fileController;
  private CommandResolver resolver;

  @Override
  public void doImport()
  {
    log.info("doImport()...");
    
    File nextFile = fileController.getNextInputFile();
    if( nextFile != null )
    {
      log.info("Next file name=" + nextFile.getAbsolutePath());

      try
      {
        FileInputStream fis = new FileInputStream(nextFile);
        resolver.resolveCommand(fileController, fis, log);
        fis.close();
      }
      catch( Exception e )
      {
        fileController.moveErrorFile(nextFile);
        log.error(e.getMessage());
        e.printStackTrace();
      }
      
      //log.info("Next output file name=" + outputFile.getAbsolutePath());
    }
    else
    {
      log.info("Input directories is empty");
    }
  }

  public FileController getFileController()
  {
    return fileController;
  }

  public void setFileController(FileController _fileController)
  {
    fileController = _fileController;
  }

  public void setResolver(CommandResolver _resolver)
  {
    resolver = _resolver;
  }

}