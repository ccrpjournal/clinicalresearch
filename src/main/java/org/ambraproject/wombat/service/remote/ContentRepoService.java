package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.apache.http.Header;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface ContentRepoService {

  public static interface ContentRepoResponse extends Closeable {

    public abstract Header[] getAllHeaders();

    public abstract InputStream getStream() throws IOException;

  }

  public abstract ContentRepoResponse request(String bucket, String key, Optional<Integer> version)
      throws IOException;

}
