package com.pokitdok.tests;

import com.pokitdok.UnauthorizedException;
import com.pokitdok.PokitDok;
import com.pokitdok.tests.categories.IntegrationFailureTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Map;

import static org.junit.Assert.fail;

public class PokitDokIntegrationFailureTests {
    private static PokitDok pd;
    private static Map<String, String> env = System.getenv();

    @BeforeClass
    public static void setup() throws Exception {

        String clientId = "INVALID_CLIENT_ID";
        String clientSecret = "INVALID_CLIENT_SECRET";
        if ((clientId == null) || (clientSecret == null)) {
            fail("Please provide a PokitDok client ID and secret in the environment variables PD_CLIENT_ID and PD_CLIENT_SECRET.");
        }

        String apiBase = env.get("PD_API_BASE");
        pd = new PokitDok(clientId, clientSecret, null, apiBase);
    }

    @Test
    @Category(IntegrationFailureTests.class)
    public void unauthorizedExceptionTest() throws Exception {

        boolean raised = false;

        try {
            pd.activities();
        }
        catch (UnauthorizedException unauthorized) {
            raised = true;
        }

        assert(raised);
    }

}
