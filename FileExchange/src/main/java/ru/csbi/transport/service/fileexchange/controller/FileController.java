package ru.csbi.transport.service.fileexchange.controller;

import java.io.File;

/**
 * @author d.pogorelov
 */
public interface FileController
{
  File getNextInputFile();

  File getNextOutputFile(String fileName);
  
  File moveErrorFile(File file);
}
