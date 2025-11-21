package ai.lab.inlivefilemanager.client.api;

import ai.lab.inlivefilemanager.client.configuration.ClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "${spring.application.file-api.name}", url = "${spring.application.file-api.url}", configuration = ClientConfiguration.class)
public interface FileManagerApi {
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{directory}/upload/files",
            produces = "*/*",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<List<String>> uploadFiles(@PathVariable("directory") String directory, @RequestPart("files") List<MultipartFile> files, @RequestParam("generate-file-name") boolean generateFileName);

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{directory}/remove/files/{filename}",
            produces = "*/*"
    )
    ResponseEntity<String> deleteFile(@PathVariable("directory") String directory, @PathVariable("filename") String filename);
}
