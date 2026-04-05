package com.dbms.databasemanagementsystem.service;

import com.dbms.databasemanagementsystem.model.asset;
import com.dbms.databasemanagementsystem.payload.AssetDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface assetservice {


    //static  asset storeFile(MultipartFile file) throws IOException ;

    List<asset> getAllAsset();

    AssetDto addAsset(AssetDto Asset);

  asset getAllAssetById(Long id);

    asset updateAsset(Long id, asset Asset);

    String deleteAsset(Long id);

     List<asset> searchAssets(String query);
}
