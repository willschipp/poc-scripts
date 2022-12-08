package com.example.code;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class DeploymentParserTest {

    @Test
    public void test_trim() throws Exception {
        //input a file location
        //run it through the parser
        //make sure the elements aren't there
        Application.DeploymentParser deploymentParser = new Application().new DeploymentParser();
        String location = "/test.yaml";

        //get pack the map
        Map<String,Object> map = deploymentParser.parse(location);
        //check
        assertFalse(map.containsKey("status"));//the first key to remove
        assertFalse(((Map<String,Object>) map.get("metadata")).containsKey("generation"));
        assertTrue(((Map<String,Object>) map.get("metadata")).containsKey("donottouchkey"));
    }
    

    @Test
    public void test_trimToYaml() throws Exception {
        //input a file location
        //run it through the parser
        //make sure the elements aren't there
        Application.DeploymentParser deploymentParser = new Application().new DeploymentParser();
        String location = "/test.yaml";
        String outputLocation = "/output.yaml";

        //get pack the map
        Map<String,Object> map = deploymentParser.parse(location);
        //check
        assertFalse(map.containsKey("status"));//the first key to remove
        assertFalse(((Map<String,Object>) map.get("metadata")).containsKey("generation"));
        assertTrue(((Map<String,Object>) map.get("metadata")).containsKey("donottouchkey"));
        //now dump to yaml
        File tempFile = File.createTempFile("test", "yaml");
        deploymentParser.write(map,tempFile);
        //now stream the results
        InputStream input = new BufferedInputStream(new FileInputStream(tempFile));
        byte[] buffer = new byte[8192];

        try {
            for (int length = 0; (length = input.read(buffer)) != -1;) {
                System.out.write(buffer, 0, length);
            }
        } finally {
            input.close();
        }
    }
}
