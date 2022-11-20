package software.amazon.adot.sampleapp.extension;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.List;

@AutoService(AutoConfigurationCustomizerProvider.class)
public class Configurer implements AutoConfigurationCustomizerProvider {
    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration.addResourceCustomizer((resource, configProperties) -> {
            return resource.merge(
                    Resource.create(
                            Attributes.of(
                                    ResourceAttributes.AWS_LOG_GROUP_NAMES,
                                    List.of(System.getProperty("adot.sampleapp.logroup", "sample-app-trace-logs"))
                            )
                    )
            );
        });
    }
}
