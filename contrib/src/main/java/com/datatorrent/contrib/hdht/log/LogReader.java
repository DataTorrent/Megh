/**
 * Copyright (c) 2015 DataTorrent, Inc.
 * All rights reserved.
 */
package com.datatorrent.contrib.hdht.log;

import java.io.Closeable;
import java.io.IOException;

public interface LogReader<T> extends Closeable
{
  /**
   * Close WAL after read.
   *
   * @param offset seek offset.
   * @throws IOException
   * @Override public void close() throws IOException;
   *
   * /**
   * Seek to middle of the WAL. This is used primarily during recovery,
   * when we need to start recovering data from middle of WAL file.
   */
  public void seek(long offset) throws IOException;

  /**
   * Advance WAL by one entry, returns true if it can advance, else false
   * in case of any other error throws an Exception.
   *
   * @return true if next data item is read successfully, false if data can not be read.
   * @throws IOException
   */
  public boolean advance() throws IOException;

  /**
   * Return current entry from WAL, returns null if end of file has reached.
   *
   * @return MutableKeyValue
   */
  public T get();
}
