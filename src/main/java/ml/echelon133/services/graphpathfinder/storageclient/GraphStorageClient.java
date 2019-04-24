package ml.echelon133.services.graphpathfinder.storageclient;

import ml.echelon133.graph.Graph;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "${feign.graph-storage-service.name}", url = "${feign.graph-storage-service.url}")
public interface GraphStorageClient {

    @GetMapping("/api/graphs/{id}")
    Graph<BigDecimal> getGraph(@PathVariable String id);
}
