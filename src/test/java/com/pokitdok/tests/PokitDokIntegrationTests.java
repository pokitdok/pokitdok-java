package com.pokitdok.tests;

import java.io.*;
import java.util.*;
import com.pokitdok.*;
import com.pokitdok.tests.categories.*;
import org.apache.commons.io.IOUtils;
import org.json.simple.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.experimental.categories.*;
import org.junit.runner.*;
import org.junit.runners.Suite;
import static org.mockito.Mockito.*;

public class PokitDokIntegrationTests {
    private static PokitDok pd;
    private static Map<String, String> env = System.getenv();

    @BeforeClass
    public static void setup() throws Exception {

        String clientId = env.get("PD_CLIENT_ID");
        String clientSecret = env.get("PD_CLIENT_SECRET");
        if ((clientId == null) || (clientSecret == null)) {
            fail("Please provide a PokitDok client ID and secret in the environment variables PD_CLIENT_ID and PD_CLIENT_SECRET.");
        }

        String apiBase = env.get("PD_API_BASE");
        pd = new PokitDok(clientId, clientSecret, null, apiBase);
    }

    @Test
    @Category(IntegrationTests.class)
    public void activitiesTest() throws Exception {
        Map<String, Object> response = pd.activities();

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void payersTest() throws Exception {
        Map<String, Object> response = pd.payers();

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void tradingPartnerTest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trading_partner_id", "MOCKPAYER");
        Map<String, Object> response = pd.tradingPartners(params);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void tradingPartnersTest() throws Exception {
        Map<String, Object> response = pd.tradingPartners();

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void planTest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("state", "TX");
        params.put("plan_type", "PPO");
        Map<String, Object> response = pd.plans(params);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void plansTest() throws Exception {
        Map<String, Object> response = pd.plans();

        assertNotNull(response);
    }

    /* X12/clearinghouse tests. */

    @Test
    @Category(IntegrationTests.class)
    public void authorizationsTest() throws Exception {
        String authorizations = readEntireFile(Constants.AUTHORIZATIONS_JSON);
        Map query = (JSONObject) JSONValue.parse(authorizations);
        Map<String, Object> response = pd.authorizations(query);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void eligibilityTest() throws Exception {
        String queryJSON = readEntireFile(Constants.ELIGIBILITY_JSON);
        Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
        Map<String, Object> response = pd.eligibility(query);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void claimsTest() throws Exception {
        String queryJSON = readEntireFile(Constants.CLAIMS_JSON);
        Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
        Map<String, Object> response = pd.claims(query);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void claimsStatusTest() throws Exception {
        String queryJSON = readEntireFile(Constants.CLAIMS_STATUS_JSON);
        Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
        Map<String, Object> response = pd.claimsStatus(query);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void referralsTest() throws Exception {
        String queryJSON = readEntireFile(Constants.REFERRALS_JSON);
        Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
        Map<String, Object> response = pd.referrals(query);

        assertNotNull(response);
    }


    /* Identity tests. */

    // test post
    @Test
    @Category(IntegrationTests.class)
    public void createIdentityTest() throws Exception {
        String postJSON = readEntireFile(Constants.CREATE_IDENTITY_JSON);
        Map<String, Object> post = (JSONObject) JSONValue.parse(postJSON);
        Map<String, Object> response = pd.createIdentity(post);

        assertNotNull(response);
    }

    //test update
    @Test
    @Category(IntegrationTests.class)
    public void updateIdentityTest() throws Exception {
        String updateJSON = readEntireFile(Constants.UPDATE_IDENTITY_JSON);
        Map<String, Object> update = (JSONObject) JSONValue.parse(updateJSON);
        Map<String, Object> response = pd.updateIdentity("881bc095-2068-43cb-9783-cce630364122", update);

        assertNotNull(response);
    }

    //test get uuid
    @Test
    @Category(IntegrationTests.class)
    public void getIdentityUuidTest() throws Exception {
        Map<String, Object> response = pd.identity("881bc095-2068-43cb-9783-cce630364122");

        assertNotNull(response);
    }

    //test get params
    @Test
    @Category(IntegrationTests.class)
    public void getIdentityParamsTest() throws Exception {
        String getJSON = readEntireFile(Constants.GET_IDENTITY_JSON);
        Map<String, Object> get = (JSONObject) JSONValue.parse(getJSON);

        Map<String, Object> response = pd.identity(get);

        assertNotNull(response);
    }

    /* Data tests. */

    @Test
    @Category(IntegrationTests.class)
    public void cashPricesTest() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("cpt_code", "87799");
        query.put("zip_code", "75201");
        Map<String, Object> response = pd.cashPrices(query);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void insurancePricesTest() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("cpt_code", "87799");
        query.put("zip_code", "29403");
        Map<String, Object> response = pd.insurancePrices(query);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void mpcTest() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("name", "surgery");
        Map<String, Object> response = pd.mpc(query);

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void mpcsTest() throws Exception {
        String mpc = "99213";
        Map<String, Object> response = pd.mpc(mpc, new HashMap<String, Object>());

        assertNotNull(response);
    }

    @Test
    @Category(IntegrationTests.class)
    public void providersTest() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
		query.put("npi", "1467560003");
		Map<String, Object> response = pd.providers(query);

		assertNotNull(response);

        JSONArray data = data(response);
        assertNotNull(data);
	}

    /*******************
     * Scheduling tests.
     *******************/
    public void listSchedulersTest() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
        Map<String, Object> response = pd.schedulers(query);
    }

    public void showSchedulerTest() throws Exception {
        String id = "";

        Map<String, Object> query = new HashMap<String, Object>();
        Map<String, Object> response = pd.scheduler(id, query);
    }

    public void listAppointmentTypesTest() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
        Map<String, Object> response = pd.appointmentTypes(query);
    }

    public void showAppointmentTypeTest() throws Exception {
        String id = "";

        Map<String, Object> response =
            pd.appointmentType(id, new HashMap<String, Object>());
    }

    public void bookAppointmentTest() throws Exception {
        String id = "";

        String bookAppointmentJSON = readEntireFile(Constants.BOOK_APPOINTMENT_JSON);

        Map<String, Object> query = (JSONObject) JSONValue.parse(bookAppointmentJSON);
        Map<String, Object> response = pd.bookAppointment(id, query);
    }

    public void updateAppointmentTest() throws Exception {
        String queryJSON = readEntireFile(Constants.UPDATE_APPOINTMENT_JSON);
        String id = "";

        Map<String, Object> query = (JSONObject) JSONValue.parse(queryJSON);
        Map<String, Object> response = pd.updateAppointment(id, query);
    }

    public void cancelAppointmentTest() throws Exception {
        String id = "";

        Map<String, Object> response =
            pd.cancelAppointment(id, new HashMap<String, Object>());
    }

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
    @Category(IntegrationTests.class)
    public void pharmacyPlansTest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trading_partner_id", "medicare_national");
        params.put("plan_number", "S5820003");
        Map<String, Object> response = pd.pharmacyPlans(params);

        assertNotNull(response);
        JSONObject data = (JSONObject) response.get("data");
        assertNotNull(data);
	}

    @Test
    @Category(IntegrationTests.class)
    public void pharmacyFormularyTest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trading_partner_id", "medicare_national");
        params.put("plan_number", "S5820003");
        params.put("ndc", "59310057922");
        Map<String, Object> response = pd.pharmacyFormulary(params);

        assertNotNull(response);
        JSONArray data = data(response);
        assertNotNull(data);
	}

    @Test
    @Category(IntegrationTests.class)
    public void pharmacyNetworkNoNPITest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trading_partner_id", "medicare_national");
        params.put("plan_number", "S5820003");
        params.put("zipcode", "94401");
        Map<String, Object> response = pd.pharmacyNetwork(params);

        assertNotNull(response);
        JSONArray data = data(response);
        assertNotNull(data);
	}

    @Test
    @Category(IntegrationTests.class)
    public void pharmacyNetworkNPITest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trading_partner_id", "medicare_national");
        params.put("plan_number", "S5820003");
        Map<String, Object> response = pd.pharmacyNetwork("1912301953", params);

        assertNotNull(response);
        JSONArray data = data(response);
        assertNotNull(data);
	}

    /** Test suite to assemble all integration tests */
    @RunWith(Categories.class)
    @Suite.SuiteClasses(PokitDokIntegrationTests.class)
    @Categories.IncludeCategory(IntegrationTests.class)
    public class PokitDokIntegrationTestSuite {}

    private JSONArray data(Map<String, Object> response) {
        return (JSONArray) response.get("data");
    }
}
