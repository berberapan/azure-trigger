package org.example.functions;

import java.util.*;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;


public class HttpTriggerJava {

    private static final String connectionString = System.getenv("COSMOS_CONNECTION_STRING");
    private static final String partitionKey = "counter";
    private static final String rowKey = "1";

    @FunctionName("HttpTriggerJava")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            TableClient tableClient = new TableClientBuilder()
                    .connectionString(connectionString)
                    .tableName("visitors")
                    .buildClient();

            TableEntity entity = tableClient.getEntity(partitionKey, rowKey);
            Integer originalValue = (Integer) entity.getProperty("value");
            Integer newValue = originalValue + 1;
            entity.getProperties().put("value", newValue);
            tableClient.updateEntity(entity);

            String json = "{\"value\":" + newValue + "}";

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();
        }
        catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error when trying to fetch data")
                    .build();
        }
    }
}
