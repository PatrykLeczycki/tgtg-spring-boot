package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.model.Photo;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PhotoService {

  @Autowired
  private PhotoRepository photoRepository;

  public void store(List<MultipartFile> files, Review savedReview) {

    for (MultipartFile file : files) {
      try {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Photo photo = new Photo(fileName, file.getContentType(), file.getBytes(), savedReview);
        photoRepository.save(photo);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Stream<Photo> getAllFiles() {
    return photoRepository.findAll().stream();
  }

  public void deleteById(String id) {
    photoRepository.deleteById(id);
  }
}
