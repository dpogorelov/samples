package ru.csbi.transport.service.fileexchange.controller;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * @author d.pogorelov
 */
public class MD5CheckSum
{
  private static final Logger log = Logger.getLogger(MD5CheckSum.class);

  public static String getMD5CheckSum(File file)
  {
    try
    {
      return convertToHex(createCheckSum(file));
    }
    catch( IOException e )
    {
      log.error("Ошибка чтения файла: " + e.getMessage());
    }

    return null;
  }

  private static String convertToHex(byte[] checkSum)
  {
    String result = "";

    for( int i = 0; i < checkSum.length; i++ )
    {
      result += Integer.toString((checkSum[i] & 0xff) + 0x100, 16).substring(1);
    }
    return result;
  }

  private static byte[] createCheckSum(File file) throws IOException
  {
    try
    {
      InputStream is = new FileInputStream(file);

      byte[] buffer = new byte[1024];

      try
      {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        int numRead;
        do
        {
          numRead = is.read(buffer);
          if( numRead > 0 )
          {
            messageDigest.update(buffer, 0, numRead);
          }
        }
        while( numRead != -1 );

        return messageDigest.digest();
      }
      catch( NoSuchAlgorithmException e )
      {
        log.error("Не поддерживаемый тип алгоритма: " + e.getMessage());
      }
      finally
      {
        is.close();
      }
    }
    catch( FileNotFoundException e )
    {
      log.error("Файл не найден: " + e.getMessage());
    }

    return null;
  }
}
