package com.example.code;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.FileCopyUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String... args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //check arg count
        if (args.length < 1) {
            throw new IllegalArgumentException("Need to know where the file is");
        } //end if
        //use the args to load the file
        File inputFile = new File(args[0]);
        //run the file through the parser
        DeploymentParser deploymentParser = new DeploymentParser();
        //remove the parts not needed
        Map<String,Object> output = deploymentParser.parse(inputFile);
        File outputFile = new File("output.yaml");
        deploymentParser.write(output, outputFile);
        //close
        System.out.println("done");
    }

    public class DeploymentParser {

        Yaml yaml;
    
        public DeploymentParser() {
            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            // Fix below - additional configuration
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            yaml = new Yaml(options);
        }

        public Map<String,Object> parse(File file) throws Exception {
            Map<String,Object> map = null;
            //load up the file
            FileInputStream fileInputStream = new FileInputStream(file);
            map = yaml.loadAs(fileInputStream,Map.class);
            //process
            map = removeKeys(map);
            //return
            return map;
        }
        
        public Map<String,Object> parse(String location) throws Exception {
            //load up the file
            InputStream inputStream = this.getClass().getResourceAsStream(location);
            File tempFile = File.createTempFile("parse", "yaml");
            tempFile.deleteOnExit();
            FileCopyUtils.copy(inputStream, new FileOutputStream(tempFile));
            //return
            return parse(tempFile);
        }
    
        public void write(Map<String,Object> map,File targetFile) throws Exception {
            FileWriter writer = new FileWriter(targetFile);
            yaml.dump(map,writer);
            // //write that out to a file
            // FileOutputStream fileOutputStream = new FileOutputStream(this.getClass().getResource(outputLocation));
            // FileCopyUtils.copy(stringWriter., out)
        }
    
        Map<String,Object> removeKeys(Map<String,Object> input) throws Exception {
            //stream and move
            Map<String,Object> output = new HashMap<>();
            input.entrySet().stream().forEach(entry -> {
                if (!entry.getKey().equals("status") 
                    && !entry.getKey().equals("metadata") 
                    && !entry.getKey().equals("spec")) {
                    //add automatically
                    output.put(entry.getKey(),entry.getValue());
                }//end if
                //handle metadata
                if (entry.getKey().equals("metadata")) {
                    //process
                    Map<String,Object> metadata = new HashMap<>();
                    ((Map<String,Object>) entry.getValue()).entrySet().stream().forEach(e -> {
                        if (!e.getKey().equals("uid") 
                            && !e.getKey().equals("generation")
                            && !e.getKey().equals("resourceVersion")
                            && !e.getKey().equals("selfLink")
                            && !e.getKey().equals("creationTimestamp")
                            && !e.getKey().equals("annotations")) {
                                //add by default
                                metadata.put(e.getKey(),e.getValue());
                        } //end if
                        //handle annotations 
                        if (e.getKey().equals("annotations")) {
                            Map<String,Object> annotations = new HashMap<>();
                            ((Map<String,Object>) e.getValue()).entrySet().stream().forEach(annotation -> {
                                if (!annotation.getKey().equals("deployment.kubernetes.io/revision") 
                                    && !annotation.getKey().equals("deployment.kubernetes.io/revision")) {
                                    //add by default
                                    annotations.put(annotation.getKey(),annotation.getValue());
                                } //end if
                            });
                            metadata.put("annotations",annotations);
                        } //end if                   
                    });
                    output.put("metadata",metadata); //add it back
                } //end if
                //handle spec
                if (entry.getKey().equals("spec")) {
                    //process
                    Map<String,Object> spec = new HashMap<>();
                    ((Map<String,Object>) entry.getValue()).entrySet().stream().forEach(s -> {
                        if (!s.getKey().equals("template")) {
                            //add by default
                            spec.put(s.getKey(),s.getValue());
                        } else {
                            //handle template
                            Map<String,Object> templates = new HashMap<>();
                            ((Map<String,Object>) s.getValue()).entrySet().stream().forEach(template -> {
                                if (!template.getKey().equals("metadata")) {
                                    //add by default
                                    templates.put(template.getKey(),template.getValue());
                                } else {
                                    //handle metadata
                                    Map<String,Object> ms = new HashMap<>();
                                    ((Map<String,Object>) template.getValue()).entrySet().stream().forEach(m -> {
                                        if (!m.getKey().equals("creationTimestamp") 
                                            && !m.getKey().equals("annotations")) {
                                            //add by default
                                            ms.put(m.getKey(),m.getValue());
                                        } else if (m.getKey().equals("annotations")) {
                                            //handle annotations
                                            Map<String,Object> metadataAnnotations = new HashMap<>();
                                            ((Map<String,Object>) m.getValue()).entrySet().stream().forEach(an -> {
                                                if (!an.getKey().equals("kubectl.kubernetes.io/restartedAt")) {
                                                    //default add
                                                    metadataAnnotations.put(an.getKey(),an.getValue());
                                                } //end if
                                            });
                                            ms.put("annotations",metadataAnnotations);
                                        } //end if
    
                                    });
                                    templates.put("metadata",ms);
                                } //end if
                            });                        
                            spec.put("template",templates);
                        } //end if
                    });
                    //add back
                    output.put("spec",spec);
                } //end if
            });
            //return
            return output;
        }
    }
}