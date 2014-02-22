package ru.csbi.transport.service.fileexchange.sources;

import java.io.File;

/**
 * @author d.pogorelov
 */
public class SourceFile
{
  private String entryPoint;
  
  private String archiveDir;
  
  private String errorDir;
  
  private String inputDir;
  
  private String outputDir;
  
  public String getInputPath()
  {
    return getSourcePath(inputDir);
  }
  
  public String getOutputPath()
  {
    return getSourcePath(outputDir);
  }
  
  public String getArchivePath()
  {
    return getSourcePath(archiveDir);
  }
  
  public String getErrorPath()
  {
    return getSourcePath(errorDir);
  }

  private String getSourcePath(String sourceDir)
  {
    return entryPoint + File.separator + sourceDir;
  }
  
  public String getEntryPoint()
  {
    return entryPoint;
  }

  public void setEntryPoint(String _entryPoint)
  {
    entryPoint = _entryPoint;
  }

  public String getArchiveDir()
  {
    return archiveDir;
  }

  public void setArchiveDir(String _archiveDir)
  {
    archiveDir = _archiveDir;
  }

  public String getErrorDir()
  {
    return errorDir;
  }

  public void setErrorDir(String _errorDir)
  {
    errorDir = _errorDir;
  }

  public String getInputDir()
  {
    return inputDir;
  }

  public void setInputDir(String _inputDir)
  {
    inputDir = _inputDir;
  }

  public String getOutputDir()
  {
    return outputDir;
  }

  public void setOutputDir(String _outputDir)
  {
    outputDir = _outputDir;
  }
}
