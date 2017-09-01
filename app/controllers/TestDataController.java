package controllers;

import com.google.inject.Inject;
import components.service.TestDataService;
import play.mvc.Controller;
import play.mvc.Result;

public class TestDataController extends Controller {

  private final TestDataService testDataService;

  @Inject
  public TestDataController(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  public Result insertDefaultTestData() {
    return insertTestData(null);
  }

  public Result insertTestData(String testType) {
    session().clear();
    if ("one".equals(testType)) {
      testDataService.deleteAllDataAndInsertOneCompanyTestData();
    } else if ("del".equals(testType)) {
      testDataService.deleteAllData();
    } else if ("other".equals(testType)) {
      testDataService.deleteAllDataAndInsertOtherUserApplications();
    } else {
      testDataService.deleteAllDataAndInsertTwoCompaniesTestData();
    }
    return redirect(controllers.routes.ApplicationListController.index());
  }

}
