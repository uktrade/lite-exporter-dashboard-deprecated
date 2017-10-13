package controllers;

import static components.util.RandomIdUtil.fileId;
import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.dao.DraftDao;
import components.exceptions.DatabaseException;
import components.upload.UploadFile;
import components.upload.UploadMultipartParser;
import components.util.FileUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import models.File;
import models.FileUploadResponse;
import models.FileUploadResponseItem;
import models.enums.DraftType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;

public class UploadController extends SamlController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

  private final DraftDao draftDao;

  @Inject
  public UploadController(DraftDao draftDao) {
    this.draftDao = draftDao;
  }

  @BodyParser.Of(UploadMultipartParser.class)
  public CompletionStage<Result> submitFiles(String draftType, String relatedId) {
    List<UploadFile> files = FileUtil.getUploadFiles(request());
    FileUploadResponse fileUploadResponse = insertDraftAttachments(relatedId, files, DraftType.valueOf(draftType));
    return completedFuture(ok(Json.toJson(fileUploadResponse)));
  }

  public CompletionStage<Result> deleteFile() {
    Map<String, String[]> formFields = request().body().asFormUrlEncoded();
    String relatedId = formFields.get("relatedId")[0];
    String fileType = formFields.get("fileType")[0];
    String fileId = formFields.get("fileId")[0];
    try {
      draftDao.deleteFile(relatedId, fileId, DraftType.valueOf(fileType));
    } catch (DatabaseException databaseException) {
      LOGGER.error("Unable to delete file", databaseException);
      return completedFuture(badRequest());
    }
    return completedFuture(ok());
  }

  private FileUploadResponse insertDraftAttachments(String relatedId, List<UploadFile> uploadFiles, DraftType draftType) {
    List<FileUploadResponseItem> fileUploadResponseItems = uploadFiles.stream()
        .map(uploadFile -> createFileUploadResponseItem(relatedId, uploadFile, draftType))
        .collect(Collectors.toList());
    return new FileUploadResponse(fileUploadResponseItems);
  }

  private FileUploadResponseItem createFileUploadResponseItem(String relatedId, UploadFile uploadFile, DraftType draftType) {
    String name = uploadFile.getOriginalFilename();
    if (uploadFile.getProcessErrorInfo() != null) {
      return new FileUploadResponseItem(name, null, uploadFile.getProcessErrorInfo(), null, relatedId, null, draftType.toString());
    } else {
      String size = FileUtil.getReadableFileSize(uploadFile.getDestinationPath());
      String fileId = createNewFile(relatedId, uploadFile, draftType);
      String link = getLink(relatedId, fileId, draftType);
      return new FileUploadResponseItem(name, link, null, size, relatedId, fileId, draftType.toString());
    }
  }

  private String getLink(String relatedId, String fileId, DraftType draftType) {
    if (draftType == DraftType.RFI_REPLY) {
      return routes.DownloadController.getRfiReplyFile(relatedId, fileId).toString();
    } else {
      return routes.DownloadController.getFile(relatedId, fileId).toString();
    }
  }

  private String createNewFile(String relatedId, UploadFile uploadFile, DraftType draftType) {
    File file = new File();
    file.setId(fileId());
    file.setFilename(uploadFile.getOriginalFilename());
    file.setUrl(uploadFile.getDestinationPath());
    draftDao.addFile(relatedId, file, draftType);
    return file.getId();
  }

}
