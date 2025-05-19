package com.app.fitrack.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // These values will be injected from your environment variables
    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        // config.put("secure", "true"); // Already default in newer versions, but can be explicit
        this.cloudinary = new Cloudinary(config);
    }

    public String uploadProfilePicture(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot upload an empty file.");
        }

        // Generate a unique public ID for the image in Cloudinary to avoid overwrites
        // and make it harder to guess filenames. You can customize this further.
        String publicId = "profile_pictures/" + UUID.randomUUID().toString();

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true, // If somehow a public_id collision happens, overwrite
                "resource_type", "image" // Explicitly state it's an image
        ));

        // "secure_url" gives you an HTTPS URL, which is preferred.
        return (String) uploadResult.get("secure_url");
    }

    public String uploadApplicationLogo() throws IOException {
        String publicId = "fitrack_app_logo"; // Specific public ID for the app logo
        String logoPath = "static/images/Fitrack.png"; // Path in classpath

        // Load the resource from the classpath
        ClassPathResource resource = new ClassPathResource(logoPath);
        if (!resource.exists()) {
            throw new IOException("Logo file not found in classpath: " + logoPath);
        }

        // Read the file into a byte array
        byte[] logoBytes;
        try (InputStream inputStream = resource.getInputStream()) {
            logoBytes = inputStream.readAllBytes();
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(logoBytes, ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true, // Overwrite if it already exists to ensure latest version
                "resource_type", "image"
        ));

        String secureUrl = (String) uploadResult.get("secure_url");
        System.out.println("Fitrack logo uploaded to: " + secureUrl + " with public ID: " + publicId);
        return secureUrl;
    }

    // Optional: Method to delete an image if a user changes their profile picture
    // You would need to store the 'public_id' of the image to delete it.
    // For simplicity, we'll skip implementing delete for now, but it's good to be aware of.
    /*
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
    */
} 