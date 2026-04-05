package com.dbms.databasemanagementsystem.controller;

import com.dbms.databasemanagementsystem.model.asset;
import com.dbms.databasemanagementsystem.payload.AssetDto;
import com.dbms.databasemanagementsystem.service.assetservice;
import com.dbms.databasemanagementsystem.service.assetserviceimp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssetController {

    @Autowired
    public assetservice AssetService;

@Autowired
    public assetserviceimp AssetServiceimp;



    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @PostMapping("/asset/add")

    public ResponseEntity<AssetDto> addAsset(@RequestBody AssetDto Asset) {
        AssetDto savedassetdto = AssetService.addAsset(Asset);
        return new ResponseEntity<>(savedassetdto, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @PostMapping("/asset/add/file")
    public ResponseEntity<asset> addAssetfile(@RequestParam("file") MultipartFile file) {
        try {
            asset saved = AssetServiceimp.storeFile(file);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')")
    @GetMapping("/public/asset")

    public ResponseEntity<List<asset>> getAllAsset() {
        List<asset> assetList = AssetService.getAllAsset();
        return new ResponseEntity<>(assetList, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')")
    @GetMapping("/asset/{id}")

    public ResponseEntity<asset> getAllAssetById(@PathVariable Long id) {
        asset assetL = AssetService.getAllAssetById(id);
        return new ResponseEntity<>(assetL, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @PutMapping("/asset/update/{id}")

    public ResponseEntity<asset> updateAsset(@PathVariable Long id, @RequestBody asset Asset) {
        AssetService.updateAsset(id, Asset);
        return new ResponseEntity<>(Asset, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @DeleteMapping("/asset/{id}")

    public ResponseEntity<String> deleteAsset(@PathVariable Long id) {
        AssetService.deleteAsset(id);
        return new ResponseEntity<>("Asset Deleted Successfully", HttpStatus.OK);
    }
    @GetMapping("/search")
    public ResponseEntity<List<asset>> searchAssets(@RequestParam String query) {
        List<asset> result = AssetService.searchAssets(query);
        return ResponseEntity.ok(result);
    }


}






//    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
//                                                 @PathVariable Long categoryId){
//        ProductDTO savedProductDTO = productService.addProduct(categoryId, productDTO);
//        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
//    }



