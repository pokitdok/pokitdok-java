package com.pokitdok.tests;

import java.io.*;
import java.util.*;
import com.pokitdok.*;
import com.pokitdok.tests.*;
import com.pokitdok.tests.categories.*;
import org.apache.commons.io.IOUtils;
import org.json.simple.*;
import org.junit.*;
import org.junit.experimental.categories.*;
import org.junit.runner.*;
import org.junit.runners.Suite;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class PokitDokUnitTests {
	private PokitDok client;
	private PokitDokHTTPConnector mockConnector;

	@Before
	public void connect() throws Exception {
		mockConnector = mock(PokitDokHTTPConnector.class);
		when(mockConnector.get(anyString(), anyMap(), anyMap())).thenReturn(Constants.BLANK_JSON);
		when(mockConnector.post(anyString(), anyMap(), anyMap())).thenReturn(Constants.BLANK_JSON);
		when(mockConnector.put(anyString(), anyMap(), anyMap())).thenReturn(Constants.BLANK_JSON);

		client = new PokitDok("client_id", "client_secret", mockConnector);
	}

	/* The basics. */

	@Test
	@Category(UnitTests.class)
	public void shouldInstantiateTest() throws Exception {
		assertNotNull(client);
	}

	/* Utility API tests. */

	@Test
	@Category(UnitTests.class)
	public void activitiesTest() throws Exception {
		Map<String, Object> response = client.activities();

		verify(mockConnector).get("activities", null, null);
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void activitiesTestWithParams() throws Exception {
		Map<String, Object> params = new HashMap();
		params.put("param1", "value1");
		Map<String, Object> response = client.activities(params);

		verify(mockConnector).get(eq("activities"), eq(params), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void payersTest() throws Exception {
		Map<String, Object> response = client.payers();

		verify(mockConnector).get("payers", null, null);
	}

	@Test
	@Category(UnitTests.class)
	public void payersTestWithParams() throws Exception {
		Map<String, Object> response = client.payers();
		Map<String, Object> params = new HashMap();
		params.put("param1", "value1");
		response = client.payers(params);

		verify(mockConnector).get(eq("payers"), eq(params), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void tradingPartnerTest() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("trading_partner_id", "MOCKPAYER");
		Map<String, Object> response = client.tradingPartners(params);

		verify(mockConnector).get("tradingpartners/MOCKPAYER", null, null);
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void tradingPartnersTest() throws Exception {
		Map<String, Object> response = client.tradingPartners();

		verify(mockConnector).get("tradingpartners/", null, null);
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void planTest() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("state", "TX");
		params.put("plan_type", "PPO");
		Map<String, Object> response = client.plans(params);

		verify(mockConnector).get(eq("plans"), eq(params), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void plansTest() throws Exception {
		Map<String, Object> response = client.plans();

		verify(mockConnector).get("plans", null, null);
		assertNotNull(response);
	}

	/* X12/clearinghouse tests. */

	@Test
	@Category(UnitTests.class)
	public void authorizationsTest() throws Exception {
		String authorizations = readEntireFile(Constants.AUTHORIZATIONS_JSON);
		Map query = (JSONObject) JSONValue.parse(authorizations);
		Map<String, Object> response = client.authorizations(query);

		verify(mockConnector).post(eq("authorizations/"), eq(query), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void eligibilityTest() throws Exception {
		String queryJSON = readEntireFile(Constants.ELIGIBILITY_JSON);
		Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
		Map<String, Object> response = client.eligibility(query);

		verify(mockConnector).post(eq("eligibility/"), eq(query), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void claimsTest() throws Exception {
		String queryJSON = readEntireFile(Constants.CLAIMS_JSON);
		Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
		Map<String, Object> response = client.claims(query);

		verify(mockConnector).post(eq("claims/"), eq(query), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void claimsStatusTest() throws Exception {
		String queryJSON = readEntireFile(Constants.CLAIMS_STATUS_JSON);
		Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
		Map<String, Object> response = client.claimsStatus(query);

		verify(mockConnector).post(eq("claims/status"), eq(query), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void referralsTest() throws Exception {
		String queryJSON = readEntireFile(Constants.REFERRALS_JSON);
		Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
		Map<String, Object> response = client.referrals(query);

		verify(mockConnector).post(eq("referrals/"), eq(query), anyMap());
		assertNotNull(response);
	}

	/* Data tests. */

	@Test
	@Category(UnitTests.class)
	public void cashPricesTest() throws Exception {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("cpt_code", "87799");
		query.put("zip_code", "75201");
		Map<String, Object> response = client.cashPrices(query);

		verify(mockConnector).get(eq("prices/cash"), eq(query), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void insurancePricesTest() throws Exception {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("cpt_code", "87799");
		query.put("zip_code", "29403");
		Map<String, Object> response = client.insurancePrices(query);

		verify(mockConnector).get(eq("prices/insurance"), eq(query), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void mpcTest() throws Exception {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("name", "surgery");
		Map<String, Object> response = client.mpc(query);

		verify(mockConnector).get(eq("mpc/"), eq(query), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void mpcsTest() throws Exception {
		String mpc = "99213";
		Map<String, Object> response = client.mpc(mpc, new HashMap<String, Object>());

		verify(mockConnector).get(eq("mpc/" + mpc), anyMap(), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void providersTest() throws Exception {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("npi", "1467560003");
		Map response = client.providers(query);

		verify(mockConnector).get(eq("providers"), eq(query), anyMap());
		assertNotNull(response);
	}


	/* Identity tests. */

	// test post
	@Test
	@Category(UnitTests.class)
	public void createIdentityTest() throws Exception {
		String postJSON = readEntireFile(Constants.CREATE_IDENTITY_JSON);
		Map<String, Object> post = (JSONObject) JSONValue.parse(postJSON);
		Map<String, Object> response = client.createIdentity(post);

		verify(mockConnector).post(eq("identity/"), eq(post), anyMap());
		assertNotNull(response);
	}

	//test update
	@Test
	@Category(UnitTests.class)
	public void updateIdentityTest() throws Exception {
		String updateJSON = readEntireFile(Constants.UPDATE_IDENTITY_JSON);
		Map<String, Object> update = (JSONObject) JSONValue.parse(updateJSON);
		Map<String, Object> response = client.updateIdentity("881bc095-2068-43cb-9783-cce630364122", update);

		verify(mockConnector).put(eq("identity/881bc095-2068-43cb-9783-cce630364122"), eq(update), anyMap());
		assertNotNull(response);
	}

	//test get uuid
	@Test
	@Category(UnitTests.class)
	public void getIdentityUuidTest() throws Exception {
		Map<String, Object> response = client.identity("881bc095-2068-43cb-9783-cce630364122", new HashMap<String, Object>());

		verify(mockConnector).get(eq("identity/881bc095-2068-43cb-9783-cce630364122"), eq(new HashMap<String, Object>()), anyMap());
		assertNotNull(response);
	}

	//test get params
	@Test
	@Category(UnitTests.class)
	public void getIdentityParamsTest() throws Exception {
		String getJSON = readEntireFile(Constants.GET_IDENTITY_JSON);
		Map<String, Object> get = (JSONObject) JSONValue.parse(getJSON);

		Map<String, Object> response = client.identity("", get);

		verify(mockConnector).get(eq("identity"), eq(get), anyMap());
		assertNotNull(response);
	}

	/*******************
	 * Scheduling tests.
	 *******************/
	public void listSchedulersTest() throws Exception {
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> response = client.schedulers(query);
	}

	public void showSchedulerTest() throws Exception {
		String id = "";

		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> response = client.scheduler(id, query);
	}

	public void listAppointmentTypesTest() throws Exception {
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> response = client.appointmentTypes(query);
	}

	public void showAppointmentTypeTest() throws Exception {
		String id = "";

		Map<String, Object> response =
			client.appointmentType(id, new HashMap<String, Object>());
	}

	public void bookAppointmentTest() throws Exception {
		String id = "";

		String bookAppointmentJSON = readEntireFile(Constants.BOOK_APPOINTMENT_JSON);

		Map<String, Object> query = (JSONObject) JSONValue.parse(bookAppointmentJSON);
		Map<String, Object> response = client.bookAppointment(id, query);
	}

	public void updateAppointmentTest() throws Exception {
		String queryJSON = readEntireFile(Constants.UPDATE_APPOINTMENT_JSON);
		String id = "";

		Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
		Map<String, Object> response = client.updateAppointment(id, query);
	}

	public void cancelAppointmentTest() throws Exception {
		String id = "";

		Map<String, Object> response =
			client.cancelAppointment(id, new HashMap<String, Object>());
	}

	public void failWithoutScopeCodeTest() throws Exception {
	}

    /** Test suite to assemble all unit tests */
    @RunWith(Categories.class)
    @Suite.SuiteClasses(PokitDokUnitTests.class)
    @Categories.IncludeCategory(UnitTests.class)
    public class PokitDokUnitTestSuite {}

	/***********************************************************************
	 * Beyond lie utility methods for testing purposes. Nothing to see here.
	 ***********************************************************************/

	private String readEntireFile(String filename) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		try {
			return IOUtils.toString(inputStream);
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
		finally {
			if (inputStream != null) inputStream.close();
		}
	}

	@Test
	@Category(UnitTests.class)
	public void pharmacyPlansTest() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
                params.put("trading_partner_id", "medicare_national");
                params.put("plan_number", "S5601034");
		Map<String, Object> response = client.pharmacyPlans(params);

		verify(mockConnector).get(eq("pharmacy/plans"), eq(params), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void pharmacyFormularyTest() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
                params.put("trading_partner_id", "medicare_national");
                params.put("plan_number", "S5820003");
                params.put("ndc", "59310057922");
		Map<String, Object> response = client.pharmacyFormulary(params);

		verify(mockConnector).get(eq("pharmacy/formulary"), eq(params), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void pharmacyNetworkNoNPI() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
                params.put("trading_partner_id", "medicare_national");
                params.put("plan_number", "S5820003");
                params.put("zipcode", "94401");
		Map<String, Object> response = client.pharmacyNetwork(params);

		verify(mockConnector).get(eq("pharmacy/network"), eq(params), anyMap());
		assertNotNull(response);
	}

	@Test
	@Category(UnitTests.class)
	public void pharmacyNetworkNPI() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
                params.put("trading_partner_id", "medicare_national");
                params.put("plan_number", "S5820003");
		Map<String, Object> response = client.pharmacyNetwork("1275827032", params);

		verify(mockConnector).get(eq("pharmacy/network/1275827032"), eq(params), anyMap());
		assertNotNull(response);
	}

}
