package com.example.springpython.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StreamUtils;

@RestController
@RequestMapping("/api")
public class FileUploadController {



    @GetMapping("/hello")
    public String hello() throws IOException {
        return "hello world";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("targetColumn") String targetColumn) throws IOException {
        String filePath = saveFile(file);
        String pythonScriptPath = "/mnt/2c446387-abe5-44fc-9478-648379af0f64/python-with-spring-boot/python_script_for_spring_boot/python_script.py";
        String result = executePythonScript(pythonScriptPath, filePath,targetColumn);
        System.out.println("-------->"+targetColumn);

        // Generate local host image links
        String imageBasePath = "http://localhost:" + 8080 + "/images/";
        result = convertImagePathsToUrls(result, imageBasePath);

        System.out.println(result);

        return ResponseEntity.ok(result);
    }

    private String convertImagePathsToUrls(String jsonResponse, String imageBasePath) {
        // Replace image paths with local host image URLs
        jsonResponse = jsonResponse.replace("/home/coder/Documents/temp_image/", imageBasePath);

        return jsonResponse;
    }

    private String saveFile(MultipartFile file) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + file.getOriginalFilename();
        File destination = new File(filePath);

        try (OutputStream outputStream = new FileOutputStream(destination)) {
            StreamUtils.copy(file.getInputStream(), outputStream);
        }

        return filePath;
    }

    private String executePythonScript(String pythonScriptPath, String filePath,String targetColumn) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("python3", pythonScriptPath, filePath,targetColumn);
        Process process = processBuilder.start();




        String output = new String(process.getInputStream().readAllBytes());
        // Wait for the process to complete
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Handle any errors if necessary
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Python script execution was interrupted", e);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Object json = objectMapper.readValue(output, Object.class);

        // Convert the parsed JSON to a string
        String jsonResponse = objectMapper.writeValueAsString(json);



        return jsonResponse;
    }
}
