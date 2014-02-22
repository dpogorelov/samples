package ru.csbi.transport.service.fileexchange.controller;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ru.csbi.transport.service.fileexchange.sources.SourceFile;

/**
 * @author d.pogorelov
 */
public class FileControllerImpl implements FileController
{
  private static final Logger log = Logger.getLogger(FileControllerImpl.class);

  private static final String INPUT_EXTENSION = "xml";
  private static final String OUTPUT_EXTENSION = "xml";
  private static final String CHECKSUM_EXTENSION = "md5";

  private SourceFile sourceFile;
  private boolean isCheckSum;

  private boolean isFailCheckSourceDirectories;

  public FileControllerImpl(SourceFile _sourceFile, boolean _isCheckSum)
  {
    sourceFile = _sourceFile;
    isCheckSum = _isCheckSum;

    checkSourceDirectories();
  }

  @Override
  public File getNextInputFile()
  {
    File sourceDir = new File(sourceFile.getInputPath());

    File nextFile = getFile(sourceDir);

    if( nextFile != null )
    {
      return move(nextFile, sourceFile.getArchivePath());
    }

    return null;
  }

  @Override
  public File moveErrorFile(File file)
  {
    if( isCheckSum() )
    {
      File checksumFile = new File(getChecksumFilename(file));
      if( move(checksumFile, sourceFile.getErrorPath()) == null )
      {
        return null;
      }
    }
    return move(file, sourceFile.getErrorPath());
  }

  @Override
  public File getNextOutputFile(String fileName)
  {
    File outputFile = new File(getOutputFilename(fileName));
    if( !outputFile.exists() )
    {
      try
      {
        if( !outputFile.createNewFile() )
        {
          log.error("Ошибка создания файла " + outputFile);
          return null;
        }
      }
      catch( IOException e )
      {
        log.error("Ошибка создания файла " + outputFile + ":" + e.getMessage());
        return null;
      }
    }

    return outputFile;
  }

