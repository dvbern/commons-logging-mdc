# lib-commons-logging-mdc
Helper methods to facilitate filling the Logging [Mapped Diagnostics Context MDC](https://www.slf4j.org/api/org/slf4j/MDC.html) with most used values

Available versions: see [Releases](https://www.github.com/dvbern/commons-logging-mdc/releases)

## lib-commons-logging-mdc-api
Basic API to fill ***and cleanup** the MDC.

Workhorse is the class `MDCDAO` which is used to fill the MDC with the most used values.

### Usage
Maven-Dependency:

```xml
<dependency>
    <groupId>ch.dvbern.oss.commons-logging-mdc</groupId>
    <artifactId>commons-logging-mdc-api</artifactId>
    <version>see github releases for available versions</version>
</dependency>
```

```java
import ch.dvbern.oss.commons.logging.mdc.MDCDAO;
import ch.dvbern.oss.commons.logging.mdc.MDCDAOSlf4j;
import ch.dvbern.oss.commons.logging.mdc.MDCValues;

public class MyClass {
	private final MDCDAO mdcDAO = new MDCDAODefaultImpl();

    public void doSomethingWithMDC() {
        // fill MDC
        MDCValues values = MDCValues.empty()
            .with("foo", "The Foo")
            .with("bar", "The Bar");
        mdcDAO.applyToMDC(values);
		
        // call business logic
        doSomething();
        
        // clear MDC
        mdcDAO.clearMDC();
    }
}
```

## lib-commons-logging-mdc-jaxrs-filter
JAX-RS filter to fill the MDC with common values used by all DV Bern applications.

### Usage
Maven-Dependency:

```xml
<dependency>
    <groupId>ch.dvbern.oss.commons-logging-mdc</groupId>
    <artifactId>commons-logging-mdc-jaxrs-filter</artifactId>
    <version>see github releases for available versions</version>
</dependency>
```

Initialize the MDC in a ***request*** filter:
```java
@Provider
@PreMatching
// should be after any authentication filter, i.e. at least: jakarta.ws.rs.Priorities.AUTHENTICATION + 1
@Priority(jakarta.ws.rs.Priorities.AUTHENTICATION + 1)
@RequestScoped
@NoArgsConstructor
@AllArgsConstructor(
    access = AccessLevel.PACKAGE,
    onConstructor_ = @SuppressWarnings("rawtypes")
)
public class InitLoggingRequestFilter implements ContainerRequestFilter {

    private final CommonMDCFieldRequestFilterHelper helper = CommonMDCFieldRequestFilterHelper.usingDefaults();

	// roll your own deployment config
    @Inject
    DeploymentConfig deploymentConfig;
	// roll your own tenant provider
	@Inject
	TenantProvider tenantProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var loggingPrincipal = Optional.ofNullable(requestContext.getSecurityContext().getUserPrincipal())
            .map(p -> new LoggingPrincipal(p.getName()))
            .orElse(null);
		
		var loggingTenant = Optional.ofNullable(tenantProvider.getTenant())
		    .map(t -> new LoggingTenant(t.getIdentifier()))
		    .orElse(null);

        helper.filter(
            requestContext,
            new LoggingApp(
                deploymentConfig.appProject(),
                deploymentConfig.appModule(),
                deploymentConfig.version(),
                deploymentConfig.instance()
            ),
            new LoggingSource(requestContext.getUriInfo().getRequestUri().getPath(), requestContext.getMethod()),
            loggingPrincipal,
            loggingTenant
        );

    }

}
```

Do not forget to register the ***response*** filter to cleanup the MDC:

```java
@Provider
@PreMatching
@Priority(/* see Request Filter */)
@RequestScoped
@NoArgsConstructor
public class InitLoggingResponseFilter implements ContainerResponseFilter {

    private final CommonMDCFieldResponseFilterHelper helper = CommonMDCFieldResponseFilterHelper.usingDefaults();

    @Override
    public void filter(
        ContainerRequestContext requestContext,
        ContainerResponseContext responseContext
    ) {
        helper.filter(requestContext, responseContext);
    }

}
```