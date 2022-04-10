package com.qa.opencart.tests;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.opencart.base.BaseTest;
import com.qa.opencart.utils.Constants;
import com.qa.opencart.utils.ExcelUtil;

public class RegisterPageTest extends BaseTest{
	
	@BeforeClass
//	@BeforeClass -It's a class pre-condition, not a global pre-condition like @Test. 
	public void registerationPageSetup() {
		registerPage = loginPage.navigateToRegisterPage();
	}
	
	
	@DataProvider(parallel = true)
//	getRegisterData() method will read the data from Excel and return a 2 dim Object Array
	public Object[][] getRegisterData() {
		Object data[][] = ExcelUtil.getTestData(Constants.REGISTER_SHEET_NAME);
		return data;
	}
	
	@Test(dataProvider = "getRegisterData")
//	dataProvider - who is providing data to the test case, Its the getRegisterData method
	public void userRegsiterationTest(String firstName, String lastName, String emailID, String phone, 
			String password, String subscribe) {
		
		Assert.assertTrue(registerPage.accountRegistration(firstName, lastName, emailID, phone, 
							password, subscribe));
	}
	
	

}