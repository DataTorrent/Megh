/*
 * Copyright (c) 2014 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.bucket;

import org.junit.*;
import org.junit.runner.Description;

import com.google.common.collect.Sets;

public class ExpirableHdfsBucketStoreTest
{
  public static class TestMeta extends HdfsBucketStoreTest.TestMeta
  {

    @Override
    protected void starting(Description description)
    {
      super.starting(description);
    }

    @Override
    protected HdfsBucketStore<DummyEvent> getBucketStore()
    {
      ExpirableHdfsBucketStore<DummyEvent> lBucketStore = new ExpirableHdfsBucketStore<DummyEvent>();
      lBucketStore.setNoOfBuckets(TOTAL_BUCKETS);
      lBucketStore.setWriteEventKeysOnly(true);
      lBucketStore.setConfiguration(7, applicationPath, Sets.newHashSet(0), 0);
      return lBucketStore;
    }

    protected HdfsBucketStore<DummyEvent> getBucketStoreAsync()
    {
      ExpirableHdfsBucketStoreAsync<DummyEvent> lBucketStore = new ExpirableHdfsBucketStoreAsync<DummyEvent>();
      lBucketStore.setNoOfBuckets(TOTAL_BUCKETS);
      lBucketStore.setWriteEventKeysOnly(true);
      lBucketStore.setConfiguration(7, applicationPath, Sets.newHashSet(0), 0);
      return lBucketStore;
    }
  }

  @Rule
  public TestMeta testMeta = new TestMeta();

  @Test
  public void testExpirableStore() throws Exception
  {
    testMeta.util.storeBucket(0);
    ((BucketStore.ExpirableBucketStore) testMeta.bucketStore).deleteExpiredBuckets(1);
    Assert.assertTrue(!testMeta.util.bucketExists(0));
  }

  @Test
  public void testExpirableStoreAsync() throws Exception
  {
    testMeta.util.storeBucket(0);
    ((BucketStore.ExpirableBucketStore)testMeta.bucketStore).deleteExpiredBuckets(1);
    Assert.assertTrue(!testMeta.util.bucketExists(0));
  }
}
