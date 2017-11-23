package components.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import components.exceptions.ServiceException;
import filters.common.JwtRequestFilter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

public class OgelRegistrationServiceClientImpl implements OgelRegistrationServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int timeout;
  private final JwtRequestFilter jwtRequestFilter;
  private final String address;

  @Inject
  public OgelRegistrationServiceClientImpl(HttpExecutionContext httpExecutionContext,
                                           WSClient wsClient,
                                           @Named("permissionsServiceAddress") String address,
                                           @Named("permissionsServiceTimeout") int timeout,
                                           JwtRequestFilter jwtRequestFilter) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.address = address;
    this.timeout = timeout;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  @Override
  public OgelRegistrationView getOgelRegistration(String userId, String registrationReference) {
    String url = String.format("%s/ogel-registrations/user/%s?registrationReference=%s", address, userId, registrationReference);
    WSRequest req = wsClient.url(url)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(ServiceClientLogger.requestFilter("Ogel Registrations", "GET", httpExecutionContext))
        .withRequestFilter(jwtRequestFilter)
        .setRequestTimeout(timeout);
    CompletionStage<OgelRegistrationView[]> request = req.get().handle((response, error) -> {
      if (error != null) {
        String message = String.format("Unable to get ogel registrations with user id %s", userId);
        throw new ServiceException(message, error);
      } else if (response.getStatus() != 200) {
        String message = String.format("Unexpected HTTP status code %d. "
                + "Unable to get ogel registration with user id %s and registration reference %s",
            response.getStatus(), userId, registrationReference);
        throw new ServiceException(message);
      } else {
        return Json.fromJson(response.asJson(), OgelRegistrationView[].class);
      }
    });
    try {
      OgelRegistrationView[] ogelRegistrationViews = request.toCompletableFuture().get();
      return ogelRegistrationViews[0];
    } catch (InterruptedException | ExecutionException error) {
      String message = String.format("Unable to get ogel registration with user id %s and registration reference %s",
          userId, registrationReference);
      throw new ServiceException(message, error);
    }
  }

  @Override
  public List<OgelRegistrationView> getOgelRegistrations(String userId) {
    String url = String.format("%s/ogel-registrations/user/%s", address, userId);
    WSRequest req = wsClient.url(url)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(ServiceClientLogger.requestFilter("Ogel Registrations", "GET", httpExecutionContext))
        .withRequestFilter(jwtRequestFilter)
        .setRequestTimeout(timeout);
    CompletionStage<OgelRegistrationView[]> request = req.get().handle((response, error) -> {
      if (error != null) {
        String message = String.format("Unable to get ogel registrations with user id %s", userId);
        throw new ServiceException(message, error);
      } else if (response.getStatus() != 200) {
        String message = String.format("Unexpected HTTP status code %d. Unable to get ogel registrations with user id %s", response.getStatus(), userId);
        throw new ServiceException(message);
      } else {
        return Json.fromJson(response.asJson(), OgelRegistrationView[].class);
      }
    });
    try {
      OgelRegistrationView[] ogelRegistrationViews = request.toCompletableFuture().get();
      return Arrays.asList(ogelRegistrationViews);
    } catch (InterruptedException | ExecutionException error) {
      String message = String.format("Unable to get ogel registrations with user id %s", userId);
      throw new ServiceException(message, error);
    }
  }

}
