package munit.docs;

import java.util.*;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.api.gax.paging.Page;

public class StorageProxy {
  public static Page<Blob> list(Storage storage, String bucketName, String prefix) {
    return storage.list(bucketName, BlobListOption.pageSize(1000), BlobListOption.prefix(prefix));
  }
}
