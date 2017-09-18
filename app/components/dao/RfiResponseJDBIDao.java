package components.dao;

import models.RfiResponse;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import java.util.List;

@UseStringTemplate3StatementLocator
public interface RfiResponseJDBIDao {

  @Mapper(RfiResponseRSMapper.class)
  @SqlQuery("SELECT * FROM RFI_RESPONSE WHERE RFI_ID in (<rfiIds>)")
  List<RfiResponse> getRfiResponses(@BindIn("rfiIds") List<String> rfiIds);

  @Mapper(RfiResponseRSMapper.class)
  @SqlQuery("SELECT * FROM RFI_RESPONSE WHERE RFI_ID = :rfiId")
  RfiResponse getRfiResponse(@Bind("rfiId") String rfiId);

  @SqlUpdate("INSERT INTO RFI_RESPONSE (RFI_ID, SENT_BY, SENT_TIMESTAMP, MESSAGE, ATTACHMENTS) VALUES (:rfiId, :sentBy, :sentTimestamp, :message, :attachments)")
  void insertRfiResponse(@Bind("rfiId") String rfiId,
                         @Bind("sentBy") String sentBy,
                         @Bind("sentTimestamp") Long sentTimestamp,
                         @Bind("message") String message,
                         @Bind("attachments") String attachments);

  @SqlUpdate("DELETE FROM RFI_RESPONSE")
  void truncateTable();

  @SqlUpdate("DELETE FROM RFI_RESPONSE WHERE RFI_ID in (<rfiIds>)")
  void deleteRfiResponsesByRfiIds(@BindIn("rfiIds") List<String> rfiIds);

}
