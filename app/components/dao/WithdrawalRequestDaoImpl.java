package components.dao;

import com.google.inject.Inject;
import components.util.JsonUtil;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.bis.lite.exporterdashboard.api.WithdrawalRequest;

import java.util.List;

public class WithdrawalRequestDaoImpl implements WithdrawalRequestDao {

  private final DBI dbi;

  @Inject
  public WithdrawalRequestDaoImpl(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public WithdrawalRequest getWithdrawalRequest(String appId) {
    try (final Handle handle = dbi.open()) {
      WithdrawalRequestJDBIDao withdrawalRequestJDBIDao = handle.attach(WithdrawalRequestJDBIDao.class);
      return withdrawalRequestJDBIDao.getWithdrawalRequest(appId);
    }
  }

  @Override
  public void insertWithdrawalRequest(WithdrawalRequest withdrawalRequest) {
    try (final Handle handle = dbi.open()) {
      WithdrawalRequestJDBIDao withdrawalRequestJDBIDao = handle.attach(WithdrawalRequestJDBIDao.class);
      withdrawalRequestJDBIDao.insert(withdrawalRequest.getId(),
          withdrawalRequest.getAppId(),
          withdrawalRequest.getCreatedByUserId(),
          withdrawalRequest.getCreatedTimestamp(),
          withdrawalRequest.getMessage(),
          JsonUtil.convertListToJson(withdrawalRequest.getAttachments()));
    }
  }

  @Override
  public void deleteAllWithdrawalRequests() {
    try (final Handle handle = dbi.open()) {
      WithdrawalRequestJDBIDao withdrawalRequestJDBIDao = handle.attach(WithdrawalRequestJDBIDao.class);
      withdrawalRequestJDBIDao.truncateTable();
    }
  }

  @Override
  public void deleteWithdrawalRequestsByAppIds(List<String> appIds) {
    if (!appIds.isEmpty()) {
      try (final Handle handle = dbi.open()) {
        WithdrawalRequestJDBIDao withdrawalRequestJDBIDao = handle.attach(WithdrawalRequestJDBIDao.class);
        withdrawalRequestJDBIDao.deleteWithdrawalRequestsByAppIds(appIds);
      }
    }
  }

}
