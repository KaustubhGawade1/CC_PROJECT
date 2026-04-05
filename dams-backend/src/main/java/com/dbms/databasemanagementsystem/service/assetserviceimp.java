package com.dbms.databasemanagementsystem.service;

import com.dbms.databasemanagementsystem.model.Users;
import com.dbms.databasemanagementsystem.model.asset;
import com.dbms.databasemanagementsystem.payload.AssetDto;
import com.dbms.databasemanagementsystem.repository.AssetRepository;
import com.dbms.databasemanagementsystem.repository.UserRepository;
import com.dbms.databasemanagementsystem.security.services.UserDetailsServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class assetserviceimp implements assetservice {
    @Autowired
    UserRepository userRepository;
    @Value("${upload.path}")
    private String uploadDir;

    @Autowired
    public AssetRepository assetrepo;
@Autowired
    private UserDetailsServiceImpl userDetailsServicee;

    @Autowired
private ModelMapper modelMapper;


    public asset storeFile(MultipartFile file) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Users userr = userRepository.findByusername(username);

        // Create the directory if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        // Save file to disk
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filepath = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), filepath);

        // Save metadata to DB
        asset aasset = new asset();
        aasset.setFilename(file.getOriginalFilename());
        aasset.setContentType(file.getContentType());
        aasset.setSize(file.getSize());
        aasset.setFilePath(filepath.toString());
        aasset.setUploadedAt(LocalDateTime.now());
        aasset.setUploadedBy(userr);
        return assetrepo.save(aasset);
    }



    @Override
    public List<asset> getAllAsset() {
        List<asset> assetList = assetrepo.findAll();
        return assetList;
    }

    @Override
    public AssetDto addAsset(AssetDto Asset) {
        asset assetL = modelMapper.map(Asset,asset.class);

        asset savedasset = assetrepo.save(assetL);
        return modelMapper.map(savedasset,AssetDto.class);
    }



    @Override
    public asset getAllAssetById(Long id) {
        Optional<asset> assetL = assetrepo.findById(id);
        return assetL.orElse(null);
    }

    @Override
    public asset updateAsset(Long id, asset Asset) {
       List <asset> assset = assetrepo.findAll();
       Optional<asset> optionalAsset = assset.stream().filter(c -> c.getAsset_id().equals(id)).findFirst();
        if(optionalAsset.isPresent()){
            asset existingasset = optionalAsset.get();
            existingasset.setFilename(Asset.getFilename());
            asset savedAsset = assetrepo.save(existingasset);
            return savedAsset;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
    }

    @Override
    public String deleteAsset(Long id) {
        List <asset> assset = assetrepo.findAll();
        Optional<asset> optionalAsset = assset.stream().filter(c -> c.getAsset_id().equals(id)).findFirst();
        if(optionalAsset.isPresent()){
            asset existingasset = optionalAsset.get();
            assetrepo.delete(existingasset);
            return "Asset Deleted Successfully";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found");
        }

    }
    @Override
    public List<asset> searchAssets(String query) {
        return assetrepo.searchAllFields(query);
    }
}