  private String getOutputFilename(String fileName)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmssSSS");
    String outputFilename = dateFormat.format(new Date()) + "_" + fileName + "." + OUTPUT_EXTENSION;
    return sourceFile.getOutputPath() + File.separator + outputFilename;
  }

  private String getChecksumFilename(File sourceFile)
  {
    String checksumFilename = FilenameUtils.getBaseName(sourceFile.getPath()) + "." + CHECKSUM_EXTENSION;
    return FilenameUtils.getFullPath(sourceFile.getPath()) + checksumFilename;
  }

  private String getCurrentDateDirectory(String destinationPath)
  {
    SimpleDateFormat dirFormat = new SimpleDateFormat("yyyy-MM-dd");
    String destinationDir = destinationPath + File.separator + dirFormat.format(new Date());
    File fileDisDir = new File(destinationDir);
    if( !fileDisDir.exists() && !fileDisDir.mkdir() )
    {
      log.error("Ошибка создания директории: " + fileDisDir);
      return destinationPath;
    }

    return destinationDir;
  }

  private File move(File source, String destinationPath)
  {
    File destination = new File(getCurrentDateDirectory(destinationPath) + File.separator + source.getName());
    if( !source.getAbsolutePath().equalsIgnoreCase(destination.getAbsolutePath()) )
    {
      deleteFile(destination);

      try
      {
        FileChannel sourceChannel = (new FileInputStream(source)).getChannel();
        FileChannel destinationChannel = (new FileOutputStream(destination)).getChannel();
        try
        {
          sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        }
        catch( IOException e )
        {
          log.error("Ошибка перемещения файла: " + e.getMessage());
          destination = null;
        }
        finally
        {
          try
          {
            sourceChannel.close();
            destinationChannel.close();
            deleteFile(source);
          }
          catch( IOException e )
          {
            log.error("Ошибка закрытия файла: " + e.getMessage());
            destination = null;
          }
        }
      }
      catch( FileNotFoundException e )
      {
        log.error("Файл не найден: " + e.getMessage());
        destination = null;
      }
    }

    return destination;
  }

  private void deleteFile(File deleteFile)
  {
    if( deleteFile.exists() )
    {
      if( !deleteFile.delete() )
      {
        log.info("Ошибка удаления файла " + deleteFile);
      }
    }
  }

  private File getFile(File sourceDir)
  {
    if( !isFailCheckSourceDirectories() )
    {
      File[] files = sourceDir.listFiles();

      if( files == null ) return null;

      Arrays.sort(files, new Comparator<File>()
      {
        @Override
        public int compare(File o1, File o2)
        {
          if( o1.isDirectory() && !o2.isDirectory() )
          {
            return -1;
          }
          else if( !o1.isDirectory() && o2.isDirectory() )
          {
            return 1;
          }
          else
          {
            return o1.getAbsolutePath().compareToIgnoreCase(o2.getAbsolutePath());
          }
        }
      });

      File f = findFile(files);
      if( f != null )
      {
        log.info("Чтение файла: " + f);
        return f;
      }
    }

    return null;
  }

  private File findFile(File[] files)
  {
    for( File f : files )
    {
      if( f.isDirectory() )
      {
        if( !f.delete() )
        {
          f = getFile(f);
          if( f != null && f.isFile() )
          {
            if( checkFile(f) ) return f;
          }
        }
      }
      else
      {
        if( checkFile(f) ) return f;
      }
    }

    return null;
  }

  private boolean checkFile(File file)
  {
    if( !file.canRead() )
    {
      return false;
    }

    if( !FilenameUtils.isExtension(file.getPath(), INPUT_EXTENSION) )
    {
      return false;
    }

    return isCheckSum ? checkSum(file) : true;
  }

  private boolean checkSum(File file)
  {
    log.info("Проверка контрольной суммы файла: " + file);

    File findCheckSumFile = new File(getChecksumFilename(file));
    if( !findCheckSumFile.exists() )
    {
      log.error("Не найден файл с контрольной суммой: " + findCheckSumFile);
      return false;
    }

    String md5Reference = "";
    try
    {
      Scanner scanner = new Scanner(findCheckSumFile);
      try
      {
        if( scanner.hasNext() )
        {
          md5Reference = scanner.next();
        }
      }
      finally
      {
        scanner.close();
      }
    }
    catch( FileNotFoundException e )
    {
      log.error("Не найден файл с контрольной суммой: " + findCheckSumFile);
    }

    String md5 = MD5CheckSum.getMD5CheckSum(file);
    log.info("Контрольная сумма: " + md5);

    boolean result = md5Reference.equals(md5);
    log.info("Контрольная сумма: " + (result ? "OK" : "не совпадает"));

    if( result )
    {
      File checkSum = move(findCheckSumFile, sourceFile.getArchivePath());
      if( checkSum == null )
      {
        log.error("Ошибка перемещения файла контрольной суммы: " + findCheckSumFile);
        return false;
      }
    }

    return result;
  }

  private void checkSourceDirectories()
  {
    checkDir(sourceFile.getInputPath());

    checkDir(sourceFile.getOutputPath());

    checkDir(sourceFile.getErrorPath());

    checkDir(sourceFile.getArchivePath());
  }

  private void checkDir(String sourceDir)
  {
    if( !checkEntryPoint(sourceDir) )
    {
      log.error("Проверка " + sourceDir + " прошла не удачно");
      isFailCheckSourceDirectories = false;
    }
  }

  private boolean checkEntryPoint(String ep)
  {
    if( ep == null )
    {
      return true;
    }

    File dir = new File(ep);
    File checkDir;
    if( dir.isFile() )
    {
      checkDir = new File(dir.getParent());
    }
    else
    {
      checkDir = new File(ep.trim());
    }

    if( checkDir.exists() )
    {
      if( !checkDir.isDirectory() )
      {
        log.error(ep + " не является каталогом");
        return false;
      }
      if( !checkDir.canRead() )
      {
        log.error("Каталог " + ep + " недоступен для чтения");
        return false;
      }
      if( !checkDir.canWrite() )
      {
        log.error("Каталог " + ep + " недоступен для записи");
        return false;
      }
    }
    else
    {
      if( !checkDir.mkdirs() )
      {
        log.error("Невозможно создать каталог " + ep);
        return false;
      }
    }

    return true;
  }

  public boolean isFailCheckSourceDirectories()
  {
    return isFailCheckSourceDirectories;
  }

  public boolean isCheckSum()
  {
    return isCheckSum;
  }
}
