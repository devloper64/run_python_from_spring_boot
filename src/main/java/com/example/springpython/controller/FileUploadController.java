package com.example.springpython.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StreamUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FileUploadController {



    @GetMapping("/hello")
    public String hello() throws IOException {
        return "hello world";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String filePath = saveFile(file);
        String pythonScriptPath = "C:\\Users\\User\\Documents\\python_script\\python_script.py";
        String result = executePythonScript(pythonScriptPath, filePath);

        System.out.println(result);

        return result;
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

    private String executePythonScript(String pythonScriptPath, String filePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("python3", pythonScriptPath, filePath);
        Process process = processBuilder.start();



        String output = new String(process.getInputStream().readAllBytes());
        ObjectMapper objectMapper = new ObjectMapper();
        Object json = objectMapper.readValue(output, Object.class);

        // Convert the parsed JSON to a string
        String jsonResponse = objectMapper.writeValueAsString(json);


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

        return jsonResponse;
    }

}
