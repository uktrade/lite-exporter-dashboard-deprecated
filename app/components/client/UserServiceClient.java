package components.client;

import uk.gov.bis.lite.user.api.view.UserPrivilegesView;

public interface UserServiceClient {

  UserPrivilegesView getUserPrivilegeView(String userId);

}
