/*
 * Copyright (c) 2015 DataTorrent, Inc. ALL Rights Reserved.
 *
 */
package com.datatorrent.apps.ingestion.io.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datatorrent.apps.ingestion.Application;
import com.datatorrent.apps.ingestion.IngestionConstants;
import com.datatorrent.apps.ingestion.common.BlockNotFoundException;
import com.datatorrent.apps.ingestion.io.FilterStreamProviders;
import com.datatorrent.apps.ingestion.io.FilterStreamProviders.TimedCipherOutputStream;
import com.datatorrent.apps.ingestion.io.input.IngestionFileSplitter.IngestionFileMetaData;
import com.datatorrent.apps.ingestion.lib.CipherProvider;
import com.datatorrent.apps.ingestion.lib.CryptoInformation;
import com.datatorrent.apps.ingestion.lib.SymmetricKeyManager;

/**
 * This operator merges the blocks into a file. 
 * The list of blocks is obtained from the IngestionFileMetaData. 
 * The implementation extends OutputFileMerger (which uses reconsiler), 
 * hence the file merging operation is carried out in a separate thread.
 * 
 */

public class IngestionFileMerger extends OutputFileMerger<IngestionFileMetaData>
{
  private boolean overwriteOutputFile;
  private boolean encrypt;

  private CryptoInformation cryptoInformation;

  private static final Logger LOG = LoggerFactory.getLogger(IngestionFileMerger.class);
  
  @Override
  protected void mergeOutputFile(IngestionFileMetaData ingestionFileMetaData) throws IOException
  {
    LOG.debug("Processing file: {}", ingestionFileMetaData.getOutputRelativePath());
    
    Path outputFilePath = new Path(filePath, ingestionFileMetaData.getOutputRelativePath());
    if (ingestionFileMetaData.isDirectory()) {
      createDir(outputFilePath);
      completedFilesMetaOutput.emit(ingestionFileMetaData);
      return;
    }
    
    if (outputFS.exists(outputFilePath) && !overwriteOutputFile) {
      LOG.debug("Output file {} already exits and overwrite flag is off. Skipping.", outputFilePath);
      //TODO:add to skipped files
      completedFilesMetaOutput.emit(ingestionFileMetaData);
      return;
    }
    //Call super method for serial merge of blocks
    super.mergeOutputFile(ingestionFileMetaData);
    mergerCounters.getCounter(Counters.TOTAL_DATA_INGESTED).add(ingestionFileMetaData.getFileLength());
  }
  
  /* (non-Javadoc)
   * @see com.datatorrent.apps.ingestion.io.output.OutputFileMerger#writeTempOutputFile(com.datatorrent.apps.ingestion.io.output.OutputFileMetaData)
   */
  @Override
  protected OutputStream writeTempOutputFile(IngestionFileMetaData outFileMetadata) throws IOException, BlockNotFoundException
  {
    OutputStream outputStream = super.writeTempOutputFile(outFileMetadata);
    if(isEncrypt() && outputStream instanceof TimedCipherOutputStream){
      TimedCipherOutputStream timedCipherOutputStream = (TimedCipherOutputStream) outputStream;
      LOG.debug("Adding to counter TIME_TAKEN_FOR_ENCRYPTION : {}", timedCipherOutputStream.getTimeTaken());
      mergerCounters.getCounter(IngestionConstants.IngestionCounters.TIME_TAKEN_FOR_ENCRYPTION).add(timedCipherOutputStream.getTimeTaken());
    }
    return outputStream;
  }

  private void createDir(Path outputFilePath) throws IOException
  {
    if (!outputFS.exists(outputFilePath)) {
      outputFS.mkdirs(outputFilePath);
    }
  }


  @Override
  protected OutputStream getOutputStream(Path partFilePath) throws IOException
  {
    OutputStream outputStream = outputFS.create(partFilePath);
    TimedCipherOutputStream timedCipherOutputStream = null;
    if (isEncrypt()) {
      timedCipherOutputStream = getCipherOutputStream(outputStream);
      return new ObjectOutputStream(timedCipherOutputStream);
    }
    return outputStream;
  }

  @SuppressWarnings("resource")
  protected TimedCipherOutputStream getCipherOutputStream(OutputStream outputStream) throws IOException
  {
    Cipher cipher;
    if (isPKI()) {
      cipher = getCipherForAsymmetricEncryption(outputStream);
    } else {
      cipher = getCipherForSymmetricEncryption(outputStream);
    }
    return new FilterStreamProviders.TimedCipherOutputStream(outputStream, cipher);
  }

  private Cipher getCipherForSymmetricEncryption(OutputStream outputStream) throws IOException
  {
    EncryptionMetaData metaData = new EncryptionMetaData();
    metaData.setTransformation(cryptoInformation.getTransformation());
    writeMetadataToFile(outputStream, metaData);
    return new CipherProvider(cryptoInformation.getTransformation()).getEncryptionCipher(cryptoInformation.getSecretKey());
  }

  /*
   * generates symmetric session key and initializes cipher for symmetric encryption to encrypt file data. Given PKI
   * encryption key is used to encrypt session key and is stored in file as metadata.
   */
  private Cipher getCipherForAsymmetricEncryption(OutputStream outputStream) throws IOException
  {
    // create and encrypt session key
    Key sessionKey = SymmetricKeyManager.getInstance().generateRandomKey();
    byte[] encryptedSessionKey = encryptSessionkeyWithPKI(sessionKey);

    // write session key to file
    EncryptionMetaData metaData = new EncryptionMetaData();
    metaData.setTransformation(cryptoInformation.getTransformation());
    metaData.setKey(encryptedSessionKey);
    writeMetadataToFile(outputStream, metaData);
    return new CipherProvider(Application.AES_TRANSOFRMATION).getEncryptionCipher(sessionKey);
  }

  private byte[] encryptSessionkeyWithPKI(Key sessionKey)
  {
    try {
      Cipher rsaCipher = new CipherProvider(cryptoInformation.getTransformation()).getEncryptionCipher(cryptoInformation.getSecretKey());
      return rsaCipher.doFinal(sessionKey.getEncoded());
    } catch (BadPaddingException e) {
      throw new RuntimeException(e);
    } catch (IllegalBlockSizeException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeMetadataToFile(OutputStream outputStream, EncryptionMetaData metaData) throws IOException
  {
    ObjectOutputStream oos = new ObjectOutputStream(outputStream);
    oos.writeObject(metaData);
    oos.flush();
  }

  private boolean isPKI()
  {
    if (cryptoInformation.getTransformation().equals(Application.RSA_TRANSFORMATION)) {
      return true;
    }
    return false;
  }

  public boolean isOverwriteOutputFile()
  {
    return overwriteOutputFile;
  }

  public void setOverwriteOutputFile(boolean overwriteOutputFile)
  {
    this.overwriteOutputFile = overwriteOutputFile;
  }

  
  public boolean isEncrypt()
  {
    return encrypt;
  }

  public void setEncrypt(boolean encrypt)
  {
    this.encrypt = encrypt;
  }

  public void setCryptoInformation(CryptoInformation cipherProvider)
  {
    this.cryptoInformation = cipherProvider;
  }

}
