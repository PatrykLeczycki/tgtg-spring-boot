package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.response.ResponsePhoto;
import com.pleczycki.tgtg.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class PhotoController {

    @Autowired
    private PhotoService storageService;

    @GetMapping
    public ResponseEntity<List<ResponsePhoto>> getListFiles() {
        List<ResponsePhoto> files = storageService.getAllFiles().map(photo -> {
            String photoDownloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/files/")
                    .path(photo.getId())
                    .toUriString();

            return new ResponsePhoto(
                    photo.getName(),
                    photoDownloadUri,
                    photo.getType(),
                    photo.getData().length);
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(files);
    }
}
