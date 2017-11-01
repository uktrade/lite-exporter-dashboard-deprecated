package components.service;

import com.google.inject.Inject;
import components.client.CustomerServiceClient;
import components.client.OgelRegistrationServiceClient;
import components.client.OgelServiceClient;
import components.util.EnumUtil;
import components.util.LicenceUtil;
import components.util.TimeUtil;
import models.enums.OgelStatus;
import models.view.OgelItemView;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OgelItemViewServiceImpl implements OgelItemViewService {

  private final OgelRegistrationServiceClient ogelRegistrationServiceClient;
  private final CustomerServiceClient customerServiceClient;
  private final OgelServiceClient ogelServiceClient;

  @Inject
  public OgelItemViewServiceImpl(OgelRegistrationServiceClient ogelRegistrationServiceClient,
                                 CustomerServiceClient customerServiceClient,
                                 OgelServiceClient ogelServiceClient) {
    this.ogelRegistrationServiceClient = ogelRegistrationServiceClient;
    this.customerServiceClient = customerServiceClient;
    this.ogelServiceClient = ogelServiceClient;
  }

  @Override
  public boolean hasOgelItemViews(String userId) {
    return !ogelRegistrationServiceClient.getOgelRegistrations(userId).isEmpty();
  }

  @Override
  public List<OgelItemView> getOgelItemViews(String userId) {
    List<OgelRegistrationView> ogelRegistrationViews = ogelRegistrationServiceClient.getOgelRegistrations(userId);
    Map<String, SiteView> sites = getSites(ogelRegistrationViews);
    Map<String, CustomerView> customers = getCustomers(ogelRegistrationViews);
    Map<String, OgelFullView> ogels = getOgels(ogelRegistrationViews);

    return ogelRegistrationViews.stream()
        .map(view -> {
          CustomerView customerView = customers.get(view.getCustomerId());
          SiteView siteView = sites.get(view.getSiteId());
          OgelFullView ogelFullView = ogels.get(view.getOgelType());
          return getOgelItemView(view, customerView, siteView, ogelFullView);
        })
        .collect(Collectors.toList());
  }

  private Map<String, OgelFullView> getOgels(List<OgelRegistrationView> ogelRegistrationViews) {
    return ogelRegistrationViews.stream()
        .map(OgelRegistrationView::getOgelType)
        .distinct()
        .map(ogelServiceClient::getOgel)
        .collect(Collectors.toMap(OgelFullView::getId, Function.identity()));
  }

  private Map<String, SiteView> getSites(List<OgelRegistrationView> ogelRegistrationViews) {
    return ogelRegistrationViews.stream()
        .map(OgelRegistrationView::getSiteId)
        .distinct()
        .map(customerServiceClient::getSite)
        .collect(Collectors.toMap(SiteView::getSiteId, Function.identity()));
  }

  private Map<String, CustomerView> getCustomers(List<OgelRegistrationView> ogelRegistrationViews) {
    return ogelRegistrationViews.stream()
        .map(OgelRegistrationView::getCustomerId)
        .distinct()
        .map(customerServiceClient::getCustomer)
        .collect(Collectors.toMap(CustomerView::getCustomerId, Function.identity()));
  }

  private OgelItemView getOgelItemView(OgelRegistrationView ogelRegistrationView, CustomerView customerView, SiteView siteView, OgelFullView ogelFullView) {
    long registrationTimestamp = TimeUtil.parseOgelRegistrationDate(ogelRegistrationView.getRegistrationDate());
    String registrationDate = TimeUtil.formatDate(registrationTimestamp);
    OgelStatus ogelStatus = EnumUtil.parse(ogelRegistrationView.getStatus().toString(), OgelStatus.class, OgelStatus.UNKNOWN);
    String ogelStatusName = LicenceUtil.getOgelStatusName(ogelStatus);
    return new OgelItemView(ogelRegistrationView.getRegistrationReference(),
        ogelFullView.getName(),
        customerView.getCompanyName(),
        siteView.getSiteName(),
        registrationDate,
        registrationTimestamp,
        ogelStatusName);
  }

}
