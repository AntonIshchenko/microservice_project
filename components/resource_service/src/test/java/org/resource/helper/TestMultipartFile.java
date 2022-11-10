package org.resource.helper;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class TestMultipartFile implements MultipartFile {
   @Override public String getName() {
      return "MultipartFileName";
   }

   @Override public String getOriginalFilename() {
      return "OriginalFileName";
   }

   @Override public String getContentType() {
      return "audio/mpeg";
   }

   @Override public boolean isEmpty() {
      return false;
   }

   @Override public long getSize() {
      return 10;
   }

   @Override public byte[] getBytes() throws IOException {
      return new byte[10];
   }

   @Override public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(new byte[10]);
   }

   @Override public Resource getResource() {
      return MultipartFile.super.getResource();
   }

   @Override public void transferTo(File dest) throws IOException, IllegalStateException {

   }

   @Override public void transferTo(Path dest) throws IOException, IllegalStateException {
      MultipartFile.super.transferTo(dest);
   }
}
